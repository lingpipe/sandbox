package com.lingpipe.mitre2011;


import com.aliasi.io.FileLineReader;

public class Chars {

    public static void main(String[] args) throws IOException {
        System.out.println("Counting Chars");


        int[] histo = new int[256];
        

        for (int i = 0; i < args.length; ++i) {
            InputStream in = new BufferedInputStream(new FileInputStream(args[i]));
            
            int c;
            while ((c = in.read()) != -1)
                ++histo[c];
            in.close();
        }

        for (int i = 0; i < histo.length; ++i)
            if (histo[i] > 0)
                System.out.printf("%3h  %10d\n",i,histo[i]);
    }

}