package com.aliasi.annotate.corpora;

import com.aliasi.corpus.XMLParser;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XMLParser;

import com.aliasi.xml.DelegatingHandler;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xml.sax.Attributes;

import org.xml.sax.helpers.DefaultHandler;

/**
 * An <code>AnnotatorCorpusParser</code> provides a chunk parser for
 * the XML file format produced by the LingPipe graphical annotation
 * tool.
 *
 * <p>A corpus parser is constructed from a set of types.  These
 * are the types of chunk elements the content of which will be
 * used for training.  All chunks within the specified chunks
 * will be treated as training data.
 *
 * <p>For example, consider an XML document with the following content:
 *
 * <blockquote><pre>
 * &lt;document&gt;
 *   &lt;head&gt;
 *     &lt;chunk type=&quot;title&quot;&gt;This is a title&lt;/chunk&gt;
 *   &lt;/head&gt;
 *   &lt;body&gt;
 *     &lt;chunk type=&quot;p&quot;&gt;
 *       &lt;chunk type=&quot;s&quot;&gt;See &lt;chunk type=&quot;PERSON&quot;&gt;John&lt;/chunk&gt;.&lt;/chunk&gt;
 *       &lt;chunk type=&quot;s&quot;&gt;See &lt;chunk type=&quot;PERSON&quot;&gt;John&lt;/chunk&gt; run.&lt;/chunk&gt;
 *     &lt;/chunk&gt;
 *     &lt;chunk type=&quot;p&quot;&gt;
 *       &lt;chunk type=&quot;s&quot;&gt;See &lt;chunk type=&quot;ANIMAL&quot;&gt;Spot&lt;/chunk&gt;.&lt;/chunk&gt;
 *       &lt;chunk type=&quot;s&quot;&gt;See &lt;chunk type=&quot;ANIMAL&quot;&gt;Spot&lt;/chunk&gt; jump.&lt;/chunk&gt;
 *     &lt;/chunk&gt;
 *   &lt;/body&gt;
 * &lt;/document&gt;
 * </pre></blockquote>
 *
 * We may set the set of chunk types to be <code>{ "title", "s" }</code>,
 * indicating we will train within sentences and within the title.  The
 * higher-level containing chunk of type <code>p</code> is ignored, as
 * are the non-chunk elements <code>head</code> and <code>body</code>.
 * Within the sentence chunks, there is text content and entity chunks,
 * in this case of type <code>PERSON</code> and <code>ANIMAL</code>.
 *
 * @author  Bob Carpenter
 * @version 3.2
 * @since   LingPipe3.2
 */
public class AnnotatorCorpusParser extends XMLParser<ObjectHandler<Chunking>> {

    final Set<String> mContainingTypeSet;

    /**
     * Construct an annotator corpus parser which extracts annotations
     * within chunk elements of any type in the specified type set.
     *
     * @param containingTypeSet Set of chunk types the content of which
     * is used for training.
     */
    public AnnotatorCorpusParser(Set<String> containingTagSet) {
        super();
        mContainingTypeSet = containingTagSet;
    }

    /**
     * For internal use only, this provides the SAX handler
     * to the underlying XML parser.
     *
     * @return The SAX handler for this corpus parser.
     */
    protected DefaultHandler getXMLHandler() {
        return new DocHandler(getHandler());
    }

    class DocHandler extends DefaultHandler {
        StringBuilder mBuf;
        String mType;
        int mStart;
        int mEnd;
        final List<Chunk> mChunkList = new ArrayList<Chunk>();
        boolean mInContainer;
        boolean mInEntity;

        ObjectHandler<Chunking> mHandler;

        DocHandler(ObjectHandler<Chunking> handler) {
            mHandler = handler;
        }
        public void startDocument() {
            mBuf = new StringBuilder();
            mChunkList.clear();
            mInContainer = false;
            mInEntity = false;
        }
        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes) {
            if (!mInContainer
                && "chunk".equals(qName)
                && mContainingTypeSet.contains(attributes.getValue("type"))) {

                mInContainer = true;
                mInEntity = false;
                mChunkList.clear();
                mBuf = new StringBuilder();
            } else if (mInContainer
                       && "chunk".equals(qName)) {

                mInEntity = true;
                mType = attributes.getValue("type");
                mStart = mBuf.length();
            }
        }
        public void endElement(String uri, String localName, String qName) {
            if (!mInContainer) return;
            if (mInEntity) {
                mEnd = mBuf.length();
                Chunk chunk = ChunkFactory.createChunk(mStart,mEnd,mType,0);
                mChunkList.add(chunk);
                mInEntity = false;
            } else {
                mInContainer = false;
                ChunkingImpl chunking = new ChunkingImpl(mBuf);
                for (Chunk chunk : mChunkList)
                    chunking.add(chunk);
                // System.out.println("Chunking=" + chunking);
                mHandler.handle(chunking);
            }
        }
        public void characters(char[] cs, int start, int length) {
            if (!mInContainer) return;
            mBuf.append(cs,start,length);
        }
    }

}
