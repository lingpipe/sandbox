package com.aliasi.annotate.corpora;

import com.aliasi.chunk.*;
import com.aliasi.classify.*;
import com.aliasi.corpus.ChunkHandler;

import com.aliasi.util.Files;

import com.aliasi.xml.*;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class InterAnnotatorAgreement {

    static Set<String> sContainingTagSet = new HashSet<String>();

    public static void main(String[] args) throws Exception {
        String[] containingTags = args[0].split(",");
        for (String tag : containingTags)
            sContainingTagSet.add(tag.trim());
        System.out.println("Containing tags=" + sContainingTagSet);

        for (int i = 1; i < args.length; ++i)
            for (int j = i+1; j < args.length; ++j)
                interAnnotatorDir(new File(args[i]),
                                  new File(args[j]));
    }

    static void interAnnotatorDir(File dir1, File dir2) throws Exception {
        System.out.println("\n\nINTER-ANNOTATOR EVALUATION");
        System.out.println("Directory 1=" + dir1.getCanonicalPath());
        System.out.println("Directory 2=" + dir2.getCanonicalPath());
        Set<String> commonFileNames = commonFileNames(dir1,dir2);
        System.out.println(commonFileNames.size()
                           + " files to evaluate");

        ChunkingEvaluation globalEval = new ChunkingEvaluation();
        for (String fileName : commonFileNames)
            interAnnotator(new File(dir1,fileName),
                           new File(dir2,fileName),
                           globalEval);
        PrecisionRecallEvaluation globalPrEval
            = globalEval.precisionRecallEvaluation();

        System.out.printf("P=%5.3f R=%5.3f F=%5.3f\n",
                          globalPrEval.precision(),
                          globalPrEval.recall(),
                          globalPrEval.fMeasure());
        System.out.println("+Annotator1 -Annotator2");
        for (ChunkAndCharSeq chunk : globalEval.falseNegativeSet())
            System.out.println("    " + chunk);
        System.out.println("-Annotator1 +Annotator2");
        for (ChunkAndCharSeq chunk : globalEval.falsePositiveSet())
            System.out.println("    " + chunk);
    }

    static void interAnnotator(File file1, File file2,
                               ChunkingEvaluation globalEval)
        throws Exception{

        Chunking[] chunkings1 = toChunkings(file1);
        Chunking[] chunkings2 = toChunkings(file2);
        ChunkingEvaluation fileEval = new ChunkingEvaluation();
        for (int i = 0; i < chunkings1.length; ++i) {
            fileEval.addCase(chunkings1[i],
                             chunkings2[i]);
            globalEval.addCase(chunkings1[i],
                               chunkings2[i]);
        }
    }

    static Set<String> commonFileNames(File dir1, File dir2) throws Exception {
        File[] files1 = dir1.listFiles();
        File[] files2 = dir2.listFiles();
        Set<String> fileNameSet1 = fileNameSet(files1);
        Set<String> fileNameSet2 = fileNameSet(files2);
        fileNameSet1.retainAll(fileNameSet2);
        return fileNameSet1;
    }

    static Set<String> fileNameSet(File[] files) {
        Set<String> nameSet = new HashSet<String>(files.length*2);
        for (File file : files)
            nameSet.add(file.getName());
        return nameSet;
    }

    static Chunking[] toChunkings(File file) throws Exception {
        System.out.println("file=" + file.getCanonicalPath());
        CollectingHandler handler = new CollectingHandler();
        AnnotatorCorpusParser parser
            = new AnnotatorCorpusParser(sContainingTagSet);
        parser.setHandler(handler);
        parser.parse(file);
        return handler.getChunkings();
    }

    static class CollectingHandler implements ChunkHandler {
        List<Chunking> mChunkingList = new ArrayList<Chunking>();
        public void handle(Chunking chunking) {
            mChunkingList.add(chunking);
        }
        public Chunking[] getChunkings() {
            return mChunkingList
                .<Chunking>toArray(new Chunking[mChunkingList.size()]);
        }
    }
}

