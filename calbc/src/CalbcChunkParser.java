import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.ChunkHandler;
import com.aliasi.corpus.XMLParser;

import com.aliasi.util.ObjectToCounterMap;

import com.aliasi.xml.DelegatingHandler;

import java.util.ArrayList;
import java.util.List;


import org.xml.sax.Attributes;

import org.xml.sax.helpers.DefaultHandler;

public class CalbcChunkParser extends XMLParser<ChunkHandler> {

    static final String SENTENCE_TAG = "s";
    static final String ENTITY_TAG = "e";
    static final String ENTITY_TYPE_ATT ="id";

    long mCharCount = 0L;
    long mSentCount = 0L;
    final ObjectToCounterMap<String> mEntityTypeCounter
        = new ObjectToCounterMap<String>();

    @Override
    protected DefaultHandler getXMLHandler() {
        return new CalbcHandler();
    }

    public void report() {
        System.out.println("#Chars=" + mCharCount);
        System.out.println("#Sents=" + mSentCount);
        System.out.println("Entity Type Counts=\n" + mEntityTypeCounter);
    }

    class CalbcHandler extends DelegatingHandler {
        SentenceHandler mSentHandler;
        CalbcHandler() {
            mSentHandler = new SentenceHandler();
            setDelegate(SENTENCE_TAG,mSentHandler);
        }
        @Override
        public void finishDelegate(String qName, DefaultHandler handler) {
            ++mSentCount;
            Chunking chunking = mSentHandler.getChunking();
            ChunkHandler chunkHandler = getHandler();
            chunkHandler.handle(chunking);
        }
    }

    class SentenceHandler extends DefaultHandler {
        StringBuilder mBuf;
        String mType;
        int mStart;
        int mEnd;
        final List<Chunk> mChunkList = new ArrayList<Chunk>();
        @Override
        public void startDocument() {
            mBuf = new StringBuilder();
            mChunkList.clear();
        }
        @Override
        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes) {
            if (!ENTITY_TAG.equals(qName)) return;
            mType = attributes.getValue(ENTITY_TYPE_ATT);
            mEntityTypeCounter.increment(mType);
            mStart = mBuf.length();
        }
        @Override
        public void endElement(String uri, String localName, String qName) {
            if (!ENTITY_TAG.equals(qName)) return;
            mEnd = mBuf.length();
            Chunk chunk = ChunkFactory.createChunk(mStart,mEnd,mType,0);
            mChunkList.add(chunk);
        }
        @Override
        public void characters(char[] cs, int start, int length) {
            mCharCount += length;
            mBuf.append(cs,start,length);
        }
        public Chunking getChunking() {
            ChunkingImpl chunking = new ChunkingImpl(mBuf);
            for (Chunk chunk : mChunkList)
                chunking.add(chunk);
            return chunking;
        }
    }

}
