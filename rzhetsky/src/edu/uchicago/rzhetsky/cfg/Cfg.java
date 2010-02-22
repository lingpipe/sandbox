package edu.uchicago.rzhetsky.cfg;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cfg {

    private final Set<Production> mProductionSet;
    private final Set<LexEntry> mLexEntrySet;

    public Cfg(Collection<Production> productions,
               Collection<LexEntry> lexEntries) {
        mProductionSet = new HashSet<Production>(productions);
        mLexEntrySet = new HashSet<LexEntry>(lexEntries);
    }

    
    public Cfg(File productionFile,
               File lexEntryFile,
               String encoding) throws IOException {
        mProductionSet = new HashSet<Production>();
        mLexEntrySet = new HashSet<LexEntry>();
        for (String line : lines(productionFile,encoding)) {
            String[] cats = line.split("\\s+");
            if (cats.length == 0) 
                continue; // could throw at this point
            String mother = cats[0];
            String[] dtrs = new String[cats.length-1];
            for (int i = 0; i < dtrs.length; ++i)
                dtrs[i] = cats[i+1];
            Production production = Production.general(mother,dtrs);
            mProductionSet.add(production);
        }
        
        for (String line : lines(lexEntryFile,encoding)) {
            String[] wordCat = line.split("\\s+");
            if (wordCat.length != 2) 
                continue; // could throw illegal
            String word = wordCat[0];
            String mother = wordCat[1];
            LexEntry lexEntry = new LexEntry(mother,word);
            mLexEntrySet.add(lexEntry);
        }
    }


    public Set<Production> productions() {
        return Collections.unmodifiableSet(mProductionSet);
    }

    public Set<LexEntry> lexEntries() {
        return Collections.unmodifiableSet(mLexEntrySet);
    }
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RULES\n");
        for (Production production : productions()) {
            sb.append(production);
            sb.append('\n');
        }
        sb.append("\nLEXICON\n");
        for (LexEntry entry : lexEntries()) {
            sb.append(entry);
            sb.append('\n');
        }
        return sb.toString();
    }
    
    static List<String> lines(File file, String encoding) 
        throws IOException {

        InputStream in = null;
        InputStreamReader inReader = null;
        BufferedReader bufReader = null;
        try {
            in = new FileInputStream(file);
            inReader = new InputStreamReader(in,encoding);
            bufReader = new BufferedReader(inReader);
            List<String> lines = new ArrayList<String>();
            String line;
            while ((line = bufReader.readLine()) != null) {
                if (line.length() == 0)
                    continue;
                if (line.startsWith("#"))
                    continue;
                lines.add(line);
            }
            return lines;
        } finally {
            close(bufReader);
            close(inReader);
            close(in);
        }
    }

    static void close(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }
                    
    

}