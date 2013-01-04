package com.lingpipe.book.regex;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

class RegexFragments {

    void one() {
        /*x RegexFragments.1 */
        Pattern p1 = Pattern.compile("(xyz)*");
        Pattern p2 = Pattern.compile("(xyz(xyz)*)|");
        /*x*/
    }

    void two() {
        /*x RegexFragments.2 */
        Pattern p = Pattern.compile("cat");
        Matcher m = p.matcher("one cat two cats in the yard");
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "dog");
        }
        m.appendTail(sb);
        System.out.println(sb.toString());
        /*x*/
    }
    
    /*x RegexFragments.3 */
        public String[] split(String regex) {
            return split(regex, 0);
        }

        public String[] split(String regex, int limit) {
            return Pattern.compile(regex).split(this, limit);
        }
    }

}
