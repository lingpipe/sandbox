package com.aliasi.xhtml;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Html extends AbstractInternationalizedElement {

    private Head mHead;
    private Body mBody;

    /*
    Html(Html html) {
        this(html.toConstant(),
             body.toConstant(),
             false);
    }
    */

    public Html() {
        this(new Head(), new Body());
    }

    public Html(Head head, Body body) { 
        this(head,body,true);
    }

    Html(Head head, Body body, boolean mutable) {
        super(HTML,null,mutable);
        mHead = head;
        mBody = body;
    }


    public Head head() {
        return mHead;
    }

    public Body body() {
        return mBody;
    }



    public void set(Head head) {
        throwUnsupportedExceptionIfNotMutable();
        mHead = head;
    }

    public void set(Body body) {
        throwUnsupportedExceptionIfNotMutable();
        mBody = body;
    }

    public void setXmlns() {
        throwUnsupportedExceptionIfNotMutable();
        set(XMLNS,"http://www.w3.org/1999/xhtml");
    }



    public List contents() {
        return contentsInternal();
    }

    List contentsInternal() {
        return Arrays.asList(new Content[] { mHead, mBody });
    }

    void propagateMask(int mask) { }

    boolean isCyclic(Set visitedSet) { 
        return false;
    }

}
