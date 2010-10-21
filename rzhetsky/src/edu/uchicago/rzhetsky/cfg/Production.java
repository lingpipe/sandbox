package edu.uchicago.rzhetsky.cfg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A {@code Production} consists of a mother category
 * and sequence of daughter categories.
 *
 * <h3>Construction</h3>
 *
 * <p>Productions are constructed from a mother category and list of
 * daughter categories using the variale-length static factory method
 * {@link #create(String,String[])}.
 *
 * <h3>Equality, Comparability and Hash Codes</h3>
 *
 * <p>A production is equal to another production if they have the
 * same mother and daughter categories.  Comparison is lexicographic
 * starting with the mother category and working left to right through
 * the daughter categories.  Comparison is consistent with equality.
 * Hash coding is consistent with both equality and comparison.
 *
 * @author Bob Carpenter
 * @version 1.0
 * @since 1.0
 */
public abstract class Production implements Comparable<Production> {

    Production() {
    }

    /**
     * Return the mother category for this production.
     *
     * @return Mother category.
     */
    public abstract String mother();
    
    /**
     * Return an unmodifiable view of the daughter categories for this
     * production.
     * 
     * @return The daughter categories for this production.
     */
    public abstract List<String> daughters();

    /**
     * Returns the number of daughters.
     *
     * @return The number of daughters.
     */
    public int numDaughters() {
        return daughters().size();
    }

    /**
     * Returns the {@code n}-the daughter category, numbering from
     * zero (0).
     *
     * @param n Order of daughter category.
     * @return The {@code n}-th daughter.
     * @throws IndexOutOfBoundsException If the order is
     * less than 0 or greater than or equal to the number
     * of daughters.
     */
    public String daughter(int n) {
        return daughters().get(n);
    }

    /**
     * Return {@code true} if the specified object is a production
     * with the same mother and daughter categories as this one.
     *
     * @param that Object to compare to this production.
     * @return {@code true} if they are equal.
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Production))
            return false;
        @SuppressWarnings("unchecked")
        Production thatProduction = (Production) that;
        // relies on intern of daughters
        if (mother() != thatProduction.mother())
            return false;
        if (numDaughters() != thatProduction.numDaughters())
            return false;
        for (int n = 0; n < numDaughters(); ++n)
            if (daughter(n) != thatProduction.daughter(n))
                return false;
        return true;
    }

    /**
     * Return the hash code for this production. 
     *
     * @return The hash code for this production.
     */
    @Override
    abstract public int hashCode();

    /**
     * Return the result of comparing this production to the specified
     * production.
     *
     * <p>Productions are sorted in lexicographic order of their
     * categories, starting with the mother and working left to right
     * through the daughters.
     *
     * @param that The production to which this production is compared.
     * @return Less than zero if this production is smaller, greater
     * than zero if it is bigger and zero if they are the same.
     */
    public int compareTo(Production that) {
        int c = mother().compareTo(that.mother());
        if (c != 0) return c;
        int end = Math.min(numDaughters(),that.numDaughters());
        for (int i = 0; i < end; ++i) {
            c = daughter(i).compareTo(that.daughter(i));
            if (c != 0) return c;
        }
        return numDaughters() - that.numDaughters();
    }

    /**
     * Return a string-based representation of this production.
     *
     * @return String representation of this production.
     */
    @Override
    public String toString() {
        return mother() + " -> " + daughters();
    }

    /**
     * Return the production with the specified mother category and
     * sequence of daugther categories.
     *
     * <p>If the daughters are specified with an array rather than a
     * variable-length arguments call, the array will be copied so
     * that further modifications to the array do not affect the
     * production.
     *
     * <p>The input daughter array is modified by replacing member
     * strings with the result of calling {@link String#intern()} on
     * them (i.e. for all {@code i}, setting {@code daughters[i] =
     * daughters[i].intern()}).
     *
     * @param mother Mother categories.
     * @param daughters Sequence of daughter categories.
     */
    public static Production create(String mother,
                                    String... daughters) {
        mother = mother.intern();
        for (int i = 0; i < daughters.length; ++i)
            daughters[i] = daughters[i].intern();
        if (daughters.length == 0)
            return new NullaryProduction(mother);
        if (daughters.length == 1)
            return new UnaryProduction(mother,
                                       daughters[0]);
        if (daughters.length == 2)
            return new BinaryProduction(mother,
                                        daughters[0],
                                        daughters[1]);
        return new GeneralProduction(mother,daughters);
    }

    static int hashCode(String mother, String... daughters) {
        return mother.hashCode() 
            + 31 * Arrays.asList(daughters).hashCode();
    }

    static abstract class BaseProduction extends Production {
        private final String mMother;
        private final int mHashCode;
        BaseProduction(String mother, int hashCode) {
            mMother = mother;
            mHashCode = hashCode;
        }
        @Override
        public String mother() { 
            return mMother;
        }
        @Override
        public int hashCode() {
            return mHashCode;
        }
    }

    static class NullaryProduction extends BaseProduction {
        NullaryProduction(String mother) {
            super(mother,
                  hashCode(mother));
        }
        public List<String> daughters() {
            return Collections.emptyList();
        }
    }

    static class UnaryProduction extends BaseProduction {
        private final String mDaughter;
        UnaryProduction(String mother, String daughter) {
            super(mother,
                  hashCode(mother,daughter));
            mDaughter = daughter;
        }
        @Override
        public List<String> daughters() {
            return Collections.singletonList(mDaughter);
        }
    }

    static class BinaryProduction extends BaseProduction {
        private final String mLeftDaughter;
        private final String mRightDaughter;
        BinaryProduction(String mother, String leftDaughter, String rightDaughter) {
            super(mother,
                  hashCode(mother,leftDaughter,rightDaughter));
            mLeftDaughter = leftDaughter;
            mRightDaughter = rightDaughter;
        }
        @Override
        public List<String> daughters() {
            return Arrays.asList(mLeftDaughter,mRightDaughter);
        }
    }

    static class GeneralProduction extends BaseProduction {
        private final String[] mDaughters;
        GeneralProduction(String mother, String... daughters) {
            super(mother,
                  hashCode(mother,daughters));
            mDaughters = daughters;
        }
        @Override
        public List<String> daughters() {
            return Collections.unmodifiableList(Arrays.asList(mDaughters));
        }
    }



}