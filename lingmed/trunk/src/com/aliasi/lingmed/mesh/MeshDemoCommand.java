package com.aliasi.lingmed.mesh;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.zip.GZIPInputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@code MeshDemoCommand} class provides a static main method
 * which demonstrates the use of the MeSH parser by reading
 * an input file and printing all of the entries.  This class is
 * most helpful at the code level.
 * 
 * <b>Usage</b>
 *
 * <blockquote><pre>
 * java MeshDemoCommand <i>desc.gz</i></pre></blockquote>
 *
 * where the <code><i>desc.gz</i> argument is a path to the
 * gzipped XML version of MeSH.
 *
 *
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshDemoCommand {

    private MeshDemoCommand() { 
        /* don't construct */ 
    }

    /**
     * Main method from which to run the demo.  See the class documentation
     * for usage notes.
     *
     * @param args Command-line arguments.
     * @throws IOException If there is an error reading from the MeSH gzipped
     * XML.
     * @throws SAXException If there is an error parsing the data.
     */
    public static void main(String[] args) throws IOException, SAXException {
        File meshGzipFile = new File(args[0]);
        String fileURL = Files.fileToURLName(meshGzipFile);
        System.out.println("FILE URL=" + fileURL);

        MeshParser parser = new MeshParser();
        DemoHandler handler = new DemoHandler();
        parser.setHandler(handler);

        InputStream fileIn = new FileInputStream(meshGzipFile);
        InputStream gzipIn = new GZIPInputStream(fileIn);
        InputSource inSource = new InputSource(gzipIn);
        inSource.setEncoding(Strings.UTF8);
        
        try { 
            parser.parse(inSource);
        } catch (Exception e) {
            System.out.println("Exception=" + e);
            e.printStackTrace(System.out);
            System.exit(0);
        }
        System.out.println("============================================================");
        System.out.println("Final record count=" + handler.mRecordCount);
    }

    static class DemoHandler implements ObjectHandler<Mesh> {
        int mRecordCount = 0;
        public void handle(Mesh mesh) {
            System.out.println("============================================================");
            System.out.println(mesh);
            ++mRecordCount;
        }
    }



}