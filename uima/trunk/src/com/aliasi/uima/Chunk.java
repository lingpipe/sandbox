

/* First created by JCasGen Sun Jan 08 17:35:43 EST 2006 */
package com.aliasi.uima;

import com.ibm.uima.jcas.impl.JCas; 
import com.ibm.uima.jcas.cas.TOP_Type;

import com.ibm.uima.jcas.tcas.Annotation;


/** A chunk.
 * Updated by JCasGen Sun Jan 08 17:35:43 EST 2006
 * XML source: C:/Documents and Settings/Bob Carpenter/workspace/com.aliasi.uima/desc/SentenceAnnotator.xml
 * @generated */
public class Chunk extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCas.getNextIndex();
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Chunk() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Chunk(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Chunk(JCas jcas) {
    super(jcas);
    readObject();   
  } 
  
  public Chunk(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: chunkType

  /** getter for chunkType - gets The type of chunk
   * @generated */
  public String getChunkType() {
    if (Chunk_Type.featOkTst && ((Chunk_Type)jcasType).casFeat_chunkType == null)
      JCas.throwFeatMissing("chunkType", "com.aliasi.uima.Chunk");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Chunk_Type)jcasType).casFeatCode_chunkType);}
    
  /** setter for chunkType - sets The type of chunk 
   * @generated */
  public void setChunkType(String v) {
    if (Chunk_Type.featOkTst && ((Chunk_Type)jcasType).casFeat_chunkType == null)
      JCas.throwFeatMissing("chunkType", "com.aliasi.uima.Chunk");
    jcasType.ll_cas.ll_setStringValue(addr, ((Chunk_Type)jcasType).casFeatCode_chunkType, v);}    
   
    
  //*--------------*
  //* Feature: score

  /** getter for score - gets The type of chunk
   * @generated */
  public float getScore() {
    if (Chunk_Type.featOkTst && ((Chunk_Type)jcasType).casFeat_score == null)
      JCas.throwFeatMissing("score", "com.aliasi.uima.Chunk");
    return jcasType.ll_cas.ll_getFloatValue(addr, ((Chunk_Type)jcasType).casFeatCode_score);}
    
  /** setter for score - sets The type of chunk 
   * @generated */
  public void setScore(float v) {
    if (Chunk_Type.featOkTst && ((Chunk_Type)jcasType).casFeat_score == null)
      JCas.throwFeatMissing("score", "com.aliasi.uima.Chunk");
    jcasType.ll_cas.ll_setFloatValue(addr, ((Chunk_Type)jcasType).casFeatCode_score, v);}    
  }

    