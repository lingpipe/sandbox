
/* First created by JCasGen Sun Jan 08 17:35:44 EST 2006 */
package com.aliasi.uima;

import com.ibm.uima.jcas.impl.JCas;
import com.ibm.uima.cas.impl.CASImpl;
import com.ibm.uima.cas.impl.FSGenerator;
import com.ibm.uima.cas.FeatureStructure;
import com.ibm.uima.cas.impl.TypeImpl;
import com.ibm.uima.cas.Type;

/** A mention
 * Updated by JCasGen Sun Jan 08 17:35:44 EST 2006
 * @generated */
public class Mention_Type extends Chunk_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;};
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Mention_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Mention_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Mention(addr, Mention_Type.this);
  			   Mention_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Mention(addr, Mention_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Mention.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCas.getFeatOkTst("com.aliasi.uima.Mention");


  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Mention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    