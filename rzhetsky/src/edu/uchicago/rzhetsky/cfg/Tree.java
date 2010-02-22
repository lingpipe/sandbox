package edu.uchicago.rzhetsky.cfg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Tree {

    private final String mMother;

    public Tree(String mother) {
        mMother = mother;
    }

    public String mother() {
        return mMother;
    }

    public static class Lexical  extends Tree {
        private final String mWord;
        public Lexical(String mother,
                       String word) {
            super(mother);
            mWord = word;
        }
        public String word() {
            return mWord;
        }
        @Override
        public String toString() {
            return mother() + "|" + word() + "|";
        }
    }

    public static class NonTerminal extends Tree {
        private final List<Tree> mDaughters;
        public NonTerminal(String mother,
                           Tree... daughters) {
            super(mother);
            mDaughters = Arrays.asList(daughters);
        }
        public List<Tree> daughters() {
            return Collections.unmodifiableList(mDaughters);
        }
        public String toString() {
            return mother() + "(" + mDaughters + ")";
        }
    }

}