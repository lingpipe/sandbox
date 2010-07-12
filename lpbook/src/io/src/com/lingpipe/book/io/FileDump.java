package com.lingpipe.book.io;

import java.io.File;
import java.io.IOException;

public class FileDump {

    public static void main(String[] args) throws IOException {

        System.out.println("Available Root Directories on this System");
        File[] rootDirs = File.listRoots();
        for (File rootDir : rootDirs)
            System.out.println("    root dir=" + rootDir);

        File foo = new File("foo");
        File fooCaps = new File("Foo");
        File fooAbs = new File("c:\\lpb\\src\\io\\Foo");

        File canonFoo = foo.getCanonicalFile();
        File canonFooAbs = fooAbs.getCanonicalFile();

        System.out.println("foo equals fooCaps=" + foo.equals(fooCaps));
        System.out.println("foo equals canonFoo=" + foo.equals(canonFoo));
        System.out.println("canonFoo equals canonFooAbs=" + canonFoo.equals(canonFooAbs));
    }

}