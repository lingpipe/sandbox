package classify;

import com.aliasi.classify.Classifier;
import com.aliasi.classify.Classification;

import com.aliasi.xml.SAXWriter;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.xml.sax.SAXException;

import patient.Corpora;
import patient.RecordSetHandler;

public class Writer1 {

    private final SAXWriter mWriter;
    private final OutputStream mOutputStream;

    public Writer1(File file) throws IOException, SAXException {
        mOutputStream = new FileOutputStream(file);
        mWriter = new SAXWriter(mOutputStream,Strings.UTF8);
        mWriter.startDocument();
        mWriter.characters("\n");
        mWriter.startSimpleElement("diseaseset");
    }

    public void startSource(String source) throws IOException, SAXException {
        mWriter.characters("\n");
        mWriter.startSimpleElement("diseases","source",source);
    }

    public void endSource() throws IOException, SAXException {
        mWriter.characters("\n");
        mWriter.endSimpleElement("diseases");
    }

    public void startDisease(String disease) throws IOException, SAXException {
        mWriter.characters("\n");
        mWriter.startSimpleElement("disease","name",disease);
    }

    public void endDisease() throws IOException, SAXException {
        mWriter.characters("\n");
        mWriter.endSimpleElement("disease");
    }

    public void judgment(String id, String judgment) throws IOException, SAXException {
        mWriter.characters("\n");
        mWriter.startSimpleElement("doc","id",id,"judgment",judgment);
        mWriter.endSimpleElement("doc");
    }

    public void close() throws IOException, SAXException {
        mWriter.characters("\n");
        mWriter.endSimpleElement("diseaseset");
        mWriter.characters("\n");
        mWriter.endDocument();
        Streams.closeOutputStream(mOutputStream);
    }
    

    public static void main(String[] args) throws Exception {
        String inputFileName = "data/eval/obesity_patient_records_test.xml";
        String modelDirName = "non-cvs/models/run1";
        String intuitiveOutputFileName = "submission/alias-i-intuitive-1.xml";
        String textualOutputFileName = "submission/alias-i-textual-1.xml";

        File inputFile = new File(inputFileName);
        File modelDir = new File(modelDirName);
        
        System.out.println("input file=" + inputFile);
        System.out.println("model dir=" + modelDir);
        System.out.println("textual output file=" + textualOutputFileName);
        System.out.println("intuitive output file=" + intuitiveOutputFileName);

        RecordSetHandler recordSetHandler = new RecordSetHandler();
        Corpora.handleXml(inputFile,recordSetHandler);
        Map<String,String> idToTextRecord = recordSetHandler.idToTextRecord();
        
        for (String source : modelDir.list()) {
            System.out.println("SOURCE=" + source);
            String outputFileName = "intuitive".equals(source)
                ? intuitiveOutputFileName
                : textualOutputFileName;
            File outputFile = new File(outputFileName);
            Writer writer = new Writer(outputFile);
            writer.startSource(source);
            File sourceDir = new File(modelDir,source);
            for (String diseaseModel : sourceDir.list()) {
                String disease = diseaseModel.substring(0,diseaseModel.indexOf('.'));
                System.out.println("     DISEASE=" + disease);
                writer.startDisease(disease);
                File modelFile = new File(sourceDir,diseaseModel);
                Classifier classifier = (Classifier) AbstractExternalizable.readObject(modelFile);
                String[] orderedIds = idToTextRecord.keySet().<String>toArray(new String[idToTextRecord.size()]);
                Arrays.sort(orderedIds,NUMERICAL_ORDER);
                for (String id : orderedIds) {
                    String text = idToTextRecord.get(id);
                    Classification c = classifier.classify(text);
                    String bestCategory = c.bestCategory();
                    writer.judgment(id,bestCategory);
                }
                writer.endDisease();
            }
            writer.endSource();
            writer.close();
        }
    }

    private static Comparator<String> NUMERICAL_ORDER = new Comparator<String>() {
        public int compare(String s1, String s2) {
            Long n1 = Long.parseLong(s1);
            Long n2 = Long.parseLong(s2);
            return n1.compareTo(n2);
        }
    };

}