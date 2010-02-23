package edu.uchicago.rzhetsky.cfg;

import static edu.uchicago.rzhetsky.cfg.Production.binary;
import static edu.uchicago.rzhetsky.cfg.Production.unary;
import static edu.uchicago.rzhetsky.cfg.LexEntry.entry;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class DemoCfg {

    public static void main(String[] args) {

        List<Production> productions
            = asList(binary("N","N","PP"),
                     binary("NP","DET","N"),
                     unary("NP","PN"),
                     binary("PP","P","NP"),
                     binary("S","NP","VP"),
                     unary("VP","IV"),
                     binary("VP","TV","NP"),
                     binary("VP","VP","PP") );
            

        List<LexEntry> lexEntries
            = asList(entry("DET","the"),
                     entry("IV","ran"),
                     entry("N","block"),
                     entry("N","box"),
                     entry("N","cat"),
                     entry("N","dog"),
                     entry("N","table"),
                     entry("PN","John"),
                     entry("P","on"),
                     entry("P","near"),
                     entry("TV","saw") );
        
        ContextFreeGrammar cfg 
            = new ContextFreeGrammar(productions,lexEntries);

        System.out.println("1. Grammar\n");
        System.out.println(cfg);

        ShiftReduceParser parser = new ShiftReduceParser(cfg);
        System.out.println("\n2. Parser\n");
        System.out.println(parser);

        System.out.println("\n3. Search");

        parse(parser,"John","ran");
        parse(parser,"the","dog","ran");
        parse(parser,"John","saw","Mary");
        parse(parser,"John","saw","the","dog");
        parse(parser,"the","dog","saw","John");
        parse(parser,"the","dog","saw","the","cat");
        parse(parser,"the","dog","near","John","ran");
        parse(parser,"the","dog","near","the","cat","ran");
        parse(parser,"John","saw","the","dog","near","the","cat");
    }

    static void parse(ShiftReduceParser parser,
                      String... words) {
        System.out.println("\nInput=" + Arrays.asList(words));
        Iterator<Tree> parseIt = parser.parse(words);
        for (int i = 0; parseIt.hasNext(); ++i) {
            Tree parse = parseIt.next();
            System.out.println("     " + i + ".  " + parse);
        }
    }

}