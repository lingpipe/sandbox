package com.aliasi.annotate.corpora;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import com.aliasi.xml.SAXWriter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

public class PdfToXml {

    static final List<File> sErrorFileList
        = new ArrayList<File>();

    static final List<Exception> sExceptionList
        = new ArrayList<Exception>();

    static int sTotalFileCount = 0;

    public static void main(String[] args) 
        throws IOException, SAXException {

        File dirIn = new File(args[0]);
        File dirOut = new File(args[1]);
        int baseDirLength = dirIn.toString().length();
        convert(baseDirLength,dirIn,dirOut);

        System.out.println("\n\nTOTAL FILE COUNT=" + sTotalFileCount);
        System.out.println("     #converted=" 
                           + (sTotalFileCount - sErrorFileList.size()));
        System.out.println("     #errors=" + sErrorFileList.size());
        System.out.println("\n\nFILES NOT CONVERTED:");
        for (int i = 0; i < sErrorFileList.size(); ++i) {
            System.out.println("\n");
            System.out.println(sErrorFileList.get(i));
            sExceptionList.get(i).printStackTrace(System.out);
        }
    }

    static void convert(int baseDirLength, File pathIn, File dirOut) 
        throws IOException, SAXException {

        if (pathIn.isDirectory()) {
            File[] subpathsIn = pathIn.listFiles();
            for (File subpathIn : subpathsIn) {
                convert(baseDirLength,subpathIn,dirOut);
            }
        } else if (pathIn.getName().endsWith(".pdf")) {
            String urlName = "http:/" 
                + pathIn.toString().substring(baseDirLength).replace('\\','/');
            convert(urlName,pathIn,dirOut);
        }
    }


    static void convert(String urlName, File pdfFile, File dirOut)
        throws IOException, SAXException {

        ++sTotalFileCount;
        
	String text = null;
        try {
	    text = extractText(pdfFile);
        } catch (IOException e) {
            String msg = "Could not convert file=" + pdfFile;
            System.out.println(msg);
            sErrorFileList.add(pdfFile);
            sExceptionList.add(e);
	    return;
	}
        String normalizedText = normalize(text);

        String nameWithSuffix = pdfFile.getName();
        int nameLength = nameWithSuffix.length() - ".pdf".length();
        String name = nameWithSuffix.substring(0,nameLength);
        File xmlFileOut = new File(dirOut,name + ".xml");

        System.out.println("convert " + urlName
                           + "  " + pdfFile
                           + " -> " + xmlFileOut);


        FileOutputStream out = null;
        BufferedOutputStream bufOut = null;
        try {
            out = new FileOutputStream(xmlFileOut);
            bufOut = new BufferedOutputStream(out);


            SAXWriter writer = new SAXWriter(bufOut,Strings.UTF8);

            writer.startDocument();

            writer.startSimpleElement("document","url",urlName);
	    writer.startSimpleElement("chunk","type","document");

            writer.characters(normalizedText);

	    writer.endSimpleElement("chunk");
            writer.endSimpleElement("document");

            writer.endDocument();

        } finally {
            Streams.closeQuietly(bufOut);
            Streams.closeQuietly(out);
        }
    }


    static String extractText(File pdfFile) throws IOException {
        PDDocument doc = PDDocument.load(pdfFile);
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return text;
        } finally {
            if (doc != null)
		doc.close();
        }
    }


    static String normalize(String text) {
        // replace PDFBox errors
        for (int k = 0; k < TEXT_SUBSTITUTIONS.length; ++k)
            text = text.replaceAll(TEXT_SUBSTITUTIONS[k][0],
                                   TEXT_SUBSTITUTIONS[k][1]);

        // replace illegal XML controls
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            sb.append((Character.isDefined(c) 
                       && (c >= 0x20    // non-control
                           || c == 0x9 // horizontal tab
                           || c == 0xA // line feed
                           || c == 0xD // cr
                           )
                       )
                      ? c
                      : '?');
        }
        return sb.toString();
    }

    static final String[][] TEXT_SUBSTITUTIONS
        = new String[][]
        {
            { "currency1a", "\u00E4" },
            { "currency1e", "\u00EB" },
            { "currency1o", "\u00F6" },
            { "currency1u", "\u00FC" },
            { "currency1b", "\u00F6" },
            { "currency1 ", "\u00EF" }
	};


}