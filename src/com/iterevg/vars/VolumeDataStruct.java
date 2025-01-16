package com.iterevg.vars;

public class VolumeDataStruct {
    //In Java an int8 would be a byte, and you can use short for int16.

    char[] ident = new char[10];
    DataFileName ptr;
    int diskNumber;
    Integer size;

    public char[] getIdent() {
        return ident;
    }

    public char getIdent(int i) {
        return ident[i];
    }

    public void setIdent(char[] ident) {
        this.ident = ident;
    }

    public void setIdent(int i, char ident) {
        this.ident[i] = ident;
    }

    public DataFileName getPtr() {
        return ptr;
    }

    public void setPtr(DataFileName ptr) {
        this.ptr = ptr;
    }

    public int getDiskNumber() {
        return diskNumber;
    }

    public void setDiskNumber(int diskNumber) {
        this.diskNumber = diskNumber;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
