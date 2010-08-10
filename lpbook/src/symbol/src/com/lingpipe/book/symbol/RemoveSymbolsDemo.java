package com.lingpipe.book.symbol;

import com.aliasi.symbol.MapSymbolTable;

public class RemoveSymbolsDemo {

    public static void main(String[] args) {
        MapSymbolTable st = new MapSymbolTable();

        /*x RemoveSymbolsDemo.1 */
        st.getOrAddSymbol("foo");
        st.getOrAddSymbol("bar");
        int n1 = st.numSymbols();

        st.removeSymbol("foo");
        int n2 = st.numSymbols();

        st.clear();
        int n3 = st.numSymbols();
        /*x*/

        System.out.println("n1=" + n1 
                           + "     n2=" + n2
                           + "     n3=" + n3);
        
    }

}