package com.aliasi.lingmed.mesh;

import com.aliasi.lingpipe.xml.DelegateHandler;
import com.aliasi.lingpipe.xml.DelegatingHandler;
import com.aliasi.lingpipe.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

class BaseListHandler<E> extends BaseHandler<List<E>> {
    

    private final BaseHandler<E> mHandler;
    private final List<E> mList;

    public BaseListHandler(DelegatingHandler parent,
                       BaseHandler<E> handler,
                       String tag) {
        super(parent);
        mHandler = handler;
        setDelegate(tag,mHandler);
        mList = new ArrayList<E>();
    }

    @Override
    public void reset() {
        mHandler.reset();
        mList.clear();
    }

    @Override
    public void finishDelegate(String qName, DefaultHandler handler) {
        mList.add(mHandler.getObject());
    }

    public List<E> getObject() {
        return new ArrayList<E>(mList);
    }



}
