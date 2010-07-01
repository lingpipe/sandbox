package com.lingpipe.book.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

    public static void main(String[] args) {
        /*x Regex.1 */
        String regex = args[0];
        String text = args[1];

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        boolean matches = matcher.matches();
        /*x*/

        System.out.println("Regex=|" + regex + "|");
        System.out.println("Text=|" + text + "|");
        System.out.println("Matches=" + matches);

        /*x Regex.2 */
        Matcher finder = pattern.matcher(text);
        while (finder.find()) {
            String found = finder.group();
            int start = finder.start();
            int end = finder.end();
        /*x*/
            System.out.println("Found |" + found + "|"
                               + " at (" + start + "," + end + ")");
        }
    }

}