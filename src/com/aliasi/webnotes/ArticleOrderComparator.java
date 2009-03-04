package com.aliasi.webnotes;

import java.util.Comparator;

public class ArticleOrderComparator implements Comparator<ArticleRec> {
    public int compare(ArticleRec art1, ArticleRec art2) {
	return art1.ordering() - art2.ordering();
    }
}
