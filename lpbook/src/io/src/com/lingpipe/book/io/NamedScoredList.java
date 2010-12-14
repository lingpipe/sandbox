package com.lingpipe.book.io;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/*x NamedScoredList.1 */
public class NamedScoredList<E> implements Serializable {

    static final long serialVersionUID = -3215750891469163883L;

    private final List<E> mEs;
    private final String mName;
    private final double mScore;
    private final Object mNotSerializable = new Object();

    public NamedScoredList(List<E> es, String name, 
                           double score) {
        mEs = new ArrayList<E>(es);
        mName = name;
        mScore = score;
    }
/*x*/

/*x NamedScoredList.2 */
    Object writeReplace() {
        return new Externalizer<E>(this);
    }
/*x*/

    @Override
    public String toString() {
        return "NSL(" + mEs + "," + mName + "," + mScore + ")";
    }

    /*x NamedScoredList.3 */
    static class Externalizer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 1576954554681604249L;
        private final NamedScoredList<F> mAe;
        public Externalizer() { this(null); }
        public Externalizer(NamedScoredList<F> ae) {
            mAe = ae;
        }
    /*x*/

    /*x NamedScoredList.4 */
        @Override
        public void writeExternal(ObjectOutput out) 
            throws IOException {

            out.writeObject(mAe.mEs);
            out.writeUTF(mAe.mName);
            out.writeDouble(mAe.mScore);
        }
    /*x*/

    /*x NamedScoredList.5 */
        @Override
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {

            @SuppressWarnings("unchecked")
            List<F> es = (List<F>) in.readObject();
            String s = in.readUTF();
            double x = in.readDouble();
            return new NamedScoredList<F>(es,s,x);
        }
    }
    /*x*/

    public static void main(String[] args)
        throws IOException, ClassNotFoundException {

        /*x NamedScoredList.6 */
        List<String> xs = Arrays.asList("a","b","c");
        NamedScoredList<String> p1
            = new NamedScoredList<String>(xs,"foo",1.4);

        @SuppressWarnings("unchecked")
        NamedScoredList<String> p2 = (NamedScoredList<String>)
            AbstractExternalizable.serializeDeserialize(p1);
        /*x*/

        System.out.println("p1=" + p1);
        System.out.println("p2=" + p2);
    }

}