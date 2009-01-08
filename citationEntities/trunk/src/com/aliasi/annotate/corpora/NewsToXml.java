package com.aliasi.annotate.corpora;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.util.Files;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.xml.SAXWriter;

import java.io.*;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

public class NewsToXml {

    public static void main(String[] args) throws Exception {
        SentenceModel sentenceModel
            = new IndoEuropeanSentenceModel(true,false);
        Chunker sentenceChunker
            = new SentenceChunker(IndoEuropeanTokenizerFactory.FACTORY,
                                  sentenceModel);

        File inDir = new File(args[0]);
        File outDir = new File(args[1]);
        File[] inFiles = inDir.listFiles(new FileExtensionFilter(".txt"));
        for (File inFile : inFiles) {
            String name = inFile.getName();
            String baseName = name.substring(0,name.length()-".txt".length());
            File outFile = new File(outDir,baseName+".xml");
            convert(inFile,outFile,sentenceChunker);
        }
    }

    static void convert(File in, File out, Chunker sentenceChunker)
        throws Exception {

        byte[] bytes = Files.readBytesFromFile(in);
        CharsetDetector detector = new CharsetDetector();
        detector.setText(bytes);
        CharsetMatch match = detector.detect();
        String charset = match.getName();
        String text = match.getString();
        String[] lines = text.split("\n");

        System.out.println("file=" + in
                           + " charset=" + charset
                           + " title=" + lines[0]);

        OutputStream outStream = new FileOutputStream(out);
        SAXWriter writer = new SAXWriter(outStream,Strings.UTF8);
        writer.startDocument();
        writer.characters("\n");
        writer.startSimpleElement("doc");
        writer.characters("\n");
        writer.startSimpleElement("chunk","type","title");
        writer.characters(lines[0]);
        writer.endSimpleElement("chunk");
        writer.characters("\n");
        writer.startSimpleElement("chunk","type","byline");
        writer.characters(lines[1]);
        writer.endSimpleElement("chunk");
        writer.characters("\n");
        writer.startSimpleElement("body");
        for (int i = 2; i < lines.length; ++i) {
            if (lines[i].length() == 0) continue;
            writer.characters("\n");
            writer.startSimpleElement("chunk","type","p");
            Chunking sentenceChunking = sentenceChunker.chunk(lines[i]);
            for (Chunk chunk : sentenceChunking.chunkSet()) {
                writer.startSimpleElement("chunk","type","s");
                String sentenceText = lines[i].substring(chunk.start(),chunk.end());
                writer.characters(sentenceText);
                writer.endSimpleElement("chunk");
            }
            writer.endSimpleElement("chunk");
        }
        writer.characters("\n");
        writer.endSimpleElement("body");
        writer.characters("\n");
        writer.endSimpleElement("doc");
        writer.endDocument();
        Streams.closeOutputStream(outStream);
    }
}