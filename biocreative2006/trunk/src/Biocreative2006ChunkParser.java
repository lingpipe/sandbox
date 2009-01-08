import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.ChunkHandler;
import com.aliasi.corpus.StringParser;

import com.aliasi.util.Files;
import com.aliasi.util.ObjectToSet;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Biocreative2006ChunkParser extends StringParser {
    
    ObjectToSet mIdToChunkSet = new ObjectToSet();

    public Biocreative2006ChunkParser(File goldFormatFile) throws IOException {
        this(goldFormatFile,null);
    }

    public Biocreative2006ChunkParser(File goldFormatFile, ChunkHandler handler) 
        throws IOException {

        super(handler);
        readChunks(goldFormatFile);
    }

    public ChunkHandler getChunkHandler() {
        return (ChunkHandler) super.getHandler();
    }

    public void parseString(char[] cs, int start, int end) {
        String s = new String(cs,start,end-start);
        String[] lines = s.split("\n");
        for (int i = 0; i < lines.length; ++i) {
            int firstSpaceIdx = lines[i].indexOf(' ');
            if (firstSpaceIdx < 0) continue;
            String id = lines[i].substring(0,firstSpaceIdx);
            String text = lines[i].substring(firstSpaceIdx+1);
            if (text.length() == 0) continue;
            int[] mapping = new int[text.length()];
            int target = 0;
            for (int k = 0; k < mapping.length; ++k)
                if (text.charAt(k) != ' ')
                    mapping[target++] = k;
            ChunkingImpl chunking = new ChunkingImpl(text);
            Set chunkSet = mIdToChunkSet.getSet(id);
            Iterator it = chunkSet.iterator();
            List chunkList = new ArrayList();
            while (it.hasNext()) {
                Chunk nextChunk = (Chunk) it.next();
                int chunkStart = mapping[nextChunk.start()];
                int chunkEnd = mapping[nextChunk.end()];
                Chunk remappedChunk 
                    = ChunkFactory.createChunk(chunkStart,chunkEnd+1,
                                               GENE_CHUNK_TYPE);
                if (!overlap(remappedChunk,chunkList)) {
                    chunkList.add(remappedChunk);
                    chunking.add(remappedChunk);
                }
            }
            // System.out.println("id=" + id + " Chunking=" + chunking);
            getChunkHandler().handle(chunking);
        }
    }

    static boolean overlap(Chunk chunk, List chunkList) {
        for (int i = 0; i < chunkList.size(); ++i)
            if (overlap(chunk,(Chunk) chunkList.get(i)))
                return true;
        return false;
    }

    static boolean overlap(Chunk chunk1, Chunk chunk2) {
        boolean result=  overlap(chunk1.start(),chunk1.end(),
                                 chunk2.start(),chunk2.end());
        if (result)
            System.out.println("     found overlapping chunks=" 
                               + chunk1 + "," + chunk2);
        return result;
    }

    static boolean overlap(int start1, int end1,
                           int start2, int end2) {
        return (start1 <= start2
                && start2 < end1)
            || (start2 < start1
                && start1 < end2);
    }

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
        int start = Integer.parseInt(nums[0]);
        int end = Integer.parseInt(nums[1]);
        Chunk chunk 
            = ChunkFactory.createChunk(start,end);
        mIdToChunkSet.addMember(sentenceId,chunk);
    }

    public static final String GENE_CHUNK_TYPE = "GENE";

}
