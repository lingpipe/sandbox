import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;

import java.io.CharArrayReader;
import java.io.BufferedReader;
import java.io.IOException;

public class GigawordTextParser extends StringParser<ObjectHandler<CharSequence>> {


    @Override
    public void parseString(char[] cs, int start, int end) throws IOException {
        CharArrayReader charReader = new CharArrayReader(cs,start,end-start);
        BufferedReader bufReader = new BufferedReader(charReader);
        String line;
        while ((line = bufReader.readLine()) != null) {
            // wait for doc
            if (!line.startsWith("<DOC ")) continue;
            // ignore non-story
            if (line.indexOf("type=\"story") < 0)
                while ((line = bufReader.readLine()) != null)
                    if (line.startsWith("</DOC>"))
                        break;
            while (!line.startsWith("<TEXT>"))
                if ((line = bufReader.readLine()) == null)
                    return;
            if (line.startsWith("<TEXT>")) {
                StringBuilder sb = new StringBuilder();
                boolean continuing = false;
                while ((line = bufReader.readLine()) != null) {
                    if (line.startsWith("</TEXT>")) {
                        getHandler().handle(sb.toString().trim());
                        break;
                    }
                    if (line.startsWith("<P>")) {
                        continuing = false;
                        sb.append("\t ");
                    } else if (!line.startsWith("</P>")) {
                        if (continuing) sb.append(' ');
                        else continuing = true;
                        sb.append(line.indexOf('&') >= 0
                                  ? removeEscapes(line)
                                  : line);
                    }
                }
            }
        }
    }

    private static String removeEscapes(String line) {
        return line.replaceAll("&(amp|AMP);","&");
    }

}
