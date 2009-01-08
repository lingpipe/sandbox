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

public class OmimField {
    private final String mLabel;
    private final String[] mText;

    public OmimField(String label, String text[]) {
	mLabel = label;
	mText = text;
    }

    String label() { return mLabel; }
    String[] text() { return mText; }

    String concatText(String sep) {
	StringBuffer result = new StringBuffer();
	for (int i=0; i<mText.length; i++) {
	    result.append(mText[i]);
	    result.append(sep);
	}
	return result.toString();
    }

}

