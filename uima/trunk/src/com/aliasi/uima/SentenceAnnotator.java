package com.aliasi.uima;

import java.util.Iterator;
import java.util.Set;

import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Reflection;
import com.ibm.uima.analysis_engine.ResultSpecification;
import com.ibm.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import com.ibm.uima.analysis_engine.annotator.AnnotatorContext;
import com.ibm.uima.analysis_engine.annotator.AnnotatorContextException;
import com.ibm.uima.analysis_engine.annotator.AnnotatorInitializationException;
import com.ibm.uima.analysis_engine.annotator.AnnotatorProcessException;
import com.ibm.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import com.ibm.uima.jcas.impl.JCas;

public class SentenceAnnotator extends JTextAnnotator_ImplBase {

	private SentenceChunker mChunker;
	private SentenceModel mModel;
	private TokenizerFactory mFactory;
	
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}

	public void initialize(AnnotatorContext arg0) throws AnnotatorInitializationException, AnnotatorConfigurationException {
		// TODO Auto-generated method stub
		super.initialize(arg0);
		reconfigure();
	}

	public void reconfigure() throws AnnotatorConfigurationException, AnnotatorInitializationException {
		String smodel=null;
		String sfact=null;
		try {
			smodel = (String) getContext().getConfigParameterValue("ModelClass");
			sfact  = (String) getContext().getConfigParameterValue("TokenizerFactoryClass");
		} catch (AnnotatorContextException e) {
			throw new AnnotatorConfigurationException(e);
		}
		 
		mModel = (SentenceModel) Reflection.newInstance(smodel);
		if (mModel == null) {
			throw new AnnotatorConfigurationException(new Exception("Null sentence model"));
		}
		
		mFactory = (TokenizerFactory) Reflection.newInstance(sfact);
		if (mFactory == null) {
			throw new AnnotatorConfigurationException(new Exception("Null factory"));
		}
				
		mChunker = new SentenceChunker(mFactory,mModel);
	}

	public void process(JCas jcas, ResultSpecification arg1)
			throws AnnotatorProcessException {
		String text = jcas.getDocumentText();
		Chunking chunking = mChunker.chunk(text);
		Set chunkSet = chunking.chunkSet();
		Iterator it = chunkSet.iterator();
		while (it.hasNext()) {
			com.aliasi.chunk.Chunk chunk = (com.aliasi.chunk.Chunk) it.next();
			Sentence s = new Sentence(jcas);
			s.setBegin(chunk.start());
			s.setEnd(chunk.end());
			s.setChunkType(chunk.type().toString());
			s.setScore((float)chunk.score());
			s.addToIndexes();
		}
		
	}

}
