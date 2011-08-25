import java.io.*;
import java.util.*;

public class Munger {

    public static void main(String[] args) throws IOException {
	File dataFile = new File(args[0]);
	System.out.println("Reading data from file\n  " 
			   + dataFile.getCanonicalPath());
	InputStream in = new FileInputStream(dataFile);
	Reader reader = new InputStreamReader(in,"ASCII");
	BufferedReader buf = new BufferedReader(reader);
	String header = buf.readLine();
	System.out.println("header:\n  " + header);
	List<String> topics = new ArrayList<String>();
	List<String> hits = new ArrayList<String>();
	List<String> workers = new ArrayList<String>();
	List<String> docs = new ArrayList<String>();
	List<String> truths = new ArrayList<String>();
	List<String> labels = new ArrayList<String>();
	SymTab topicSymTab = new SymTab();
	SymTab hitSymTab = new SymTab();
	SymTab workerSymTab = new SymTab();
	SymTab docSymTab = new SymTab();
	Map<String,String> docToTopic = new HashMap<String,String>();
	String line;
	while ((line = buf.readLine()) != null) {
	    String[] fields = line.split("\\s+");
	    if (fields.length != 6)
		throw new IllegalStateException("line=" + line);
	    String topic = fields[0];
	    String hit = fields[1];
	    String worker = fields[2];
	    String doc = fields[3];
	    String truth = fields[4];
	    String label = fields[5];
	    docToTopic.put(doc,topic);
	    topics.add(topic);
	    hits.add(hit);
	    workers.add(worker);
	    docs.add(doc);
	    truths.add(truth);
	    labels.add(label);
	    topicSymTab.addSym(topic);
	    hitSymTab.addSym(hit);
	    workerSymTab.addSym(worker);
	    docSymTab.addSym(doc);
	}
	buf.close();
	System.out.println("#topics=" + topicSymTab.size());
	System.out.println("#hits=" + hitSymTab.size());
	System.out.println("#workers=" + workerSymTab.size());
	System.out.println("#docs=" + docSymTab.size());
	System.out.println("#judgments=" + labels.size());
	
	PrintWriter truthPrinter 
	    = new PrintWriter("data/munged/doc_truth.csv","ASCII");
	Set<String> nonNistDocs = new HashSet<String>(docs);
	Map<Integer,String> truthMap = new HashMap<Integer,String>();
	for (int i = 0; i < truths.size(); ++i) {
	    if (truths.get(i).equals("-1")) continue;
	    nonNistDocs.remove(docs.get(i));
	    truthMap.put(docSymTab.symToId(docs.get(i)),
			 truths.get(i));
	}
	System.out.println("#NIST judgments=" + truthMap.size());
	for (Map.Entry<Integer,String> entry : truthMap.entrySet()) {
	    truthPrinter.println(entry.getKey() + "," + entry.getValue());
	}
	truthPrinter.close();
	System.out.println("#Docs w/o NIST judgments=" + nonNistDocs.size());

	PrintWriter unknownPrinter
	    = new PrintWriter("data/munged/doc_unknown.csv");
	for (String doc : nonNistDocs)
	    unknownPrinter.println(doc);
	unknownPrinter.close();

	PrintWriter labelPrinter
	    = new PrintWriter("data/munged/doc_anno_label.csv", "ASCII");
	for (int i = 0; i < labels.size(); ++i)
	    labelPrinter.println(docSymTab.symToId(docs.get(i))
				 + "," + workerSymTab.symToId(workers.get(i))
				 + "," + labels.get(i));
	labelPrinter.close();

	PrintWriter docTopicPrinter
	    = new PrintWriter("data/munged/doc_topic.csv","ASCII");
	for (Map.Entry<String,String> entry : docToTopic.entrySet())
	    docTopicPrinter.println(docSymTab.symToId(entry.getKey())
				    + "," 
				    + topicSymTab.symToId(entry.getValue()));
	docTopicPrinter.close();

	printSymTab("topic",topicSymTab);
	printSymTab("hit",hitSymTab);
	printSymTab("worker",workerSymTab);
	printSymTab("doc",docSymTab);
    }

    static void printSymTab(String label, SymTab st) 
	throws IOException {
	System.out.println("#" + label + "=" + st.mIdToSym.size());
	PrintWriter printer
	    = new PrintWriter("data/munged/" + label + "_" + "sym.csv",
			      "ASCII");
	int i = 0;
	for (String sym : st.mIdToSym)
	    printer.println((++i) + "," + sym);
	printer.close();
    }
			    
    

    static class SymTab {
	final Map<String,Integer> mSymToId
	    = new HashMap<String,Integer>();
	final List<String> mIdToSym
	    = new ArrayList<String>();
	public int size() {
	    return mIdToSym.size();
	}
	public void addSym(String symbol) {
	    if (mSymToId.containsKey(symbol))
		return;
	    mIdToSym.add(symbol);
	    mSymToId.put(symbol,mIdToSym.size());
	}
	public int symToId(String sym) {
	    return mSymToId.get(sym);
	}
	public String idToSym(int id) {
	    return mIdToSym.get(id);
	}
    }

}