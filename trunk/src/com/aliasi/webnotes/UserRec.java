package com.aliasi.webnotes;

/* UserRec is tuple {<int>id, <String>name} */

public class UserRec {

    private final int mDbId;
    private final String mName;

    public UserRec(int dbId, String name) {
	mDbId = dbId;
	mName = name;
    }

    public int id() { 
	return mDbId; 
    }

    public String name() { 
	return mName; 
    }

    public String toString() {
	return("id: "+mDbId+"\tname: "+mName);
    }

    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final UserRec other = (UserRec) obj;
	if (mDbId != other.id()
	    || !mName.equals(other.name()))
	    return false;
	return true;
    }

    public int hashCode() {
	final int PRIME = 31;
	int result = 1;
	result = PRIME * result + mDbId;
	result = PRIME * result + mName.hashCode();
	return result;
    }

}

