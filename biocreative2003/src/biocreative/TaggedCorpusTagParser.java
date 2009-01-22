package biocreative;

import com.aliasi.ne.TrainableEstimator;

import com.aliasi.tokenizer.TokenCategorizer;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.train.UnknownTokensFilter;

import com.aliasi.util.Streams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;


public class TaggedCorpusTagParser { // implements TagParser {

    // extract from original

    String[] mSentenceIds;

    String[][] mOriginalTokens;

    String[][] mOriginalTags;

    String[][] mSpanTags;

    // extract from tokenization

    String[][] mTokens;

    String[][] mTagBoundaries;

    // extract on third pass

    String[][] mKeyTags;

    // extract by running ne

    String[][] mResponseTags;

    TokenizerFactory mTokenizerFactory;

    TokenCategorizer mTokenCategorizer;

    public TaggedCorpusTagParser(TokenizerFactory factory,
                                 TokenCategorizer categorizer) {
        mTokenizerFactory = factory;
        mTokenCategorizer = categorizer;
    }

    public TaggedCorpusTagParser() {
        this(Constants.TOKENIZER_FACTORY,
             Constants.TOKEN_CATEGORIZER);
    }



    public void readFile(File f) throws IOException {
        String[] lines = readLines(f);
        processLines(lines);
    }

    public static String[] concatenate(String[][] xs) {
        ArrayList resultList = new ArrayList();
        for (int i = 0; i < xs.length; ++i)
            for (int j = 0; j < xs[i].length; ++j)
                resultList.add(xs[i][j]);
        String[] result = new String[resultList.size()];
        resultList.toArray(result);
        return result;
    }

    public void train(TrainableEstimator estimator) throws IOException {
        for (int i = 0; i < mTokens.length; ++i)
            estimator.handle(mTokens[i], mKeyTags[i]);
        String[] tokens = concatenate(mTokens);
        String[] tags = concatenate(mKeyTags);
        UnknownTokensFilter unknownTokenFilter
            = new UnknownTokensFilter(Constants.KNOWN_TOKEN_COUNT,
                                      mTokenCategorizer);
        unknownTokenFilter.filter(tokens);
        estimator.handle(tokens,tags);
    }

    public void processLines(String[] lines) {
        int size = lines.length;
        ArrayList sentenceIds = new ArrayList(size);
        ArrayList originalTokens = new ArrayList(size);
        ArrayList originalTags = new ArrayList(size);
        ArrayList tokens = new ArrayList(size);
        ArrayList tagBoundaries = new ArrayList(size);
        ArrayList keyTags = new ArrayList(size);
        for (int i = 0; i < lines.length; ++i)
            processLine(lines[i],sentenceIds,originalTokens,originalTags,
                        tokens,tagBoundaries,keyTags);

        mSentenceIds = toStringArray(sentenceIds);

        mOriginalTokens = new String[originalTokens.size()][];
        originalTokens.toArray(mOriginalTokens);

        mOriginalTags = new String[originalTags.size()][];
        originalTags.toArray(mOriginalTags);

        mTokens = new String[tokens.size()][];
        tokens.toArray(mTokens);

        mTagBoundaries = new String[tagBoundaries.size()][];
        tagBoundaries.toArray(mTagBoundaries);

        mKeyTags = new String[keyTags.size()][];
        keyTags.toArray(mKeyTags);

    }

    public void processLine(String line, ArrayList sentenceIdList,
                            ArrayList originalTokenList,
                            ArrayList originalTagList,
                            ArrayList tokenList, ArrayList tagBoundaryList,
                            ArrayList keyTagList) {
        ArrayList lineTokenList = new ArrayList(100);
        ArrayList lineTagList = new ArrayList(100);
        String id = parseLine(line,lineTokenList,lineTagList);
        sentenceIdList.add(id);
        String[] lineTokens = toStringArray(lineTokenList);
        originalTokenList.add(lineTokens);
        String[] lineTags = toStringArray(lineTagList);
        originalTagList.add(lineTags);

        ArrayList newLineTokenList = new ArrayList();
        ArrayList lineTagBoundaryList = new ArrayList();
        tokenizeLine(lineTokens,lineTags,
                     newLineTokenList,lineTagBoundaryList);

        String[] newLineTokens = toStringArray(newLineTokenList);
        tokenList.add(newLineTokens);

        String[] tagBoundaries = toStringArray(lineTagBoundaryList);
        tagBoundaryList.add(tagBoundaries);

        ArrayList lineKeyTagsList = new ArrayList();
        transformTags(tagBoundaries,lineKeyTagsList);
        keyTagList.add(toStringArray(lineKeyTagsList));
    }

    public void transformTags(String[] tagBoundaries, ArrayList newKeyTags) {
        String lastTag = "OUT";
        for (int i = 0; i < tagBoundaries.length; ++i) {
            if (tagBoundaries[i] == null) {
                newKeyTags.add(getTag(lastTag));
            } else if (tagBoundaries[i].equals("NEWGENE")) {
                if (!lastTag.equals("NEWGENE")) {
                    newKeyTags.add("ST_GENE");
                    lastTag = "NEWGENE";
                } else {
                    newKeyTags.add("GENE");
                }
            } else if (tagBoundaries[i].equals("NEWGENE1")) {
                if (!lastTag.equals("NEWGENE1")) {
                    newKeyTags.add("ST_GENE");
                    lastTag = "NEWGENE1";
                } else {
                    newKeyTags.add("GENE");
                }
            } else {
                newKeyTags.add("OUT");
                lastTag = "OUT";
            }
        }
    }

    public String getTag(String lastTag) {
        if (lastTag.equals("NEWGENE")) return "GENE";
        if (lastTag.equals("NEWGENE1")) return "GENE";
        return "OUT";
    }


    public String parseLine(String line, ArrayList lineTokens,
                            ArrayList lineTags) {
        int afterIDIndex = line.indexOf(' ');
        String id = line.substring(0,afterIDIndex);
        String rest = line.substring(afterIDIndex+1);
        String[] tokenTagPairs = line.split(" ");
        for (int i = 0; i < tokenTagPairs.length; ++i) {
            if (tokenTagPairs[i].length() < 1) continue;
            if (tokenTagPairs[i].indexOf('/') < 0) continue;
            int lastIndexOfSlash = tokenTagPairs[i].lastIndexOf('/');
            String token = tokenTagPairs[i].substring(0,lastIndexOfSlash);
            String tag = tokenTagPairs[i].substring(lastIndexOfSlash+1);
            lineTokens.add(token);
            lineTags.add(tag);
        }
        return id;
    }

    public void tokenizeLine(String[] lineTokens, String[] lineTags,
                             ArrayList newLineTokenList,
                             ArrayList lineTagBoundaryList) {
        for (int i = 0; i < lineTokens.length; ++i) {
            String tokenIn = lineTokens[i];
            String tagIn = lineTags[i];
            String[] tokensOut
                = mTokenizerFactory.tokenizer(tokenIn.toCharArray(),
                                              0,tokenIn.length()).tokenize();
            for (int j = 0; j < tokensOut.length; ++j) {
                newLineTokenList.add(tokensOut[j]);
                lineTagBoundaryList.add(j == 0 ? tagIn : null);
            }
        }
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mSentenceIds.length; ++i)
            toStringBuffer(sb,i,mResponseTags);
        return sb.toString();
    }

    public void toStringBuffer(StringBuffer sb, int lineId, String[][] tags) {
        sb.append(mSentenceIds[lineId]);
        String[] tokens = mTokens[lineId];
        String[] tagBoundaries = mTagBoundaries[lineId];
        String[] outTags = tags[lineId];

        ArrayList finalTokensList = new ArrayList();
        ArrayList finalTagsList = new ArrayList();
        for (int i = 0; i < tokens.length; ) {
            StringBuffer tokenBuf = new StringBuffer(tokens[i]);
            String tag = outTags[i];
            while (++i < tokens.length && tagBoundaries[i] == null) {
                tokenBuf.append(tokens[i]);
                tag = unifyTag(tag,outTags[i]);
            }
            finalTokensList.add(tokenBuf.toString());
            finalTagsList.add(tag);
        }
        String[] finalTokens = toStringArray(finalTokensList);
        String[] finalTags = toStringArray(finalTagsList);
        toBioCreativeTags(finalTags);
        for (int i = 0; i < finalTokens.length; ++i) {
            sb.append(' ');
            sb.append(finalTokens[i]);
            sb.append('/');
            sb.append(finalTags[i]);
        }
        sb.append('\r');
        sb.append('\n');
    }

    // convert ST_GENE, GENE, OUT to OUT, NEWGENE1, NEWGENE
    public void toBioCreativeTags(String[] tags) {
        if (tags.length == 0) return;
        boolean inGene = false;
        String lastTag = "OUT";
        for (int i = 0; i < tags.length; ++i) {
            if (tags[i].equals("OUT")) {
                lastTag = "OUT";
            } else if (tags[i].equals("GENE")) {
                tags[i] = lastTag;
            } else if (tags[i].equals("ST_GENE")) {
                if (!Constants.USE_GENE1 || lastTag.equals("OUT") || lastTag.equals("NEWGENE1")) {
                    tags[i] = "NEWGENE";
                    lastTag = "NEWGENE";
                } else if (lastTag.equals("NEWGENE")) {
                    tags[i] = "NEWGENE1";
                    lastTag = "NEWGENE1";
                }
            }
        }
    }

    public static String unifyTag(String tag1, String tag2) {
        if (tag1.equals("OUT")) return tag2;
        if (tag2.equals("OUT")) return tag1;
        if (tag1.equals("ST_GENE")) return "ST_GENE";
        if (tag2.equals("ST_GENE")) return "ST_GENE";
        return "GENE";
    }

    public static String[] toStringArray(Collection c) {
        String[] result = new String[c.size()];
        c.toArray(result);
        return result;
    }

    public static String[] readLines(File f) throws IOException {
        FileInputStream fileIn = null;
        InputStreamReader inReader = null;
        BufferedReader bufReader = null;
        ArrayList lines = new ArrayList();
        try {
            fileIn = new FileInputStream(f);
            inReader = new InputStreamReader(fileIn);
            bufReader = new BufferedReader(inReader);
            String line;
            while ((line = bufReader.readLine()) != null)
                lines.add(line);
            return toStringArray(lines);
        } finally {
            Streams.closeInputStream(fileIn);
            Streams.closeReader(inReader);
            Streams.closeReader(bufReader);
        }
    }


    public static class WholeWordTokenizer extends Tokenizer {
        private String mToken;
        boolean mReturned = false;
        public WholeWordTokenizer(String token) {
            mToken = token;
        }
        public String nextToken() {
            if (mToken != null) {
                String result = mToken;
                mToken = null;
                return result;
            } else {
                return null;
            }
        }
    }

    public static class WholeWordTokenizerFactory implements TokenizerFactory {
        public WholeWordTokenizerFactory() { }
        public Tokenizer tokenizer(char[] ch, int start, int length) {
            return new WholeWordTokenizer(new String(ch,start,length));
        }
    }

}
