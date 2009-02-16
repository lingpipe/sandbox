package search;

import com.aliasi.util.Scored;

import java.util.Iterator;

public interface SearchState extends Scored {

    public Iterator next();
    
    public boolean isFinal();
    
    public Object result();

    public Object toEquivalenceRepresentation();

    public Comparable toOrderRepresentation();

    public double minCompletionCost();


}

