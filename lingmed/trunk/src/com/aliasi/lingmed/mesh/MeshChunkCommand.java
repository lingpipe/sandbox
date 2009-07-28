package com.aliasi.lingmed.mesh;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob Carpenter
 * @version 1.4
 * @since LingMed1.4
 */
public class MeshChunkCommand {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File chunkerModelFile = new File(args[0]);
        System.out.println("Reading model from file=" + chunkerModelFile);
        Chunker chunker = (Chunker) AbstractExternalizable.readObject(chunkerModelFile);
        for (int i = 1; i < args.length; ++i) {
            String text = args[i];
            Chunking chunking = chunker.chunk(text);
            String chunkedText = chunking.charSequence().toString();
            System.out.println(chunkedText);
            for (Chunk chunk : chunking.chunkSet()) {
                int start = chunk.start();
                int end = chunk.end();
                String heading = chunk.type();
                String textSpan = chunkedText.substring(start,end);
                System.out.printf("(%5d,%5d) %s [heading=%s]\n",
                                  start,end,textSpan,heading);
            }
        }
    }
    

}