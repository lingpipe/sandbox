package com.lingpipe.book.chars;

public class ByteTable {

    public static void main(String[] args) {
        for (int i = 0; i < 255; ++i) {
            byte b = 0;
            b = b | i;
            System.out.printf("%3d %4d %3d\n",
                              i, b, Integer.toHexString(i));
        }
    }

}