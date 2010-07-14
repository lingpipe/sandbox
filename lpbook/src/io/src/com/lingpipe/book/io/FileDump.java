package com.lingpipe.book.io;

import java.io.File;
import java.io.IOException;

public class FileDump {

    public static void main(String[] args) throws IOException {

        File x = new File("c:/lpb");
        System.out.println(java.util.Arrays.asList(x.listFiles()));

        File foo = new File("foo");
        File fooCaps = new File("Foo");
        File fooAbs = new File("c:\\lpb\\src\\io\\Foo");

        File canonFoo = foo.getCanonicalFile();
        File canonFooCaps = fooCaps.getCanonicalFile();
        File canonFooAbs = fooAbs.getCanonicalFile();

        System.out.println("foo=" + foo + " canonical=" + canonFoo);
        System.out.println("fooCaps=" + fooCaps + " canonical=" + canonFooCaps);
        System.out.println("fooAbs=" + fooAbs + " canonical=" + canonFooAbs);

        System.out.println("foo equals fooCaps=" + foo.equals(fooCaps));
        System.out.println("foo equals canonFoo=" + foo.equals(canonFoo));
        System.out.println("canonFoo equals canonFooAbs=" + canonFoo.equals(canonFooAbs));
    }

}