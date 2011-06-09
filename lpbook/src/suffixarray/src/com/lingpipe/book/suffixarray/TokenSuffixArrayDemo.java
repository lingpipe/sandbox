package com.lingpipe.book.suffixarray;

import com.aliasi.suffixarray.TokenSuffixArray;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.Tokenization;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.List;

public class TokenSuffixArrayDemo {

    public static void main(String[] args) {
        String text = args[0];

        /*x TokenSuffixArrayDemo.1 */
        TokenizerFactory tf 
            = IndoEuropeanTokenizerFactory.INSTANCE;
        tf = new LowerCaseTokenizerFactory(tf);
        Tokenization tok = new Tokenization(text,tf);
        TokenSuffixArray tsa = new TokenSuffixArray(tok);
        /*x*/

        CharSuffixArrayDemo.printTextPos(text);

        System.out.println("\nTOKENS");

        /*x TokenSuffixArrayDemo.2 */
        Tokenization tokenization = tsa.tokenization();
        for (int i = 0; i < tokenization.numTokens(); ++i) {
            String token = tokenization.token(i);
            int start = tokenization.tokenStart(i);
            int end = tokenization.tokenEnd(i);
        /*x*/
            System.out.printf("%3d (%3d,%3d) %s\n", i, start, end, token);
        }

        System.out.println("\nSUFFIX ARRAY (idx,array,suffix)");

        /*x TokenSuffixArrayDemo.3 */
        for (int i = 0; i < tsa.suffixArrayLength(); ++i) {
            int suffixArrayI = tsa.suffixArray(i);
            String suffix = tsa.substring(i,Integer.MAX_VALUE);
        /*x*/
            System.out.printf("  %3d %3d %s\n", i, suffixArrayI, suffix);
        }

        System.out.println("\nMATCHING SUBSTRINGS");
        System.out.printf("  %3s  %3s  %3s  %s\n", 
                          "len", "sa", "txt", "suffix");

        /*x TokenSuffixArrayDemo.4 */
        for (int len = 20; --len > 0; ) {
            List<int[]> prefixMatches = tsa.prefixMatches(len);
            for (int[] match : prefixMatches) {
                for (int j = match[0]; j < match[1]; ++j) {
                    int textPos = tsa.suffixArray(j);
                    String suffix = tsa.substring(j,len);
        /*x*/
                    System.out.printf("  %3d  %3d  %3d  %s\n", len, j, textPos, suffix);
                }
            }
        }
    }

}