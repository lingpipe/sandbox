import java.io.*;
import java.util.*;

import com.aliasi.util.Streams;

public class Csv {

    // hack temps for parsing
    private int mPos = 0;
    private String mText;
    private final char mFieldSep;

    // persistent
    private final List<String> mHeader;
    private final List<List<String>> mRows = new ArrayList<List<String>>();

    public Csv(Reader r, char fieldSep, boolean hasHeader) 
        throws IOException {

        mFieldSep = fieldSep;
        BufferedReader buf = new BufferedReader(r);
        mText = new String(Streams.toCharArray(buf));

        // DEBUG
        // for (int i = 0; i < mText.length(); ++i)
        // System.out.println("charAt " + i + "=" + mText.charAt(i));

        Streams.closeQuietly(buf);
        mHeader = hasHeader ? nextRow() : null;
        List<String> row;
        while ((row = nextRow()) != null)
            mRows.add(row);
        mText = null; // don't need it later
    }

    // exposing privates
    public List<String> header() {
        return mHeader;
    }

    // exposing privates
    public List<String> row(int n) {
        return mRows.get(n);
    }

    public int numRows() {
        return mRows.size();
    }

    char nextChar() {
        return mText.charAt(mPos);
    }

    boolean hasNextChar() {
        return mPos < mText.length();
    }

    boolean atEol() {
        return hasNextChar()
            && (nextChar() == '\n' || nextChar() == '\r');
    }
    
    void skipEol() {
        if (nextChar() == '\n') {
            ++mPos;
            return;
        }
        if (nextChar() == '\r')
            ++mPos;
        if (hasNextChar() && nextChar() == '\n')
            ++mPos;
    }


    List<String> nextRow() {
        // DEBUG: System.out.println("next row, mPos=" + mPos);
        if (!hasNextChar())
            return null;
        List<String> row = new ArrayList<String>();
        String field;
        while ((field = nextField()) != null)
            row.add(field);
        return row;
    }

    String nextField() {
        if (!hasNextChar())
            return null;
        if (atEol()) {
            skipEol();
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (nextChar() == '"') {
            ++mPos;
            nextQuotedField(sb);
        } else {
            nextSimpleField(sb);
        }
        return sb.toString();
    }

    void nextQuotedField(StringBuilder sb) {
        // DEBUG: System.out.println("next quoted field, mPos=" + mPos);
        while (hasNextChar()) {
            if (nextChar() == '"') {
                ++mPos;
                if (hasNextChar()  && nextChar() == '"') {
                    sb.append('"');
                    ++mPos;
                    continue;
                }
                if (hasNextChar() && (nextChar() == mFieldSep))
                    ++mPos;
                return;
            } else {
                sb.append(nextChar());
                ++mPos;
            }
        }
    }

    void nextSimpleField(StringBuilder sb) {
        // DEBUG: System.out.println("next simple field, mPos=" + mPos);
        while (hasNextChar()) {
            if (nextChar() == mFieldSep) {
                ++mPos;
                return;
            } 
            if (atEol())
                return;
            sb.append(nextChar());
            mPos++;
        }
    }

    
    public static void main(String[] args) throws IOException {
        InputStream in = new FileInputStream(args[0]);
        Reader reader = new InputStreamReader(in,args.length > 1 ? args[1] : "UTF-8");
        boolean hasHeader = true;
        char separator = ',';
        Csv csv = new Csv(reader,separator,hasHeader);
        printRow(csv.header(),"HEADER");
        for (int i = 0; i < csv.numRows(); ++i)
            printRow(csv.row(i),"ROW " + i);
    }

    static void printRow(List<String> row, String msg) {
        System.out.println();
        for (int i = 0; i < row.size(); ++i) {
            String field = row.get(i);
            System.out.println(msg + ", COL " + i + "=|" + field + "|");
        }
    }

}