package com.aliasi.xhtml;

public class TableOfRowsWithColGroups extends TableOfRows {


    public TableOfRowsWithColGroups() {
        super(true);
    }

    public void add(ColGroup colGroup) {
        mColList.add(colGroup);
    }
}
