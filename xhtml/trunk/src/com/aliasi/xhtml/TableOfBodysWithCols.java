package com.aliasi.xhtml;

public class TableOfBodysWithCols extends TableOfBodys {

    public TableOfBodysWithCols() {
        super(true);
    }

    public void add(Col col) {
        mColList.add(col);
    }

}
