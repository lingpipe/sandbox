package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class TableOfBodys extends Table {

    public TableOfBodys() {
        this(true);
    }

    TableOfBodys(boolean mutable) {
        super(mutable);
    }

    public List rowList() {
        return mRowList.isEmpty()
            ? Collections.singletonList(new TBody())
            : mRowList;
    }

    public void add(TBody body) {
        mRowList.add(body);
    }
}

