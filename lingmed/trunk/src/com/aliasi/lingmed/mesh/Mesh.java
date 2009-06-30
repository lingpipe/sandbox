package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An instance of {@code Mesh} represents a single concept
 * from the Medical Subject Headings (MeSH).
 */
public class Mesh {

    private final DescriptorClass mDescriptorClass;
    private final String mDescriptorUI;
    private final String mDescriptorName;

    public Mesh(DescriptorClass descriptorClass,
                String descriptorUI,
                String descriptorName) {
        mDescriptorClass = descriptorClass;
        mDescriptorUI = descriptorUI;
        mDescriptorName = descriptorName;
    }

    public DescriptorClass descriptorClass() {
        return mDescriptorClass;
    }

    public String descriptorUI() {
        return mDescriptorUI;
    }

    public String descriptorName() {
        return mDescriptorName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Descriptor UI=" + descriptorUI() + "\n");
        sb.append("Descriptor Class=" + descriptorClass() + "\n");
        sb.append("Descriptor Name=" + descriptorName() + "\n");
        return sb.toString();
    }

    static class Handler extends DelegateHandler {
        private DescriptorClass mDescriptorClass;
        private String mDescriptorUI;
        private String mDescriptorName;
        public Handler(DelegatingHandler parent) {
            super(parent);
        }

        @Override
        public void startDocument() {
            mDescriptorClass = null;
            mDescriptorUI = null;
            mDescriptorName = null;
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) 
            throws SAXException {
            if (MeshParser.DESCRIPTOR_RECORD_ELEMENT.equals(qName)) {
                String descriptorString = atts.getValue(MeshParser.DESCRIPTOR_CLASS_ATT);
                if ("2".equals(descriptorString))
                    mDescriptorClass = DescriptorClass.TWO;
                else if ("3".equals(descriptorString))
                    mDescriptorClass = DescriptorClass.THREE;
                else if ("4".equals(descriptorString))
                    mDescriptorClass = DescriptorClass.FOUR;
                else
                    mDescriptorClass = DescriptorClass.ONE;
            }                    
            
        }
        public Mesh getMesh() {
            return new Mesh(mDescriptorClass,
                            mDescriptorUI,
                            mDescriptorName);
        }
    }
    
}