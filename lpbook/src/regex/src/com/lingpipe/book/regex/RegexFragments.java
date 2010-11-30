package com.lingpipe.book.regex;

import java.util.regex.Pattern;

class RegexFragments {

    void one() {
        /*x RegexFragments.1 */
        Pattern p1 = Pattern.compile("(xyz)*");
        Pattern p2 = Pattern.compile("(xyz(xyz)*)|");
        /*x*/
    }

}