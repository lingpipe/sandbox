
package com.lingpipe.book.suffixarray;

import com.aliasi.suffixarray.DocumentTokenSuffixArray;
import com.aliasi.suffixarray.TokenSuffixArray;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenization;

import com.aliasi.util.Files;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

public class DocSuffixArrayDemo {


    public static void main(String[] args) 
        throws IOException {

        File dir = new File(args[0]);
        int len = Integer.valueOf(args[1]);

        System.out.println("Extracting files from path=" 
                           + dir.getCanonicalPath());
        System.out.println("Match length=" + len);

        /*x DocSuffixArrayDemo.1 */
        Map<String,String> idToDocMap = new HashMap<String,String>();
        addFiles(dir,idToDocMap);
        /*x*/

        System.out.println("# of docs=" + idToDocMap.size());
        
        /*x DocSuffixArrayDemo.3 */
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        tf = new LowerCaseTokenizerFactory(tf);
        tf = new RegExFilteredTokenizerFactory(tf,Pattern.compile("\\w+"));
        /*x*/
        
        /*x DocSuffixArrayDemo.4 */
        String boundaryToken = "eeooff";
        int maxSuffixLength = Integer.MAX_VALUE;
        DocumentTokenSuffixArray dtsa
            = new DocumentTokenSuffixArray(idToDocMap, tf, 
                                           maxSuffixLength, 
                                           boundaryToken);
        /*x*/

        System.out.println("# of tokens=" + dtsa.suffixArray().tokenization().numTokens());

        /*x DocSuffixArrayDemo.5 */
        TokenSuffixArray tsa = dtsa.suffixArray();

        List<int[]> prefixMatches = tsa.prefixMatches(len);
        for (int[] match : prefixMatches) {
            for (int j = match[0]; j < match[1]; ++j) {
                String matchedText = tsa.substring(j,len);
                int textPos = tsa.suffixArray(j);
                String docId = dtsa.textPositionToDocId(textPos); 
        /*x*/
                System.out.printf(" %7d  %7d %20s  %s\n", 
                                  j, textPos, docId, matchedText);
            }
        }
        
        


    }

    /*x DocSuffixArrayDemo.2 */
    static void addFiles(File path, Map<String,String> idToDocMap) 
        throws IOException {

        if (path.isDirectory()) {
            for (File subpath : path.listFiles())
                addFiles(subpath, idToDocMap);
        } else if (path.isFile()) {
            String fileName = path.toString();
            String text = Files.readFromFile(path,"ASCII");
            text = text.replaceAll("\\s+"," "); // norm whitespace
            idToDocMap.put(fileName,text);
        }
    }
    /*x*/

}