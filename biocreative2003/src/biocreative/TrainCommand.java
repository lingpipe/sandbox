package biocreative;

import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenCategorizer;

import com.aliasi.ne.TrainableEstimator;
import com.aliasi.ne.NEDictionaryTrain;

import com.aliasi.util.Files;
import com.aliasi.util.Streams;

import com.aliasi.xml.XMLFileVisitor;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;

public class TrainCommand {

    // TrainCommand fileIn modelOut
    public static void main(String[] args) throws Exception {
        System.out.println("STARTING");

        File fileIn = new File(args[0]);
        File modelFile = new File(args[1]);
        File dictionaryFile = new File(args[2]);
        File geniaFile = new File(args[3]);

        FileOutputStream fileOut = new FileOutputStream(modelFile);
        BufferedOutputStream bufOut = new BufferedOutputStream(fileOut);
        DataOutputStream dataOut = new DataOutputStream(bufOut);

        TrainableEstimator estimator
            = new TrainableEstimator(Constants.LAMBDA_FACTOR,
                                     Constants.LOG_UNIFORM_VOCAB_ESTIMATE,
                                     Constants.TOKEN_CATEGORIZER);

        TaggedCorpusTagParser tagParser = new TaggedCorpusTagParser();

        tagParser.readFile(fileIn);
        System.out.println("READ INPUT");

        tagParser.train(estimator);
        System.out.println("TRAINED ESTIMATOR ON INPUT");

        if (Constants.TRAIN_GENIA) {
            GeniaHandler handler = new GeniaHandler();
            XMLFileVisitor.handlePath(geniaFile,handler);
            String[] geniaTokens = handler.getTokens();
            String[] geniaTags = handler.getTags();
            System.out.println("TRAIN GENIA, file=" + geniaFile
                               + " tokens=" + geniaTokens.length);

            estimator.handle(geniaTokens,geniaTags);
        }

        if (Constants.TRAIN_DICTIONARY) {
            NEDictionaryTrain dictionaryTrainer
                = new NEDictionaryTrain(estimator,Constants.TOKENIZER_FACTORY);

            XMLFileVisitor.handlePath(dictionaryFile,dictionaryTrainer);
            System.out.println("TRAINED DICTIONARY");
        }

        estimator.prune(Constants.PRUNE_TAG_MIN,
                        Constants.PRUNE_TOKEN_MIN);
        System.out.println("PRUNED");

        estimator.writeTo(dataOut);
        System.out.println("WROTE MODEL");

        Streams.closeOutputStream(dataOut);
        Streams.closeOutputStream(bufOut);
        Streams.closeOutputStream(fileOut);

    }

}
