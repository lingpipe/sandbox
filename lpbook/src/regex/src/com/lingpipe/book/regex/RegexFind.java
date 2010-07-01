package com.lingpipe.book.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.*;
import java.text.*;

public class RegexFind {

    public static void main(String[] args) {
        String regex = args[0];
        String text = args[1];


        /*x RegexFind.1 */
        Pattern pattern = Pattern.compile(regex);
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