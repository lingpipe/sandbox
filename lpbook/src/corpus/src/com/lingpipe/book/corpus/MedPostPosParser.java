package com.lingpipe.book.corpus;

public class MedPostPosParser
    extends StringParser<ObjectHandler<Tagging<String>>> {
    
    public void parseString(char[] cs, int start, int end) {
        String in = new String(cs,start,end-start);
        for (String sentence : in.split("\n")) {
            if (sentence.indexOf('_') >= 0) 
                process(sentence);
        }
    }

    private void process(String sentence) {
        String[] tagTokenPairs = sentence.split(" ");
        List<String> tokenList = new ArrayList<String>();
        List<String> tagList = new ArrayList<String>();
        
        for (String pair : tagTokenPairs) {
            String[] tokTag = pair.split("_");
            tokenList.add(tokTag[0].trim());
            tagList.add(tokTag[1].trim());
        }
        Tagging<String> tagging
            = new Tagging<String>(tokenList,tagList);
        getHandler().handle(tagging);
    }

}