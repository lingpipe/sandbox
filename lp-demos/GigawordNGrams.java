import com.aliasi.corpus.Parser;
import com.aliasi.corpus.TextHandler;

import com.aliasi.corpus.parsers.GigawordTextParser;

import com.aliasi.lm.TokenizedLM;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.ScoredObject;

import java.io.File;
import java.io.FileInputStream;

import java.util.zip.GZIPInputStream;

import org.xml.sax.InputSource;

public class GigawordNGrams {

    public static void main(String[] args) throws Exception {
        int maxNGram = Integer.valueOf(args[0]);
        int maxTerms = Integer.valueOf(args[1]);

        TokenizedLM lm = new TokenizedLM(IndoEuropeanTokenizerFactory.INSTANCE,
                                         maxNGram);

        Parser<TextHandler> parser = new GigawordTextParser(lm);
        for (int i = 2; i < args.length; ++i) {

            File dataDir = new File(args[i]);
            // System.out.println("Data Directory=" + dataDir);

            int fileCount = 0;
            for (File file : dataDir.listFiles()) {
                // if (fileCount++ > 0) break;
                // System.out.println("     Next training file=" + file);
                FileInputStream fileIn = new FileInputStream(file);
                GZIPInputStream zipIn = new GZIPInputStream(fileIn);
                InputSource in = new InputSource(zipIn);
                parser.parse(in);
                zipIn.close(); // not robust
            }
        }

        for (int nGram = 1; nGram <= maxNGram; ++nGram) {
            // System.out.println("\n# NGRAM SIZE=" + nGram);
            for (ScoredObject<String[]> collocation : lm.frequentTermSet(nGram,maxTerms)) {
                String[] nGramTokens = collocation.getObject();
                double count = collocation.score();
                System.out.print((int) (count + 0.5));
                for (String token : nGramTokens)
                    System.out.print(" " + token);
                System.out.print("\n");
            }
        }
    }

}