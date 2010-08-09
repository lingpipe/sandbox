package com.lingpipe.book.tok;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenization;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.List;


public class DisplayTokenization {

    public static void main(String[] args) {
        String text = args[0];
        
        /*x DisplayTokenization.1 */
        TokenizerFactory tokFact
            = new RegExTokenizerFactory("\\p{L}+");

        Tokenization tokenization = new Tokenization(text,tokFact);

        List<String> tokenList = tokenization.tokenList();
        List<String> whitespaceList = tokenization.whitespaceList();
        String textTok = tokenization.text();
        /*x*/
        
        System.out.println("tokenList=" + tokenList);
        System.out.println("whitespaceList=" + whitespaceList);
        System.out.println("textTok=|" + textTok + "|");
        
        System.out.println();
        DisplayTokens.displayTextPositions(text);
        
        System.out.printf("\n%4s %5s %5s %5s %10s\n",
                          "n","start","end","whsp","token");
        /*x DisplayTokenization.2 */
        for (int n = 0; n < tokenization.numTokens(); ++n) {
            int start = tokenization.tokenStart(n);
            int end = tokenization.tokenEnd(n);
            String whsp = tokenization.whitespace(n);
            String token = tokenization.token(n);
        /*x*/
            System.out.printf("%4d %5d %5d %5s %10s\n",
                              n,start,end,"|" + whsp + "|",
                              "|" + token + "|");
        }
        /*x DisplayTokenization.3 */
        String lastWhitespace 
            = tokenization.whitespace(tokenization.numTokens());
        /*x*/
        System.out.printf("lastWhitespace=%4s\n","|" + lastWhitespace + "|");
    }

}