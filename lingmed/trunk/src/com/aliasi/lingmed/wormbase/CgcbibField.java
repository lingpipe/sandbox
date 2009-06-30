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

package com.aliasi.lingmed.wormbase;

import java.util.LinkedHashSet;

public class CgcbibField {

    public enum CGC_FIELDS {
        ABSTRACT, AUTHORS, CITATION, GENES, KEY, MEDLINE, TYPE, TITLE
            };

    static final String LBL_ABSTRACT = "Abstract";
    static final String LBL_AUTHORS = "Authors";
    static final String LBL_CITATION = "Citation";
    static final String LBL_GENES = "Genes";
    static final String LBL_KEY = "Key";
    static final String LBL_MEDLINE = "Medline";
    static final String LBL_TYPE = "Type";
    static final String LBL_TITLE = "Title";

    static final String RECORD_SEPARATOR_REGEX = "\\s+-+";

    private CGC_FIELDS mLabel;
    private LinkedHashSet<String> mText;

    CgcbibField() { 
        mText = new LinkedHashSet<String>();
    }

    static boolean isSep(String line) {
        if (line == null) return false;
        return line.matches(RECORD_SEPARATOR_REGEX);
    }

    static boolean isStart(String line) {
        if (line == null) return false;
        CGC_FIELDS label = null;
        int idx = line.indexOf(":");
        if (idx > 0) {
            String lbl = line.substring(0,idx);
            label = toLabel(lbl);
        }
        if (label != null)  return true;
        return false;
    }


    CGC_FIELDS label() { return mLabel; }

    void processLine(String line) {
        line = line.trim();
        CGC_FIELDS label = null;
        int idx = line.indexOf(":");
        if (idx > 0) {
            String lbl = line.substring(0,idx);
            label = toLabel(lbl);
        }
        if (label != null) {
            mLabel = label;
            mText.add(line.substring(++idx));
        } else {
            mText.add(line);
        }
    }

    String text() { 
        StringBuffer result = new StringBuffer();
        for (String text : mText) {
            result.append(text);
            result.append(" ");
        }
        return result.toString();
    }

    public String toString() { 
        StringBuffer result = new StringBuffer();
        result.append("label: "+mLabel+"\n");
        result.append("text: ");
        for (String text : mText) {
            result.append(text);
            result.append(" ");
        }
        return result.toString();
    }

    static CGC_FIELDS toLabel(String lbl) {
        if (lbl.equals(LBL_ABSTRACT)) return CGC_FIELDS.ABSTRACT;
        if (lbl.equals(LBL_AUTHORS)) return CGC_FIELDS.AUTHORS;
        if (lbl.equals(LBL_CITATION)) return CGC_FIELDS.CITATION;
        if (lbl.equals(LBL_GENES)) return CGC_FIELDS.GENES;
        if (lbl.equals(LBL_KEY)) return CGC_FIELDS.KEY;
        if (lbl.equals(LBL_MEDLINE)) return CGC_FIELDS.MEDLINE;
        if (lbl.equals(LBL_TYPE)) return CGC_FIELDS.TYPE;
        if (lbl.equals(LBL_TITLE)) return CGC_FIELDS.TITLE;
        return null;
    }

    public static void main(String[] args) {
        final String lineNoValue = "    Medline:";
        final String lineLabelValue = "    Authors: Abdulkader N;Brun JL";
        final String lineNoLabel = "             free-living nematode C. elegans.";
        final String lineSep = "             -------------------";
        final String lineNull = null;
        final String lineEmpty = "";


        System.out.println("isStart(lineNoValue): "
                           +isStart(lineNoValue));
        System.out.println("isStart(lineLabelValue): "
                           +isStart(lineLabelValue));
        System.out.println("isStart(lineNoLabel): "
                           +isStart(lineNoLabel));
        System.out.println("isStart(lineSep): "
                           +isStart(lineSep));
        System.out.println("isStart(lineNull): "
                           +isStart(lineNull));
        System.out.println("isStart(lineEmpty): "
                           +isStart(lineEmpty));

        System.out.println("isSep(lineNoValue): "
                           +isSep(lineNoValue));
        System.out.println("isSep(lineLabelValue): "
                           +isSep(lineLabelValue));
        System.out.println("isSep(lineNoLabel): "
                           +isSep(lineNoLabel));
        System.out.println("isSep(lineSep): "
                           +isSep(lineSep));
        System.out.println("isSep(lineNull): "
                           +isSep(lineNull));
        System.out.println("isSep(lineEmpty): "
                           +isSep(lineEmpty));


        CgcbibField f = new CgcbibField();
        f.processLine(lineNoValue);
        System.out.println("lineNoValue as field: ");
        System.out.println(f.toString());

        f = new CgcbibField();
        f.processLine(lineLabelValue);
        System.out.println("lineLabelValue:");
        System.out.println(f.toString());

        f = new CgcbibField();
        f.processLine(lineLabelValue);
        f.processLine(lineNoLabel);
        System.out.println("lineLabelValue + next line");
        System.out.println(f.toString());
    }        
}

