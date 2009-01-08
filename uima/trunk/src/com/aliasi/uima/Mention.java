

/* First created by JCasGen Sun Jan 08 17:35:43 EST 2006 */
package com.aliasi.uima;

import com.ibm.uima.jcas.impl.JCas; 
import com.ibm.uima.jcas.cas.TOP_Type;



/** A mention
 * Updated by JCasGen Sun Jan 08 17:35:43 EST 2006
 * XML source: C:/Documents and Settings/Bob Carpenter/workspace/com.aliasi.uima/desc/SentenceAnnotator.xml
 * @generated */
public class Mention extends Chunk {
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
  protected Mention() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Mention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Mention(JCas jcas) {
    super(jcas);
    readObject();   
  } 
  
  public Mention(JCas jcas, int begin, int end) {
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
     
}

    