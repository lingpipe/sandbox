package com.lingpipe.book.io;

import com.aliasi.util.AbstractExternalizable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/*x SerialProxied.1 */
public class SerialProxied implements Serializable {

    private final String mS;
    private final int mCount;
    private final Object mNotSerializable = new Object();

    public SerialProxied(String s, int count) {
        mS = s;  
        mCount = count;
    }
/*x*/

    public String toString() {
        return mS + ":" + mCount;
    }

    /*x SerialProxied.2 */
    private Object writeReplace() {
        return new Serializer(this);
    }
    /*x*/

    static final long serialVersionUID = -688378786294424932L;

    /*x SerialProxied.3 */
    private static class Serializer implements Externalizable {

        SerialProxied mObj;

        public Serializer() { }

        Serializer(SerialProxied obj) {
            mObj = obj;
        }
    /*x*/

        /*x SerialProxied.4 */
        public void writeExternal(ObjectOutput out) 
            throws IOException {

            out.writeUTF(mObj.mS);
            out.writeInt(mObj.mCount);
        }

        public void readExternal(ObjectInput in) throws IOException {
            String s = in.readUTF();
            int count = in.readInt();
            mObj = new SerialProxied(s,count);
        }

        Object readResolve() {
            return mObj;
        }
        /*x*/

        static final long serialVersionUID = -9070600003619854277L;
    }

    public static void main(String[] args)
        throws IOException, ClassNotFoundException {
        
        SerialProxied p = new SerialProxied("foo",7);
        @SuppressWarnings("unchecked")
        SerialProxied p2 = (SerialProxied)
            AbstractExternalizable.serializeDeserialize(p);
        System.out.println("p2=" + p2);
    }

}