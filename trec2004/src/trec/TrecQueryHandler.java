package trec;

import com.aliasi.xml.SimpleElementHandler;

import org.xml.sax.Attributes;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TrecQueryHandler extends SimpleElementHandler {
    private StringBuffer mId = new StringBuffer();
    private StringBuffer mTitle = new StringBuffer();
    private StringBuffer mNeed = new StringBuffer();
    private StringBuffer mContext = new StringBuffer();
    private ArrayList mQueries = new ArrayList();
    private StringBuffer mContent;
    
    public Iterator queryIterator() {
	return mQueries.iterator();
    }
    public List queryList() {
	return Collections.unmodifiableList(mQueries);
    }
    public void startElement(String namespaceURI, String localName,
			     String qName, Attributes atts) {
	if (qName.equals(TOPIC_ELT)) {
	    mId.setLength(0);
	    mTitle.setLength(0);
	    mNeed.setLength(0);
	    mContext.setLength(0);
	} else if (qName.equals(ID_ELT)) {
	    mContent = mId;
	} else if (qName.equals(TITLE_ELT)) {
	    mContent = mTitle;
	} else if (qName.equals(NEED_ELT)) {
	    mContent = mNeed;
	} else if (qName.equals(CONTEXT_ELT)) {
	    mContent = mContext;
	}
    }
    public void endElement(String namespaceURI, String localName,
			   String qName) {
	if (qName.equals(TOPIC_ELT)) {
	    try { 
		TrecQuery query = new TrecQuery(mId.toString(),
						mTitle.toString(),
						mNeed.toString(),
						mContext.toString());

		mQueries.add(query);
	    } catch (IOException e) {
		// ignore
	    }
	}
	mContent = null;
    }
    public void characters(char[] ch, int start, int length) {
	if (mContent != null) {
	    mContent.append(ch,start,length);
	}
    }

    private static final String TOPIC_ELT = "TOPIC";
    private static final String ID_ELT = "ID";
    private static final String TITLE_ELT = "TITLE";
    private static final String NEED_ELT = "NEED";
    private static final String CONTEXT_ELT = "CONTEXT";

}
