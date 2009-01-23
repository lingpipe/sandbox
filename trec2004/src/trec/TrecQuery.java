package trec;

import com.aliasi.ne.Decoder;
import com.aliasi.ne.Tags;

import com.aliasi.tokenizer.EnglishStopListFilterTokenizer;
import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;
import com.aliasi.tokenizer.IndoEuropeanTokenizer;
import com.aliasi.tokenizer.LowerCaseFilterTokenizer;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.Strings;

import org.apache.lucene.index.Term;

import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanQuery;

import java.io.File;
import java.io.IOException;


public class TrecQuery {
    public final String mId;
    public final String mTitle;
    private final String mNeed;
    private final String mContext;
    private final Decoder mDecoder;
    private boolean mUseLingPipeTerms = false;
    private boolean mUseStopList = false;
    public TrecQuery(String id, String title, String need, String context)
        throws IOException {
        mId = id;
        mTitle = title;
        mNeed = need;
        mContext = context;
        mDecoder = new Decoder(new File("models/EN_GENOMICS.model"),
                               new IndoEuropeanTokenCategorizer(),
                               8.0);
    }
    public String toString() {
        return "ID=" + mId
            + "\n" + "TITLE=" + mTitle
            + "\n" + "NEED=" + mNeed
            + "\n" + "CONTEXT=" + mContext;
    }
    public Query toLuceneQuery() {
        Query titleQuery = constructQuery(mTitle);
        titleQuery.setBoost(4.0f);
        Query needQuery = constructQuery(mNeed);
        needQuery.setBoost(2.0f);
        Query contextQuery = constructQuery(mContext);

        BooleanQuery query = new BooleanQuery();
        query.add(titleQuery,false,false);
        query.add(needQuery,false,false);
        query.add(contextQuery,false,false);

        return query;
    }

    private Query constructQuery(String in) {
        BooleanQuery query = new BooleanQuery();
        query.setMaxClauseCount(1000000);
        String[] tokens = normalTokenize(in);
        for (int i = 0; i < tokens.length; ++i) {
            if (Strings.allPunctuation(tokens[i])) continue;
            Term term = new Term(MedlineDoc.WEIGHTED_TEXT_FIELD,tokens[i]);
            TermQuery termQuery = new TermQuery(term);
            query.add(termQuery,false,false);
        }
        if (mUseLingPipeTerms)
            addTerms(in,query);
        return query;
    }

    public String[] normalTokenize(String in) {
        Tokenizer tokenizer = new IndoEuropeanTokenizer(in);
        tokenizer = new LowerCaseFilterTokenizer(tokenizer);
        if (mUseStopList)
            tokenizer = new EnglishStopListFilterTokenizer(tokenizer);
        return tokenizer.tokenize();
    }

    public void addTerms(String text, BooleanQuery query) {
        StringBuffer termBuf = new StringBuffer();
        String[] tokens = IndoEuropeanTokenizer.tokenize(text);
        String[] tags = mDecoder.decodeTags(tokens);
        for (int i = 0; i < tags.length; ) {
            if (Tags.isStartTag(tags[i])) {
                PhraseQuery pQuery = new PhraseQuery();
                pQuery.add(new Term(MedlineDoc.WEIGHTED_TEXT_FIELD,tokens[i].toLowerCase()));
                while (++i < tags.length && Tags.isInnerTag(tags[i])) {
                    pQuery.add(new Term(MedlineDoc.WEIGHTED_TEXT_FIELD,tokens[i].toLowerCase()));
                }
                pQuery.setBoost(4.0f); // terms * 4.0
                query.add(pQuery,false,false);
            } else {
                ++i;
            }
        }
    }

    public void useLingPipeTerms(boolean useTerms) {
        mUseLingPipeTerms = useTerms;
    }
    public void useStopList(boolean useStopList) {
        mUseStopList = useStopList;
    }

}


