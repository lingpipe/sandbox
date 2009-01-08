
/* First created by JCasGen Sun Jan 08 17:35:43 EST 2006 */
package com.aliasi.uima;

import com.ibm.uima.jcas.impl.JCas;
import com.ibm.uima.cas.impl.CASImpl;
import com.ibm.uima.cas.impl.FSGenerator;
import com.ibm.uima.cas.FeatureStructure;
import com.ibm.uima.cas.impl.TypeImpl;
import com.ibm.uima.cas.Type;
import com.ibm.uima.cas.impl.FeatureImpl;
import com.ibm.uima.cas.Feature;
import com.ibm.uima.jcas.tcas.Annotation_Type;

/** A chunk.
 * Updated by JCasGen Sun Jan 08 17:35:43 EST 2006
 * @generated */
public class Chunk_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;};
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Chunk_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Chunk_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Chunk(addr, Chunk_Type.this);
  			   Chunk_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Chunk(addr, Chunk_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Chunk.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCas.getFeatOkTst("com.aliasi.uima.Chunk");
 
  /** @generated */
  final Feature casFeat_chunkType;
  /** @generated */
  final int     casFeatCode_chunkType;
  /** @generated */ 
  public String getChunkType(int addr) {
        if (featOkTst && casFeat_chunkType == null)
      JCas.throwFeatMissing("chunkType", "com.aliasi.uima.Chunk");
    return ll_cas.ll_getStringValue(addr, casFeatCode_chunkType);
  }
  /** @generated */    
  public void setChunkType(int addr, String v) {
        if (featOkTst && casFeat_chunkType == null)
      JCas.throwFeatMissing("chunkType", "com.aliasi.uima.Chunk");
    ll_cas.ll_setStringValue(addr, casFeatCode_chunkType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_score;
  /** @generated */
  final int     casFeatCode_score;
  /** @generated */ 
  public float getScore(int addr) {
        if (featOkTst && casFeat_score == null)
      JCas.throwFeatMissing("score", "com.aliasi.uima.Chunk");
    return ll_cas.ll_getFloatValue(addr, casFeatCode_score);
  }
  /** @generated */    
  public void setScore(int addr, float v) {
        if (featOkTst && casFeat_score == null)
      JCas.throwFeatMissing("score", "com.aliasi.uima.Chunk");
    ll_cas.ll_setFloatValue(addr, casFeatCode_score, v);}
    
  


  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Chunk_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_chunkType = jcas.getRequiredFeatureDE(casType, "chunkType", "uima.cas.String", featOkTst);
    casFeatCode_chunkType  = (null == casFeat_chunkType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_chunkType).getCode();

 
    casFeat_score = jcas.getRequiredFeatureDE(casType, "score", "uima.cas.Float", featOkTst);
    casFeatCode_score  = (null == casFeat_score) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_score).getCode();

  }
}



    