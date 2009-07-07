package com.aliasi.anno.cmd;

import com.aliasi.io.FileLineReader;

import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.TreeMap;

public class CensorNeZeros {

    public static void main(String[] args) throws IOException {
        File annoTsvFile = new File(args[0]);
        File goldTsvFile = new File(args[1]);

        
        File censoredAnnoTsvFile = new File(args[2]);
        File censoredGoldTsvFile = new File(args[3]);
        
        File censoredToOriginalFile = new File(args[4]);

        System.out.println("Reading lines from file");
        String[] lines = FileLineReader.readLineArray(annoTsvFile,"ASCII");
        System.out.println("     #lines=" + lines.length);


        // cut-and-paste from ByAnnoCmd
        System.out.println("Parsing raw data");
        boolean[] annotations = new boolean[lines.length];
        int[] annotators = new int[lines.length];
        int[] items = new int[lines.length];
        for (int k = 0; k < lines.length; ++k) {
            String[] fields = lines[k].split("\t");
            items[k] = Integer.valueOf(fields[0]);
            annotators[k] = Integer.valueOf(fields[1]);
            annotations[k] = "1".equals(fields[2]);
        }

        Map<Integer,Integer> itemsWithOnes = new TreeMap<Integer,Integer>();
        int nextNewId = 1;
        for (int k = 0; k < lines.length; ++k)
            if (annotations[k] && !itemsWithOnes.containsKey(items[k]))
                itemsWithOnes.put(items[k],nextNewId++);
                
        String[] gsLines = FileLineReader.readLineArray(goldTsvFile,"ASCII");
        for (int i = 0; i < gsLines.length; ++i)
            if ("1".equals(gsLines[i]) && !itemsWithOnes.containsKey(Integer.valueOf(i+1)))
                itemsWithOnes.put(Integer.valueOf(i+1),nextNewId++);

        StringBuilder sbAnno = new StringBuilder();
        for (int k = 0; k < lines.length; ++k) {
            Integer newKey = itemsWithOnes.get(items[k]);
            if (newKey == null) continue;
            if (sbAnno.length() > 0) sbAnno.append("\n");
            sbAnno.append(newKey + "\t" + annotators[k] + "\t" + (annotations[k] ? "1" : "0"));
        }
        Files.writeStringToFile(sbAnno.toString(),censoredAnnoTsvFile,"ASCII");

        Map<Integer,String> inverseMap = new TreeMap<Integer,String>();
        for (int i = 0; i < gsLines.length; ++i) {
            int item = i+1;
            if (!itemsWithOnes.containsKey(item)) continue;
            inverseMap.put(itemsWithOnes.get(item),gsLines[i]);
        }
        StringBuilder sbGold = new StringBuilder();
        for (Map.Entry<Integer,String> entry : inverseMap.entrySet()) {
            if (sbGold.length() > 0) sbGold.append("\n");
            sbGold.append(entry.getValue());
        }
        Files.writeStringToFile(sbGold.toString(),censoredGoldTsvFile,"ASCII");

        StringBuilder sbOriginalItems = new StringBuilder();
        for (Map.Entry<Integer,Integer> entry : itemsWithOnes.entrySet()) {
            if (sbOriginalItems.length() > 0) sbOriginalItems.append("\n");
            sbOriginalItems.append(entry.getKey() + "\t" + entry.getValue());
        }
        Files.writeStringToFile(sbOriginalItems.toString(),censoredToOriginalFile,"ASCII");
        
    }

}
