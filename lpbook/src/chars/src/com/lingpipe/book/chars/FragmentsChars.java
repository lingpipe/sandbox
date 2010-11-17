package com.aliasi.lingpipe.chars;

import java.nio.CharBuffer;

class FragmentsChars {

    void foo() {

        /*x FragmentsChars.1 */
        String s = new String("abc");
        String t = new String("abc");
        /*x*/

        /*x FragmentsChars.2 */
        String sIn = s.intern();
        String tIn = t.intern();
        /*x*/
    }

    void bar() {
        /*x FragmentsChars.3 */
        int n = 7;    String s = "ABC";    boolean b = true;  
        char c = 'C';    Object z = null;
        String t = new StringBuilder().append(n).append(s).append(b)
            .append(c).append(z).toString();
        /*x*/
    }

    void baz() {
        /*x FragmentsChars.4 */
        String u = 7 + "ABC";
        String v = String.valueOf(7) + "ABC";
        /*x*/
    }

    void ping() {
        /*x FragmentsChars.5 */
        String[] xs = new String[] { "a", "b", "c" };
        CharBuffer cb = CharBuffer.allocate(1000);
        for (String s : xs)
            cb.put(s);
        cb.flip();
        String s = cb.toString();
        /*x*/
    }


}