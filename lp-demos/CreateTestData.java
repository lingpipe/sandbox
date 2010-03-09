import com.aliasi.util.Files;
import java.io.File;

public class CreateTestData {
    public static void main(String[] args) throws Exception {
	File file = new File("f:/data/leipzig/unpacked/en300k/sentences.txt");
	String s = Files.readFromFile(file,"ISO-8859-1");
	String[] lines = s.split("\\n");
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < lines.length; ++i) {
	    int id = lines[i].indexOf("\t");
	    sb.append(lines[i].substring(id+1));
	    if (i > 100000) break;
	    sb.append(" ");
	}
	File outFile = new File("f:/leipzig-en.txt");
	Files.writeStringToFile(sb.toString(),outFile,"ISO-8859-1");
    }
}