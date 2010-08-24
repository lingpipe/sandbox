package com.lingpipe.book.lda;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.cluster.LatentDirichletAllocation;

import com.aliasi.io.FileLineReader;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ftp://ftp.wormbase.org/pub/wormbase/misc/literature/2007-12-01-wormbase-literature.endnote.gz

public class LdaWorm {

    public static void main(String[] args) throws IOException {
        File corpusFile = new File(args[0]);
        int minTokenCount = Integer.parseInt(args[1]); // 5
        short numTopics = Short.parseShort(args[2]); // 50
        double topicPrior = Double.parseDouble(args[3]); // 0.1;
        double wordPrior = Double.parseDouble(args[4]); // 0.01;
        long randomSeed = Long.parseLong(args[5]); //  6474835;
        int numSamples = Integer.parseInt(args[6]);
        int burninEpochs = 0;
        int sampleLag = 1; // no lag

        System.out.println("MEDLINE Citation File=" + corpusFile);
        System.out.println("Minimum token count=" + minTokenCount);
        System.out.println("Number of topics=" + numTopics);
        System.out.println("Topic prior in docs=" + topicPrior);
        System.out.println("Word prior in topics=" + wordPrior);
        System.out.println("Burnin epochs=" + burninEpochs);
        System.out.println("Sample lag=" + sampleLag);
        System.out.println("Number of samples=" + numSamples);

        CharSequence[] articleTexts = readCorpus(corpusFile);
        
        SymbolTable symbolTable = new MapSymbolTable();
        int[][] docTokens
            = LatentDirichletAllocation
            .tokenizeDocuments(articleTexts,WORMBASE_TOKENIZER_FACTORY,symbolTable,minTokenCount);
        
        System.out.println("Number of unique words above count threshold=" + symbolTable.numSymbols());

        int numTokens = 0;
        for (int[] tokens : docTokens)
            numTokens += tokens.length;
        System.out.println("Tokenized.  #Tokens After Pruning=" + numTokens);

        int reportPeriod = 5;
        ReportingLdaSampleHandler handler
            = new ReportingLdaSampleHandler(symbolTable,reportPeriod);

        LatentDirichletAllocation.GibbsSample sample
            = LatentDirichletAllocation
            .gibbsSampler(docTokens,

                          numTopics,
                          topicPrior,
                          wordPrior,

                          burninEpochs,
                          sampleLag,
                          numSamples,

                          new Random(randomSeed),
                          handler);

        int maxWordsPerTopic = 200;
        int maxTopicsPerDoc = 10;
        boolean reportTokens = true;
        handler.reportParameters(sample);
        handler.reportTopics(sample,maxWordsPerTopic);
        handler.reportDocs(sample,maxTopicsPerDoc,reportTokens);

    }

    static CharSequence[] readCorpus(File file) throws IOException {
        boolean gzipped = true;
        String encoding = "ASCII";
        FileLineReader reader = new FileLineReader(file,encoding,gzipped);
        List<CharSequence> articleTextList = new ArrayList<CharSequence>(15000);
        StringBuilder docBuf = new StringBuilder();
        for (String line : reader) {
            // docs may only have title
            if (line.startsWith("%T")) {
                docBuf.append(line.substring(3));
            } else if (line.startsWith("%X")) {
                docBuf.append(' ');
                docBuf.append(line.substring(3)); // leave space
            } else if (line.length() == 0 && docBuf.length() > 0) {
                articleTextList.add(docBuf);
                docBuf = new StringBuilder();
            }

        }
        reader.close();
            
        int charCount = 0;
        for (CharSequence cs : articleTextList)
            charCount += cs.length();

        System.out.println("#articles=" + articleTextList.size() 
                           + " #chars=" + charCount);

        CharSequence[] articleTexts
            = articleTextList.toArray(new CharSequence[0]);
        return articleTexts;
    }

    
    /*x LdaWorm.1 */
    static final TokenizerFactory wormbaseTokenizerFactory() {
        String regex = "[\\x2D\\p{L}\\p{N}]{2,}";
        TokenizerFactory factory 
            = new RegExTokenizerFactory(regex);
        Pattern alpha = Pattern.compile(".*\\p{L}.*");
        factory = new RegExFilteredTokenizerFactory(factory,alpha);
        factory = new LowerCaseTokenizerFactory(factory);
        factory = new EnglishStopTokenizerFactory(factory);
        factory = new StopTokenizerFactory(factory,STOPWORD_SET);
        factory = new StemTokenizerFactory(factory);
        return factory;
    }
    /*x*/



    static final String[] STOPWORD_LIST
        = new String[] {

        "these",
        "elegan",
        "caenorhabditi",
        "both",
        "may",
        "between",
        "our",
        "et",
        "al",
        "however",
        "many",

        "thu",
        "thus", // thus

        "how",
        "while",
        "same",
        "here",
        "although",
        "those",
        "might",
        "see",
        "like",
        "likely",
        "where",

        // looked at all 100 count plus

        // "first",
        // "second",
        // "third",
        // "fourth",
        // "fifth",
        // "sixth",
        // "seventh",
        // "eighth",
        // "ninth",

        "i",
        "ii",
        "iii",
        "iv",
        "v",
        "vi",
        "vii",
        "viii",
        "ix",
        "x",
        "zero",
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine",
        "ten",
        "eleven",
        "twelve",
        "thirteen",
        "fourteen",
        "fifteen",
        "sixteen",
        "seventeen",
        "eighteen",
        "nineteen",
        "twenty",
        "thirty",
        "forty",
        "fifty",
        "sixty",
        "seventy",
        "eighty",
        "ninety",
        "hundred",
        "thousand",
        "million"
    };

    static final Set<String> STOPWORD_SET
        = new HashSet<String>(Arrays.asList(STOPWORD_LIST));

    static final TokenizerFactory WORMBASE_TOKENIZER_FACTORY
        = wormbaseTokenizerFactory();


    /*x LdaWorm.2 */
    static class StemTokenizerFactory 
        extends ModifyTokenTokenizerFactory {

        public StemTokenizerFactory(TokenizerFactory factory) {
            super(factory);
        }

        public String modifyToken(String token) {
            Matcher matcher = SUFFIX_PATTERN.matcher(token);
            return matcher.matches() ? matcher.group(1) : token;
        }

        static final Pattern SUFFIX_PATTERN
            = Pattern.compile("(.+?[aeiouy].*?|.*?[aeiouy].+?)"
                              + "(ss|ies|es|s)");
        
    /*x*/
        static final long serialVersionUID = -6045422132691926248L;


    }

}