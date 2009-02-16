package stem;

import com.aliasi.cluster.ClusterScore;

import com.aliasi.util.ObjectToSet;
import com.aliasi.util.Streams;
import com.aliasi.util.Tuple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


public class Scorer {

    public static void main(String[] args) throws IOException {
        File refFile = new File(args[0]);
        String refCharset = args[1];
        File respFile = new File(args[2]);
        String respCharset = args[3];

        System.out.println("CLUSTER SCORING");
        System.out.println("Reference: " + refFile.getCanonicalPath());
        System.out.println("           charset=" + refCharset);
        System.out.println(" Response: " + respFile.getCanonicalPath());
        System.out.println("           charset=" + respCharset);
        System.out.println();

        score(refFile,refCharset,respFile,respCharset);
    }

    public static void score(File refFile, String refCharset, 
                             File respFile, String respCharset)
        throws IOException {
        
        Set[] refEquivalences = toEquivalences(refFile,refCharset);
        Set[] respEquivalences = toEquivalences(respFile,respCharset);
        ClusterScore score 
            = new ClusterScore(refEquivalences,respEquivalences);
        System.out.println(score.equivalenceEvaluation());
        System.out.println("\nFalse Positives");
        printUpperDiagonal(score.falsePositives());
        System.out.println("\nFalse Negatives");
        printUpperDiagonal(score.falseNegatives());
    }

    static final Comparator STRING_TUPLE_COMPARATOR = new Comparator() {
            public int compare(Object obj1, Object obj2) {
                Tuple t1 = (Tuple) obj1;
                Tuple t2 = (Tuple) obj2;
                String x1 = t1.get(0).toString();
                String x2 = t2.get(0).toString();
                int cX = x1.compareTo(x2);
                if (cX != 0) return cX;
                String y1 = t1.get(1).toString();
                String y2 = t2.get(1).toString();
                return y1.compareTo(y2);
            }
        };

    static void printUpperDiagonal(Set tuples) {
        Set orderedTuples = new TreeSet(STRING_TUPLE_COMPARATOR);
        orderedTuples.addAll(tuples);
        Iterator it = orderedTuples.iterator();
        while (it.hasNext()) {
            Tuple tuple = (Tuple) it.next();
            String x = tuple.get(0).toString();
            String y = tuple.get(1).toString();
            if (x.compareTo(y) < 0) continue;
            System.out.println(x + " = " + y);
        }
    }

    public static Set[] toEquivalences(File file, String charset) 
        throws IOException {

        ObjectToSet stemToWordSet = new ObjectToSet();
        FileInputStream fileIn = null;
        InputStreamReader isReader = null;
        BufferedReader bufReader = null;
        try {
            fileIn = new FileInputStream(file);
            isReader = new InputStreamReader(fileIn,charset);
            bufReader = new BufferedReader(isReader);
            String line;
            while ((line = bufReader.readLine()) != null)
                parseLine(line,stemToWordSet);
        } finally {
            Streams.closeReader(bufReader);
            Streams.closeReader(isReader);
            Streams.closeInputStream(fileIn);
        }
        Collection eqClasses = stemToWordSet.values();
        return (Set[]) eqClasses.toArray(new Set[eqClasses.size()]);
    }

    private static void parseLine(String line, ObjectToSet stemToWordSet) {
        if (line.length() == 0 || line.charAt(0) == '#') return;
        int dotIndex = line.indexOf('.');
        if (dotIndex < 0) return;
        String stem = line.substring(0,dotIndex);
        String suffix 
            = (dotIndex == line.length() - 1)
            ? "" 
            : line.substring(dotIndex+1);
        stemToWordSet.addMember(stem,stem+suffix);
    }
    

}
