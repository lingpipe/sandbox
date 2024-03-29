package edu.uchicago.rzhetsky.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@code ShiftReduceParser} implements the parsing interface
 * using shift-reduce parsing over a specified context-free grammar.
 * It returns all valid parses for a given input with respect to
 * a grammar.
 *
 * <h3>Grammar Restrictions</h3>
 *
 * <p>Nullary productions (i.e. productions with a zero-length
 * sequence of daughters) are not allowed.
 * 
 * <p>There should not be a sequence of unary productions producing
 * a cycle.  For instance, the three productions A&rarr;B, B&rarr;C, and
 * C&rarr;A produce a cycle A&rarr;A.  
 * 
 * <h3>Shift-Reduce Parsing</h3>
 * 
 * Shift-reduce parsing is a search procedure with a search state
 * characterized by a list of remaining words to process and
 * a stack of completed derivations represented as trees.  It
 * then allows shift and reduce operations to apply to transform
 * the state.
 *
 * <h4>Example</h4>
 *
 * Here's an example for a simple parse:
 *
 * <blockquote><table border="1" cellpadding="3">
 * <tr><th>Words</th><th>Subderivations</th><th>Operation</th></tr>
 * <tr><td>the, kid, ran</td><td></td><td>(n/a)</td></tr>
 * <tr><td>kid, ran</td><td><b>DET</b>[the]</td><td>shift DET&rarr;the</td></tr>
 * <tr><td>ran</td><td><b>DET</b>[the], <b>N</b>[kid]</td><td>shift N&rarr;kid</td></tr>
 * <tr><td>ran</td><td><b>NP</b>[DET[the],N[kid]]</td><td>reduce NP&rarr;DET,N</td></tr>
 * <tr><td></td><td><b>NP</b>[DET[the],N[kid]], <b>IV</b>[ran]</td><td>shift IV&rarr;ran</td></tr>
 * <tr><td></td><td><b>NP</b>[DET[the],N[kid]], <b>VP</b>[IV[ran]]</td><td>reduce VP&rarr;IV</td></tr>
 * <tr><td></td><td><b>S</b>[NP[DET[the],N[kid]],VP[IV[ran]]]</td><td>reduce S&rarr;NP,VP</td></tr>
 * </table></blockquote>
 *
 * <h4>Initialization</h4>
 *
 * <p>Shift-reduce parsing starts with the words set to the
 * sequence of input words and the sequence of subderivations
 * empty.  For each line other than the first, we provide
 * an operation, which is either a shift operation involving
 * a lexical entry or a reduce operation involving a production
 * rule.
 *
 * <h4>Shift Operation</h4>
 * 
 * <p>Shift operations remove the first word from the list
 * of words and add a subderivation consisting of a lexical
 * tree for that word with a category determined from the
 * lexcion supplied in the grammar backing the parser.  
 *
 * <p>Continuing the above example, the first operation after
 * initialization shifts the word "the" from the list of words to the
 * subderivations stack using the lexical entry NP&rarr;the.  The
 * entry on the list of subderivations is <b>NP</b>[the].  We use
 * boldface on the roots of the trees in the list of subderivation
 * trees so that they are easy to distinguish from their derivations.
 *
 * <h4>Reduce Operations</h4>
 * 
 * <p>Reduce operations use a production rule to remove one
 * or more categories from the top of the subderivation stack
 * that match the right-hand side of a production rule and
 * replace them with a new tree rooted at the mother category
 * of the production rule and the matching subderivations as
 * subtrees.  
 *
 * <p>Continuing the example, the third operation after shifting
 * the words "the" and "kid" as DET and N categories, is to
 * use the production rule NP&rarr;DET,N to reduce the two 
 * categories to a single noun phrase category.  Here, the
 * subderivation trees <b>DET</b>[the] and <b>N</b>[kid] are
 * reduced to a single tree <b>NP</b>[DET[the],N[kid]]
 * rooted at NP and having the subderivation trees as subtrees.
 * 
 * <h4>Completion</h4>
 *
 * The parser terminates with a complete parse when there are
 * no input words in the word list and a single tree on the
 * derivation stack. 
 *
 * <p>Continuing the example, after further shift and reduce
 * operations, the last line completes the parse of "the,kid,ran" as
 * the single tree on the subderivation stack.
 * 
 * <h4>Non-Deterministic Search</h4>
 *
 * For some search states, more than one shift and/or reduce
 * operation may be applicable.  
 *
 * <p>In the above example, in addition to the first reduction
 * operation reducing the DET and N to an NP, it is also legal to
 * shift the word ran to an IV.  This IV could then be reduced,
 * but we would then be left with a stack consisting of DET,N,VP,
 * which cannot be reduced.  Only the top of the stack (here
 * written on the right) may be reduced.
 *
 * <p>This implementation pursues a reduce-first, longest grammar rule
 * first strategy.  That is, when there is a conflict between shift
 * and reduce, reduce is tried first.  If there is a conflict between
 * two rules, the one reducing more categories is used first.  If
 * there is a conflict between two production rules with the same
 * categories or two lexical rules with the same word, they are
 * pursued in alphabetical order of root category name.
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
     * @throws IllegalArgumentException If the grammar contains
     * a nullary production.
     */
    public ShiftReduceParser(ContextFreeGrammar cfg) {
        super(cfg);
        verifyNoEmptyProductions(cfg);
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
                  LinkedList<SearchState> stack,
                  String[] words) {
        if (state.wordsFinished(words))
            return;
        String word = words[state.mPosition];
        String[] lexCats = mLexIndex.get(word);
        if (lexCats == null) 
            return; // no parses because no lex
        for (String cat : lexCats) {
            SearchState state2 
                = new SearchState(state.mPosition+1,
                                  -1,
                                  new CatList(cat,state.mCatList),
                                  state);
            stack.addLast(state2);
        }
    }

    void applyRules(SearchState state,
                    LinkedList<SearchState> stack) {
        CatList catList = state.mCatList;
        RuleIndexNode node = mRuleIndexRoot;
        for (int numDtrs = 0; node != null; ++numDtrs) {
            for (String mother : node.mMotherCats) {
                // convert the stack to three parallel arrays
                stack.add(new SearchState(state.mPosition,
                                          numDtrs,
                                          new CatList(mother,catList),
                                          state));
            }
            if (catList == null) return;
            String cat = catList.mCat;
            node = node.mExtensionMap.get(cat);
            catList = catList.mTail;
        }
    }

    class ShiftReduceIterator implements Iterator<Tree> {
        private final String[] mWords;
        private final LinkedList<SearchState> mStack;
        private Tree mNext;
        ShiftReduceIterator(String[] words) {
            mWords = words;
            mStack = new LinkedList<SearchState>();
            mStack.addLast(new SearchState());
        }
        public boolean hasNext() {
            if (mNext != null)
                return true;
            while (!mStack.isEmpty()) {
                SearchState state = mStack.removeLast();
                // uncomment to track search states
                // System.out.println("     state=" + state);
                applyLex(state,mStack,mWords);
                applyRules(state,mStack);
                if (state.isComplete(mWords)) {
                    mNext = state.getTree(mWords);
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

    static void verifyNoEmptyProductions(ContextFreeGrammar cfg) {
        for (Production production : cfg.productions()) {
            if (production.numDaughters() == 0) {
                String msg = "Shift reduce does not allow empty productions."
                    + " Found production=" + production;
                throw new IllegalArgumentException(msg);
            }
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

    // node in trie representing RHS of productions; mother cats are output
    static class RuleIndexNode {
        private final List<String> mMotherCats
            = new ArrayList<String>(1);
        private final Map<String,RuleIndexNode> mExtensionMap
            = new IdentityHashMap<String,RuleIndexNode>(1);
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

    static class CatList {
        private final String mCat;
        private CatList mTail;
        public CatList(String cat) {
            this(cat,null);
        }
        public CatList(String cat,
                       CatList tail) {
            mCat = cat;
            mTail = tail;
        }
        @Override
        public String toString() {
            return mTail == null
                ? mCat
                : (mCat + "+" + mTail);
        }
    }

    
    static class SearchState {
        final int mPosition;
        final int mNumDtrs;
        final CatList mCatList;
        final SearchState mPrevious;
        public SearchState() {
            this(0,-2,null,null); // -2 for initial state not needed
        }
        public SearchState(int position,
                           int numDtrs,
                           CatList catList,
                           SearchState previous) {
            mPosition = position;
            mNumDtrs = numDtrs;
            mCatList = catList;
            mPrevious = previous;
        }
        public boolean wordsFinished(String[] words) {
            return mPosition == words.length;
        }
        public boolean isComplete(String[] words) {
            return wordsFinished(words)
                && mCatList.mTail == null;
        }
        public Tree getTree(String[] words) {
            // precondition: isComplete() == true
            if (mNumDtrs == -1)
                return Tree.createLexical(mCatList.mCat,
                                          words[mPosition-1]);
            Tree[] dtrTrees = new Tree[mNumDtrs];
            getTree(mPrevious,dtrTrees,mNumDtrs,words);
            return Tree.createPhrasal(mCatList.mCat,
                                      dtrTrees);
        }
        public static SearchState getTree(SearchState state, 
                                          Tree[] trees, 
                                          int position,
                                          String[] words) {
            while (--position >= 0) {
                if (state.mNumDtrs == -1) {
                    trees[position] 
                        = Tree.createLexical(state.mCatList.mCat,
                                             words[state.mPosition-1]);
                    state = state.mPrevious;
                } else {
                    Tree[] dtrTrees = new Tree[state.mNumDtrs];
                    String cat = state.mCatList.mCat;
                    state = getTree(state.mPrevious,
                                    dtrTrees,
                                    state.mNumDtrs,
                                    words);
                    trees[position] 
                        = Tree.createPhrasal(cat,dtrTrees);
                }
            }
            return state;
        }
        @Override
        public String toString() {
            return 
                "state(" + mPosition + "," + mCatList + "," +mNumDtrs + ")"
                + ( mPrevious == null ? "" : "; " + mPrevious);
        }
    }

}
