package edu.uchicago.rzhetsky.ne;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;

import com.aliasi.util.Files;
import com.aliasi.util.ObjectToSet;

import java.io.File;
import java.io.IOException;

public class GeneTagChunkParser 
    extends StringParser<ObjectHandler<Chunking>> {

    ObjectToSet<String,Chunk> mIdToChunkSet
        = new ObjectToSet<String,Chunk>();

    /**
     * Construct a GeneTag chunk parser with the specified gold standard
     * file and no specified handler.
     *
     * @param goldFormatFile The gold standard format file.
     * @throws IOException If there is an I/O error reading the gold
     * standard file.
     */
    public GeneTagChunkParser(File goldFormatFile) throws IOException {
        this(goldFormatFile,null);
    }

    /**
     * Construct a GeneTag chunk parser with the specified gold standard
     * file and the specified chunk handler.
     *
     * @param goldFormatFile The gold standard format file.
     * @param handler Chunk handler for this parser.
     * @throws IOException If there is an I/O error reading the gold
     * standard file.
     */
    public GeneTagChunkParser(File goldFormatFile, 
                              ObjectHandler<Chunking> handler)
        throws IOException {

        super(handler);
        readChunks(goldFormatFile);
    }

    @Override
    public void parseString(char[] cs, int start, int end) {
        String s = new String(cs,start,end-start);
        String[] lines = s.split("\n");
        for (int i = 0; i < lines.length; ) {
            String id = lines[i++];
            String text = lines[i++];
            if (text.length() == 0) continue;
            int[] mapping = new int[text.length()];
            int target = 0;
            for (int k = 0; k < mapping.length; ++k)
                if (text.charAt(k) != ' ')
                    mapping[target++] = k;
            ChunkingImpl chunking = new ChunkingImpl(text);
            for (Chunk nextChunk : mIdToChunkSet.getSet(id)) {
                int chunkStart = mapping[nextChunk.start()];
                int chunkEnd = mapping[nextChunk.end()];
                Chunk remappedChunk
                    = ChunkFactory.createChunk(chunkStart,chunkEnd+1,
                                               GENE_CHUNK_TYPE);
                if (!overlap(remappedChunk,chunking))
                    chunking.add(remappedChunk);
            }
            getHandler().handle(chunking);
        }
    }

    static boolean overlap(Chunk chunk, Chunking chunking) {
        for (Chunk chunk2 : chunking.chunkSet())
            if (ChunkingImpl.overlap(chunk,chunk2))
                return true;
        return false;
    }


    /**
     * The type assigned to the chunks extracted by this parser,
     * namely <code>&quot;GENE&quot;</code>.
     */
    public static final String GENE_CHUNK_TYPE = "GENE";

    final void readChunks(File formatFile) throws IOException {
        String s = Files.readFromFile(formatFile,"ASCII");
        String[] lines = s.split("\n");
        for (int i = 0; i < lines.length; ++i)
            readChunk(lines[i]);
    }

    final void readChunk(String line) {
        int i = line.indexOf('|');
        if (i < 0) return;
        int j = line.indexOf('|',i+1);
        String sentenceId = line.substring(0,i);
        String numSection = line.substring(i+1,j);
        String[] nums = numSection.split(" ");
        int start = Integer.valueOf(nums[0]);
        int end = Integer.valueOf(nums[1]);
        Chunk chunk
            = ChunkFactory.createChunk(start,end);
        mIdToChunkSet.addMember(sentenceId,chunk);
    }


}
