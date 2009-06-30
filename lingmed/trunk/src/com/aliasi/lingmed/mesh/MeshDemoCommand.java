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

public class MeshDemoCommand {

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
        System.out.println("\n===================================================");
        System.out.println("Final record count=" + handler.mRecordCount);
    }

    static class DemoHandler implements ObjectHandler<Mesh> {
        int mRecordCount = 0;
        public void handle(Mesh mesh) {
            System.out.println("\n=================================================");
            System.out.println(mesh);
            ++mRecordCount;
        }
    }



}