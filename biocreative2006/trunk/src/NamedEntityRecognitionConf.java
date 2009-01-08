import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ConfidenceChunker;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.io.File;

import java.util.Iterator;

public class NamedEntityRecognitionConf {

    static final TokenizerFactory TOKENIZER_FACTORY 
        = new IndoEuropeanTokenizerFactory();
    static final int NGRAM_ORDER = 8;
    static final int NUM_CHARS = 128;
    static final double INTERPOLATION_RATIO = 8;
    static final int MAX_N_BEST_CHUNKS = 32;

    public static void main(String[] args) throws Exception {
        System.out.println("BIOCREATIVE 2006: NAMED ENTITY RECOGNITION");

        File sentenceTextFile = new File(args[0]);
        File goldStandardFile = new File(args[1]);
        File testFile = new File(args[2]);
        File outputFile = new File(args[3]);

        double confThreshold = Double.parseDouble(args[4]);

        System.out.println("\nInput Parameters");
        System.out.println("  sentence text file=" + sentenceTextFile);
        System.out.println("  gold standard file=" + goldStandardFile);
        System.out.println("  test file=" + testFile);
        System.out.println("  output file=" + outputFile);
        System.out.println("  confidence threshold=" + confThreshold);
        
        HmmCharLmEstimator hmmEstimator
            = new HmmCharLmEstimator(NGRAM_ORDER,NUM_CHARS,INTERPOLATION_RATIO);
        CharLmHmmChunker chunker
            = new CharLmHmmChunker(TOKENIZER_FACTORY,hmmEstimator);

        // train
        System.out.println("\nTraining Chunker");
        Biocreative2006ChunkParser parser 
            = new Biocreative2006ChunkParser(goldStandardFile,chunker);
        parser.parse(sentenceTextFile);

        // compile
        System.out.println("\nCompiling (in memory)");
        ConfidenceChunker compiledChunker 
            = (ConfidenceChunker) AbstractExternalizable.compile(chunker);

        // test
        System.out.println("\nRunning on Test File");
        StringBuffer sb = new StringBuffer();
        String testSet = Files.readFromFile(testFile,Strings.UTF8);
        String[] testCases = testSet.split("\n");
        for (int i = 0; i < testCases.length; ++i) {
            int spaceIndex = testCases[i].indexOf(' ');
            if (spaceIndex < 0) continue;
            String id = testCases[i].substring(0,spaceIndex);
            String sentenceText = testCases[i].substring(spaceIndex+1);
            int[] mapping = new int[sentenceText.length()+1];
            int offset = 0;
            for (int j = 0; j < sentenceText.length(); ++j) {
                mapping[j] = offset;
                if (sentenceText.charAt(j) != ' ')
                    ++offset;
            }
            mapping[mapping.length-1] = mapping[mapping.length-2] + 1;

            char[] cs = sentenceText.toCharArray();
            Iterator chunkIt 
                = compiledChunker.nBestChunks(cs,0,cs.length,MAX_N_BEST_CHUNKS);
            while (chunkIt.hasNext()) {
                Chunk chunk = (Chunk) chunkIt.next();
                double score = Math.pow(2.0,chunk.score());
                if (score >= confThreshold) {
                    int start = mapping[chunk.start()];
                    int end = mapping[chunk.end()] - 1;
                    String line = id + "|" + start + " " + end + "|"
                        + format(score) + "\n";
                    System.out.print(line);
                    sb.append(line);
                } else {
                    continue;
                }
            }
        }
        Files.writeStringToFile(sb.toString(),outputFile,Strings.UTF8);
    }

    static String format(double x) {
        return Strings.decimalFormat(x,"0.00000",7);
    }

}
