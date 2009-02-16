package corpus;

import com.aliasi.corpus.Parser;

import com.aliasi.corpus.parsers.GigawordTextParser;

import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.zip.GZIPInputStream;

import org.xml.sax.InputSource;

public class GigawordFreq {

    static final Parser GIGAWORD_PARSER = new GigawordTextParser();

    // GigawordFreq <outputFile> <inPath_1> ... <inPath_n>
    public static void main(String[] args) throws Exception {
        File outputCountFile = new File(args[0]);
        WordFreqTextHandler freqHandler = new WordFreqTextHandler();
        GigawordTextParser parser = new GigawordTextParser();
        parser.setHandler(freqHandler);
        for (int i = 1; i < args.length; ++i)
            increment(new File(args[i]),parser);
        freqHandler.writeCountsTo(outputCountFile);
    }

    private static void increment(File path, Parser parser) {
        if (path.isDirectory()) 
            incrementDir(path.listFiles(),parser);
        else if (path.getName().endsWith(".gz"))
            incrementGzipFile(path,parser);
    }

    private static void incrementDir(File[] files, Parser parser) {
        for (int i = 0; i < files.length; ++i)
            increment(files[i],parser);
    }

    private static void incrementGzipFile(File file, Parser parser) {
        System.out.println("Parsing data file=" + file);
        FileInputStream fileIn = null;
        BufferedInputStream bufIn = null;
        GZIPInputStream gzipIn = null;
        try { 
            fileIn = new FileInputStream(file);
            bufIn = new BufferedInputStream(fileIn);
            gzipIn = new GZIPInputStream(bufIn);
            InputSource in = new InputSource(gzipIn);
            in.setEncoding(Strings.UTF8);
            parser.parse(in);
        } catch (IOException e) {
            System.out.println("IOException=" + e);
            e.printStackTrace(System.out);
        } finally {
            Streams.closeInputStream(gzipIn);
            Streams.closeInputStream(bufIn);
            Streams.closeInputStream(fileIn);
        }
    }


}
