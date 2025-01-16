package com.iterevg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Unpack {

    static class UnpackCtx {
        int size, datasize;
        long crc; // Utiliser long pour correspondre à uint32
        long chk; // Utiliser long pour correspondre à uint32
        char[] dst;
        char[] src;
        int pos_src;

        int pos_dst;
    }

    private static int rcr(UnpackCtx uc, int CF) {
        int rCF = (int) (uc.chk & 1); //Fait un AND bit à bit entre les 2 valeurs. Ex : 0101 XOR 0110 = 0100
        uc.chk = (uc.chk >> 1) & 0xFFFFFFFFL; //décale les bits vers la droite. Ex : 0100 -> 0010
        if (CF != 0) {
            uc.chk = (uc.chk | 0x80000000) & 0xFFFFFFFFL; //Fait un OR bit à bit entre les 2 valeurs. Ex : 0010 OR 0001 = 0011
        }
        return rCF;
    }

    private static int nextChunk(UnpackCtx uc) {
        int CF = rcr(uc, 0);
        if (uc.chk == 0) {
            uc.chk = readBEUInt32(uc, uc.pos_src);
            uc.pos_src = movPtrSrc(uc.pos_src, -4); // Simule le déplacement du pointeur
            uc.crc = (uc.crc ^ uc.chk) & 0xFFFFFFFFL; //Fait un XOR bit à bit entre les 2 valeurs. Ex : 0101 XOR 0110 = 0011
            CF = rcr(uc, 1);
        }
        return CF;
    }

    private static int getCode(UnpackCtx uc, byte numChunks) {
        int c = 0;
        while (numChunks-- > 0) {
            c = c << 1; //décale les bits vers la gauche. Ex : 0001 -> 0010
            if (nextChunk(uc) != 0) {
                c = c | 1; //Fait un OR bit à bit entre les 2 valeurs. Ex : 0010 OR 0001 = 0011
            }
        }
        return c;
    }

    private static void unpackHelper1(UnpackCtx uc, byte numChunks, byte addCount) {
        int count = getCode(uc, numChunks) + addCount + 1;
        uc.datasize -= count;
        try {
            while (count-- > 0) {
                putByte(uc, (char) getCode(uc, (byte) 8));
                uc.pos_dst = movPtrDst(uc.pos_dst, -1);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("ArrayIndexOutOfBoundsException");
        }
    }

    private static void unpackHelper2(UnpackCtx uc, byte numChunks) {
        int i = getCode(uc, numChunks);
        int count = uc.size + 1;
        uc.datasize -= count;
        while (count-- > 0) {
            putByte(uc, uc.dst[uc.pos_dst + i]);
            //uc.pos_dst = uc.pos_dst + i;
            uc.pos_dst = movPtrDst(uc.pos_dst, -1);
        }
    }

    public static char[] delphineUnpack(char[] src, int len, int extlen) {
        UnpackCtx uc = new UnpackCtx();
        uc.src = src;
        uc.pos_src = 0;
        uc.pos_src = movPtrSrc(uc.pos_src, len - 4);
        uc.datasize = (int) readBEUInt32(uc, uc.pos_src);

        uc.pos_src = movPtrSrc(uc.pos_src, -4);

        uc.dst = new char[extlen];
        uc.pos_dst = 0;
        uc.pos_dst = movPtrDst(uc.pos_dst, uc.datasize - 1);

        uc.size = 0;
        uc.crc = readBEUInt32(uc, uc.pos_src); //430094584
        uc.pos_src = movPtrSrc(uc.pos_src, -4);
        uc.chk = readBEUInt32(uc, uc.pos_src); //519
        uc.pos_src = movPtrSrc(uc.pos_src, -4);
        uc.crc ^= uc.chk;

        do {
            if (nextChunk(uc) == 0) {
                uc.size = 1;
                if (nextChunk(uc) == 0) {
                    unpackHelper1(uc, (byte) 3, (byte) 0);
                } else {
                    unpackHelper2(uc, (byte) 8);
                }
            } else {
                int c = getCode(uc, (byte) 2);
                if (c == 3) {
                    unpackHelper1(uc, (byte) 8, (byte) 8);
                } else if (c < 2) {
                    uc.size = c + 2;
                    unpackHelper2(uc, (byte) (c + 9));
                } else {
                    uc.size = getCode(uc, (byte) 8);
                    unpackHelper2(uc, (byte) 12);
                }
            }
        } while (uc.datasize > 0);
        //return uc.crc == 0 ? uc.dst : null;
        return uc.dst;
    }

    private static int movPtrSrc(int pos, int i) {
        int npos = pos + i;
        //System.out.println("<< Position src = (DEC)" + npos + " - (HEX)" + (Integer.toHexString(npos)).toUpperCase());
        return npos;
    }

    private static int movPtrDst(int pos, int i) {
        int npos = pos + i;
        //System.out.println("  >>Position dst = (DEC)" + npos + " - (HEX)" + (Integer.toHexString(npos)).toUpperCase());
        return npos;
    }

    private static void putByte(UnpackCtx uc, char code) {
        uc.dst[uc.pos_dst] = code;
    }

    private static long readBEUInt32(UnpackCtx uc, int pos) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte)uc.src[pos+3];
        bytes[2] = (byte)uc.src[pos+2];
        bytes[1] = (byte)uc.src[pos+1];
        bytes[0] = (byte)uc.src[pos+0];
        int i = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
        long l = i & 0xFFFFFFFFL;
        //uc.pos = movPtr(uc.pos, 4);
        return l;
    }

    //---------------------------------------//
}