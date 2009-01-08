package com.aliasi.uima;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import com.aliasi.chunk.Chunking;
import com.aliasi.ne.CompiledEstimator;
import com.aliasi.ne.Decoder;
import com.aliasi.ne.NEChunker;
import com.aliasi.tokenizer.TokenCategorizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Reflection;
import com.aliasi.util.Streams;
import com.ibm.uima.analysis_engine.ResultSpecification;
import com.ibm.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import com.ibm.uima.analysis_engine.annotator.AnnotatorContext;
import com.ibm.uima.analysis_engine.annotator.AnnotatorContextException;
import com.ibm.uima.analysis_engine.annotator.AnnotatorInitializationException;
import com.ibm.uima.analysis_engine.annotator.AnnotatorProcessException;
import com.ibm.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import com.ibm.uima.jcas.impl.JCas;

public class MentionAnnotator extends JTextAnnotator_ImplBase {

	private NEChunker mChunker;

	public void initialize(AnnotatorContext arg0)
			throws AnnotatorInitializationException,
			AnnotatorConfigurationException {
		// TODO Auto-generated method stub
		super.initialize(arg0);
		reconfigure();
	}

	public void reconfigure() throws AnnotatorConfigurationException,
			AnnotatorInitializationException {
		String tokenCategorizerClassName = null;
		String tokenizerFactoryClassName = null;
		File modelFileName = null;

		double pruningThreshold;
		try {
			tokenCategorizerClassName = (String) getContext()
					.getConfigParameterValue("TokenCategorizerClass");
			tokenizerFactoryClassName = (String) getContext()
					.getConfigParameterValue("TokenizerFactoryClass");
			modelFileName = new File((String) getContext()
					.getConfigParameterValue("ModelFile"));
			pruningThreshold = ((Float) getContext().getConfigParameterValue(
					"PruningThreshold")).doubleValue();
		} catch (AnnotatorContextException e) {
			throw new AnnotatorConfigurationException(e);
		}

		TokenCategorizer categorizer = (TokenCategorizer) Reflection
				.newInstance(tokenCategorizerClassName);
		if (categorizer == null) {
			throw new AnnotatorConfigurationException(new Exception(
					"Null tok categorizer"));
		}

		TokenizerFactory factory = (TokenizerFactory) Reflection
				.newInstance(tokenizerFactoryClassName);
		if (factory == null) {
			throw new AnnotatorConfigurationException(new Exception(
					"Null tok factory"));
		}

		Decoder decoder = null;
		try {
			decoder = new Decoder(modelFileName, categorizer, pruningThreshold);
		} catch (IOException e) {
			throw new AnnotatorConfigurationException(e);
		}
		mChunker = new NEChunker(factory, decoder);
	}

	public void process(JCas jcas, ResultSpecification arg1)
			throws AnnotatorProcessException {
		Iterator sentIt = jcas.getJFSIndexRepository().getAnnotationIndex(
				Sentence.type).iterator();
		while (sentIt.hasNext()) {
			Chunk s = (Chunk) sentIt.next();

			Chunking chunking = mChunker.chunk(s.getCoveredText());
			Set chunkSet = chunking.chunkSet();
			Iterator it = chunkSet.iterator();
			while (it.hasNext()) {
				com.aliasi.chunk.Chunk chunk = (com.aliasi.chunk.Chunk) it
						.next();
				Mention m = new Mention(jcas);
				m.setBegin(chunk.start()+s.getStart());
				m.setEnd(chunk.end()+s.getStart());
				m.setChunkType(chunk.type().toString());
				m.setScore((float) chunk.score());
				m.addToIndexes();
			}
		}

	}

}
