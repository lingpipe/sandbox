package edu.uchicago.rzhetsky.cfg;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShiftReduce {

    private final Cfg mCfg;
    private final Map<String,String[]> mLexIndex;

    public ShiftReduce(Cfg cfg) {
        mCfg = cfg;
        mLexIndex = lexIndex(cfg);
    }

    public Cfg cfg() {
        return mCfg;
    }

    public Iterator<Tree> shiftReduce(String[] words) {
        return new ShiftReduceIterator(words);
    }

    void applyLex(SearchState state,
                  LinkedList<SearchState> stack) {
        if (state.wordsFinished())
            return;
    }

    void applyRules(SearchState state,
                    LinkedList<SearchState> stack) {
        
    }

    class ShiftReduceIterator implements Iterator<Tree> {
        private final LinkedList<SearchState> mStack;
        private Tree mNext;
        ShiftReduceIterator(String[] words) {
            mStack = new LinkedList<SearchState>();
            mStack.addLast(new SearchState(words));
        }

        public boolean hasNext() {
            if (mNext != null)
                return true;
            while (!mStack.isEmpty()) {
                SearchState state = mStack.removeLast();
                applyLex(state,mStack);
                applyRules(state,mStack);
                if (state.isComplete()) {
                    mNext = state.getTree();
                    return true;
                }
            }
            return false;
        }
        public Tree next() {
            if (!hasNext())
                throw new NoSuchElementException();
            Tree result = mNext;
            mNext = null;
            return result;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static Map<String,String[]> lexIndex(Cfg cfg) {
        Map<String,Set<String>> lexMap
            = new HashMap<String,Set<String>>();
        for (LexEntry entry : cfg.lexEntries()) {
            String cat = entry.mother();
            String word = entry.word();
            Set<String> cats = lexMap.get(word);
            if (cats == null) {
                cats = new HashSet<String>(2);
                lexMap.put(word,cats);
            }
            cats.add(word);
        }
        Map<String,String[]> lexIndex
            = new HashMap<String,String[]>(lexMap.size()*2);
        for (Map.Entry<String,Set<String>> entry : lexMap.entrySet()) {
            String word = entry.getKey();
            Set<String> catSet = entry.getValue();
            String[] cats = catSet.toArray(new String[catSet.size()]);
            lexIndex.put(word,cats);
        }
        return lexIndex;
    }

    static class TreeListEntry {
        private TreeListEntry mNext;
        private final Tree mTree;
        public TreeListEntry(Tree tree) {
            this(tree,null);
        }
        public TreeListEntry(Tree tree,
                             TreeListEntry next) {
            mTree = tree;
            mNext = next;
        }
    }

    
    static class SearchState {
        final int mPosition;
        final String[] mWords;
        final TreeListEntry mEntry;
        public SearchState(String[] words) {
            this(words,0,null);
        }
        public SearchState(String[] words,
                           int position,
                           TreeListEntry entry) {
            mWords = words;
            mPosition = position;
            mEntry = entry;
        }
        public boolean wordsFinished() {
            return mPosition == mWords.length;
        }
        public boolean isComplete() {
            return wordsFinished()
                && mEntry != null
                && mEntry.mNext == null;
        }
        public Tree getTree() {
            // requires isComplete();
            return mEntry.mTree;
        }
    }

}
