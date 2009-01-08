package com.aliasi.xhtml;

public class TableOfRowsWithCols extends TableOfRows {

    public TableOfRowsWithCols() {
        super(true);
    }

    public void add(Col col) {
        mColList.add(col);
    }
    
}
