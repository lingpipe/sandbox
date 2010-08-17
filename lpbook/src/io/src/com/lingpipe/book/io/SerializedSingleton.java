package com.lingpipe.book.io;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/*x SerializedSingleton.1 */
public class SerializedSingleton implements Serializable {

    private final String mKey;
    private final int mValue;

    private SerializedSingleton(String key, int value) {
        mKey = key;
        mValue = value;
    }

    public static final SerializedSingleton INSTANCE
        = new SerializedSingleton("foo",42);
/*x*/

/*x SerializedSingleton.2 */
    Object writeReplace() {
        return new Serializer();
    }
    
    static class Serializer extends AbstractExternalizable {

        public Serializer() { }

        @Override
        public Object read(ObjectInput in) {
            return INSTANCE;
        }

        @Override
        public void writeExternal(ObjectOutput out) { }

        static final long serialVersionUID 
            = 8538000269877150506L;
    }
/*x*/

    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {

        /*x SerializedSingleton.3 */
        SerializedSingleton s = SerializedSingleton.INSTANCE;
        Object deser = AbstractExternalizable.serializeDeserialize(s);
        boolean same = (s == deser);
        /*x*/
        System.out.println("same=" + same);
    }
    
    static final long serialVersionUID = 8908457830707847716L;
}