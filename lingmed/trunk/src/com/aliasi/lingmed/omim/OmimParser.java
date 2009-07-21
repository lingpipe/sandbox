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

package com.aliasi.lingmed.omim;

import com.aliasi.corpus.Handler;
import com.aliasi.corpus.InputSourceParser;
import com.aliasi.corpus.ObjectHandler;

import java.io.IOException;
import java.io.LineNumberReader;

import java.util.ArrayList;

import org.xml.sax.InputSource;

/**
 * Parser for omim.txt, the distribution of
 * <A href="http://www.ncbi.nlm.nih.gov/sites/entrez?db=omim">
 * OMIM</A>, Online Mendelian Inheritance in Man Database.
 * The distribution is an ASCII text file which contains a set of 
 * fielded records.
 * Records begin with the header: 
 * <CODE>*RECORD*</CODE> on its own line.
 * Fields begin with the header: 
 * <CODE>*FIELD* &lt;FIELD KEY&gt;</CODE> also on its own line.
 * 
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class OmimParser extends InputSourceParser<ObjectHandler<OmimRecord>> {

    private boolean mSaveRaw;

    public OmimParser() {
        mSaveRaw = false;
    }

    public OmimParser(boolean saveRaw) {
        mSaveRaw = saveRaw;
    }

    public ObjectHandler<OmimRecord> omimHandler() {
        return (ObjectHandler<OmimRecord>) getHandler();
    }

    public void parse(InputSource is) throws IOException { 
        OmimRecord curRec = null;
        ArrayList<String> lines = new ArrayList<String>();
        StringBuffer rawText = new StringBuffer();
        String line = null;
        LineNumberReader in = new LineNumberReader(is.getCharacterStream());
        while ((line = in.readLine()) != null) {
            if (isRecordStart(line)) {
                if (curRec != null) {
                    if (lines.size() > 0) {
                        OmimField field = parseField(lines);
                        curRec.processField(field);
                    }
                    if (mSaveRaw) curRec.setRawText(rawText.toString());
                    omimHandler().handle(curRec);
                    lines.clear();
                    if (mSaveRaw) rawText.setLength(0);
                }
                curRec = new OmimRecord();
                if (mSaveRaw) rawText.append(line+"\n");
            }
            else if (isFieldStart(line)) {
                if (curRec == null) {
                    throw new IllegalStateException("parse error at line: "+in.getLineNumber());
                } 
                if (lines.size() > 0) {
                    OmimField field = parseField(lines);
                    curRec.processField(field);
                }
                lines.clear();
                lines.add(line);
                if (mSaveRaw) rawText.append(line+"\n");
            }
            else {
                lines.add(line);
                if (mSaveRaw) rawText.append(line+"\n");
            }
        }
        in.close();
        // finish processing last record in file
        if (curRec != null) {
            if (lines.size() > 0) {
                OmimField field = parseField(lines);
                curRec.processField(field);
            }
            if (mSaveRaw) curRec.setRawText(rawText.toString());
            omimHandler().handle(curRec);
        }
    }

    boolean isFieldStart(String line) {
        if (line.startsWith("*FIELD*")) return true;
        return false;
    }

    boolean isRecordStart(String line) {
        if (line.startsWith("*RECORD*")) return true;
        return false;
    }

    OmimField parseField(ArrayList<String> lines) {
        // get label following token "*FIELD* "
        String label = lines.get(0);
        int i = label.indexOf(' ');
        if (i > 0) label = label.substring(i+1);
        lines.remove(0);
        // remaining lines are text
        String[] text = new String[lines.size()];
        text = lines.toArray(text);
        return new OmimField(label, text);
    }

}