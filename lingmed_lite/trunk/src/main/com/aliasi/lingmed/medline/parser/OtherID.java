/*
 * LingPipe v. 3.9
 * Copyright (C) 2003-2010 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.lingmed.medline.parser;

import com.aliasi.lingpipe.xml.TextAccumulatorHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An <code>OtherID</code> provides an alternative identifier
 * from a specified source.
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td><i>Source Abbreviation</i></td><td><i>Description</i></td>
 *     <td><i>Note</i></td></tr>
 * <tr><td><code>NASA</code></td>
 *     <td>National Aeronautics and Space Administration</td>
 *     <td>&nbsp;</td></tr>
 * <tr><td><code>KIE</code></td>
 *     <td>Kennedy Institute of Ethics, Georgetown University</td>
 *     <td>&nbsp;</td></tr>
 * <tr><td><code>PIP</code></td>
 *     <td>Population Information Program, Johns Hopkins School of Public
 *         Health</td>
 *     <td>not currently used</td></tr>
 * <tr><td><code>POP</code></td>
 *     <td>former NLM POPLINE database</td>
 *     <td>not currently used</td></tr>
 * <tr><td><code>ARPL</code></td>
 *     <td>Annual Review of Population Law</td>
 *     <td>not currently used</td></tr>
 * <tr><td><code>CPC</code></td>
 *     <td>Carolina Population Center</td>
 *     <td>not currently used</td></tr>
 * <tr><td><code>IND</code></td>
 *     <td>Population Index</td>
 *     <td>not currently used</td></tr>
 * <tr><td><code>CPFH</code></td>
 *     <td>Center for Population and Family Health Library/Information
 *         Program</td>
 *     <td>not currently used</td></tr>
 * <tr><td><code>CLML</code></td>
 *     <td>Current List of Medical Literature</td>
 *     <td>reserved for future use</td></tr>
 * <tr><td><code>IM</code></td>
 *     <td>Index Medicus</td>
 *     <td>reserved for future use (intended for
 *         pre-1966 publications)</td></tr>
 * <tr><td><code>NLM</code></td>
 *     <td>PubMed Central</td>
       <td>&nbsp;</td>
 * </tr>
 * <tr><td><code>SGC</code></td>
 *     <td>Surgeon General's Catalog</td>
 *      <td>reserved for future use</td></tr>
 * <tr><td><code>NRCBL</code></td>
 *     <td>National Reference Center for Biomedical Literature</td>
 *     <td>for
 *         the KIE Reference Library shelving location</td></tr>
 * </table>
 * </blockquote>
 *
 * @author  Bob Carpenter
 * @version 3.7
 * @since   LingPipe2.0
 */
public class OtherID {

    private final String mSource;
    private final String mPrefix;
    private final String mID;

    OtherID(String source, String id, String prefix) {
        mSource = source;
        mID = id;
        mPrefix = prefix;
    }

    /**
     * Returns the prefix attribute for this other identifier, which
     * may be <code>null</code> if none was specified.
     *
     * @return The prefix attribute for this other identifier.
     */
    public String prefix() {
        return mPrefix;
    }

    /**
     * Returns the source of this identifier.  The valid source
     * values are listed in the class documentation above.
     *
     * @return The source of this identifier.
     */
    public String source() {
        return mSource;
    }

    /**
     * Return the identifier.  This identifier is interpreted
     * relative to the source.
     *
     * @return The identifier.
     */
    public String id() {
        return mID;
    }

    /**
     * Return a string-based representation of this other identifier.
     *
     * @return A string-based representation of this other identifier.
     */
    @Override
    public String toString() {
        return "Source=" + source() + " ID=" + mID;
    }

    static class Handler extends TextAccumulatorHandler {
        private String mPrefix;
        private String mSource;
        @Override
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            mSource = atts.getValue(MedlineCitationSet.SOURCE_ATT);
            mPrefix = atts.getValue(MedlineCitationSet.PREFIX_ATT);
            super.startElement(namespaceURI,localName,qName,atts);
        }
        public OtherID getOtherID() {
            return new OtherID(mSource,getText(),mPrefix);
        }

    }
}
