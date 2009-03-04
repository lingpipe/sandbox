package com.aliasi.webnotes;

/* EntityRec is tuple {<int>dbId, <String>sourceId, <String>entityId} */

public class EntityRec {

    private final int mDbId;
    private final String mSourceId;
    private final String mEntityId;

    public EntityRec(int dbId, String sourceId, String entityId) {
	mDbId = dbId;
	mSourceId = sourceId;
	mEntityId = entityId;
    }

    public int id() { 
	return mDbId; 
    }

    public String sourceId() { 
	return mSourceId;
    }

    public String entityId() { 
	return mEntityId;
    }

    public int getEntityAsInt() { 
	int result = -1;
	try {
	    result = Integer.parseInt(mEntityId);
	} catch ( NumberFormatException e ) { }
	return result;
    }

    public String toString() {
	return("id: "+mDbId+"\tsource_id: "+mSourceId+"\tentity_id: "+mEntityId);
    }

    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final EntityRec other = (EntityRec) obj;
	if (mDbId != other.id()
	    || !mSourceId.equals(other.sourceId())
	    || !mEntityId.equals(other.entityId()))
	    return false;
	return true;
    }

    public int hashCode() {
	final int PRIME = 31;
	int result = 1;
	result = PRIME * result + mDbId;
	result = PRIME * result + mSourceId.hashCode();
	result = PRIME * result + mEntityId.hashCode();
	return result;
    }

}
