package com.aliasi.lingmed.mesh;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.HashMap;
import java.util.Map;

import java.util.zip.GZIPInputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@code MeshDictionaryCommand} class provides a static main method
 * for compiling a chunker based on MeSH terms.
 *
 * @author Bob Carpenter
 * @version 1.4
 * @since LingMed1.4
 */
public class MeshDictionaryCommand {

    private MeshDictionaryCommand() {
        /* no instances */
    }

    public static void main(String[] args) throws SAXException, IOException {
        File meshGzipFile = new File(args[0]);
        File dictChunkerFile = new File(args[1]);
        dictChunkerFile.mkdirs();

        String fileURL = meshGzipFile.toURI().toURL().toString();
        System.out.println("FILE URL=" + fileURL);

        MeshParser parser = new MeshParser();
        DictionaryHandler handler = new DictionaryHandler();
        parser.setHandler(handler);

        InputStream fileIn = new FileInputStream(meshGzipFile);
        InputStream gzipIn = new GZIPInputStream(fileIn);
        InputSource inSource = new InputSource(gzipIn);
        inSource.setEncoding(Strings.UTF8);
        
        parser.parse(inSource);
        handler.compileDictionary(dictChunkerFile);
    }


    static final TokenizerFactory TOKENIZER_FACTORY
        = IndoEuropeanTokenizerFactory.INSTANCE;

    static class DictionaryHandler implements ObjectHandler<Mesh> {
        final MapDictionary<String> mDictionary 
            = new MapDictionary<String>();
        public void handle(Mesh mesh) {
            MeshNameUi descriptor = mesh.descriptor();
            String headingName = descriptor.name(); 
            for (MeshConcept concept : mesh.conceptList()) {
                for (MeshTerm term : concept.termList()) {
                    MeshNameUi termNameUi = term.nameUi();
                    String termName = termNameUi.name();
                    DictionaryEntry<String> entry 
                        = new DictionaryEntry<String>(termName,headingName);
                    mDictionary.addEntry(entry);
                }
            }
        }
        void compileDictionary(File file) throws IOException {
            ChunkerSerializer serializer = new ChunkerSerializer(mDictionary);
            AbstractExternalizable.serializeTo(serializer,file);
        }

    }

    static class ChunkerSerializer extends AbstractExternalizable {
        static final long serialVersionUID = 1879892051066513198L;
        private final MapDictionary<String> mDictionary;
        public ChunkerSerializer() { 
            this(null); 
        }
        public ChunkerSerializer(MapDictionary<String> dictionary) {
            mDictionary = dictionary;
        }
        @Override
        public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
            @SuppressWarnings("unchecked")
            MapDictionary<String> dictionary = 
                (MapDictionary<String>) in.readObject();
            boolean returnAllMatches = true;
            boolean caseSensitive = false;
            return new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                              returnAllMatches,caseSensitive);
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mDictionary);
        }
        
    }

}