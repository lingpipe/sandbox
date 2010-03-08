import com.aliasi.chunk.Chunking;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.io.FileExtensionFilter;

import java.io.File;
import java.io.IOException;

public class Muc6XvalCorpus
    extends XValidatingObjectCorpus<Chunking>
    implements ObjectHandler<Chunking> {

    public Muc6XvalCorpus(int numFolds, File muc6XmlDir)
        throws IOException {

        super(numFolds);
        @SuppressWarnings("deprecation")
        com.aliasi.corpus.parsers.Muc6ChunkParser parser = new com.aliasi.corpus.parsers.Muc6ChunkParser(); // PLEASE IGNORE DEPRECATION FOR NOW
        parser.setHandler(this);
        for (File file : muc6XmlDir.listFiles(XML_FILE_FILTER))
            parser.parse(file);
        // could permute by default
    }

    static final FileExtensionFilter XML_FILE_FILTER
        = new FileExtensionFilter(".xml");

    public static void main(String[] args) throws IOException {
        File muc6XmlDir = new File(args[0]);
        Muc6XvalCorpus corpus
            = new Muc6XvalCorpus(10,muc6XmlDir);
        System.out.println("TEST");
        corpus.visitTest(new ObjectHandler<Chunking>() {
                             public void handle(Chunking c) {
                                 System.out.println(c);
                             }});
        System.out.println("TRAIN");
        corpus.visitTest(new ObjectHandler<Chunking>() {
                             public void handle(Chunking c) {
                                 System.out.println(c);
                             }});
    }

}