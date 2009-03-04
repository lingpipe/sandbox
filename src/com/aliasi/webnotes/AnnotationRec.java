package com.aliasi.webnotes;

/* AnnotationRec is tuple {<int>article, <int>entity, <int>user} */

public class AnnotationRec {

    private final int mArticleId;
    private final int mEntityId;
    private final int mUserId;

    public AnnotationRec(int articleId, int entityId, int userId) {
	mArticleId = articleId;
	mEntityId = entityId;
	mUserId = userId;
    }

    public int articleId() { 
	return mArticleId; 
    }

    public int entityId() { 
	return mEntityId; 
    }

    public int userId() { 
	return mUserId; 
    }

    public String toString() {
	return("article: "+mArticleId+"\tentity: "+mEntityId+"\tuser: "+mUserId);
    }

    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final AnnotationRec other = (AnnotationRec) obj;
	if (mArticleId != other.articleId()
	    || mEntityId != other.entityId()
	    || mUserId != other.userId())
	    return false;
	return true;
    }

    public int hashCode() {
	final int PRIME = 31;
	int result = 1;
	result = PRIME * result + mArticleId;
	result = PRIME * result + mEntityId;
	result = PRIME * result + mUserId;
	return result;
    }

}