package biocreative;

import com.aliasi.ne.ClosureDictionary;
import com.aliasi.ne.Decoder;
import com.aliasi.ne.NEDictionary;
import com.aliasi.ne.Tags;

import com.aliasi.util.Files;
import com.aliasi.util.Streams;

import com.aliasi.xml.XMLFileVisitor;
import com.aliasi.xml.SimpleElementHandler;

import org.xml.sax.Attributes;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;

public class AnnotateCommand {

    // AnnotateCommand modelIn fileIn fileOut
    public static void main(String[] args) throws Exception {
        System.out.println("ANNOTATE CMD");

        File modelFile = new File(args[0]);
        File inFile = new File(args[1]);
        File outFile = new File(args[2]);
        File dictionaryFile = new File(args[3]);

        TaggedCorpusTagParser tagParser = new TaggedCorpusTagParser();

        tagParser.readFile(inFile);
        System.out.println("READ INPUT");

        Decoder decoder = new Decoder(modelFile,
                                      Constants.TOKEN_CATEGORIZER,
                                      Constants.PRUNING_THRESHOLD);
        System.out.println("READ DECODER");

        String[][] tokens = tagParser.mTokens;


        tagParser.mResponseTags = new String[tokens.length][];
        for (int i = 0; i < tokens.length; ++i)
            tagParser.mResponseTags[i] = decoder.decodeTags(tokens[i]);
        System.out.println("DECODED");

        if (Constants.DICTIONARY_PASS) {
            NEDictionaryReader reader = new NEDictionaryReader();
            XMLFileVisitor.handlePath(dictionaryFile,reader);
            NEDictionary dictionary = reader.getDictionary();
            System.out.println("Dictionary size = " + dictionary.toString().length());
            for (int i = 0; i < tokens.length; ++i) {
                String[] oldTags = copy(tagParser.mResponseTags[i]);
                dictionary.addDictionaryEntriesTo(tagParser.mResponseTags[i],
                                                  tokens[i],
                                                  Constants.DICTIONARY_OVERWRITE);
                if (diff(oldTags,tagParser.mResponseTags[i])) {
                    System.out.println("Dictionary ping.");
                }
            }
        }

        for (int i = 0; i < tokens.length; ++i) {
            String[] tagsSent = tagParser.mResponseTags[i];
            String[] tokensSent = tokens[i];
            massageSentence(tokens[i],tagParser.mResponseTags[i]);
            massageSentence(tokens[i],tagParser.mResponseTags[i]);
        }

        if (Constants.CLOSURE) {
            System.out.println("CLOSED");
            String[] allTokens = TaggedCorpusTagParser.concatenate(tokens);
            String[] allTags = TaggedCorpusTagParser.concatenate(tagParser.mResponseTags);
            ClosureDictionary closureDict = new ClosureDictionary();
            System.out.println("allTokens.length=" + allTokens.length);
            closureDict.addEntries(allTokens,allTags);
            for (int i = 0; i < tokens.length; ++i) {
                String[] tagsCopy = new String[tagParser.mResponseTags[i].length];
                closureDict.addDictionaryEntriesTo(tagParser.mResponseTags[i],tokens[i],
                                                   Constants.CLOSURE_OVERWRITE);
            }
        }

        String outFileContents = tagParser.toString();
        Files.writeStringToFile(outFileContents,outFile);
        System.out.println("WROTE TO FILE=" + outFile);
    }

    public static void massageSentence(String[] tokens, String[] tags) {
        // length
        if (Constants.MIN_GENE_LENGTH > 1) {
            for (int i = 0; i < tokens.length; ++i) {
                if (!tags[i].equals("ST_GENE")) continue;
                if (i + 1 >= tokens.length || !tags[i+1].equals("GENE")) {
                    if (tokens[i].length() < Constants.MIN_GENE_LENGTH) {
                        tags[i] = "OUT";
                    }
                }
            }
        }
        // remove unbalanced parens stuff
        if (Constants.CLEANUP_CONJUNCTION) {
            for (int i = 0; i < tokens.length; ++i) {
                if (!tags[i].equals("ST_GENE")) continue;
                int j = i+1;
                while (j < tokens.length && tags[j].equals("GENE")) ++j;
                cleanupConjunction(tokens,tags,i,j);
            }
        }
        if (Constants.REMOVE_UNBALANCED_PARENS) {
            for (int i = 0; i < tokens.length; ++i) {
                if (!tags[i].equals("ST_GENE")) continue;
                int j = i+1;
                while (j < tokens.length && tags[j].equals("GENE")) ++j;
                if (!balancedParens(tokens,i,j)) remove(tags,i,j);
            }
        }
        if (Constants.REMOVE_DRUG_SUFFIXES) {
            for (int i = 0; i < tokens.length; ++i) {
                if (!tags[i].equals("ST_GENE")) continue;
                int j = i+1;
                while (j < tokens.length && tags[j].equals("GENE")) ++j;
                if (badSuffix(tokens[j-1])) remove(tags,i,j);
            }
        }
    }

    public static void cleanupConjunction(String[] tokens, String[] tags, int start, int end) {
        for (int i = start; i < end; ++i) {
            if (conjunct(tokens[i])) {
                tags[i] = "OUT";
                if (i+1 < end) tags[i+1] = "ST_GENE";
            }
        }
    }

    public static boolean conjunct(String term) {
        return term.equals("and")
            || term.equals("or");
    }

    public static boolean badSuffix(String term) {
        return term.endsWith("ole")
            || (term.endsWith("ite") && !term.endsWith("site"))
            || term.endsWith("ate")
            || term.endsWith("ine")
            || term.endsWith("mediated")
            || term.endsWith("ose");
    }

    public static void remove(String[] tags, int start, int end) {
        for (int i = start; i < end; ++i)
            tags[i] = "OUT";
    }

    public static boolean balancedParens(String[] tokens, int start, int end) {
        int depth = 0;
        for (int i = start; i < end; ++i) {
            if (tokens[i].equals("(")) {
                ++depth;
            } else if (tokens[i].equals(")")) {
                if (depth < 1) return false;
                --depth;
            }
        }
        return depth == 0;
    }

    public static String[] copy(String[] xs) {
        String[] result = new String[xs.length];
        for (int i = 0; i < xs.length; ++i) result[i] = xs[i];
        return result;
    }

    public static boolean diff(String[] xs, String[] ys) {
        int diffs = 0;
        if (xs.length != ys.length) return true;
        for (int i = 0; i < xs.length; ++i)
            if (!xs[i].equals(ys[i])) ++diffs;
        return (diffs > 0);
    }

    public static class NEDictionaryReader extends SimpleElementHandler {
        StringBuffer mBuf;
        NEDictionary mDictionary = new NEDictionary();
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts) {
            if (qName.equals("entry")) {
                mBuf = new StringBuffer();
            } else {
                mBuf = null;
            }
        }
        public void endElement(String namespaceURI, String localName,
                               String qName) {
            if (qName.equals("entry")) {
                String entry = mBuf.toString();
                String[] tokens = Constants.TOKENIZER_FACTORY.tokenizer(entry.toCharArray(),
                                                                        0, entry.length()).tokenize();
                mDictionary.addEntry(tokens,0,tokens.length,"GENE");
            }
        }
        public void characters(char[] cs, int start, int length) {
            mBuf.append(cs,start,length);
        }
        public NEDictionary getDictionary() {
            return mDictionary;
        }
    }


}
