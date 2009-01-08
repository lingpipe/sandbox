package com.aliasi.xhtml;

import java.util.List;
import java.util.ArrayList;

public abstract class Table 
    extends AbstractElement 
    implements LittleBlockElement {

    private Caption mCaption;
    private THead mHead;
    private TFoot mFoot;

    final ArrayList mColList = new ArrayList();
    final ArrayList mRowList = new ArrayList();

    Table(boolean mutable) {
        super(TABLE,mutable);
    }

    List contentsInternal() {
        List rowList = rowList();
        ArrayList list = new ArrayList(mColList.size() + rowList.size() + 3);
        if (mCaption != null) list.add(mCaption);
        list.addAll(mColList);
        if (mHead != null) list.add(mHead);
        if (mFoot != null) list.add(mFoot);
        list.addAll(rowList);
        return list;
    }

    abstract List rowList();


    public void setSummary(String text) {
        set(SUMMARY,text);
    }

    public void setWidth(String length) {
        set(WIDTH,length);
    }

    public void setBorder(String pixels) {
        set(BORDER,pixels);
    }

    public void setFrameVoid() {
        set(FRAME,"void");
    }
    public void setFrameAbove() {
        set(FRAME,"above");
    }
    public void setFrameBelow() {
        set(FRAME,"below");
    }
    public void setFrameHSides() {
        set(FRAME,"hsides");
    }
    public void setFrameLHS() {
        set(FRAME,"lhs");
    }
    public void setFrameRHS() {
        set(FRAME,"rhs");
    }
    public void setFrameVSides() {
        set(FRAME,"vsides");
    }
    public void setFrameBox() {
        set(FRAME,"box");
    }
    public void setFrameBorder() {
        set(FRAME,"border");
    }

    public void setRulesNone() {
        set(RULES,"none");
    }
    public void setRulesGroups() {
        set(RULES,"groups");
    }
    public void setRulesRows() {
        set(RULES,"rows");
    }
    public void setRulesCols() {
        set(RULES,"cols");
    }
    public void setRulesAll() {
        set(RULES,"all");
    }

    public void setCellSpacing(String length) {
        set(CELLSPACING,length);
    }

    public void setCellPadding(String length) {
        set(CELLPADDING,length);
    }

    public void setCaption(Caption caption) {
        mCaption = caption;
    }

    public void setHead(THead head) {
        mHead = head;
    }

    public void setFoot(TFoot foot) {
        mFoot = foot;
    }
    
}
