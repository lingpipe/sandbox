package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class TableOfRows extends Table {

    public TableOfRows() {
        this(true);
    }

    TableOfRows(boolean mutable) {
        super(mutable);
    }

    public List rowList() {
        return mRowList.isEmpty()
            ? Collections.singletonList(new Tr())
            : mRowList;
    }

    public void add(Tr row) {
        mRowList.add(row);
    }
}

