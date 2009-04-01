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

package com.aliasi.lingmed.genelinkage;

import com.aliasi.chunk.Chunk;

import java.util.Set;

/**
 * An <code>ArticleMention</code> composes the citation text
 * and scored chunkings for a pubmed article.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class ArticleMention {
    private String mPubmedId;
    private String mText;
    private double mTotalScore;
    private Set<Chunk> mChunkSet;

    public ArticleMention(String pubmedId,
			  String text,
			  double totalScore,
			  Set<Chunk> chunkSet) {
	mPubmedId = pubmedId;
	mText = text;
	mTotalScore = totalScore;
	mChunkSet = chunkSet;
    }

    String pubmedId() { return mPubmedId; }

    String text() { return mText; }

    double totalScore() { return mTotalScore; }

    Set<Chunk> chunkSet() { return mChunkSet; }

}
