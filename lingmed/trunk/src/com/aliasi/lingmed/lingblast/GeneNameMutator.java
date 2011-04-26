/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.lingmed.lingblast;

import java.io.*;

import java.util.HashSet;
import java.util.Set;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import org.apache.log4j.Logger;

/**
 * <P>A <code>GeneNameMutator</code> creates variations on a gene name
 */

public class GeneNameMutator {

    public static final String A = "A";
    public static final String B = "B";
    public static final String G = "G";
    public static final String D = "D";
    public static final String DASH = "-";
    public static final String R1 = "I";
    public static final String R2 = "II";
    public static final String R3 = "III";
    public static final String R4 = "IV";
    public static final String R5 = "V";
    public static final String R6 = "VI";
    public static final String R7 = "VII";
    public static final String R8 = "VIII";
    public static final String R9 = "IX";

    public static final String caseSensitiveRegex = "[a-z]+|[A-Z]+|[0-9][\\p{Punct}]+";
    public static final TokenizerFactory byCaseTokenizerFactory
        = new RegExTokenizerFactory(caseSensitiveRegex);

    private static IndoEuropeanTokenizerFactory indoEuropeanTokenizerFactory = new IndoEuropeanTokenizerFactory();

    private static void munge(Tokenizer tokenizer, HashSet<String> variants) {
         StringBuffer var = new StringBuffer();
         String token = null;
         String ws = tokenizer.nextWhitespace();
         while ((token = tokenizer.nextToken()) != null) {
             ws = tokenizer.nextWhitespace();
             if (A.equals(token)) {
                 var.append("Alpha");
                 var.append(ws);
             } else if (B.equals(token)) {
                 var.append("Beta");
                 var.append(ws);
             } else if (G.equals(token)) {
                 var.append("Gamma");
                 var.append(ws);
             } else if (D.equals(token)) {
                 var.append("Delta");
                 var.append(ws);
             } else if (DASH.equals(token)) {
                 var.append(" ");
             } else if (R1.equals(token)) {
                 var.append("1");
                 var.append(ws);
             } else if (R2.equals(token)) {
                 var.append("2");
                 var.append(ws);
             } else if (R3.equals(token)) {
                 var.append("3");
                 var.append(ws);
             } else if (R4.equals(token)) {
                 var.append("4");
                 var.append(ws);
             } else if (token.length() > 1 && token.endsWith(A)) {
                 var.append(token.substring(0,token.length()-1));
                 var.append("-Alpha");
             } else if (token.length() > 1 && token.endsWith(B)) {
                 var.append(token.substring(0,token.length()-1));
                 var.append("-Beta");
             } else if (token.length() > 1 && token.endsWith(G)) {
                 var.append(token.substring(0,token.length()-1));
                 var.append("-Gamma");
             } else if (token.length() > 1 && token.endsWith(D)) {
                 var.append(token.substring(0,token.length()-1));
                 var.append("-Delta");
             } else {
                 var.append(token);
                 var.append(ws);
             }
         }
         String variant = var.toString().trim();
         variants.add(variant);
         variants.add(variant.replaceAll("-"," "));
         variants.add(variant.replaceAll("-",""));
         variants.add(variant.replaceAll("[\\(\\)\\,]",""));
    }

    private static void permute( String text, HashSet<String> variants) {
        String [] frags = text.split(", ");
        if (frags.length < 2) return;
        for (int i = 0; i < frags.length; i++) {
            StringBuffer var = new StringBuffer();
            for (int j = frags.length-1; j >= i; j--) {
                var.append(frags[j]+" ");
            }
            variants.add(var.toString().trim());
        }
    }


    public static  String[] getVariants(String text) {
        HashSet<String> variants = new HashSet<String>();
        variants.add(text);
        variants.add(text.toLowerCase());
        variants.add(text.replaceAll("-"," "));
        variants.add(text.toLowerCase().replaceAll("-"," "));
        variants.add(text.replaceAll("[\\(\\)\\,]",""));
        variants.add(text.toLowerCase().replaceAll("[\\(\\)\\,]",""));

        char[] cs = text.toCharArray();
        munge(indoEuropeanTokenizerFactory.tokenizer(cs,0,cs.length),variants);
        String[] result = new String[variants.size()];
        result = variants.toArray(result);
        return result;
    }


    public static void main(String[] args)  throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            System.out.println("enter text:");
            System.out.flush();
            String text = in.readLine();
            if (text.equals("q")) break;
            String[] vars = getVariants(text);
            System.out.println("text: "+ text);
            for (String var : vars) {
                System.out.println(" var: "+ var);
            }
        }
    }
}