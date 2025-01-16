package com.iterevg;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VolumeConfigReader {

    private static final int MAX_DISKS = 20;
    private static final String PATH_DATAS = "/Users/greg/Documents/workspace/IntelliJ/cruiseTool/src/com/iterevg/datas/";
    private static final String PATH_DUMP = "/Users/greg/Documents/workspace/cruisedump/";
    private VolumeData[] volumeData = new VolumeData[MAX_DISKS];
    private boolean volumeDataLoaded = false;
    private int numOfDisks;

    public int readVolCnf() {
        for (int i = 0; i < MAX_DISKS; i++) {
            volumeData[i] = new VolumeData();
            volumeData[i].ident = new char[10];
            volumeData[i].diskNumber = i + 1;
            volumeData[i].size = 0;
        }

        try (RandomAccessFile fileHandle = new RandomAccessFile(PATH_DATAS + "VOL.CNF", "r")) {

            /*int a = 0;
            while(a != -1) {
                a = fileHandle.read();
                System.out.print((char)a+",");
            }*/

            numOfDisks = readSint16BE(fileHandle);
            fileHandle.skipBytes(2); // Skip size of one header entry - 20 bytes

            for (int i = 0; i < numOfDisks; i++) {
                byte[] ident = new byte[10];
                fileHandle.readFully(ident);
                volumeData[i].ident = toChar(ident);
                //for (int b=0 ; b<volumeData[i].ident.length ; b++) {
                //    System.out.print(volumeData[i].ident[b]+",");
                //}
                //System.out.println();

                fileHandle.skipBytes(4);
                volumeData[i].ptr = new DataFileName();
                volumeData[i].diskNumber = readSint16BE(fileHandle);
                volumeData[i].size = readSint32BE(fileHandle);
                debug("Disk number: " + volumeData[i].diskNumber);
            }

            for (int i = 0; i < numOfDisks; i++) {
                volumeData[i].size = readSint32BE(fileHandle);
                byte[] data = new byte[volumeData[i].size];
                fileHandle.readFully(data);
                volumeData[i].ptr.data = toChar(data, volumeData[i].ptr.data.length);
            }

            volumeDataLoaded = true;

            //for (int i = 0; i < numOfDisks; i++) {
            for (int i = 0; i < 1; i++) {
                String nameBuffer = "D" + (i + 1);
                try (RandomAccessFile diskFile = new RandomAccessFile(PATH_DATAS+nameBuffer, "r")) {
                    short numEntry = readSint16BE(diskFile);
                    short sizeEntry = readSint16BE(diskFile);
                    FileEntry[] buffer = new FileEntry[numEntry];

                    for (int j = 0; j < numEntry; j++) {
                    //for (int j = 0; j < 1; j++) {
                        diskFile.seek(4 + j * 0x1E);
                        buffer[j] = new FileEntry();

                        byte[] nameBytes = new byte[14]; // Taille maximale du nom de fichier
                        diskFile.readFully(nameBytes);
                        // Construire le nom du fichier en s'arrêtant au premier byte nul
                        // Un tableau de byte contient la valeur DEC. En transformant en HEX on a la valeur dans HxD.
                        StringBuilder fileNameBuilder = new StringBuilder();
                        for (byte b : nameBytes) {
                            if (b == 0) {
                                break; // Arrête la lecture si le byte est nul
                            }
                            fileNameBuilder.append((char) b); // Ajoute le caractère au nom
                        }
                        //debug(1, String.valueOf(diskFile.getFilePointer()));
                        buffer[j].name = fileNameBuilder.toString().trim().toCharArray();
                        buffer[j].offset = readSint32BE(diskFile);
                        buffer[j].size = readSint32BE(diskFile);
                        buffer[j].extSize = readSint32BE(diskFile);
                        buffer[j].unk3 = readSint32BE(diskFile);

                        //debug(1, String.valueOf(diskFile.getFilePointer()));
                        diskFile.seek(buffer[j].offset);
                        //debug(1, String.valueOf(diskFile.getFilePointer()));
                        byte[] bufferLocal = new byte[buffer[j].size];
                        diskFile.readFully(bufferLocal);
                        debug("Ecriture de : " +  new String(buffer[j].name));
                        if (buffer[j].size == buffer[j].extSize) {
                            try (FileOutputStream fout = new FileOutputStream(PATH_DUMP + new String(buffer[j].name).trim())) {
                                fout.write(bufferLocal);
                            }
                        } else {
                            char[] blc = toChar(bufferLocal);
                            char[] uncompBuffer = Unpack.delphineUnpack(blc, buffer[j].size, buffer[j].extSize + 500);

                            //Contacter https://www.b0nk.com/posts/cirugia-retro/

                            try (FileOutputStream fout = new FileOutputStream(PATH_DUMP + new String(buffer[j].name).trim())) {
                                FileOutputStream foutz = new FileOutputStream(PATH_DUMP + new String(buffer[j].name).trim()+".z");
                                fout.write(toByte(uncompBuffer));
                                foutz.write(bufferLocal);
                            }
                        }
                    }
                }
            }
            return 1;

        } catch (IOException e) {
            e.printStackTrace();
            return -2;
        }
    }

    private byte[] toByte(char[] c) {
        byte[] b = new byte[c.length];
        for (int i=0 ; i<c.length ; i++) {
            b[i] = (byte) c[i];
        }
        return b;
    }

    private char[] toChar(byte[] b, int length) {
        char[] c = new char[length];
        for (int i=0 ; i<length ; i++) {
            if (b[i] < 0) {
                c[i] = (char) (b[i]+256);
            } else {
                c[i] = (char) b[i];
            }
        }
        return c;
    }

    private char[] toChar(byte[] b) {
        char[] c = new char[b.length];
        for (int i=0 ; i<b.length ; i++) {
            if (b[i] < 0) {
                c[i] = (char) (b[i]+256);
            } else {
                c[i] = (char) b[i];
            }
        }
        return c;
    }

    private short readSint16BE(RandomAccessFile file) throws IOException {
        byte[] bytes = new byte[2];
        file.readFully(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    private int readSint32BE(RandomAccessFile file) throws IOException {
        byte[] bytes = new byte[4];
        file.readFully(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private void debug(String message) {
        System.out.println(message);
    }

    class VolumeData {
        char[] ident; // Identifiant de volume
        DataFileName ptr; // Pointeur vers un DataFileName
        int diskNumber; // Numéro de disque
        int size; // Taille
    }

    class DataFileName {
        char[] data = new char[13]; // Simule le tableau de caractères de C++
    }

    class FileEntry {
        char[] name = new char[14];
        int offset;
        int size;
        int extSize;
        int unk3;
    }
}
