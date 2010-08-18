package com.lingpipe.book.lda;

import java.util.Random;

class FragmentsLda {

    public static void frag1() {
        /*x FragmentsLda.1 */
        Random seeder = new Random();
        long seed = seeder.nextLong();
        System.out.printf("Using seed=" + seed);
        Random random = new Random(seed);
        /*x*/
    }

}