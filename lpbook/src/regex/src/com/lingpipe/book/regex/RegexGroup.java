package com.lingpipe.book.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexGroup {

    public static void main(String[] args) {
        String regex = args[0];
        String text = args[1];
        /*x RegexGroup.1 */
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        for (int i = 0; i < matcher.groupCount(); ++i) {
            String group = matcher.group(i);
        /*x*/
            System.out.printf("Group %2d=|%s|\n",i,group);
        }
    }

}