package biocreative;

import com.aliasi.util.Streams;

import java.io.InputStream;

public class EvalCommand {

    public static void main(String[] args) throws Exception {
        String modelFileName = args[0];
        String trainingFileName = args[1];
        String testCorpusName = args[2];
        String outputFileName = args[3];
        String swissProtDictName = args[4];
        String geniaCorpusName = args[5];
        TrainCommand.main(new String[] { trainingFileName, modelFileName,
                                         swissProtDictName, geniaCorpusName});
        AnnotateCommand.main(new String[] { modelFileName, testCorpusName,
                                            outputFileName, swissProtDictName });
    }

}
