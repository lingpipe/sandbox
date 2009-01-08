package com.aliasi.annotate.corpora;


import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.aliasi.util.Files;
import com.aliasi.util.AbstractCommand;

import java.io.File;
import java.io.IOException;

public class NormalizeHtmlToUTF8 extends AbstractCommand {

    public NormalizeHtmlToUTF8 (String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        NormalizeHtmlToUTF8 cmd = new NormalizeHtmlToUTF8(args) ;
        cmd.run();
    }
            
    public void run() {
        File dirIn  = new File(getBareArgument(0));
        File dirOut = new File(getBareArgument(1));
        try {
            convert(dirIn, dirOut);
        }
        catch (Exception e) {
            System.out.println("exception" + e);
        }
    }

    void convert(File input, File output) throws IOException {
        if (input.isDirectory()) {
            File[] files = input.listFiles();
            for (int i = 0; i < files.length; ++i) {
                convert(files[i],output);
            }
        }
        byte[] bytes = Files.readBytesFromFile(input);
        CharsetDetector detector = new CharsetDetector();
        detector.setText(bytes);
        CharsetMatch match = detector.detect();
        System.out.println("Got charset: " + match.getName());
        
    }        





}
