/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.lingmed.lingblast;

import com.aliasi.chunk.*;
import com.aliasi.dict.*;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Arrays;
import com.aliasi.util.Pair;

import com.aliasi.lingmed.entrezgene.EntrezGeneSearcher;
import com.aliasi.lingmed.medline.MedlineSearcher;
import com.aliasi.lingmed.utils.FileUtils;

import com.aliasi.lm.CompiledNGramProcessLM;

import com.aliasi.stats.Statistics;

import com.aliasi.util.Arrays;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;

import java.io.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

import org.apache.log4j.*;

/**
 * Find gene mentions in text and use language models
 * to assign score to each gene.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class LingBlast {
    private final Logger mLogger
        = Logger.getLogger(LingBlast.class);

    private File mModelDir;
    private EntrezGeneSearcher mEntrezGeneSearcher;
    private MedlineSearcher mMedlineSearcher;
    private ExactDictionaryChunker mDictionaryChunker;
    private CompiledNGramProcessLM mGenomicsLM;
    private int mNGram;
    private double mGenomicsThreshold;

    public LingBlast(MedlineSearcher medlineSearcher,
                     EntrezGeneSearcher entrezGeneSearcher,
                     ExactDictionaryChunker dictionaryChunker,
                     File modelDir,
                     double threshold) throws IOException, ClassNotFoundException {
	Appender appender = new ConsoleAppender(new SimpleLayout());
	mLogger.addAppender(appender);
        mMedlineSearcher = medlineSearcher;
        mEntrezGeneSearcher = entrezGeneSearcher;
        mDictionaryChunker = dictionaryChunker;
        mModelDir = modelDir;
        mGenomicsLM = readGenomicsLM();
        mGenomicsThreshold = threshold;
    }

    /**
     * Find all gene mentions in input text.
     * Score text against genomics model.
     * For genes mentioned in text, score text against per-gene model.
     */
    public Pair<Double, Chunking>  lingblast(CharSequence input) throws IOException, ClassNotFoundException {
        double genomicsCrossEntropyRate = 
            getCrossEntropyRate(input,mGenomicsLM);
        if (mLogger.isDebugEnabled())
            mLogger.debug("genomicsCrossEntropyRate: "+genomicsCrossEntropyRate);
        if (genomicsCrossEntropyRate < mGenomicsThreshold) {
            Chunking geneNames = lingblastGenes(input);
            return new Pair(genomicsCrossEntropyRate,geneNames);
        }
        return new Pair(genomicsCrossEntropyRate,null);
    }

    private Chunking lingblastGenes(CharSequence input) throws IOException, ClassNotFoundException {
        Set<String> allGeneIds = new HashSet<String>();
        Chunking geneNames = mDictionaryChunker.chunk(input);
        if (mLogger.isDebugEnabled())
            mLogger.debug("found chunks: "+geneNames.chunkSet().size());
        for (Chunk chunk : geneNames.chunkSet()) {
            int start = chunk.start();
            int end = chunk.end();
            String phrase = input.subSequence(start,end).toString();
            String genes = chunk.type();
            if (mLogger.isDebugEnabled())
                mLogger.debug("     phrase=|" + phrase + "|"
                              + " start=" + start
                              + " end=" + end
                              + " genes=" + genes);
            String[] geneArray = Arrays.csvToArray(genes);
            for (String gene : geneArray) {
                allGeneIds.add(gene);
            }
        }
        Map<String,Double> perGeneLmScores = new HashMap<String,Double>();
        for (String geneId : allGeneIds) {
            CompiledNGramProcessLM perGeneLM = readPerGeneLM(geneId);
            if (perGeneLM != null) {
                double perGeneCrossEntropyRate = 
                    getCrossEntropyRate(input,perGeneLM);
                if (mLogger.isDebugEnabled())
                    mLogger.debug("geneId: "+geneId
                                  +" perGeneCrossEntropyRate: "+perGeneCrossEntropyRate);
                perGeneLmScores.put(geneId,perGeneCrossEntropyRate);
            }
        }
        Set<Chunk> resultChunkSet = new LinkedHashSet<Chunk>();
        for (Iterator geneIt = perGeneLmScores.entrySet().iterator(); geneIt.hasNext(); ) {
            Entry<String,Double> entry = (Entry<String,Double>)geneIt.next();
            String geneId = entry.getKey();
            double score = entry.getValue();
            for (Chunk chunk : geneNames.chunkSet()) {
                String[] geneArray = Arrays.csvToArray(chunk.type());
                if (Arrays.member(geneId,geneArray)) {
                    Chunk newChunk = ChunkFactory.createChunk(chunk.start(),chunk.end(),geneId,score);
                    resultChunkSet.add(newChunk);
                }
            }
        }
        ChunkingImpl result = new ChunkingImpl(input);
        result.addAll(resultChunkSet);
        return result;
    }

    private CompiledNGramProcessLM readGenomicsLM() throws IOException, ClassNotFoundException {
        return readLM(Constants.GENOMICS_LM);
    }

    private CompiledNGramProcessLM readPerGeneLM(String geneId) throws IOException, ClassNotFoundException {
        String modelFileName = geneId+Constants.LM_SUFFIX;
        return readLM(modelFileName);
    }

    private CompiledNGramProcessLM readLM(String modelFileName) throws IOException, ClassNotFoundException {
        File modelFile = new File(mModelDir,modelFileName);
        if (!FileUtils.checkInputFile(modelFile)) {
            if (mLogger.isDebugEnabled())
                mLogger.debug("No model file: "+modelFileName);
            return null;
        }
        CompiledNGramProcessLM lm = (CompiledNGramProcessLM)
            AbstractExternalizable.readObject(modelFile);
        // if (mLogger.isDebugEnabled()) {
		//	mLogger.debug("Read model: "+modelFileName);
        // }
        return lm;
    }

    private double getCrossEntropyRate(CharSequence input, CompiledNGramProcessLM lm) {
        String text = input.toString();
        return -1.0*(lm.log2Estimate(text)/text.length());
    }
}
