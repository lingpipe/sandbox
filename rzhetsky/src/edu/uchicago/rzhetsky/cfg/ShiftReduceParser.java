package edu.uchicago.rzhetsky.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShiftReduceParser {

    private final Cfg mCfg;
    private final Map<String,String[]> mLexIndex;
    private final RuleIndexNode mRuleIndexRoot;

    public ShiftReduceParser(Cfg cfg) {
        mCfg = cfg;
        mLexIndex = lexIndex(cfg);
        mRuleIndexRoot = ruleIndex(cfg);
    }

    public Cfg cfg() {
        return mCfg;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LEXICON REVERSE INDEX\n");
        for (Map.Entry<String,String[]> entry : mLexIndex.entrySet())
            sb.append("|" + entry.getKey() + "|=" + Arrays.asList(entry.getValue()) + "\n");
        return sb.toString();
    }

    public Iterator<Tree> shiftReduce(String[] words) {
        return new ShiftReduceIterator(words);
    }

    void applyLex(SearchState state,
                  LinkedList<SearchState> stack) {
        if (state.wordsFinished())
            return;
        String word = state.mWords[state.mPosition];
        String[] lexCats = mLexIndex.get(word);
        if (lexCats == null) 
            return; // no parses because no lex
        for (String cat : lexCats)
            stack.addLast(new SearchState(state.mWords,
                                          state.mPosition+1,
                                          new TreeListEntry(new Tree.Lexical(cat,word),
                                                            state.mEntry)));
    }

    void applyRules(SearchState state,
                    LinkedList<SearchState> stack) {
        RuleIndexNode node = mRuleIndexRoot;
        List<Tree> dtrs = new ArrayList<Tree>();
        for (TreeListEntry entry = state.mEntry;
             entry != null && node != null && node.mExtensionMap != null;
             entry = entry.mNext) {

            for (String mother : node.mMotherCats) {
                stack.add(new SearchState(state.mWords,
                                          state.mPosition,
                                          new TreeListEntry(new Tree.NonTerminal(mother,dtrs),
                                                            entry)));
            }
            String cat = entry.mTree.mother();
            node = node.mExtensionMap.get(cat);
            if (node == null) return;
            dtrs.add(entry.mTree);
        }
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
            cats.add(cat);
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

    static RuleIndexNode ruleIndex(Cfg cfg) {
        RuleIndexNode root = new RuleIndexNode();
        for (Production production : cfg.productions())
            root.index(production);
        return root;
    }

    static class RuleIndexNode {
        private final List<String> mMotherCats
            = new ArrayList<String>(1);
        private final Map<String,RuleIndexNode> mExtensionMap
            = new HashMap<String,RuleIndexNode>(1);
        public void index(Production production) {
            List<String> dtrs = production.daughters();
            RuleIndexNode node = this;
            for (int i = dtrs.size(); --i >= 0; ) {
                String cat = dtrs.get(i);
                RuleIndexNode next = node.mExtensionMap.get(cat);
                if (next == null) {
                    next = new RuleIndexNode();
                    mExtensionMap.put(cat,next);
                }
                node = next;
            }
            String mother = production.mother();
            node.mMotherCats.add(mother);
        }
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
