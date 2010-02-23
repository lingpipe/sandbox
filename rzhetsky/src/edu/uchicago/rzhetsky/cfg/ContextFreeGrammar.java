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

/**
 * A {@code ContextFreeGrammar} represents the productions and lexical
 * entries of a context-free grammar.
 *
 * <h3>File Format</h3>
 *
 * A context-free grammar may be read in from a file specifying a set
 * of production rules (i.e. a grammar), a file specifying a set of
 * lexical entries (i.e. a dictionary), and a string specifying the
 * character encoding of the files.  The grammars may include arbitrary
 * unicode characters as either words or category names.
 *
 * <p>The two files contain the production rules and lexical entries
 * for the grammar.  Each of the files contains a single rule per
 * line.  Empty lines in files are ignored.  Lines beginning with the
 * symbol "#" are ignored and assumed to be comments.
 *
 * <p>The format for a production rule is the mother category followed
 * by one or more spaces, followed by a sequence of daughter categories
 * separated by spaces.  For example:
 *
 * <blockquote style="border:1px solid gray"><pre>
 * # Production Rules
 *
 * N N PP
 * N ADJ N
 *
 * NP DET N
 * NP PN
 *
 * PP P NP
 *
 * S NP VP
 *
 * VP IV
 * VP TV VP
 * VP DV VP VP
 * VP VP PP</pre></blockquote>
 *
 * <blockquote style="border:1px solid gray"><pre>
 * # Lexical Entries
 *
 * John PN
 * Mary PN
 * a DET
 * block N
 * box N
 * dog N
 * gave DV
 * lectue N
 * on P
 * near P
 * present N
 * present TV
 * ran IV
 * saw TV
 * table N
 * the DET
 * 
 * 
 *
 * NP DET N
 *
 * PP P NP
 *
 * S NP VP
 *
 * VP IV
 * VP TV VP
 * VP DV VP VP
 * VP VP PP</pre></blockquote>
 * 
 * @author Bob Carpenter
 * @version 1.0
 * @since 1.0
 */
public class ContextFreeGrammar {

    private final Set<Production> mProductionSet;
    private final Set<LexEntry> mLexEntrySet;

    /**
     * Construct a context-free grammar with the specified collections
     * of productions and lexical entries.  The collections are deeply
     * copied into sets, so that further changes to the collections do
     * not affect the constructed grammar.
     *
     * @param productions Grammar production rules.
     * @param lexEntries Grammar lexical entries.
     */
    public ContextFreeGrammar(Collection<Production> productions,
                              Collection<LexEntry> lexEntries) {
        mProductionSet = new HashSet<Production>(productions);
        mLexEntrySet = new HashSet<LexEntry>(lexEntries);
    }

    
    /**
     * Construct a context-free grammar by reading productions
     * and lexical entries from the specified files.  The files
     * are closed and their associated resources released after
     * they are read and before the grammar is constructed.
     *
     * @param productionFile File containing grammar productions.
     * @param lexEntryFile File containing lexical entries.
     * @param encoding Character encoding for the specified grammar
     * files.
     * @throws IOException If there is an exception reading from
     * the files.
     */
    public ContextFreeGrammar(File productionFile,
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