package com.aliasi.html;

import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.SAXWriter;
import com.aliasi.xml.TextAccumulatorHandler;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Reader;

import java.util.HashSet;
import java.util.Set;

import org.cyberneko.html.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class HtmlToText {


    public static void main(String[] args) throws Exception {
        File inDir = new File(args[0]);
        File outDir = new File(args[1]);
        for (File file : inDir.listFiles()) {
            byte[] bytes = Files.readBytesFromFile(file);
            CharsetDetector detector = new CharsetDetector();
            detector.setText(bytes);
            CharsetMatch match = detector.detect();
            String charset = match.getName();
            System.out.println("file=" + file + "\n  charset=" + charset);
            Reader reader = match.getReader();
            InputSource in = new InputSource(reader);

            XMLReader xmlReader = new SAXParser();
            TextHandler textAccum = new TextHandler();
            xmlReader.setContentHandler(textAccum);

            xmlReader.parse(in);

            File outFile = new File(outDir,file.getName() + ".xml");
            FileOutputStream out = new FileOutputStream(outFile);
            SAXWriter writer = new SAXWriter(out,Strings.UTF8);
            writer.startDocument();
            writer.characters("\n");
            writer.startSimpleElement("document","name",file.getName());

            if (textAccum.title().length() > 0) {
                System.out.println("     title=" + textAccum.title().trim());
                writer.characters("\n");
                writer.startSimpleElement("chunk","type","title");
                writer.characters(fix(textAccum.title().trim()));
                writer.endSimpleElement("chunk");
                writer.characters("\n");
            }

            writer.characters("\n");
            writer.startSimpleElement("chunk","type","doc");
            writer.characters(fix(textAccum.text()));
            writer.endSimpleElement("chunk");
            writer.characters("\n");

            writer.characters("\n");
            writer.endSimpleElement("document");
            writer.endDocument();
            out.close();
        }

    }

    static String fix(String in) {
        // problem introduced by NekoHTML giving invalid unicode chars
        // which JDOM can't parse
        return in.replaceAll("\u0000"," ");
    }

    static boolean breakingQName(String qName) {
        return BREAKING_Q_NAMES.contains(qName.toLowerCase());
    }

    static Set<String> BREAKING_Q_NAMES
        = new HashSet<String>();
    static {
        BREAKING_Q_NAMES.add("body");
        BREAKING_Q_NAMES.add("caption");
        BREAKING_Q_NAMES.add("center");
        BREAKING_Q_NAMES.add("dd");
        BREAKING_Q_NAMES.add("dt");
        BREAKING_Q_NAMES.add("li");
        BREAKING_Q_NAMES.add("hr");
        BREAKING_Q_NAMES.add("pre");
        BREAKING_Q_NAMES.add("script");
        BREAKING_Q_NAMES.add("");
        BREAKING_Q_NAMES.add("head");
        BREAKING_Q_NAMES.add("p");
        BREAKING_Q_NAMES.add("br");
        BREAKING_Q_NAMES.add("div");
        BREAKING_Q_NAMES.add("title");
        BREAKING_Q_NAMES.add("tr");
        BREAKING_Q_NAMES.add("blockquote");
        BREAKING_Q_NAMES.add("h1");
        BREAKING_Q_NAMES.add("h2");
        BREAKING_Q_NAMES.add("h3");
        BREAKING_Q_NAMES.add("h4");
        BREAKING_Q_NAMES.add("h5");
        BREAKING_Q_NAMES.add("h6");
    }

    static class TextHandler0 extends TextAccumulatorHandler {
        StringBuilder mTitle = new StringBuilder();
        public String title() {
            return "";
        }
        public String text() {
            return getText();
        }

        public void startElement(String uri, String localName,
                                 String qName, Attributes atts)
            throws SAXException {
            super.startElement(uri,localName,qName,atts);
            super.characters(new char[] { breakingQName(qName)
                                          ? '\n'
                                          : ' ' }, 0 , 1);
            if (qName.equalsIgnoreCase("title"))
                System.out.println("      ***** found title=" + qName + " *****");
        }
    }

    static class TextHandler extends DelegatingHandler {

        TextAccumulatorHandler mTextHandler = new TextAccumulatorHandler();
        TextAccumulatorHandler mTitleHandler = new TextAccumulatorHandler();

        public TextHandler() {
            setDelegate("TITLE",mTitleHandler);
            setDelegate("BODY",mTextHandler);
        }

        String title() {
            return mTitleHandler.getText();
        }

        String text() {
            return normalize(mTextHandler.getText());
        }

        String normalize(String in) {
            return
                in
                .replaceAll("[\\s&&[^\n]]+\n","\n") // remove space before newlines
                .replaceAll("\n\n\n+","\n\n\n");  // 3+ newlines reduce to 3
        }



        public void startDocument() throws SAXException {
            mTextHandler.reset();
            mTitleHandler.reset();
            super.startDocument();
        }
        public void startElement(String uri, String localName,
                                 String qName, Attributes atts)
            throws SAXException {
            super.startElement(uri,localName,qName,atts);
            super.characters(new char[] { breakingQName(qName)
                                          ? '\n'
                                          : ' ' }, 0 , 1);
        }

    }
}