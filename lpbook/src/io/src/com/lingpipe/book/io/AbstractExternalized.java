package com.lingpipe.book.io;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class AbstractExternalized implements Serializable {

    private final String mS;
    private final int mCount;
    private final Object mNotSerializable = new Object();

    public AbstractExternalized(String s, int count) {
        mS = s;  
        mCount = count;
    }

    public String toString() {
        return mS + ":" + mCount;
    }

    private Object writeReplace() {
        return new Serializer(this);
    }

    static final long serialVersionUID = -688378786294424932L;

    /*x AbstractExternalized.1 */
    private static class Serializer 
        extends AbstractExternalizable {
    /*x*/

        final AbstractExternalized mObj;

        public Serializer() { 
            this(null); 
        }

        Serializer(AbstractExternalized obj) {
            mObj = obj;
        }

        /*x AbstractExternalized.2 */
        public Object read(ObjectInput in) throws IOException {
            String s = in.readUTF();
            int count = in.readInt();
            return new AbstractExternalized(s,count);
        }
        /*x*/

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(mObj.mS);
            out.writeInt(mObj.mCount);
        }

        static final long serialVersionUID = -9070600003619854277L;
    }

    public static void main(String[] args)
        throws IOException, ClassNotFoundException {
        
        AbstractExternalized p = new AbstractExternalized("foo",7);
        @SuppressWarnings("unchecked")
        AbstractExternalized p2 = (AbstractExternalized)
            AbstractExternalizable.serializeDeserialize(p);
        System.out.println("p2=" + p2);
    }

}