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

import java.util.Comparator;

/**
 * An <code>ArticleComparator</code> orders {@link ArticleMention}s
 * in order of best (i.e. lowest) score.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */


public class ArticleMentionComparator implements Comparator<ArticleMention> {
    // lower is better - ordering is reversed
    public int compare(ArticleMention art_a, ArticleMention art_b) {
	if (art_a.totalScore() > art_b.totalScore()) return -1;
	if (art_a.totalScore() < art_b.totalScore()) return 1;
	return 0;
    }
}
