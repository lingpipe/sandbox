package com.lingpipe.mitre2011;

import com.aliasi.io.FileLineReader;

import java.io.IOException;
import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class Corpus {

    final List<String[]> mIndex;
    final List<String[]> mQueries;

    public Corpus(File dir) throws IOException {
        mIndex = parseFile(new File(dir,"index.txt"));
        mQueries = parseFile(new File(dir,"queries.txt"));
    }

    static List<String[]> parseFile(File file) throws IOException {
        System.out.println("parsing file=" + file);
        List<String[]> entries = new ArrayList<String[]>();
        FileLineReader lines = new FileLineReader(file,"ISO-8859-1");
        for (String line : lines) {
            String[] fields = line.split("\\x7C");
            if (fields.length != 3) {
                System.out.println("bad line=^" + line + "$");
                continue;
            }
            entries.add(fields);
        }
        lines.close();
        System.out.println("    #entries=" + entries.size());
        return entries;
    }

}