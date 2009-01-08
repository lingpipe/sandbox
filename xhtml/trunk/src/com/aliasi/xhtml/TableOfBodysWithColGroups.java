package com.aliasi.xhtml;

public class TableOfBodysWithColGroups extends TableOfBodys {

    public TableOfBodysWithColGroups() {
        super(true);
    }

    public void add(ColGroup colGroup) {
        mColList.add(colGroup);
    }

}
