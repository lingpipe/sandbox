package edu.uchicago.rzhetsky.cfg;

import edu.uchicago.rzhetsky.cfg.Production;
import edu.uchicago.rzhetsky.cfg.LexEntry;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class DemoCfg {

    private DemoCfg() { /* no instances */ }

    /**
     * Run the demo on the specified command-line arguments.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {

        List<Production> productions
            = Arrays
            .asList(Production.create("N","N","PP"),
                   Production.create("NP","DET","N"),
                   Production.create("NP","PN"),
                   Production.create("PP","P","NP"),
                   Production.create("S","NP","VP"),
                   Production.create("VP","IV"),
                   Production.create("VP","TV","NP"),
                   Production.create("VP","VP","PP") );
            

        List<LexEntry> lexEntries
            = Arrays
            .asList(LexEntry.create("DET","the"),
                    LexEntry.create("IV","ran"),
                    LexEntry.create("N","block"),
                    LexEntry.create("N","box"),
                    LexEntry.create("N","cat"),
                    LexEntry.create("N","dog"),
                    LexEntry.create("N","table"),
                    LexEntry.create("PN","John"),
                    LexEntry.create("P","on"),
                    LexEntry.create("P","near"),
                    LexEntry.create("TV","saw") );
        
        ContextFreeGrammar cfg 
            = new ContextFreeGrammar(productions,lexEntries);

        System.out.println("1. Grammar\n");
        System.out.println(cfg);

        Parser parser = new ShiftReduceParser(cfg);
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

    static void parse(Parser parser,
                      String... words) {
        System.out.println("\nInput=" + Arrays.asList(words));
        Iterator<Tree> parseIt = parser.parse(words);
        for (int i = 0; parseIt.hasNext(); ++i) {
            Tree parse = parseIt.next();
            System.out.println("     " + i + ".  " + parse);
        }
    }

}