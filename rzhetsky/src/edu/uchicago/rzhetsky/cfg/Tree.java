package edu.uchicago.rzhetsky.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * A {@code Tree} represents a complete derivation of a root category
 * from a sequence of words.  
 *
 * <p>All trees have a root category, as returned by {@link
 * #rootCategory()}.
 * 
 * <p>Trees are either lexical or phrasal.  Their type may be
 * determined with the method {@link #isLexical()}.
 *
 * <h3>Lexical Trees</h3>
 * 
 * <p>Lexical trees have a word in addition to a root category.
 * Lexical trees are created with the static factory method 
 * {@link #createLexical(String,String)}.  For lexical trees,
 * the method {@link #word()} returns the word.
 *
 * <h3>Phrasal Trees</h3>
 *
 * <p>Phrasal trees have a sequence of subtrees.  Phrasal trees are
 * created with the static factory method {@link
 * #createPhrasal(String,List)}.  For phrasal trees, the method
 * {@link #subtrees()} returns a view of the list of subtrees.
 *
 * @author Bob Carpenter
 * @version 1.0
 * @since 1.0
 */
public abstract class Tree {

    private final String mRootCategory;

    Tree(String rootCategory) {
        mRootCategory = rootCategory;
    }

    /**
     * Return the root category for this tree.
     *
     * @return the root category.
     */
    public String rootCategory() {
        return mRootCategory;
    }

    /**
     * Return {@code true} if this is a lexical tree.
     *
     * @return {@code true} if this is a lexical tree.
     */
    public boolean isLexical() {
        return false;
    }

    /**
     * Return the word if this is a lexical tree.
     *
     * @return The word for this tree.
     * @throws UnsupportedOperationException If this is
     * not a lexical tree.
     */
    public String word() {
        String msg = "Only supported for lexical trees";
        throw new UnsupportedOperationException(msg);
    }

    /**
     * Return the list of subtrees if this is a phrasal tree.
     *
     * @return The list of subtrees for this tree.
     * @throws UnsupportedOperationException If this is not a phrasal
     * tree.
     */
    public List<Tree> subtrees() {
        String msg = "Only supported for phrasal trees";
        throw new UnsupportedOperationException(msg);
    }

    /**
     * Return a lexical tree with the specified root category
     * and word.
     *
     * @param rootCategory Root for the tree.
     * @param word Word for the tree.
     * @return The lexical tree with the specified root and word.
     */
    public static Tree createLexical(String rootCategory,
                                     String word) {
        return new Lexical(rootCategory,word);
    }

    /**
     * Return a phrasal tree with the specified root category
     * and subtrees.
     *
     * @param rootCategory Root category for the tree.
     * @param subtrees Subtrees for the tree.
     * @return The phrasal tree with the specified root and subtrees.
     */
    public static Tree createPhrasal(String rootCategory,
                                     List<Tree> subtrees) {
        return new Phrasal(rootCategory,subtrees);
    }

    static class Lexical  extends Tree {
        private final String mWord;
        public Lexical(String rootCategory,
                       String word) {
            super(rootCategory);
            mWord = word;
        }
        public String word() {
            return mWord;
        }
        @Override
        public String toString() {
            return rootCategory() + "[" + word() + "]";
        }
    }

    static class Phrasal extends Tree {
        private final List<Tree> mSubtrees;
        public Phrasal(String rootCategory,
                       List<Tree> subtrees) {
            super(rootCategory);
            mSubtrees = new ArrayList<Tree>(subtrees);
        }
        public List<Tree> subtrees() {
            return Collections.unmodifiableList(mSubtrees);
        }
        public String toString() {
            return rootCategory() + "(" + mSubtrees + ")";
        }
    }

}