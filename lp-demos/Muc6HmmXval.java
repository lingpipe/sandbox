import com.aliasi.util.AbstractExternalizable;

import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkerEvaluator;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.io.File;
import java.io.IOException;

public class Muc6HmmXval {

    public static void main(String[] args)
        throws IOException, ClassNotFoundException {

        File muc6XmlDir = new File(args[0]);
        int numFolds = 10;
        Muc6XvalCorpus corpus
            = new Muc6XvalCorpus(numFolds,muc6XmlDir);

        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;

        for (int fold = 0; fold < numFolds; ++fold) {
            System.out.println("\nFOLD=" + fold);
            corpus.setFold(fold);

            int charLmMaxNgram = 8;
            int maxCharacters = 128;
            double charLmInterpolation = charLmMaxNgram;
            boolean smoothStates = false;
            HmmCharLmEstimator hmmEstimator
                = new HmmCharLmEstimator(charLmMaxNgram,
                                         maxCharacters,
                                         charLmInterpolation,
                                         smoothStates);
            // CharLmHmmChunker chunker
            // = new CharLmHmmChunker(tokenizerFactory,
            // hmmEstimator);

            int rescoringNGram = 8;
            CharLmRescoringChunker chunker
                = new CharLmRescoringChunker(tokenizerFactory,
                                             128,
                                             charLmMaxNgram,
                                             maxCharacters,
                                             charLmInterpolation,
                                             smoothStates);

            System.out.println("\nTraining");
            corpus.visitTrain(chunker);

            System.out.println("\nCompiling");
            @SuppressWarnings("unchecked") // required for serialized compile
            Chunker compiledChunker
                = (Chunker)
                AbstractExternalizable.compile(chunker);
            System.out.println("     compiled");

            System.out.println("\nEvaluating");
            ChunkerEvaluator evaluator
                = new ChunkerEvaluator(compiledChunker);

            corpus.visitTest(evaluator);
            System.out.println("\nEvaluation");
            System.out.println(evaluator);
        }

    }

}