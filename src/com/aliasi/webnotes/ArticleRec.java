package com.aliasi.webnotes;

/* ArticleRec is tuple {articleId, pmid, ordering} */

public class ArticleRec {

    private final int mDbId;
    private final int mPmid;
    private final int mOrdering;


    public ArticleRec(int dbId, int pmid, int ordering) {
	mDbId = dbId;
	mPmid = pmid;
	mOrdering = ordering;

    }

    public int id() { 
	return mDbId; 
    }

    public int pmid() { 
	return mPmid; 
    }

    public int ordering() { 
	return mOrdering; 
    }

    public String toString() {
	return("id: "+mDbId+"\tpmid: "+mPmid+"\tordering: "+mOrdering);
    }

    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final ArticleRec other = (ArticleRec) obj;
	if (mDbId != other.id()
	    || mPmid != other.pmid()
	    || mOrdering != other.ordering())
	    return false;
	return true;
    }

    public int hashCode() {
	final int PRIME = 31;
	int result = 1;
	result = PRIME * result + mDbId;
	result = PRIME * result + mPmid;
	result = PRIME * result + mOrdering;
	return result;
    }

}
