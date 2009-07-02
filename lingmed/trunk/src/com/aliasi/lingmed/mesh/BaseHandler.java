package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

abstract class BaseHandler<E> extends DelegateHandler {

    public BaseHandler(DelegatingHandler parent) {
        super(parent);
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        reset();
    }

    public void reset() {
    }

    abstract public E getObject();

}