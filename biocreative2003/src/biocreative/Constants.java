package biocreative;

import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenCategorizer;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;


public class Constants {

    /*
    // BASELINE
    static double PRUNING_THRESHOLD = 16.0; // very high
    static double LAMBDA_FACTOR = 4.0;
    static int PRUNE_TAG_MIN = 1;
    static int PRUNE_TOKEN_MIN = 1;
    static boolean TRAIN_GENIA = false;
    static boolean TRAIN_DICTIONARY = false;
    static boolean DICTIONARY_PASS = false;
    static boolean DICTIONARY_OVERWRITE = false;
    static boolean CLOSURE = false;
    static boolean CLOSURE_OVERWRITE = false;
    static boolean USE_GENE1 = true;
    static double LOG_UNIFORM_VOCAB_ESTIMATE = Math.log(1.0/1000000.0);
    static TokenizerFactory TOKENIZER_FACTORY
        // = new TaggedCorpusTagParser.WholeWordTokenizerFactory();
        = new IndoEuropeanTokenizerFactory();
    static TokenCategorizer TOKEN_CATEGORIZER
        = new IndoEuropeanTokenCategorizer();
    static int KNOWN_TOKEN_COUNT = 8;
    static int MIN_GENE_LENGTH = 1;
    static boolean REMOVE_UNBALANCED_PARENS = false;
    static boolean REMOVE_DRUG_SUFFIXES = false;
    static boolean CLEANUP_CONJUNCTION = false;
    */

    // TUNED
    static double PRUNING_THRESHOLD = 16.0; // very high
    static double LAMBDA_FACTOR = 8.0;
    static int PRUNE_TAG_MIN = 1;
    static int PRUNE_TOKEN_MIN = 1;
    static boolean TRAIN_GENIA = false;
    static boolean TRAIN_DICTIONARY = false;
    static boolean DICTIONARY_PASS = false;
    static boolean DICTIONARY_OVERWRITE = false;
    static boolean CLOSURE = false;
    static boolean CLOSURE_OVERWRITE = false;
    static boolean USE_GENE1 = true;
    static double LOG_UNIFORM_VOCAB_ESTIMATE = Math.log(1.0/1000000.0);
    static TokenizerFactory TOKENIZER_FACTORY
        // = new TaggedCorpusTagParser.WholeWordTokenizerFactory();
        = new IndoEuropeanTokenizerFactory();
    static TokenCategorizer TOKEN_CATEGORIZER
        = new IndoEuropeanTokenCategorizer();
    static int KNOWN_TOKEN_COUNT = 2;
    static int MIN_GENE_LENGTH = 1;
    static boolean REMOVE_UNBALANCED_PARENS = false;
    static boolean REMOVE_DRUG_SUFFIXES = false;
    static boolean CLEANUP_CONJUNCTION = false;


    /*
    // HEURISTIC
    static double PRUNING_THRESHOLD = 16.0; // very high
    static double LAMBDA_FACTOR = 8.0;
    static int PRUNE_TAG_MIN = 1;
    static int PRUNE_TOKEN_MIN = 1;
    static boolean TRAIN_DICTIONARY = false;
    static boolean TRAIN_GENIA = false;
    static boolean DICTIONARY_PASS = false;
    static boolean DICTIONARY_OVERWRITE = false;
    static boolean CLOSURE = false;
    static boolean CLOSURE_OVERWRITE = false;
    static boolean USE_GENE1 = false;
    static double LOG_UNIFORM_VOCAB_ESTIMATE = Math.log(1.0/1000000.0);
    static TokenizerFactory TOKENIZER_FACTORY
        // = new TaggedCorpusTagParser.WholeWordTokenizerFactory();
        = new IndoEuropeanTokenizerFactory();
    static TokenCategorizer TOKEN_CATEGORIZER
        = new IndoEuropeanTokenCategorizer();
    static int KNOWN_TOKEN_COUNT = 2;
    static int MIN_GENE_LENGTH = 2;
    static boolean REMOVE_UNBALANCED_PARENS = true;
    static boolean REMOVE_DRUG_SUFFIXES = true;
    static boolean CLEANUP_CONJUNCTION = true;
    */
}
