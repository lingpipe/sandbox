import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.File;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;

public class NamedEntityRecognition {

    static final TokenizerFactory TOKENIZER_FACTORY 
        = new IndoEuropeanTokenizerFactory();
    static final int NUM_CHUNKINGS_RESCORED = 1024;
    static final int NGRAM_ORDER = 8;
    static final int NUM_CHARS = 128;
    static final double INTERPOLATION_RATIO = 8;

    public static void main(String[] args) throws Exception {
        System.out.println("BIOCREATIVE 2006: NAMED ENTITY RECOGNITION");

        File sentenceTextFile = new File(args[0]);
        File goldStandardFile = new File(args[1]);
        File testFile = new File(args[2]);
        File outputFile = new File(args[3]);
        System.out.println("\nInput Parameters");
        System.out.println("  sentence text file=" + sentenceTextFile);
        System.out.println("  gold standard file=" + goldStandardFile);
        System.out.println("  test file=" + testFile);
        System.out.println("  output file=" + outputFile);
        
        CharLmRescoringChunker chunker 
            = new CharLmRescoringChunker(TOKENIZER_FACTORY,
                                         NUM_CHUNKINGS_RESCORED,
                                         NGRAM_ORDER,
                                         NUM_CHARS,
                                         INTERPOLATION_RATIO);
        // train
        System.out.println("\nTraining Chunker");
        Biocreative2006ChunkParser parser 
            = new Biocreative2006ChunkParser(goldStandardFile,chunker);
        parser.parse(sentenceTextFile);

        // compile
        System.out.println("\nCompiling (in memory)");
        Chunker compiledChunker 
            = (Chunker) AbstractExternalizable.compile(chunker);

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
            Chunking chunking = compiledChunker.chunk(sentenceText);
            Set chunkSet = chunking.chunkSet();
            Chunk[] chunks = (Chunk[]) chunkSet.toArray(new Chunk[chunkSet.size()]);
            Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);
            for (int j = 0; j < chunks.length; ++j) {
                int start = mapping[chunks[j].start()];
                int end = mapping[chunks[j].end()] - 1;
                String line = id + "|" + start + " " + end + "|\n";
                System.out.print(line);
                sb.append(line);
            }
        }
        Files.writeStringToFile(sb.toString(),outputFile,Strings.UTF8);
    }

}
