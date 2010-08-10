package com.lingpipe.book.symbol;

import com.aliasi.symbol.MapSymbolTable;

public class SymbolTableDemo {

    private SymbolTableDemo() { }

    public static void main(String[] args) {
        
        /*x SymbolTableDemo.1 */
        MapSymbolTable st = new MapSymbolTable();

        int id1 = st.getOrAddSymbol("foo");
        int id2 = st.getOrAddSymbol("bar");
        int id3 = st.getOrAddSymbol("foo");
        
        int id4 = st.symbolToID("foo");
        int id5 = st.symbolToID("bazz");

        String s1 = st.idToSymbol(0);
        String s2 = st.idToSymbol(1);

        int n = st.numSymbols();
        /*x*/

        System.out.println("id1=" + id1);
        System.out.println("id2=" + id2);
        System.out.println("id3=" + id3);
        System.out.println("id4=" + id4);
        System.out.println("id5=" + id5);

        System.out.println("s1=" + s1);
        System.out.println("s2=" + s2);
        System.out.println("n=" + n);
    }
}

