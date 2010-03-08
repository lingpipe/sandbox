import com.aliasi.corpus.Parser;
import com.aliasi.corpus.TextHandler;

import com.aliasi.corpus.parsers.GigawordTextParser;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Streams;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.zip.GZIPInputStream;

import java.util.Arrays;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.InputSource;

public class GigawordWords {

    public static void main(String[] args) throws IOException {
        File gigaword3Dir = new File(args[0]);
        System.out.println("gigaword3Dir=" + gigaword3Dir);
        File[] directoryList = new File[] { new File(new File(gigaword3Dir,"disc1"), "data"),
                                            new File(new File(gigaword3Dir,"disc2"), "data") };
        

        TokenCountHandler tokenCounter = new TokenCountHandler();
        Parser<TextHandler> parser = new GigawordTextParser(tokenCounter); 
        for (int disc = 1; disc <= 2; ++disc) {
            File dataDir = new File(new File(gigaword3Dir,"disc" + disc),"data");
            for (File sourceDir : dataDir.listFiles()) {
                for (File dataFile : sourceDir.listFiles()) {
                    System.err.println("File=" + dataFile.getName() 
                                       + " token count=" + tokenCounter.mTokenCount 
                                       + " unique tok count=" + tokenCounter.mCounter.size()
                                       + " char count=" + tokenCounter.mCharCount);
                    FileInputStream fileIn = new FileInputStream(dataFile);
                    GZIPInputStream zipIn = new GZIPInputStream(fileIn);
                    InputSource in = new InputSource(zipIn);
                    parser.parse(in);
                    zipIn.close();
                }
            }
        }
        
        String[] tokens = tokenCounter.mCounter.keySet().toArray(new String[0]);
        Arrays.sort(tokens);
        for (String token : tokens)
            System.out.println(token + "\t" + tokenCounter.mCounter.getCount(token));
    }


    static boolean accept(String token) {
        for (int i = 0; i < token.length(); ++i)
            if (!Character.isLowerCase(token.charAt(i)))
                return false;
        return true;
    }

    static class TokenCountHandler implements TextHandler {
        long mTokenCount = 0;
        long mRejectedTokenCount = 0;
        long mCharCount = 0;
        final ObjectToCounterMap<String> mCounter = new ObjectToCounterMap<String>(500000);
        public void handle(char[] cs, int start, int length) {
            mCharCount += length;
            for (String token : IndoEuropeanTokenizerFactory.INSTANCE.tokenizer(cs,start,length)) {
                if (accept(token)) {
                    mCounter.increment(token);
                    ++mTokenCount;
                } else {
                    ++mRejectedTokenCount;
                }
            }
        }
    }

}