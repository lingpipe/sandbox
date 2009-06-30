package com.aliasi.lingmed.mesh;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XMLParser;

import com.aliasi.xml.DelegatingHandler;

import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;


public class MeshParser extends XMLParser<ObjectHandler<Mesh>> {


    protected DefaultHandler getXMLHandler() {
        return new XMLHandler();
    }
    
    class XMLHandler extends DelegatingHandler {
        final Mesh.Handler mHandler;
        XMLHandler() {
            mHandler = new Mesh.Handler(this);
            setDelegate(DESCRIPTOR_RECORD_ELEMENT,
                        mHandler);
        }
        @Override
        public void finishDelegate(String element,
                                   DefaultHandler handler) {
            getHandler().handle(mHandler.getMesh());
        }
        @Override
        public InputSource resolveEntity(String publicId, String systemId) 
            throws SAXException {
            if (systemId.endsWith("desc2009.dtd")) {
                InputStream in = this.getClass().getResourceAsStream("/com/aliasi/lingmed/mesh/desc2009.dtd");
                return new InputSource(in);
            } 
            // return super.resolveEntity(publicId,systemId);
            throw new UnsupportedOperationException("bah");
        }
    }

    static final String DESCRIPTOR_NAME_ELEMENT = "DescriptorName";
    static final String DESCRIPTOR_RECORD_ELEMENT = "DescriptorRecord";
    static final String DESCRIPTOR_UI_ELEMENT = "DescriptorUI";
    static final String STRING_ELEMENT = "String";
    

    static final String DESCRIPTOR_CLASS_ATT = "DescriptorClass";

    
}