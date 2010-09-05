package com.lingpipe.book.corpus;

import com.aliasi.corpus.ObjectHandler;

/*x CountingTextHandler.1 */
public class CountingTextHandler
    implements ObjectHandler<CharSequence> {

    long mNumChars = 0L;
    long mNumSeqs = 0L;

    public void handle(CharSequence cs) {
        mNumChars += cs.length();
        ++mNumSeqs;
    }
/*x*/

    public long numChars() {
        return mNumChars;
    }

    public long numSeqs() {
        return mNumSeqs;
    }


    @Override
    public String toString() {
        return "# seqs=" + numSeqs() + " # chars=" + numChars();
    }

    public static void main(String[] args) {
        /*x CountingTextHandler.2 */
        CountingTextHandler handler = new CountingTextHandler();
        handler.handle("hello world");
        handler.handle("goodbye");
        /*x*/
        System.out.println(handler);
    }

}