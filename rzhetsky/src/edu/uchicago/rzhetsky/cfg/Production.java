package edu.uchicago.rzhetsky.cfg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Production {

    public Production() {
    }

    public abstract String mother();
    
    public abstract List<String> daughters();

    public int numDaughters() {
        return daughters().size();
    }

    public String daughter(int n) {
        return daughters().get(n);
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Production))
            return false;
        @SuppressWarnings("unchecked")
        Production thatProduction = (Production) that;
        if (!mother().equals(thatProduction.mother()))
            return false;
        if (numDaughters() != thatProduction.numDaughters())
            return false;
        for (int n = 0; n < numDaughters(); ++n)
            if (!daughter(n).equals(thatProduction.daughter(n)))
                return false;
        return true;
    }

    @Override
    abstract public int hashCode();

    @Override
    public String toString() {
        return mother() + " -> " + daughters();
    }

    static int hashCode(String mother, List<String> daughters) {
        return mother.hashCode() 
            + 31 * daughters.hashCode();
        
    }

    public static Production nullary(String mother) {
        return new EmptyProduction(mother);
    }

    public static Production unary(String mother,
                                   String daughter) {
        return new UnaryProduction(mother,daughter);
    }

    public static Production binary(String mother,
                                    String leftDaughter,
                                    String rightDaughter) {
        return new BinaryProduction(mother,leftDaughter,rightDaughter);
    }

    public static Production general(String mother,
                                     String... daughters) {
        if (daughters.length == 0)
            return nullary(mother);
        if (daughters.length == 1)
            return unary(mother,daughters[0]);
        if (daughters.length == 2)
            return binary(mother,daughters[0],daughters[1]);
        return new GeneralProduction(mother,daughters);
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

    static class EmptyProduction extends BaseProduction {
        EmptyProduction(String mother) {
            super(mother,
                  hashCode(mother,
                           Collections.<String>emptyList()));
        }
        public List<String> daughters() {
            return Collections.emptyList();
        }
    }

    static class UnaryProduction extends BaseProduction {
        private final String mDaughter;
        UnaryProduction(String mother, String daughter) {
            super(mother,
                  hashCode(mother,
                           Collections.singletonList(daughter)));
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
                  hashCode(mother,
                           Arrays.asList(leftDaughter,rightDaughter)));
            mLeftDaughter = leftDaughter;
            mRightDaughter = rightDaughter;
        }
        @Override
        public List<String> daughters() {
            return Arrays.asList(mLeftDaughter,mRightDaughter);
        }
    }

    static class GeneralProduction extends BaseProduction {
        private final List<String> mDaughters;
        GeneralProduction(String mother, String... daughters) {
            super(mother,
                  hashCode(mother,Arrays.asList(daughters)));
            mDaughters = Arrays.asList(daughters);
        }
        @Override
        public List<String> daughters() {
            return Collections.unmodifiableList(mDaughters);
        }
    }

}