package com.lingpipe.book.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexGroup {

    public static void main(String[] args) {
        String regex = args[0];
        String text = args[1];
        System.out.println("regex=|" + regex + "|");
        System.out.println("text=|" + text + "|");
        /*x RegexGroup.1 */
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            for (int i = 0; i <= matcher.groupCount(); ++i) {
                String group = matcher.group(i);
                int start = matcher.start(i);
                int end = matcher.end(i);
                /*x*/
                System.out.printf("Group %2d=|%s| at (%d,%d)\n",i,group,start,end);
            }
        } else {
            System.out.println("No match");
        }
    }

}