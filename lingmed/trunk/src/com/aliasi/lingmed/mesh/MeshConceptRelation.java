package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;


/**
 * A {@code MeshConceptRelation} represents a relation between
 * concepts, most commonly, between preferred and subordinate concepts
 * in a record.
 *
 * <p>The relation name (see {@link #relationName()}) is one of the
 * following values:
 *
 * <blockquote><table border="1" cellpadding="5">
 * <tr><th>Relation Name</th><th>Description</th></tr>
 * <tr><td>BRD</td><td>broader</td></tr>
 * <tr><td>NRW</td><td>narrower</td></tr>
 * <tr><td>REL</td><td>related, but not broader or narrow</td></tr>
 * </table></blockquote>
 *
 * <p>The concepts being related are given by the two concept
 * universal identifiers.  
 * 
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshConceptRelation {

    private final String mRelationName;
    private final String mConcept1Ui;
    private final String mConcept2Ui;
    private final String mRelationAttribute;

    MeshConceptRelation(String relationName,
                        String concept1Ui,
                        String concept2Ui,
                        String relationAttribute) {
        mRelationName = relationName;
        mConcept1Ui = concept1Ui;
        mConcept2Ui = concept2Ui;
        mRelationAttribute = relationAttribute.length() == 0 ? null : relationAttribute;
        if (mRelationAttribute != null)
            System.out.println("RA123=" + mRelationAttribute);
    }

    /**
     * Returns the name of the type of this relation, which
     * can be NRW (narrower), BRD (broader), or REL (neither).
     *
     * @return The name for this relation.
     */
    public String relationName() {
        return mRelationName;
    }

    /**
     * Return the unique identifier (UI) for the first
     * concept in the relation.
     *
     * @return First concept in relation.
     */
    public String concept1Ui() {
        return mConcept1Ui;
    }

    /**
     * Return the unique identifier (UI) for the second
     * concept in the relation.
     *
     * @return Second concept in relation.
     */
    public String concept2Ui() {
        return mConcept2Ui;
    }

    /**
     * Returns free text further describing the relation (<b>warning</b>: that
     * this field is no longer current in MeSH).
     *
     * @return Further information describing the relation.
     */
    public String relationAttribute() {
        return mRelationAttribute;
    }

    /**
     * Returns a text representation of this relation.  All of the
     * information returned by this method is available progamatically
     * through the other methods in this class.
     *
     * @return String-based representation of this relation.
     */
    @Override
    public String toString() {
        return "Relation Name=" + mRelationName
            + "; Concept 1 UI=" + mConcept1Ui
            + "; Concept 2 UI=" + mConcept2Ui
            + "; Relational Attribute=" + mRelationAttribute;
    }

    static class Handler extends BaseHandler<MeshConceptRelation> {
        final TextAccumulatorHandler mConcept1UiHandler;
        final TextAccumulatorHandler mConcept2UiHandler;
        final TextAccumulatorHandler mRelationAttributeHandler;
        String mRelationName;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mConcept1UiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONCEPT_1_UI_ELEMENT,
                        mConcept1UiHandler);
            mConcept2UiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONCEPT_2_UI_ELEMENT,
                        mConcept2UiHandler);
            mRelationAttributeHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.RELATION_ATTRIBUTE_ELEMENT,
                        mRelationAttributeHandler);
        }
        @Override
        public void startElement(String url, String relName, String qName,
                                     Attributes atts) throws SAXException {
            super.startElement(url,relName,qName,atts);
            if (MeshParser.CONCEPT_RELATION_ELEMENT.equals(qName))
                mRelationName = atts.getValue(MeshParser.RELATION_NAME_ATT);
        }
        public void reset() {
            mConcept1UiHandler.reset();
            mConcept2UiHandler.reset();
            mRelationAttributeHandler.reset();
            mRelationName = null;
        }
        public MeshConceptRelation getObject() {
            return new MeshConceptRelation(mRelationName,
                                           mConcept1UiHandler.getText().trim(),
                                           mConcept2UiHandler.getText().trim(),
                                           mRelationAttributeHandler.getText().trim());
                                   
        }
    }

    static class ListHandler extends BaseListHandler<MeshConceptRelation> {
        ListHandler(DelegatingHandler parent) {
            super(parent,
                  new Handler(parent),
                  MeshParser.CONCEPT_RELATION_ELEMENT);
        }
    }

}