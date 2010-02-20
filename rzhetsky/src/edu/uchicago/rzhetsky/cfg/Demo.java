package edu.uchicago.rzhetsky.cfg;

public class Demo {

    public static void main(String[] args) {
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
        Cfg cfg = new Cfg(rules,lexEntries);
        System.out.println(cfg);
    }

}