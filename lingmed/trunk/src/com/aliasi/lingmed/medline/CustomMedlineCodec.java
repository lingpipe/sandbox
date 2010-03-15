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

import com.aliasi.lingmed.parser.Abstract;
import com.aliasi.lingmed.parser.Article;
import com.aliasi.lingmed.parser.Author;
import com.aliasi.lingmed.parser.AuthorList;
import com.aliasi.lingmed.parser.Journal;
import com.aliasi.lingmed.parser.JournalInfo;
import com.aliasi.lingmed.parser.JournalIssue;
import com.aliasi.lingmed.parser.MedlineCitation;
import com.aliasi.lingmed.parser.MeshHeading;
import com.aliasi.lingmed.parser.Name;
import com.aliasi.lingmed.parser.PubDate;
import com.aliasi.lingmed.parser.Topic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.log4j.Logger;


/**
 * A <code>CustomMedlineCodec</code> provides conversion between
 * MEDLINE citations and Lucene documents with some set of
 * searchable fields.
 *
 * <p>Conversion of the Lucene document to a citation is carried
 * out by the superclass {@link MedlineCodec}.
 * 
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */
public class CustomMedlineCodec extends MedlineCodec {

    private final Logger mLogger
        = Logger.getLogger(CustomMedlineCodec.class);


    public CustomMedlineCodec() { 
    }

    public Document toDocument(MedlineCitation citation) {
     
        if (mLogger.isDebugEnabled())
            mLogger.debug("toDocument(" + citation.pmid() + ")");

        // get doc with its basic fields
        Document doc = super.toDocument(citation);

        Article article = citation.article();

        String title = article.articleTitleText();
        add(doc,SearchableMedlineCodec.TITLE_FIELD,title);

        Abstract abstr = article.abstrct();
        if (abstr != null) {
            String abstractText = abstr.textWithoutTruncationMarker();
            add(doc,SearchableMedlineCodec.ABSTRACT_FIELD,abstractText);
        }

        PubDate date = article.journal().journalIssue().pubDate();
        if (date.isStructured()) {
            String year = date.year();
            add(doc,SearchableMedlineCodec.DATE_YEAR_FIELD,year);
            String season = date.season();
            add(doc,SearchableMedlineCodec.DATE_SEASON_FIELD,season);
            String month = date.month();
            add(doc,SearchableMedlineCodec.DATE_MONTH_FIELD,month);
            String day = date.day();
            add(doc,SearchableMedlineCodec.DATE_DAY_FIELD,day);
        } else {  // date unstructured
            String plainStringDate = date.toPlainString();
            String year = extractYear(plainStringDate);
            add(doc,SearchableMedlineCodec.DATE_YEAR_FIELD,year);
        }
        
        // headings = descriptor + qualifiers
        MeshHeading[] meshHeadings = citation.meshHeadings();
        for (MeshHeading heading : meshHeadings) {
            Topic[] topics = heading.topics();
            for (Topic topic : topics) {
                String topicName = topic.topic();
                String field = topic.isMajor() 
                    ? SearchableMedlineCodec.MESH_MAJOR_FIELD 
                    : SearchableMedlineCodec.MESH_MINOR_FIELD;
                add(doc,field,topicName);
            }
        }

        return doc;
    }

    // copied from SearchableMedlineCodec
    static void add(Document doc, String fieldName, String text) {
        if (text == null || text.length() == 0) return;
        boolean appendToExisting = doc.getField(fieldName) != null;
        if (appendToExisting)
            text = " , " + text;
        if (SearchableMedlineCodec.TEXT_FIELD_SET.contains(fieldName)) {
            SearchableMedlineCodec.addTextField(doc,
                                                fieldName+SearchableMedlineCodec.EXACT_FIELD_SUFFIX,
                                                text);
            SearchableMedlineCodec.addTextField(doc,
                                                fieldName+SearchableMedlineCodec.NGRAM_FIELD_SUFFIX,
                                                text);
        } else if (SearchableMedlineCodec.SIMPLE_FIELD_SET.contains(fieldName)) {
            SearchableMedlineCodec.addTextField(doc,fieldName,text);
        } else {
            SearchableMedlineCodec.addKeywordField(doc,fieldName,text);
        }
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

}
