import com.aliasi.lm.NGramProcessLM;

import com.aliasi.corpus.Parser;
import com.aliasi.corpus.TextHandler;

import com.aliasi.corpus.parsers.GigawordTextParser;

import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ObjectToCounterMap;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;


import org.xml.sax.InputSource;

public class GigawordTrainer  {

    public static void main(String[] args) throws Exception {
        File modelFile = new File(args[0]);
        int nGramLength = Integer.valueOf(args[1]);
        long maxDataBytes = Long.valueOf(args[2]);

        // construct trainer
        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        FixedWeightEditDistance fixedEdit
            = new FixedWeightEditDistance(0,0,0,0,0); // placeholder
        NGramProcessLM lm = new NGramProcessLM(8);
        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,fixedEdit,tokenizerFactory);

        Reverser reversedTrainer
            = new Reverser(trainer);

        // corpus parser, with trainer to get text
        Parser<TextHandler> parser = new GigawordTextParser(reversedTrainer);

        // train, stopping after specified number of bytes is exceeded
        for (int i = 3; i < args.length; ++i) {
            File dataDir = new File(args[i]);
            System.out.println("Data Directory=" + dataDir);

            for (File file : dataDir.listFiles()) {

                if (trainer.numTrainingChars() >= maxDataBytes)
                    break;

                System.out.println("     Chars so far="
                                   + trainer.numTrainingChars());
                System.out.println("     Next training file=" + file);
                FileInputStream fileIn = new FileInputStream(file);
                BufferedInputStream bufIn = new BufferedInputStream(fileIn);
                GZIPInputStream zipIn = new GZIPInputStream(bufIn);
                InputSource in = new InputSource(zipIn);
                parser.parse(in);
                zipIn.close(); // not robust
            }
        }

        // compile
        System.out.println("Total training chars="
                           + trainer.numTrainingChars());
        System.out.println("Compiling to file=" + modelFile);
        AbstractExternalizable.compileTo(trainer,modelFile);
        System.out.println("Finished.\n");

        // dumping tokens
        ObjectToCounterMap<String> tokenCounter = trainer.tokenCounter();
        List<String> keysByCount = tokenCounter.keysOrderedByCountList();
        for (String key : keysByCount) {
            String msg
                = String.format("%9d %s",
                                Integer.valueOf(tokenCounter.getCount(key)),key);
            System.out.println(msg);
        }
    }

    static abstract class FilterHandler implements TextHandler {
        final TextHandler mHandler;
        FilterHandler(TextHandler handler) {
            mHandler = handler;
        }

    }

    static class CapsFinder extends FilterHandler {
        Pattern pattern = Pattern.compile("\\p{Lu}{16}\\p{Lu}+");
        CapsFinder(TextHandler handler) {
            super(handler);
        }
        public void handle(char[] cs, int start, int length) {
            Matcher matcher = pattern.matcher(new String(cs,start,length));
            while (matcher.find()) {
                System.out.println("match=" + matcher.group());
            }
        }
    }

    static class Reverser extends FilterHandler {
        Reverser(TextHandler handler) {
            super(handler);
        }
        public void handle(char[] cs, int start, int length) {
            char[] csRev = new char[length];
            for (int i = 0, j=length-1; i < length; ++i,--j)
                csRev[i] = cs[j];
            mHandler.handle(csRev,0,length);
        }
    }

}