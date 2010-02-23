package edu.uchicago.rzhetsky.cfg;

/**
 * A {@code LexEntry} consists of a word and the category
 * to which it is assigned.
 *
 * <h3>Construction</h3>
 *
 * <p>Lexical entries are constructed from a word and a category using
 * the static factory method {@link #create(String,String)}
 * 
 * <h3>Equality, Comparability, and Hash Codes</h3>
 *
 * Two lexical entries are equal if they have the same word
 * and same category.  They are comparable, with the sort
 * being by word, and if they have the same word, by category.
 * The comparison is consistent with equals, as is the
 * hash coding.  
 * 
 * @author Bob Carpenter
 * @version 1.0
 * @since 1.0
 */
public class LexEntry implements Comparable<LexEntry> {

    private final String mMother;
    private final String mWord;

    LexEntry(String mother, 
             String word) {
        mMother = mother;
        mWord = word;
    }

    /**
     * Return the mother category for this lexical
     * entry.
     *
     * @return The mother category.
     */
    public String mother() {
        return mMother;
    }

    /**
     * Return the word for this lexical entry.
     */
    public String word() {
        return mWord;
    }

    /**
     * Returns {@code true} if the specified object is
     * a lexical entry with the same word and mother category
     * as this one.
     *
     * @param that Object to compare to this lexical entry.
     * @return {@code true} if it is equal to this lexical entry.
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof LexEntry))
            return false;
        @SuppressWarnings("unchecked")
        LexEntry thatEntry
            = (LexEntry) that;
        return mother().equals(thatEntry.mother())
            && word().equals(thatEntry.word());
    }
    
    /**
     * Returns the hash code for this lexical entry.
     *
     * The hash code is computed using integer arithmetic
     * as
     *
     * <blockquote><pre>
     * hashCode() = 31 * word().hashCode() + mother().hashCode()</pre></blockquote>
     */
    @Override
    public int hashCode() {
        return 31 * word().hashCode() + mother().hashCode();
    }

    /**
     * Returns a string-based representation of this
     * lexical entry.
     *
     * @return String-based representation of this entry.
     */
    @Override
    public String toString() {
        return mother() + " -> |" + word() + "|";
    }

    /**
     * Returns the result of comparing this lexical entry
     * to the specified lexical entry lexicographically.
     * First, the words are compared, and if they are not
     * equal, the entry sort order is determined by he
     * word sort order.  If the words are the same, the
     * result is the mothr category compared to the specified
     * lexical entry's mother category.
     *
     * <p>The comparison is compatible with {@link #equals(Object)}.
     *
     * @param that Lexical entry to which this entry is compared.
     * @return Less than 0 if this lexical entry is smaller, greater
     * than 0 if this lexical entry is larger, and 0 if they are
     * equal.
     */
    public int compareTo(LexEntry that) {
        int c = word().compareTo(that.word());
        return c != 0 ? c : mother().compareTo(that.mother());
    }

    /**
     * Create a lexcial entry with the specified mother category and
     * word.
     *
     * @param mother Mother category.
     * @param word Word.
     * @return Lexical entry with the specified mother and
     * word.
     */
    public static LexEntry create(String mother,
                                  String word) {
        return new LexEntry(mother.intern(),
                            word);
    }



}