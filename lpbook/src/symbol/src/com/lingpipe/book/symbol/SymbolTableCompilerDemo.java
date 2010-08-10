package com.lingpipe.book.symbol;

import com.aliasi.symbol.SymbolTable;
import com.aliasi.symbol.SymbolTableCompiler;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;

public class SymbolTableCompilerDemo {

    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {
        
        /*x SymbolTableCompilerDemo.1 */
        SymbolTableCompiler stc = new SymbolTableCompiler();
        stc.addSymbol("foo");
        stc.addSymbol("bar");
        
        int n1 = stc.symbolToID("foo");
        int n2 = stc.symbolToID("bar");
        int n3 = stc.symbolToID("bazz");
        /*x*/
        
        /*x SymbolTableCompilerDemo.2 */
        SymbolTable st 
            = (SymbolTable) 
            AbstractExternalizable.compile(stc);

        int n4 = stc.symbolToID("foo");
        int n5 = stc.symbolToID("bar");
        int n6 = stc.symbolToID("bazz");

        int n7 = st.symbolToID("foo");
        int n8 = st.symbolToID("bar");
        int n9 = st.symbolToID("bazz");
        /*x*/

        System.out.println("n1=" + n1 + "     n2=" + n2 + "     n3=" + n3);
        System.out.println("n4=" + n4 + "     n5=" + n5 + "     n6=" + n6);
        System.out.println("n7=" + n7 + "     n8=" + n8 + "     n9=" + n9);
        System.out.println("st.getClass()=" + st.getClass());

    }

}