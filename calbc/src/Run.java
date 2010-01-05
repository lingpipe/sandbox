import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;

public class Run {

    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {

        File modelFile = new File(args[0]);
        Chunker neChunker
            = (Chunker) AbstractExternalizable.readObject(modelFile);
        
        SentenceModel sentenceModel = new MedlineSentenceModel();

        Chunker sentenceChunker
            = new SentenceChunker(IndoEuropeanTokenizerFactory.INSTANCE,
                                  sentenceModel);

        for (int i = 1; i < args.length; ++i) {
            String text = args[i];
            Chunking sentenceChunking = sentenceChunker.chunk(text);
            for (Chunk sentenceChunk : sentenceChunking.chunkSet()) {
                int start = sentenceChunk.start();
                int end = sentenceChunk.end();
                String sentence = text.substring(start,end);
                Chunking neChunking
                    = neChunker.chunk(sentence);
                System.out.println("|" + sentence + "|");
                for (Chunk chunk : neChunking.chunkSet()) {
                    int neStart = chunk.start();
                    int neEnd = chunk.end();
                    String neType = chunk.type();
                    String neText = sentence.substring(neStart,neEnd);
                    System.out.println("   " + neText + "  type=" + neType);
                }
            }
        }
        

    }


}