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

/**
 * A {@code ShiftReduceParser} implements the parsing interface
 * using shift-reduce parsig over a specified context-free grammar.
 * 
 * @author Bob Carpenter
 * @version 1.0
 * @since 1.0
 */
public class ShiftReduceParser extends Parser {

    private final Map<String,String[]> mLexIndex;
    private final RuleIndexNode mRuleIndexRoot;

    /**
     * Construct a shift-reduce parser for the specified
     * context-free grammar.
     *
     * @param cfg Grammar for the parser.
     */
    public ShiftReduceParser(ContextFreeGrammar cfg) {
        super(cfg);
        mLexIndex = lexIndex(cfg);
        mRuleIndexRoot = ruleIndex(cfg);
    }

    /**
     * @inheritDoc
     */
    public Iterator<Tree> parse(String... words) {
        return new ShiftReduceIterator(words);
    }

    /**
     * Return a string-based representation of the
     * underlying data structures for this parser.
     *
     * @return String-based representation of this parser.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LEXICON REVERSE INDEX\n");
        for (Map.Entry<String,String[]> entry : mLexIndex.entrySet())
            sb.append("|" + entry.getKey() + "|=" + Arrays.asList(entry.getValue()) + "\n");
        sb.append('\n');
        sb.append("RULE INDEX\n");
        mRuleIndexRoot.toStringBuilder(0,sb);
        return sb.toString();
    }
    
    void applyLex(SearchState state,
                  LinkedList<SearchState> stack) {
        if (state.wordsFinished())
            return;
        String word = state.mWords[state.mPosition];
        String[] lexCats = mLexIndex.get(word);
        if (lexCats == null) 
            return; // no parses because no lex
        for (String cat : lexCats) {
            SearchState state2 
                = new SearchState(state.mWords,
                                  state.mPosition+1,
                                  new TreeListEntry(Tree.createLexical(cat,word),
                                                    state.mEntry));
            stack.addLast(state2);
        }
    }

    void applyRules(SearchState state,
                    LinkedList<SearchState> stack) {
        LinkedList<Tree> dtrs = new LinkedList<Tree>();
        TreeListEntry entry = state.mEntry;
        RuleIndexNode node = mRuleIndexRoot;
        while (node != null) {
            for (String mother : node.mMotherCats) {
                stack.add(new SearchState(state.mWords,
                                          state.mPosition,
                                          new TreeListEntry(Tree.createPhrasal(mother,dtrs),
                                                            entry)));
            }
            if (entry == null) return;
            String cat = entry.mTree.rootCategory();
            node = node.mExtensionMap.get(cat);
            dtrs.addFirst(entry.mTree);
            entry = entry.mNext;
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
                // uncomment to track search states
                // System.out.println("     state=" + state);
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

    static Map<String,String[]> lexIndex(ContextFreeGrammar cfg) {
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

    static RuleIndexNode ruleIndex(ContextFreeGrammar cfg) {
        RuleIndexNode root = new RuleIndexNode();
        for (Production production : cfg.productions())
            root.index(production);
        return root;
    }

    static void indent(int indentation, StringBuilder sb) {
        for (int i = 0; i < indentation; ++i)
            sb.append(' ');
    }

    static void nl(StringBuilder sb) {
        if (sb.length() == 0) 
            return;
        if (sb.charAt(sb.length() - 1) == '\n') 
            return;
        sb.append('\n');
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
                    node.mExtensionMap.put(cat,next);
                }
                node = next;
            }
            String mother = production.mother();
            node.mMotherCats.add(mother);
        }
        public void toStringBuilder(int indentation, StringBuilder sb) {
            if (mMotherCats.size() > 0)
                sb.append(" <- " + mMotherCats);
            for (Map.Entry<String,RuleIndexNode> entry : mExtensionMap.entrySet()) {
                nl(sb);
                indent(indentation,sb);
                String contCat = entry.getKey();
                RuleIndexNode contNode = entry.getValue();
                sb.append(contCat);
                contNode.toStringBuilder(indentation+4,sb);
            }
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
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Ws=");
            for (int i = mPosition; i < mWords.length; ++i)
                sb.append("|" + mWords[i]);
            if (mPosition < mWords.length)
                sb.append('|');
            sb.append(" Ts=");
            for (TreeListEntry entry = mEntry; entry != null; entry = entry.mNext) {
                if (entry != mEntry) sb.append(", ");
                sb.append(entry.mTree);
            }
            sb.append('\n');
            return sb.toString();
        }
    }

}
