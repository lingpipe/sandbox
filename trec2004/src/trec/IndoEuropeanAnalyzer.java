package trec;

import com.aliasi.tokenizer.*;

import com.aliasi.util.Streams;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;

public class IndoEuropeanAnalyzer extends Analyzer {

    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new IndoEuropeanTokenStream(reader);
    }

    private static class IndoEuropeanTokenStream extends TokenStream {
        private final Tokenizer mTokenizer;
        private int mPosition = 0;
        private IndoEuropeanTokenStream(Reader reader) {
            char[] cs = null;
            try {
                cs = Streams.toCharArray(reader);
            } catch (IOException e) {
                mTokenizer = null;
                return;
            }
            Tokenizer ieTokenizer = new IndoEuropeanTokenizer(cs,0,cs.length);
            mTokenizer = new LowerCaseFilterTokenizer(ieTokenizer);
        }
        public Token next() {
            // need a while loop to remove punctuation to match queries
            if (mTokenizer == null) return null;
            String token = mTokenizer.nextToken();
            if (token == null) return null;
            Token result = new Token(token,mPosition,mPosition+1);
            ++mPosition;
            return result;


        }
    }


}
