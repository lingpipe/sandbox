package com.lingpipe.book.io;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.CompiledSpellChecker;
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

public class CompilationDemo {

    public static void main(String[] args)
        throws IOException, ClassNotFoundException {

        NGramProcessLM lm = new NGramProcessLM(5);
        WeightedEditDistance dist
            = new FixedWeightEditDistance();

        /*x CompilationDemo.0 */
        TrainSpellChecker tsc
            = new TrainSpellChecker(lm,dist);
        tsc.handle("The quick brown fox jumped.");
        /*x*/


        /*x CompilationDemo.1 */
        File fc1 = new File("one.CompiledSpellChecker");
        OutputStream out = new FileOutputStream(fc1);
        ObjectOutput objOut = new ObjectOutputStream(out);
        tsc.compileTo(objOut);
        objOut.close();

        InputStream in = new FileInputStream(fc1);
        ObjectInput objIn = new ObjectInputStream(in);
        Object o1 = objIn.readObject();
        CompiledSpellChecker csc1 = (CompiledSpellChecker) o1;
        objIn.close();
        /*x*/
        

        /*x CompilationDemo.2 */
        File fc2 = new File("two.CompiledSpellChecker");
        AbstractExternalizable.compileTo(tsc,fc2);
        
        Object o2 = AbstractExternalizable.readObject(fc2);
        CompiledSpellChecker csc2 = (CompiledSpellChecker) o2;
        /*x*/


        /*x CompilationDemo.3 */
        Object o3 = AbstractExternalizable.compile(tsc);
        CompiledSpellChecker csc3 = (CompiledSpellChecker) o3;
        /*x*/

        System.out.println("tsc.getClass()=" + tsc.getClass());
        System.out.println("o1.getClass()=" + o1.getClass());
        System.out.println("o2.getClass()=" + o2.getClass());
        System.out.println("o3.getClass()=" + o3.getClass());
    }
}

