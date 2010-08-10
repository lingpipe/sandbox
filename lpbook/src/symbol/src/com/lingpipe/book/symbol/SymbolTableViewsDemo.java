package com.lingpipe.book.symbol;

import com.aliasi.symbol.MapSymbolTable;

import java.util.Set;

public class SymbolTableViewsDemo {

    private SymbolTableViewsDemo() { }

    public static void main(String[] args) {
        
        /*x SymbolTableViewsDemo.1 */
        MapSymbolTable st = new MapSymbolTable();
        Set<String> symbolSet = st.symbolSet();
        Set<Integer> idSet = st.idSet();
        System.out.println("symbolSet=" + symbolSet + " idSet=" + idSet);

        st.getOrAddSymbol("foo");
        st.getOrAddSymbol("bar");
        System.out.println("symbolSet=" + symbolSet + " idSet=" + idSet);

        st.removeSymbol("foo");
        System.out.println("symbolSet=" + symbolSet + " idSet=" + idSet);
        /*x*/
    }

}