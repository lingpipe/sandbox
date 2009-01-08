import com.aliasi.sentences.HeuristicSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class ChineseSentenceChunker extends SentenceChunker {

    public ChineseSentenceChunker() {
        super(Train.TOKENIZER_FACTORY,
              SENTENCE_MODEL);
    }

    static final TokenizerFactory TOKENIZER_FACTORY
        = new RegExTokenizerFactory("\\S");

    static final String[] POSSIBLE_STOPS = new String[] {
        Train.CIRCLE_EOS_TOKEN,
        ".",
        ";",
        " ",
    };
        
    static final Set<String> POSSIBLE_STOP_SET
        = new HashSet<String>(Arrays.<String>asList(POSSIBLE_STOPS));
    
    static final Set<String> IMPOSSIBLE_PENULTIMATE_SET
        = new HashSet<String>();

    static final Set<String> IMPOSSIBLE_START_SET
        = new HashSet<String>();

    static final SentenceModel SENTENCE_MODEL
        = new HeuristicSentenceModel(POSSIBLE_STOP_SET,
                                     IMPOSSIBLE_PENULTIMATE_SET,
                                     IMPOSSIBLE_START_SET);
    
    

}