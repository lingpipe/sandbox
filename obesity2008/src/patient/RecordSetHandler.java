package patient;


import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.TextAccumulatorHandler;
import com.aliasi.xml.SimpleElementHandler;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class RecordSetHandler extends DelegatingHandler {

    Map<String,String> mIdToTextRecord = new HashMap<String,String>();
    DocEltHandler mDocEltHandler;

    public RecordSetHandler() {
        mDocEltHandler = new DocEltHandler(this);
        setDelegate(DOC_ELT,mDocEltHandler);
    }

    public void finishDelegate(String qName, DefaultHandler handler) {
        if (qName.equals(DOC_ELT)) {
            mIdToTextRecord.put(mDocEltHandler.mId,
                           mDocEltHandler.mTextHandler.getText());
        }
    }

    public Map<String,String> idToTextRecord() {
        return mIdToTextRecord;
    }

    static String DOC_ELT = "doc";
    static String TEXT_ELT = "text";
    static String ID_ATT = "id";

    static class DocEltHandler extends DelegateHandler {
        String mId;
        TextAccumulatorHandler mTextHandler = new TextAccumulatorHandler();

        public DocEltHandler(DelegatingHandler parent) {
            super(parent);
            setDelegate(TEXT_ELT,mTextHandler);
        }

        public void startDocument() throws SAXException {
            mId = null;
            super.startDocument();
            mTextHandler.reset();
        }

        public void startElement(String nameSpace, String localName, String qName , Attributes atts) 
            throws SAXException {

            if (qName.equals(DOC_ELT)) {
                mId = atts.getValue(ID_ATT);
            }
            super.startElement(nameSpace, localName, qName, atts);
        }

    }
}
