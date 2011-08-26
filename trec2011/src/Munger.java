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
	buf.readLine(); // skip header
	List<String> topics = new ArrayList<String>();
	List<String> hits = new ArrayList<String>();
	List<String> workers = new ArrayList<String>();
	List<String> docs = new ArrayList<String>();
	List<String> truths = new ArrayList<String>();
	List<String> labels = new ArrayList<String>();
	List<String> docTopics = new ArrayList<String>();
	Map<String,String> docTopicToTruth = new HashMap<String,String>();
	Map<String,String> docTopicToDoc = new HashMap<String,String>();
	Map<String,String> docTopicToTopic = new HashMap<String,String>();
	SymTab topicSymTab = new SymTab();
	SymTab hitSymTab = new SymTab();
	SymTab workerSymTab = new SymTab();
	SymTab docSymTab = new SymTab();
	SymTab docTopic1SymTab = new SymTab();
	SymTab docTopic2SymTab = new SymTab();
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
	    String docTopic = doc + "__" + topic;

	    topics.add(topic);
	    hits.add(hit);
	    workers.add(worker);
	    docs.add(doc);
	    truths.add(truth);
	    labels.add(label);
	    docTopics.add(docTopic);
	    if ("-1".equals(truth)) {
		docTopic1SymTab.addSym(docTopic);
	    } else {
		docTopic2SymTab.addSym(docTopic);
		docTopicToTruth.put(docTopic,truth);
	    }
	    topicSymTab.addSym(topic);
	    hitSymTab.addSym(hit);
	    workerSymTab.addSym(worker);
	    docSymTab.addSym(doc);
	    docTopicToDoc.put(docTopic,doc);
	    docTopicToTopic.put(docTopic,topic);
	}
	buf.close();
	System.out.println("#topics=" + topicSymTab.size());
	System.out.println("#hits=" + hitSymTab.size());
	System.out.println("#workers=" + workerSymTab.size());
	System.out.println("#docs=" + docSymTab.size());
	System.out.println("#judgments=" + labels.size());
	System.out.println("#doc/topic pairs=" + docTopicToDoc.size());
	System.out.println("#truths=" + docTopicToTruth.size());

	PrintWriter p = new PrintWriter("data/munged/ii1_tt1.csv","ASCII");
	for (int i = 0; i < docTopic1SymTab.size(); ++i) {
	    String docTopic = docTopic1SymTab.idToSym(i);
	    p.println(docSymTab.symToId(docTopicToDoc.get(docTopic))
		      + "," + topicSymTab.symToId(docTopicToTopic
						  .get(docTopic)));
	}
	p.close();

	p = new PrintWriter("data/munged/ii2_tt2_z2.csv","ASCII");
	for (int i = 0; i < docTopic2SymTab.size(); ++i) {
	    String docTopic = docTopic2SymTab.idToSym(i);
	    p.println(docSymTab.symToId(docTopicToDoc.get(docTopic))
		      + "," + topicSymTab.symToId(docTopicToTopic
						  .get(docTopic))
		      + "," + docTopicToTruth.get(docTopic));
	}
	p.close();

	p = new PrintWriter("data/munged/ii1_jj1_y1.csv");
	for (int i = 0; i < topics.size(); ++i) {
	    String docTopic = docTopics.get(i);
	    if (docTopicToTruth.containsKey(docTopic)) continue;
	    String worker = workers.get(i);
	    String label = labels.get(i);
	    p.println(docTopic1SymTab.symToId(docTopic)
			    + "," + workerSymTab.symToId(worker)
			    + "," + label);
	}
	p.close();

	p = new PrintWriter("data/munged/ii2_jj2_y2.csv");
	for (int i = 0; i < topics.size(); ++i) {
	    String docTopic = docTopics.get(i);
	    if (!docTopicToTruth.containsKey(docTopic)) continue;
	    String worker = workers.get(i);
	    String label = labels.get(i);
	    p.println(docTopic2SymTab.symToId(docTopic)
			    + "," + workerSymTab.symToId(worker)
			    + "," + label);
	}
	p.close();

	printSymTab("doc",docSymTab);
	printSymTab("topic",topicSymTab);
	printSymTab("worker",workerSymTab);
    }

    static void printSymTab(String label, SymTab st) throws IOException {
	PrintWriter p
	    = new PrintWriter("data/munged/" + label + "_" + "sym.csv",
			      "ASCII");
	int i = 0;
	for (String sym : st.mIdToSym)
	    p.println((++i) + "," + sym);
	p.close();
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