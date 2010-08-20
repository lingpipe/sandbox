package com.lingpipe.book.java;

import java.util.Random;

class FragmentsLda {

    private FragmentsLda() { /* no instances */ }

    static void frag1() {
        /*x FragmentsJava.1 */
        Random seeder = new Random();
        long seed = seeder.nextLong();
        System.out.printf("Using seed=" + seed);
        Random random = new Random(seed);
        /*x*/
    }


    public static double random() {
        Random random = new Random();
        return random.nextDouble();
    }


}