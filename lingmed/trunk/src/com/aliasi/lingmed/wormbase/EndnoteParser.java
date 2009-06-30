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

import com.aliasi.corpus.Handler;
import com.aliasi.corpus.InputSourceParser;
import com.aliasi.corpus.ObjectHandler;

import java.io.IOException;
import java.io.LineNumberReader;

import java.util.ArrayList;

import org.xml.sax.InputSource;

/**
 * Parser for the wormbase literature index,
 * which is distributed as a flat file exported from EndNote.
 * A record is a set of lines, separated by a blank line.
 * 
 * 
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.3
 */

public class EndnoteParser extends InputSourceParser<ObjectHandler<EndnoteRecord>> {

    public EndnoteParser() {
    }

    public ObjectHandler<EndnoteRecord> endnoteHandler() {
        return (ObjectHandler<EndnoteRecord>) getHandler();
    }

    public void parse(InputSource is) throws IOException { 
        EndnoteRecord curRec = new EndnoteRecord();
        String line = null;
        LineNumberReader in = new LineNumberReader(is.getCharacterStream());
        while ((line = in.readLine()) != null) {
            if (isRecordStart(line)) {
                if (curRec.getType() != null) {
                    endnoteHandler().handle(curRec);
                }
                curRec = new EndnoteRecord();
            } else {
                curRec.processLine(line);
            }
        }
        in.close();
        // finish processing last record in file
        if (curRec.getType() != null) {
            endnoteHandler().handle(curRec);
        }
    }


    boolean isRecordStart(String line) {
        if (line.trim().length() == 0) return true;
        return false;
    }

}