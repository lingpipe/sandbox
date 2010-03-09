import com.aliasi.corpus.ChunkTagHandlerAdapter;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;

import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IupacChunkParser extends StringParser<ObjectHandler<Chunking>> {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File trainFile = new File(args[0]);
        File medlineTestFile = new File(args[1]);
        File chemTestFile = new File(args[2]);

        CharLmHmmChunker chunkerTrainer
            = new CharLmHmmChunker(IndoEuropeanTokenizerFactory.INSTANCE,
                                   new HmmCharLmEstimator(5,258,5.0,true));

        CharLmRescoringChunker chunkerTrainer2 
            = new CharLmRescoringChunker(IndoEuropeanTokenizerFactory.INSTANCE,
                                         128,
                                         8,
                                         256,
                                         8.0);

        System.out.println("TRAINING");
        IupacChunkParser parser = new IupacChunkParser();
        parser.setHandler(chunkerTrainer);
        parser.parse(trainFile);

        System.out.println("COMPILING");
        Chunker compiledChunker = (Chunker) AbstractExternalizable.compile(chunkerTrainer);

        System.out.println("EVALUATING");
        ChunkerEvaluator evaluator = new ChunkerEvaluator(compiledChunker);
        
        parser.setHandler(evaluator);
        parser.parse(medlineTestFile);
        
        System.out.println("RESULTS");
        System.out.println(evaluator);
        
        double[][] prCurve = evaluator.confidenceEvaluation().prCurve(true);
        System.out.printf("%5s %5s\n","R","P");
        for (double[] pt : prCurve)
            System.out.printf("%5.3f %5.3f\n",pt[0],pt[1]);
    }

    static class AccumHandler implements ObjectHandler<Chunking> {
        List<Chunking> chunkingList = new ArrayList<Chunking>();
        public void handle(Chunking chunking) {
            chunkingList.add(chunking);
        }
    }

    public IupacChunkParser() {
    }

    public void parseString(char[] cs, int start, int end) {
        String s = new String(cs,start,end-start);
        String[] lines = s.split("\n");
        for (int pos = 0; pos < lines.length; ++pos) {
            pos = scan(pos,lines);
        }
    }
    
    int scan(int pos, String[] lines) {
        while (pos < lines.length && lines[pos].startsWith("#"))
            ++pos;

        List<String> whitespaceList = new ArrayList<String>();
        List<String> tokenList = new ArrayList<String>();
        List<String> tagList = new ArrayList<String>();
        String lastTag = "O";
        for (int lastEnd = 0; pos < lines.length; ++pos) {
            if (lines[pos].length() == 0) 
                break;
            String[] fields = lines[pos].split("\t");

            if (fields.length < 4 || fields.length > 5)
                throw new RuntimeException("bad line # fields=" + fields.length + " line=" + lines[pos]
                                           + " fields=" + Arrays.asList(fields));
            String token = fields[0];
            int start = Integer.valueOf(fields[1]);
            int end = Integer.valueOf(fields[2]);


            int tagPos = fields.length-1;
            if (fields[tagPos].length() == 0 || fields[tagPos].charAt(0) != '|')
                throw new RuntimeException("bad tag=" + fields[tagPos] 
                                           + " line=" + lines[pos] 
                                           + " fields=" + Arrays.asList(fields));
            
            String tag = fields[tagPos].substring(1); // remove initial vbar
            if ("O".equals(tag) && !"O".equals(lastTag) && !Strings.allPunctuation(token)) {
                ++start;
                ++end;
            }
            tokenList.add(token);
            whitespaceList.add(start > lastEnd ? " " : "");
            tagList.add(tag);
            lastTag = tag;
            lastEnd = end;
        }
        whitespaceList.add("");
        Chunking chunking 
            = ChunkTagHandlerAdapter
            .toChunkingBIO(tokenList.toArray(Strings.EMPTY_STRING_ARRAY),
                           whitespaceList.toArray(Strings.EMPTY_STRING_ARRAY),
                           tagList.toArray(Strings.EMPTY_STRING_ARRAY));
        for (Chunking subChunking : sentences(chunking))
            getHandler().handle(subChunking);
        
        return pos;
    }
    

    static Chunker SENTENCE_CHUNKER
        = new SentenceChunker(IndoEuropeanTokenizerFactory.INSTANCE,
                              new MedlineSentenceModel());

    public Set<Chunking> sentences(Chunking chunking) {
        CharSequence cs = chunking.charSequence();
        Chunking sentChunking = SENTENCE_CHUNKER.chunk(cs);
        //         System.out.println("SENT CHUNKING=" + sentChunking);
        Set<Chunk> sentences = sentChunking.chunkSet();
        Set<Chunk> entities = chunking.chunkSet();
        Set<Chunking> resultSet = new HashSet<Chunking>();
        for (Chunk sentence : sentences) {
            int start = sentence.start();
            int end = sentence.end();
            CharSequence cSeq = cs.subSequence(start,end);
            ChunkingImpl subChunking = new ChunkingImpl(cSeq);
            for (Chunk chunk : entities) {
                if (contained(chunk,start,end)) {
                    Chunk subChunk = ChunkFactory.createChunk(chunk.start()-start,
                                                              chunk.end()-start,
                                                              chunk.type());
                    subChunking.add(subChunk);
                }
            }
            resultSet.add(subChunking);
        }
        return resultSet;
    }

    static boolean contained(Chunk chunk, int start, int end) {
        return chunk.start() >= start
            && chunk.end() <= end;
    }

}