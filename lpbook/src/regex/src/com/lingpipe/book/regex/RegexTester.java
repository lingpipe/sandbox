package com.lingpipe.book.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTester {

    public static void main(String[] args) {
        String regex = args[0];
        String text = args[1];
        System.out.println("Regex=|" + regex + "|");
        Pattern pattern = Pattern.compile(regex);

        System.out.println("Text=|" + text + "|");

        Matcher matcher = pattern.matcher(text);
        boolean matches = matcher.matches();
        System.out.println("Matches=" + matches);
            
        Matcher finder = pattern.matcher(text);
        while (finder.find()) {
            String found = finder.group();
            int start = finder.start();
            int end = finder.end();
            System.out.println("Found |" + found + "|"
                               + " at (" + start + "," + end + ")");
        }
    }

}