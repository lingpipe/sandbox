import com.aliasi.chunk.*;

import com.aliasi.corpus.StringParser;
import com.aliasi.corpus.TagHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

// this is here until we build handlers for the true PennBioIe corpus
// which is in a different format
public class PennBioTagParser extends StringParser<TagHandler> {

    double mStart = 0.0;
    double mEnd = 1.0;

    public PennBioTagParser() {
        super();
    }

    public PennBioTagParser(TagHandler handler) {
        super(handler);
    }

    public void setTagHandler(TagHandler handler) {
        setHandler(handler);
    }

    public void setStart(double start) {
        validProb("start",start);
        mStart = start;
    }

    public void setEnd(double end) {
        validProb("end",end);
        mEnd = end;
    }

    public void parseString(char[] cs, int start, int end) {
        String s = new String(cs,start,end);
        String[] lines = s.split("\\n");

        int startLine = (int) (mStart * (double)lines.length);
        int endLine = (int) (mEnd * (double)lines.length);
        while (endLine >= lines.length)
            --endLine;
        while (startLine < endLine && !blankLine(lines[startLine]))
            ++startLine;
        while (startLine < endLine && !blankLine(lines[endLine]))
            --endLine;

        int i = startLine;
        while (i < endLine) {
            List<String> tokenList = new ArrayList<String>(endLine-startLine);
            List<String> tagList = new ArrayList<String>(endLine-startLine);
            while (i < endLine) {
                String line = lines[i].trim();
                ++i;
                if (line.length() == 0) break;
                extract(lines[i].trim(),tokenList,tagList);
            }
            String[] tokens = new String[tokenList.size()];
            tokenList.toArray(tokens);
            String[] tags = new String[tagList.size()];
            tagList.toArray(tags);
            String[] whites = new String[tokens.length+1];
            Arrays.fill(whites," ");
            getHandler().handle(tokens,whites,tags);
        }
    }

    static boolean blankLine(String line) {
        return line.trim().length() == 0;
    }

    static boolean outLine(String line) {
        String[] tokTag = line.trim().split("\\t");
        return tokTag[0].equals("O");
    }

    static void extract(String trimmedLine,
                        List<String> tokenList, List<String> tagList) {
        if (trimmedLine.length() < 3) return;
        String[] tokenTag = trimmedLine.split("\\t");
        if (tokenTag.length != 2) return;
        tokenList.add(tokenTag[0]);
        tagList.add(normalizeTag(tokenTag[1]));
    }

    static String normalizeTag(String tag) {
        return tag;
        /*
        if (tag.equals("O")) return "O";
        if (tag.equals("B-GENE")) return "B-GENE";
        if (tag.equals("I-GENE")) return "I-GENE";
        throw new IllegalArgumentException("unexpected tag=" + tag);
        */
    }

    static void validProb(String name, double p) {
        if (p >= 0.0 && p <= 1.0) return;
        String msg = "Probability must be between 0.0 and 1.0"
            + " Found " + name + "=" + p;
        throw new IllegalArgumentException(msg);
    }


}