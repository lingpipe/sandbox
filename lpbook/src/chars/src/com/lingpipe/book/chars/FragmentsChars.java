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

    void abc1() {
        int codepoint = 0;
        /*x FragmentsChars.6 */
        int LEAD_OFFSET = 0xD800 - (0x10000 >> 10);
        int SURROGATE_OFFSET = 0x10000 - (0xD800 << 10) - 0xDC00;

        int lead = LEAD_OFFSET + (codepoint >> 10);
        int trail = 0xDC00 + (codepoint & 0x3FF);
        
        int byte1 = lead >>> 8;
        int byte2 = lead & 0xFF;
        int byte3 = trail >>> 8;
        int byte4 = trail & 0xFF;
        /*x*/

    }

    void abc2() {
        int byte1 = 0;
        int byte2 = 0;
        int byte3 = 0;
        int byte4 = 0;
        int SURROGATE_OFFSET = 0x10000 - (0xD800 << 10) - 0xDC00;

        /*x FragmentsChars.7 */
        int lead = byte1 << 8 | byte2;
        int trail = byte3 << 8 | byte4;
        /*x*/

        int codepoint = (lead << 10) + trail + SURROGATE_OFFSET;
    }

    void abc3() {
        /*x FragmentsChars.8 */
        int n = 1;
        /*x*/
    }

    void abc3b() {
        /*x FragmentsChars.9 */
        \u0069\u006E\u0074\u0020\u006E\u0020\u003D\u0020\u0031\u003B
        /*x*/
    }

    void abc4() {
        /*x FragmentsChars.10 */
        char c = 'a'; 
        /*x*/
    }

    void abc4b() {
        /*x FragmentsChars.11 */
        char c = '\u00E0'; 
        /*x*/
    }

    void abc4c() {
        /*x FragmentsChars.12 */
        char c = 0x00E0;
        /*x*/
    }

    void abc5() {
        /*x FragmentsChars.13 */
        int n = 'a';
        /*x*/
    }

    void abc6() {
        CharSequence cs = null;
        /*x FragmentsChars.14 */
        for (int i = 0; i < cs.length(); ++i) {
            char c = cs.charAt(i);
            // do something
        /*x*/
        }
    }

    void abc7() {
        /*x FragmentsChars.15 */
        String name = "Fred";
        /*x*/
    }


    void abc8() {
        /*x FragmentsChars.16 */
        String name = "\u0046r\u0065d";
        /*x*/
    }


    int abc9() {
        String s = null;
        String t = null;
        /*x FragmentsChars.17 */
        for (int i = 0; i < Math.min(s.length(),t.length()); ++i)
            if (s.charAt(i) != t.charAt(i))
                return s.charAt(i) - t.charAt(i);
        return s.length() - t.length();
        /*x*/
    }


    void abc10() {
        String s = null;
        /*x FragmentsChars.18 */
        int hashCode = 0;
        for (int i = 0; i < s.length(); ++i)
            hashCode = 31 * hashCode + s.charAt(i);
        /*x*/
    }

    void abc11() {
        String[] xs = null;
        /*x FragmentsChars.19 */
        StringBuilder sb = new StringBuilder();
        for (String x : xs)
            sb.append(x);
        String s = sb.toString();
        /*x*/
    }


    void abc12() {
        /*x FragmentsChars.20 */
        String s = "abc";
        for (int i = 0; i < s.length(); ) {
            int codepoint = s.codePointAt(i);
            // Do something with codepoint.
            i += Character.charCount(codepoint);
        }
        /*x*/
    }

   void abc13() {
       /*x FragmentsChars.21 */
       public static final char DONE = '\uFFFF';
        /*x*/
   }

}