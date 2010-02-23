package edu.uchicago.rzhetsky.cfg;

import java.util.Iterator;
import java.util.List;

/**
 * The {@code Parser} abstract base class specifies an interface for
 * parsing sequences of words into iterators over parse trees.
 * 
 * @author Bob Carpenter
 * @version 1.0
 * @since 1.0
 */
public abstract class Parser {

    /**
     * Returns an iterator over parse trees for the
     * specified sequence of words.
     *
     * @param words Sequence of words to parse.
     * @return Iterator over parse trees for the specified words.
     */
    public abstract Iterator<Tree> parse(String... words);

    /**
     * Parse the specified list of words.  This is a convenience
     * method delegating to {@link #parse(String[])}.
     *
     * @param words List of words to parse.
     * @return Iterator over parse trees for the specified words.
     */
    public Iterator<Tree> parse(List<String> words) {
        return parse(words.toArray(new String[words.size()]));
    }




}