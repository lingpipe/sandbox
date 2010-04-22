/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.lingmed.medline;

import com.aliasi.lingmed.lucene.LuceneAnalyzer;

import com.aliasi.lingmed.medline.parser.Abstract;
import com.aliasi.lingmed.medline.parser.Article;
import com.aliasi.lingmed.medline.parser.Author;
import com.aliasi.lingmed.medline.parser.AuthorList;
import com.aliasi.lingmed.medline.parser.Chemical;
import com.aliasi.lingmed.medline.parser.DataBank;
import com.aliasi.lingmed.medline.parser.DataBankList;
import com.aliasi.lingmed.medline.parser.Journal;
import com.aliasi.lingmed.medline.parser.JournalInfo;
import com.aliasi.lingmed.medline.parser.JournalIssue;
import com.aliasi.lingmed.medline.parser.KeywordList;
import com.aliasi.lingmed.medline.parser.MedlineCitation;
import com.aliasi.lingmed.medline.parser.MeshHeading;
import com.aliasi.lingmed.medline.parser.Name;
import com.aliasi.lingmed.medline.parser.OtherAbstract;
import com.aliasi.lingmed.medline.parser.PubDate;
import com.aliasi.lingmed.medline.parser.Topic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.log4j.Logger;


/**
 * A <code>SearchableMedlineCodec</code> provides conversion between
 * MEDLINE citations and Lucene documents with a rich set of
 * searchable fields.
 *
 * <p>Conversion of the Lucene document to a citation is carried
 * out by the superclass {@link MedlineCodec}.
 * 
 * @author Bob Carpenter, Mitzi Morris
 * @version 1.1
 * @since   LingMed1.0
 */
public class SearchableMedlineCodec extends MedlineCodec {

    private final Logger mLogger
        = Logger.getLogger(SearchableMedlineCodec.class);


    public SearchableMedlineCodec() { 
    }

    public Document toDocument(MedlineCitation citation) {
     
        if (mLogger.isDebugEnabled())
            mLogger.debug("toDocument(" + citation.pmid() + ")");

        // get doc with its basic fields
        Document doc = super.toDocument(citation);

        Article article = citation.article();

        String[] publicationTypes = article.publicationTypes();
        for (String publicationType : publicationTypes)
            add(doc,PUBLICATION_TYPE_FIELD,publicationType);

        String title = article.articleTitleText();
        add(doc,TITLE_FIELD,title);

        AuthorList authorList = article.authorList();
        if (authorList != null) {
            Author[] authors = authorList.authors();
            for (Author author : authors) {
                String affiliation = author.affiliation();
                add(doc,AFFILIATION_FIELD,affiliation);
                if (author.isCollective()) {
                    String collectiveName = author.collectiveName();
                    add(doc,AUTHOR_FIELD,collectiveName);
                } else {
                    Name name = author.name();
                    if (name == null) continue;
                    String fullName = name.fullName();
                    add(doc,AUTHOR_FIELD,fullName);
                    String foreName = name.foreName();
                    add(doc,AUTHOR_FORE_FIELD,foreName);
                    String middleName = name.middleName();
                    add(doc,AUTHOR_MIDDLE_FIELD,middleName);
                    String lastName = name.lastName();
                    add(doc,AUTHOR_LAST_FIELD,lastName);
                    String suffix = name.suffix();
                    add(doc,AUTHOR_SUFFIX_FIELD,suffix);
                }
            }
        }


        Abstract abstr = article.abstrct();
        if (abstr != null) {
            String abstractText = abstr.textWithoutTruncationMarker();
            add(doc,ABSTRACT_FIELD,abstractText);
        }

        OtherAbstract[] otherAbstracts = citation.otherAbstracts();
        for (OtherAbstract otherAbstract : otherAbstracts) {
            String otherAbstractText = otherAbstract.text();
            add(doc,ABSTRACT_FIELD,otherAbstractText);
        }

        Journal journal = article.journal();
        String isoAbbreviation = journal.isoAbbreviation();
        add(doc,JOURNAL_ISO_ABBREVIATION_FIELD,isoAbbreviation);
        String journalTitle = journal.title();
        add(doc,JOURNAL_TITLE_FIELD,journalTitle);
        String journalIssn = journal.issn();
        add(doc,JOURNAL_ISSN_FIELD,journalIssn);
        JournalInfo journalInfo = citation.journalInfo();
        String journalTitleAbbreviation = journalInfo.medlineTA();
        add(doc,JOURNAL_FIELD,journalTitleAbbreviation);
        JournalIssue journalIssue = journal.journalIssue();
        String issue = journalIssue.issue();
        add(doc,JOURNAL_ISSUE_FIELD,issue);
        String volume = journalIssue.volume();
        add(doc,JOURNAL_VOLUME_FIELD,volume);

        PubDate date = journalIssue.pubDate();
        if (date.isStructured()) {
            String year = date.year();
            add(doc,DATE_YEAR_FIELD,year);
            String season = date.season();
            add(doc,DATE_SEASON_FIELD,season);
            String month = date.month();
            add(doc,DATE_MONTH_FIELD,month);
            String day = date.day();
            add(doc,DATE_DAY_FIELD,day);
        } else {  // date unstructured
            String plainStringDate = date.toPlainString();
            String year = extractYear(plainStringDate);
            add(doc,DATE_YEAR_FIELD,year);
        }

        Chemical[] chemicals = citation.chemicals();
        for (Chemical chemical : chemicals) {
            String nameOfSubstance = chemical.nameOfSubstance();
            add(doc,CHEMICAL_NAME_FIELD,nameOfSubstance);
            String chemRegistryNum = chemical.registryNumber();
            add(doc,CHEMICAL_REGISTRY_FIELD,chemRegistryNum);
        }

        
        // headings = descriptor + qualifiers
        MeshHeading[] meshHeadings = citation.meshHeadings();
        for (MeshHeading heading : meshHeadings) {
            Topic[] topics = heading.topics();
            for (Topic topic : topics) {
                String topicName = topic.topic();
                String field = topic.isMajor() ? MESH_MAJOR_FIELD : MESH_MINOR_FIELD;
                add(doc,field,topicName);
            }
        }

        KeywordList[] keywordLists = citation.keywordLists();
        for (KeywordList list : keywordLists) {
            Topic[] topics = list.keywords();
            for (Topic topic : topics) {
                if (!topic.isMajor()) continue;
                String topicName = topic.topic();
                add(doc,KEYWORD_FIELD,topicName);
            }
        }

        String[] geneSymbols = citation.geneSymbols();
        for (String geneSymbol : geneSymbols)
            add(doc,GENE_SYMBOL_FIELD,geneSymbol);

        DataBankList dataBankList = article.dataBankList();
        if (dataBankList != null) {
            DataBank[] dataBanks = dataBankList.dataBanks();
            for (DataBank dataBank : dataBanks) {
                String dataBankName = dataBank.dataBankName();
                String[] accessionNumbers = dataBank.accessionNumbers();
                for (String accessionNumber : accessionNumbers) {
                    String labeledNumber = dataBankName + "=" + accessionNumber;
                    add(doc,DATA_BANK_FIELD,labeledNumber);
                }
            }
        }

        String[] languages = article.languages();
        for (String language : languages)
            add(doc,LANGUAGE_FIELD,language);

        return doc;
    }



    static void addAuthors(Document doc,
                           AuthorList authorList,
                           String affiliationField,
                           String fullField,
                           String foreField,
                           String middleField,
                           String lastField,
                           String suffixField) {
    }

    static void add(Document doc, String fieldName, String text) {
        if (text == null || text.length() == 0) return;
        boolean appendToExisting = doc.getField(fieldName) != null;
        if (appendToExisting)
            text = " , " + text;
        if (TEXT_FIELD_SET.contains(fieldName)) {
            addTextField(doc,fieldName,text);
            addTextField(doc,fieldName+EXACT_FIELD_SUFFIX,text);
            addTextField(doc,fieldName+NGRAM_FIELD_SUFFIX,text);
        } else if (SIMPLE_FIELD_SET.contains(fieldName)) {
            addTextField(doc,fieldName,text);
        } else {
            addKeywordField(doc,fieldName,text);
        }
    }


    static void addTextField(Document doc, String fieldName, String text) {
        Field field = new Field(fieldName,text,
                                Field.Store.NO,
                                Field.Index.ANALYZED);
        doc.add(field);
    }

    public static void addKeywordField(Document doc, String fieldName, String text) {
        Field field = new Field(fieldName,text,
                                Field.Store.NO,
                                Field.Index.NOT_ANALYZED);
        doc.add(field);
    }



    static String extractYear(PubDate pubDate) {
        return pubDate.isStructured() 
            ? pubDate.year()
            : extractYear(pubDate.toPlainString());
    }

    static String extractYear(String dateString) {
        Matcher m = YEAR_PATTERN.matcher(dateString);
        m.find();
        return m.group();
    }

    public static final Pattern YEAR_PATTERN 
        = Pattern.compile("((19|20)\\d\\d)");


    public static final String ABSTRACT_FIELD = "abstract";
    public static final String AFFILIATION_FIELD = "affiliation";
    public static final String AUTHOR_FIELD ="author";
    public static final String AUTHOR_FORE_FIELD ="authorFore";
    public static final String AUTHOR_MIDDLE_FIELD ="authorMiddle";
    public static final String AUTHOR_LAST_FIELD ="authorLast";
    public static final String AUTHOR_SUFFIX_FIELD ="authorSuffix";

    public static final String CHEMICAL_NAME_FIELD = "chemName";
    public static final String CHEMICAL_REGISTRY_FIELD = "chemRegistry";

    public static final String DATA_BANK_FIELD = "dataBank";

    public static final String DATE_YEAR_FIELD = "year";
    public static final String DATE_SEASON_FIELD = "season";
    public static final String DATE_MONTH_FIELD = "month";
    public static final String DATE_DAY_FIELD = "day";

    public static final String GENE_SYMBOL_FIELD = "gene";

    public static final String JOURNAL_FIELD = "journal";
    public static final String JOURNAL_ISO_ABBREVIATION_FIELD = "journalIso";
    public static final String JOURNAL_ISSN_FIELD = "journalIssn";
    public static final String JOURNAL_ISSUE_FIELD = "journalIssue";
    public static final String JOURNAL_TITLE_FIELD = "journalTitle";
    public static final String JOURNAL_VOLUME_FIELD = "journalVolume";

    public static final String KEYWORD_FIELD = "keyword";

    public static final String LANGUAGE_FIELD = "language";

    public static final String MESH_MAJOR_FIELD = "meshMaj";
    public static final String MESH_MINOR_FIELD = "meshMin";

    public static final String PUBLICATION_TYPE_FIELD = "publicationType";

    public static final String TITLE_FIELD = "title";

    public static final String EXACT_FIELD_SUFFIX = "X";
    public static final String NGRAM_FIELD_SUFFIX = "N";

    static final String[] TEXT_FIELDS = new String[] {
        ABSTRACT_FIELD,
        AFFILIATION_FIELD,
        AUTHOR_FIELD,
        AUTHOR_FORE_FIELD,
        AUTHOR_LAST_FIELD,
        AUTHOR_MIDDLE_FIELD,
        AUTHOR_SUFFIX_FIELD,
        CHEMICAL_NAME_FIELD,
        CHEMICAL_REGISTRY_FIELD,
        DATA_BANK_FIELD,
        GENE_SYMBOL_FIELD,
        JOURNAL_FIELD,
        JOURNAL_ISO_ABBREVIATION_FIELD,
        JOURNAL_ISSUE_FIELD,
        JOURNAL_TITLE_FIELD,
        KEYWORD_FIELD,
        MESH_MAJOR_FIELD,
        MESH_MINOR_FIELD,
        PUBLICATION_TYPE_FIELD,
        TITLE_FIELD,
    };

    static final String[] SIMPLE_FIELDS = new String[] {
        LANGUAGE_FIELD
    };

    static final String[] KEYWORD_FIELDS = new String[] {
        JOURNAL_VOLUME_FIELD,
        JOURNAL_ISSN_FIELD,
        DATE_DAY_FIELD,
        DATE_MONTH_FIELD,
        DATE_SEASON_FIELD,
        DATE_YEAR_FIELD,
    };

    static final Set<String> TEXT_FIELD_SET = new HashSet<String>(Arrays.<String>asList(TEXT_FIELDS));
    static final Set<String> KEYWORD_FIELD_SET = new HashSet<String>(Arrays.<String>asList(KEYWORD_FIELDS));
    static final Set<String> SIMPLE_FIELD_SET = new HashSet<String>(Arrays.<String>asList(SIMPLE_FIELDS));

    public static void main(String[] args) throws Exception {
        org.apache.lucene.store.RAMDirectory directory 
            = new org.apache.lucene.store.RAMDirectory();
        
        // org.apache.lucene.analysis.SimpleAnalyzer analyzer 
        // = new org.apache.lucene.analysis.SimpleAnalyzer();
        // org.apache.lucene.analysis.KeywordAnalyzer analyzer 
        // = new org.apache.lucene.analysis.KeywordAnalyzer();
        MedlineCodec codec = new MedlineCodec();
        LuceneAnalyzer analyzer = codec.getAnalyzer();
        org.apache.lucene.index.IndexWriter writer 
            = new org.apache.lucene.index.IndexWriter(directory,analyzer);
        Document doc = new Document();
        doc.add(new Field(MESH_MINOR_FIELD,"abc",
                          Field.Store.NO,
                          Field.Index.ANALYZED));
        doc.add(new Field(MESH_MINOR_FIELD," xyz efg",
                          Field.Store.NO,
                          Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();
        org.apache.lucene.search.IndexSearcher searcher 
            = new org.apache.lucene.search.IndexSearcher(directory);

        org.apache.lucene.queryParser.QueryParser qp
            = new org.apache.lucene.queryParser.QueryParser("foo",analyzer);
        org.apache.lucene.search.Query query
            = qp.parse(MESH_MINOR_FIELD + ":efg");

        org.apache.lucene.search.Hits hits = searcher.search(query);
        System.out.println("hits.length()=" + hits.length());


        org.apache.lucene.analysis.TokenStream ts = analyzer.tokenStream(MESH_MINOR_FIELD,new java.io.StringReader("abc xyz efg"));
        org.apache.lucene.analysis.Token token;
        while ((token = ts.next()) != null)
            System.out.println("token=|" + token.termText() + "|"
                               + " startOffset=" + token.startOffset()
                               + " endOffset=" + token.endOffset() 
                               + " positionIncr=" + token.getPositionIncrement());
    }

}
