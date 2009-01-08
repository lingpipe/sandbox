import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.RegExChunker;

import com.aliasi.xml.SAXWriter;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class AnnotateCommand {

    public static void main(String[] args) throws Exception {
        File modelFile = new File(args[0]);
        File dirIn = new File(args[1]);
        File dirOut = new File(args[2]);
        dirOut.mkdirs();
        
        System.out.println("Reading NE model from file=" + modelFile);
        Chunker neChunker = (Chunker) AbstractExternalizable.readObject(modelFile);
        for (File file : dirIn.listFiles())
            process(neChunker,file,new File(dirOut,file.getName()+".xml"));
    }

    static void process(Chunker neChunker, File inFile, File outFile) 
        throws Exception {

        System.out.println("  processing " + inFile + "->" + outFile);
        OutputStream out = new FileOutputStream(outFile);
        SAXWriter writer = new SAXWriter(out,Strings.UTF8);
        writer.startDocument();
        writer.startSimpleElement("text");
        String text = Files.readFromFile(inFile,Strings.UTF8).replaceAll("(\\s|\\u3000)+","");;
        Chunking chunking = TEXT_CHUNKER.chunk(text);
        int previous = 0;
        for (Chunk chunk : chunking.chunkSet()) {
            int start = chunk.start();
            int end = chunk.end();
            writer.characters(text.substring(previous,start));
            processText(neChunker,text.substring(start,end),writer);
            previous = end;
        }
        writer.characters(text.substring(previous));
        writer.endSimpleElement("text");
        writer.endDocument();
        out.close();
    }

    static void processText(Chunker neChunker, String text, SAXWriter writer) 
        throws Exception {

        // System.out.println("text=|" + text + "|");

        Chunking neChunking = neChunker.chunk(text);
        int previous = 0;
        for (Chunk chunk : neChunking.chunkSet()) {
            int start = chunk.start();
            int end = chunk.end();
            String entityType = chunk.type();
            writer.characters(text.substring(previous,start));
            writer.startSimpleElement("entity","type",entityType);
            writer.characters(text.substring(start,end));
            writer.endSimpleElement("entity");
            previous = end;
        }
        writer.characters(text.substring(previous));
    }
        
    static final Chunker TEXT_CHUNKER 
    = new RegExChunker("[[\\S]&&[^\\u3002]]+","txt",1);
    // = new RegExChunker("[^a-zA-Z01-9\\u3002\\s]+","txt",1); // also skip half spaces
    // = new RegExChunker("[^\\u3002]+","contiguous-text",1);

}
