package biocreative;

import com.aliasi.ne.TagParserSAXSpansHandler;

import com.aliasi.tokenizer.IndoEuropeanTokenizer;
import com.aliasi.tokenizer.Tokenizer;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Implements the parser for the GENIA corpus.  GENIA labels dozens of
 * categories in the genomics and proteomics domain.  To download this
 * corpus, see the <a
 * href="http://www-tsujii.is.s.u-tokyo.ac.jp/GENIA/">GENIA Project
 * Home Page</a>.
 *
 * <p>This span parser picks out the <code>sentence</code> element for
 * text and converts the tags labeled by entity <code>cons</code> into
 * the value of their <code >sem</code> attribute normalized to remove
 * the <code>G#</code> prefix.
 * </p>
 *
 * @author  Bob Carpenter
 * @version 1.000
 * @since   LingPipe1.0
 */
public class GeniaHandler extends TagParserSAXSpansHandler {

    /**
     * Construct a tag parser for the Genia named-entity format.
     */
    public GeniaHandler() {
        super();
    }

    /**
     * Returns <code>true</code> if the specified name and attributes
     * begin an element with relevant text content.  In this case, the
     * qualified name must be equal to {@link #START_TEXT_ELEMENT}.
     *
     * @param qName Qualified name of element.
     * @param atts Attributes of element.
     * @return <code>true</code> if the element's text content is
     * relevant.
     */
    protected boolean isStartTextElement(String qName,
                                         Attributes atts) {
        return qName.equals(START_TEXT_ELEMENT);
    }

    /**
     * Determines if the element with the specified qualified name and
     * attributes is a span for a tag.  For this object, a start tag
     * element is one with the qualified name <code>"cons"</code> and
     * one of the recognized tags.
     *
     * @param qName Qualified name of element to test.
     * @param atts Attributes of element to test.
     * @return <code>true</code> if the element's text content should
     * be tagged as a span.
     */
    protected boolean isStartTagElement(String qName,
                                        Attributes atts) {
        return qName.equals(CONSTRUCT_ELEMENT)
            && SEM_MAP.containsKey(atts.getValue(SEMANTIC_TYPE_ATTRIBUTE));
    }

    /**
     * Converts the element with the specified qualified name and
     * attributes into a tag for a span.  In this case, it returns the
     * value of the <code>"TYPE"</code> attribute of the element.
     *
     * @param qName Qualified name of element to test.
     * @param atts Attributes of element to test.
     * @return The tag represented by the specified element.
     */
    protected String startTagElementToTag(String qName,
                                          Attributes atts) {
        return SEM_MAP.get(atts.getValue(SEMANTIC_TYPE_ATTRIBUTE)).toString();
    }

    /**
     * Creates a tokenizer from the specified string buffer.  This
     * tokenizer is used to tokenize the relevant text.  In this case,
     * a fresh {@link IndoEuropeanTokenizer} is created from the
     * specified buffer.
     *
     * @param sb String buffer to tokenize.
     * @return Tokenizer for specified string buffer.
     */
    protected Tokenizer createTokenizer(StringBuffer sb) {
        return new IndoEuropeanTokenizer(sb);
    }

    /**
     * The element whose text content will be processed.
     */
    public static final String START_TEXT_ELEMENT = "sentence";

    /**
     * The element used for named entities.
     */
    public static final String CONSTRUCT_ELEMENT = "cons";

    /**
     * The attribute used to indicate the type of a named entity.
     */
    public static final String SEMANTIC_TYPE_ATTRIBUTE = "sem";


    /**
     * Mapping from GENIA semantic categories to their canonical
     * names.  This is easily modified at the source level to only
     * pick out a subset of tags.
     */
    private static final HashMap SEM_MAP = new HashMap();
    static {
        // SEM_MAP.put("G#protein_N/A","GENE");
        // SEM_MAP.put("G#protein_complex","GENE");
        SEM_MAP.put("G#protein_domain_or_region","GENE");
        SEM_MAP.put("G#protein_family_or_group","GENE");
        SEM_MAP.put("G#protein_molecule","GENE");
        SEM_MAP.put("G#protein_substructure","GENE");
        // SEM_MAP.put("G#protein_subunit","GENE");

        // SEM_MAP.put("G#DNA_N/A","GENE");
        SEM_MAP.put("G#DNA_domain_or_region","GENE");
        SEM_MAP.put("G#DNA_family_or_group","GENE");
        SEM_MAP.put("G#DNA_molecule","GENE");
        SEM_MAP.put("G#DNA_substructure","GENE");

        // SEM_MAP.put("G#RNA_N/A","GENE");
        SEM_MAP.put("G#RNA_domain_or_region","GENE");
        SEM_MAP.put("G#RNA_family_or_group","GENE");
        SEM_MAP.put("G#RNA_molecule","GENE");
        SEM_MAP.put("G#RNA_substructure","GENE");
    }

}
