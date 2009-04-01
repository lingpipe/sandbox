package tracker;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Files;
import com.aliasi.util.ScoredObject;
import com.aliasi.matrix.ProximityMatrix;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Counter;

import util.Util;

import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.LeafDendrogram;
import com.aliasi.cluster.LinkDendrogram;

import com.aliasi.xml.SAXWriter;

import com.aliasi.xhtml.Body;
import com.aliasi.xhtml.P;
import com.aliasi.xhtml.Html;
import com.aliasi.xhtml.Head;
import com.aliasi.xhtml.Title;
import com.aliasi.xhtml.Document;
import com.aliasi.xhtml.Hr;
import com.aliasi.xhtml.Ul;
import com.aliasi.xhtml.Li;
import com.aliasi.xhtml.Br;
import com.aliasi.xhtml.A;
import com.aliasi.xhtml.Link;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;

import util.GeneRecord;

public class RelatedGeneCluster extends AbstractCommand {

    final HashMap mPmid2Genes = new HashMap();
    final HashMap mGene2Pmids = new HashMap();
    IndexSearcher mEntrezGene;
    Searcher mMedline;
    HashSet mClusterGenes;
    
    public RelatedGeneCluster(String args[]) {
        super(args);
    }
    
    public void run() {
        File outFile = null;
        try {
            mEntrezGene = getLuceneIndexSearcher(ENTREZGENE_INDEX_PARAM);
            outFile = getArgumentFile(OUTPUT_FILE_PARAM);
            load(getArgumentFile(INPUT_PARAM));
            mMedline = getLuceneIndexSearcher(MEDLINE_INDEX_PARAM);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // printHashMap(mPmid2Genes);
        // printHashMap(mGene2Pmids);

        String[] clusterGenes = getCSV(GENES_TO_EVAL_PARAM);
        mClusterGenes = new HashSet();
        ProximityMatrix prox = new ProximityMatrix(clusterGenes);
        ObjectToCounterMap[] geneCooccurance = new ObjectToCounterMap[clusterGenes.length];
        for (int i = 0; i < clusterGenes.length; ++i) {
            mClusterGenes.add(clusterGenes[i]);
            ObjectToCounterMap otherGeneCounts = new ObjectToCounterMap();
            geneCooccurance[i] = otherGeneCounts;
            ArrayList pmids = (ArrayList) mGene2Pmids.get(clusterGenes[i]);      
            for (int j = 0; j < pmids.size(); ++j) {
                String candPmid = (String) pmids.get(j);
                ArrayList otherGenes = (ArrayList) mPmid2Genes.get(candPmid);
                //                System.out.println(clusterGenes[i] + " " + mPmid2Genes.get(j) + pmids);
                for (int k = 0; k < otherGenes.size(); ++k) {
                    otherGeneCounts.increment((String) otherGenes.get(k));
                }
            }
        }
        
        for (int i = 0; i < clusterGenes.length; ++i) {
            for (int j = i + 1; j < clusterGenes.length; ++j) {
                double cosine = cosine(geneCooccurance[i],
                                       geneCooccurance[j]);
                //System.out.println(clusterGenes[i] + " " + clusterGenes[j] + " Cosine is " + cosine); 
                prox.setValue(i,j,cosine);
            }
        }

        /*        for (int i = 0; i < clusterGenes.length; ++i) 
            //System.out.print(clusterGenes[i] + ",");
            //System.out.println("");
        for (int i = 0; i < clusterGenes.length; ++i) {
            for (int j = 0; j < clusterGenes.length; ++j) {
                //    System.out.print(prox.value(i,j) + ",");
            }
            System.out.println("");
        }
        */
        //        SingleLinkClusterer clusterer = new SingleLinkClusterer();

        CompleteLinkClusterer clusterer = new CompleteLinkClusterer();
        Dendrogram[] dendrograms = clusterer.hierarchicalCluster(prox,1.0);
        StringBuffer sb = new StringBuffer();

        Title title
            = new Title("Gene Clusters");
        Head head = new Head(title);
        Link styleSheet = new Link();
        styleSheet.setHref("web/css/geneCluster.css");
        styleSheet.setRel("stylesheet");
        styleSheet.setTitle("Gene Clusterer");
        styleSheet.setType("text/css");
        head.add(styleSheet);

        Body body = new Body();
        Html html = new Html(head,body);
        Document document = new Document(html);
        try {
            for (int i = 0; i < dendrograms.length; ++i) {
                body.add(new Hr());
                Ul dendUl = new Ul();
                body.add(dendUl);
                render(dendrograms[i],dendUl);        
            }
            System.out.println("writing " + outFile);
            SAXWriter writer
                = new SAXWriter(new FileOutputStream(outFile),"UTF-8");
            //= new SAXWriter(System.out,"UTF-8");
            writer.setDTDString(XML_DOCTYPE);
            document.writeTo(writer);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
            //        System.out.println("Cluster= " + dendrogram);
            //        System.out.println("Cluster= " + dendrogram.prettyPrint());
        
    }
    void printHashMap(HashMap hm) {
        Set keys = hm.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            ArrayList elts = (ArrayList) hm.get(key);
            System.out.print(key + ": ");
            for (int i = 0; i < elts.size(); ++i) {
                System.out.print(elts.get(i) + " ");
            }
            System.out.println("");
        }
    }

    void render(Dendrogram d, 
                Ul ul) throws IOException {
        int size = d.size();
        //        System.out.println("Size is " + size);
        //System.out.println("Dendro is " + d);
        String unionStr = getUnionOfPmids(d);
        //        System.out.println("union is [" + unionStr + "]");
        if (size > 1) {
            Ul subUl = ul;
            if (true || unionStr != "") { //add in layer of nesting
                Li li = new Li();
                ul.add(li);
                addNodeSummarized(li,d);
                subUl = new Ul();
                li.add(subUl);
            }
            LinkDendrogram linkD = (LinkDendrogram) d;
            Dendrogram[] leaves =  linkD.daughters();
            render(leaves[0],subUl);
            render(leaves[1],subUl);
        }
        else {
            Li li = new Li();
            li.setId("sourceGene");
            ul.add(li);
            LeafDendrogram leafD = (LeafDendrogram) d;
            
            addLinkToEg(li,(String) leafD.object());
            //            li.add((String) leafD.object());
            //li.add("L: " + (String) leafD.object() + " x " + getIntersectionOfCooccuringGenes(d));
        }
    }

    void addNodeSummarizedOld (Li li, Dendrogram d) throws IOException {
        Set elts = d.memberSet();
        Iterator it = elts.iterator();
        li.setId("node");
        if (d.size() > 2) {
            HashSet set = getIntersectionOfCooccuringGenes(d);
            li.add(d.score() + ": " + Util.join(",", set));
        }
    }

    void addNodeSummarized(Li li, Dendrogram d) throws IOException {
        Set elts = d.memberSet();
        Iterator it = elts.iterator();
        li.setId("node");
        li.add(d.score() + ": ");
        if (elts.size() > 0) {
            HashSet genes = getIntersectionOfCooccuringGenes(d);
            HashSet pmids = getSpanningSetofPmids(genes);
            Iterator it2 = genes.iterator();
            while (it2.hasNext()) {
                String geneId = (String) it2.next();
                addLinkToEg(li,geneId);
            }
            if (genes.size() > 0) {
                Iterator it3 = pmids.iterator();
                while (it3.hasNext()) {
                    A a = new A();
                    String pmid = (String) it3.next();
                    a.setHref(HREF_CITE_LOOKUP + pmid);
                    a.add(pmid);
                    li.add(a);
                    li.add(" ");
                }
            }
        }
    }

    HashSet getSpanningSetofPmids(Set genes) {
        HashSet returnSet = new HashSet();
        ObjectToCounterMap accum = new ObjectToCounterMap();
        Iterator it = genes.iterator();
        while (it.hasNext()) {
            String geneId = (String) it.next();
            ArrayList pmids = (ArrayList) mGene2Pmids.get(geneId);
            for (int i = 0; i < pmids.size(); ++i) {
                accum.increment(pmids.get(i));
            }
        }
        Object[] keys = accum.keysOrderedByCount(); 
        for (int i = 0; i < keys.length; ++i) {
            //            if (  accum.getCount(keys[i]) > 1)
            //  System.out.println(keys[i] + " " + accum.getCount(keys[i]));
            if (accum.getCount(keys[i]) > 1) {
                returnSet.add(keys[i]);
            }
        }
        return returnSet;
    }
    
    void addLinkToEg(Li li, String geneId) throws IOException {

        Term term = new Term(GeneRecord.ID,geneId);
        Query query = new TermQuery(term);
        Hits hits = mEntrezGene.search(query);
        if (hits.length() != 1) {
            System.out.println("problem with Entrez Gene id: "+geneId+" got "+hits.length()+" hits, expected 1");
            return;
        }
        org.apache.lucene.document.Document doc = hits.doc(0);
        
        String symbol = doc.get(GeneRecord.OFFICIAL_SYMBOL);
        if (symbol == null) {
            symbol = "<no symbol>";
        }
        A a = new A();
        a.setHref(HREF_GENE_LOOKUP + geneId);
        a.add(symbol);
        li.add(a);
        li.add(" ");
    }
    
    void addNode (Li li, Dendrogram d) throws IOException {
        Set elts = d.memberSet();
        Iterator it = elts.iterator();
        Ul ul = new Ul();
        //        li.add(getUnionOfPmids(d));
        li.add(ul);
        while (it.hasNext()) {
            String element = (String) it.next();
            Term term = new Term(GeneRecord.ID,element);
            Query query = new TermQuery(term);
            Hits hits = mEntrezGene.search(query);
            if (hits.length() != 1) {
                System.out.println("problem with Entrez Gene id: "+element+" got "+hits.length()+" hits, expected 1");
            }
            org.apache.lucene.document.Document doc = hits.doc(0);
            Li liSummary = new Li();
            liSummary.setId("node");

            ul.add(liSummary);
            String geneName = doc.get(GeneRecord.OFFICIAL_GENE_NAME);
            if (geneName != null) {
                liSummary.add(geneName);
            }
            else {
                liSummary.add("<none>");
            }
        }
    }

    HashSet getIntersectionOfCooccuringGenes(Dendrogram d) {
        StringBuffer sb = new StringBuffer();
        Set elts = d.memberSet();
        Iterator it = elts.iterator();
        HashSet accum = null;
        while (it.hasNext()) {
            String element = (String) it.next();
            ArrayList pmids = (ArrayList) mGene2Pmids.get(element);
            HashSet cooccurGene = new HashSet();
            for (int i = 0; i < pmids.size(); ++i) {
                ArrayList candCooccurGenes = (ArrayList) mPmid2Genes.get(pmids.get(i));
                for (int j = 0; j < candCooccurGenes.size(); ++j) {
                    String geneId = (String) candCooccurGenes.get(j);
                    if (true || !mClusterGenes.contains(geneId)) {
                        cooccurGene.add(geneId);
                    }
                }
            }
            if (accum == null) {
                accum = new HashSet();
                accum.addAll(cooccurGene);
            }
            else {
                accum.retainAll(cooccurGene);
            }
        }
        return accum;
    }

    String getUnionOfGenes(Dendrogram d) {
        StringBuffer sb = new StringBuffer();
        Set elts = d.memberSet();
        Iterator it = elts.iterator();
        HashSet accum = null;
        while (it.hasNext()) {
            String element = (String) it.next();
            ArrayList pmids = (ArrayList) mGene2Pmids.get(element);
            HashSet cooccurGene = new HashSet();
            for (int i = 0; i < pmids.size(); ++i) {
                cooccurGene.addAll((ArrayList) mPmid2Genes.get(pmids.get(i)));
            }
            //System.out.println(element + " " + util.Util.join(",",pmids));
            if (accum == null) {
                accum = new HashSet();
                accum.addAll(cooccurGene);
            }
            else {
                accum.retainAll(cooccurGene);
            }
        }
        Iterator it2 = accum.iterator();
        while (it2.hasNext()) {
            sb.append((String) it2.next() + ", ");
        }
        return sb.toString();
    }


    String getUnionOfPmids(Dendrogram d) {
        StringBuffer sb = new StringBuffer();
        Set elts = d.memberSet();
        Iterator it = elts.iterator();
        HashSet accum = null;
        while (it.hasNext()) {
            String element = (String) it.next();
            ArrayList pmids = (ArrayList) mGene2Pmids.get(element);
            //System.out.println(element + " " + util.Util.join(",",pmids));
            if (accum == null) {
                accum = new HashSet();
                accum.addAll(pmids);
            }
            else {
                accum.retainAll(pmids);
            }
        }
        Iterator it2 = accum.iterator();
        while (it2.hasNext()) {
            sb.append((String) it2.next() + ", ");
        }
        return sb.toString();
    }

    IndexSearcher getLuceneIndexSearcher(String paramName) 
        throws IOException, Exception{
        System.out.println("Loading Lucene index=" + paramName); 
        File indexDir = getArgumentFile(paramName);
        if (!indexDir.exists() || !indexDir.isDirectory()) {
            throw new Exception(indexDir + " is does not exist or is not a directory.");
        }
        Directory fsDir = FSDirectory.getDirectory(indexDir, false);
        IndexSearcher is = new IndexSearcher(fsDir);
        return is;
    }

    static double cosine(ObjectToCounterMap doc1, ObjectToCounterMap doc2) {
        //        System.out.println("Dot product is :" + dotProduct(doc1,doc2));
        return dotProduct(doc1,doc2) / (length(doc1) * length(doc2));
    }

    static double dotProduct(ObjectToCounterMap doc1, 
                             ObjectToCounterMap doc2) {
        double product = 0.0;
        Iterator it = doc1.keySet().iterator();
        while (it.hasNext()) {

            Object key = it.next();
            //  System.out.println("key " + key + " doc1 " + doc1.getCount(key) + " doc2 " + doc2.getCount(key));
            product += doc1.getCount(key) * doc2.getCount(key);
        }
        return product == 0.0?Double.POSITIVE_INFINITY:product;
    }
    
    static double length(ObjectToCounterMap doc1) {
        double sumOfSquares = 0.0;
        Iterator it = doc1.values().iterator();
        while (it.hasNext()) {
            Counter counter = (Counter) it.next();
            double counterVal = counter.doubleValue(); 
            sumOfSquares += counterVal * counterVal;
        }
        return Math.sqrt(sumOfSquares);
    }

    void load(File file) throws IOException {
        String[] lines = Files.readLinesFromFile(file,UTF8);
        int skippedCount = 0;
        int keptCount = 0;
        
        for (int i = 0; i < lines.length; ++i) {
            String[] entries = lines[i].split(",");
            if (entries[0].equals("12477932")) {
                continue;
            }
            ArrayList genes = (ArrayList) mPmid2Genes.get(entries[0]);
            if (genes == null) {
                genes = new ArrayList();
                mPmid2Genes.put(entries[0],genes);
            }
            for (int j = 1; j < entries.length; ++j) {
                String[] geneScore = entries[j].split(":");
                if (Double.parseDouble(geneScore[1]) < -10.3) {
                    //                    System.out.println("Skipping : " + geneScore[0] + " " + geneScore[1]);
                    ++skippedCount;
                    continue;
                }
                //                System.out.println("Keeping : " + geneScore[0] + " " + geneScore[1]);
                genes.add(geneScore[0]);
                ++keptCount;
                ArrayList pmids = (ArrayList) mGene2Pmids.get(geneScore[0]);
                if (pmids == null) {
                    pmids = new ArrayList();
                    mGene2Pmids.put(geneScore[0],pmids);
                }
                pmids.add(entries[0]);
            }
        }
        System.out.println("Skipped " + skippedCount);
        System.out.println("Kept " + keptCount);
    }
    
    public static void main(String[] args) {
        RelatedGeneCluster cmd = new RelatedGeneCluster(args);
        cmd.run();
    }


    private static String HREF_CITE_LOOKUP = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&itool=iconabstr&list_uids=";

    private static final String XML_DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
    private static String OUTPUT_FILE_PARAM = "output";
    private static String INPUT_PARAM = "input";
    private static String UTF8 = "utf-8";
    private static String GENES_TO_EVAL_PARAM = "genesToMap";

    public static String MEDLINE_INDEX_PARAM = "medlineIndex";
    private static String ENTREZGENE_INDEX_PARAM = "entrezGeneIndex";

    private static String ENTREZ_CGI_ROOT = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi";
    private static String HREF_GENE_LOOKUP = ENTREZ_CGI_ROOT+"?db=gene&cmd=Retrieve&dopt=Graphics&list_uids=";
    

}
