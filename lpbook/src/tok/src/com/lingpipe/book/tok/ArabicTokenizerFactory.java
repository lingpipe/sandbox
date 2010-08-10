package com.lingpipe.book.tok;

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Files;

import org.apache.lucene.util.Version;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.ar.ArabicAnalyzer;

import java.io.File;
import java.io.IOException;

public class ArabicTokenizerFactory {

    private ArabicTokenizerFactory() { }

    /*x ArabicTokenizerFactory.1 */
    public static final Analyzer ANALYZER
        = new ArabicAnalyzer(Version.LUCENE_30);

    public static final TokenizerFactory INSTANCE
        = new AnalyzerTokenizerFactory(ANALYZER,"foo");
    /*x*/

    public static void main(String[] args) throws IOException {
        String file = args[0];

        String text = Files.readFromFile(new File(file),"UTF-8");

        /*x ArabicTokenizerFactory.2 */
        System.setOut(new java.io.PrintStream(System.out,true,"UTF-8"));

        System.out.println("file=" + file);
        System.out.println("text=" + text);

        DisplayTokens.displayTokens(text,ArabicTokenizerFactory.INSTANCE);
        
    }


}