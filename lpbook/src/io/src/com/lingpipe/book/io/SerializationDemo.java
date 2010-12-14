package com.lingpipe.book.io;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class SerializationDemo {

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
        throws IOException, ClassNotFoundException {

        NGramProcessLM lm = new NGramProcessLM(5);
        WeightedEditDistance dist
            = new FixedWeightEditDistance();

        /*x SerializationDemo.0 */
        TrainSpellChecker tsc
            = new TrainSpellChecker(lm,dist);
        tsc.handle("The quick brown fox jumped.");
        /*x*/


        /*x SerializationDemo.1 */
        File fc1 = new File("one.CompiledSpellChecker");
        OutputStream out = new FileOutputStream(fc1);
        ObjectOutput objOut = new ObjectOutputStream(out);
        objOut.writeObject(tsc);
        objOut.close();

        InputStream in = new FileInputStream(fc1);
        ObjectInput objIn = new ObjectInputStream(in);
        Object o1 = objIn.readObject();
        TrainSpellChecker tsc1 = (TrainSpellChecker) o1;
        objIn.close();
        /*x*/
        

        /*x SerializationDemo.2 */
        File fc2 = new File("two.CompiledSpellChecker");
        AbstractExternalizable.serializeTo(tsc,fc2);
        
        Object o2 = AbstractExternalizable.readObject(fc2);
        TrainSpellChecker tsc2 = (TrainSpellChecker) o2;
        /*x*/

        /*x SerializationDemo.3 */
        Object o3 = AbstractExternalizable.serializeDeserialize(tsc);
        TrainSpellChecker tsc3 = (TrainSpellChecker) o3;
        /*x*/

        System.out.println("tsc.getClass()=" + tsc.getClass());
        System.out.println("o1.getClass()=" + o1.getClass());
        System.out.println("o2.getClass()=" + o2.getClass());
        System.out.println("o3.getClass()=" + o3.getClass());

    }
}

