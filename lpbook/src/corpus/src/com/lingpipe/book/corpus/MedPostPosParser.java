package com.lingpipe.book.corpus;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.StringParser;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.tag.Tagging;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/*x MedPostPosParser.1 */
public class MedPostPosParser
    extends StringParser<ObjectHandler<Tagging<String>>> {
/*x*/
    
    /*x MedPostPosParser.2 */
    @Override
    public void parseString(char[] cs, int start, int end) {
        String in = new String(cs,start,end-start);
        for (String sentence : in.split("\n"))
            if (sentence.indexOf('_') >= 0) 
                process(sentence);
    }
    /*x*/
    

    /*x MedPostPosParser.3 */
    private void process(String sentence) {
        List<String> tokenList = new ArrayList<String>();
        List<String> tagList = new ArrayList<String>();
        
        for (String pair : sentence.split(" ")) {
            String[] tokTag = pair.split("_");
            tokenList.add(tokTag[0]);
            tagList.add(tokTag[1]);
        }
        Tagging<String> tagging
            = new Tagging<String>(tokenList,tagList);
        ObjectHandler<Tagging<String>> handler = getHandler();
        handler.handle(tagging);
    }
    /*x*/

    public static void main(String[] args) throws IOException {
        File medPostDir = new File(args[0]);
        System.out.println("medPostDir=" + medPostDir.getCanonicalPath());

        /*x MedPostPosParser.4 */
        final Set<String> tagSet = new TreeSet<String>();
        ObjectHandler<Tagging<String>> handler 

            = new ObjectHandler<Tagging<String>>() {

                public void handle(Tagging<String> tagging) {
                    tagSet.addAll(tagging.tags());
                }

        };
        /*x*/

        /*x MedPostPosParser.5 */
        Parser<ObjectHandler<Tagging<String>>> parser
            = new MedPostPosParser();
        parser.setHandler(handler);
        /*x*/

        /* MedPostPosParser.6 */
        FileFilter iocFilter = new FileExtensionFilter("ioc");
        for (File file : medPostDir.listFiles(iocFilter))
            parser.parse(file,"ASCII");
        /*x*/

        System.out.println("#Tags=" + tagSet.size());
        System.out.println("Tags=");
        for (String tag : tagSet)
            System.out.println("     " + tag);
    }

}