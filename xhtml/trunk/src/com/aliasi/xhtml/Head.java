package com.aliasi.xhtml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Head extends AbstractInternationalizedElement {

    Title mTitle;
    Base mBase;

    public Head() {
        this(new Title());
    }

    public Head(Title title) {
        this(title,null);
    }

    public Head(Title title, Base base) {
        super(HEAD,true);
        mTitle = title; 
        mBase = base;
    }

    public Title title() {
        return mTitle;
    }

    public Base base() {
        return mBase;
    }

    public void setProfile(String uri) {
        set(PROFILE,uri);
    }

    public void set(Title title) {
        mTitle = title;
    }

    public void set(Base base) {
        mBase = base;
    }

    public void add(Script script) {
        addContent(script);
    }

    public void add(Style style) {
        addContent(style);
    }

    public void add(Meta meta) {
        addContent(meta);
    }

    public void add(Link link) {
        addContent(link);
    }
    
    public void add(Object obj) {
        addContent(obj);
    }

    public List contents() {
        return contentsInternal();
    }

    List contentsInternal() {
        List contentList = new ArrayList();
        contentList.add(mTitle);
        if (mBase != null)
            contentList.add(mBase);
        contentList.addAll(super.contentsInternal());
        return contentList;
    }

    void propagateMask(int mask) { }

    boolean isCyclic(Set visitedSet) { 
        return false;
    }

}
