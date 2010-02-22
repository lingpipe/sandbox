package edu.uchicago.rzhetsky.cfg;

import static edu.uchicago.rzhetsky.cfg.Production.binary;
import static edu.uchicago.rzhetsky.cfg.LexEntry.entry;

import static java.util.Arrays.asList;
import java.util.List;


public class Demo {

    public static void main(String[] args) {

        List<Production> productions
            = asList(binary("S","NP","VP"));

        List<LexEntry> lexEntries
            = asList(entry("NP","John"),
                     entry("VP","ran"));
        
        Cfg cfg = new Cfg(productions,lexEntries);

        System.out.println(cfg);

        ShiftReduceParser parser = new ShiftReduceParser(cfg);

        /*
        String[][] rules = {
            { "S", "NP", "VP" },
            { "NP", "DET", "N" },
            { "NP", "PN" },
            { "N", "ADJ", "N" },
            { "N", "N", "PP" },
            { "PP", "P", "NP" },
            { "VP", "IV" },
            { "VP", "TV", "NP" },
            { "VP", "DV", "NP", "NP" },
            { "VP", "VP", "PP" },
            { "VP", "VP", "ADV" }
        };
        String[][] lexEntries = {
            { "PN", "John" },
            { "PN", "Mary" },
            { "DET", "the" },
            { "DET", "a" },
            { "N", "book" },
            { "N", "box" },
            { "N", "pencil" },
            { "ADJ", "red" },
            { "ADJ", "old" },
            { "P", "in" },
            { "P", "near" },
            { "IV", "ran" },
            { "IV", "jumped" },
            { "TV", "saw" },
            { "TV", "wrote" },
            { "DV", "gave" },
            { "DV", "showed" },
        };
        */
    }

}