package com.aliasi.annotate.corpora;

import com.aliasi.tokenizer.RegExTokenizerFactory;

import java.util.regex.Pattern;

/**
 * Regex tokenizer factory with following disjuncts:
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><th>Regular Expression</th><th>Description</th></tr>
 * <tr><td><data>\d+</data></td>
       <td>Sequence of digits</td></tr>
 * <tr><td><data>\p{Lu}(\p{Lu}+|[\p{L}&&[^\p{Lu}]]*)</data></td>
       <td>Upper case letter followed by sequence of upper case, or sequence of lower case.</td></tr>
 * <tr><td><data>[\p{L}&&[^\p{Lu}]]+</data></td>
       <td>Sequence of lowercase letters.</td></tr>
 * <tr><td><data>\S</data></td>
       <td>Any other non whitespace char is its own token.</td></tr>
 * </table>
 * </blockquote>

 */

public class NoisyTokenizerFactory extends RegExTokenizerFactory {

    public NoisyTokenizerFactory() {
	super(regex(),Pattern.DOTALL | Pattern.MULTILINE); // Pattern.MULTILINE | Pattern.UNICODE_CASE); // | Pattern.UNICODE_CASE | Pattern.DOTALL);
    }

    static String regex() {
	System.out.println("Trying to construct.");
	return "("
	    + "(\\d+)"
	    + "|" + "(\\p{Upper}(\\p{Lower}+|\\p{Upper}+))"
	    + "|" + "(\\p{Upper})"
	    + "|" + "(\\p{Lower}+)"
	    + "|" + "([\\S])"
	    + ")"
	    ;
	

	    /*
	    + "|" + "(\\p{Lu}((([\\p{L}&&[^\\p{Lu}]])+)|((\\p{Lu})+)))"
	    + "|" + "([\\p{L}&&[^\\p{Lu}]]+)"
	    + "|" + "(\\p{Lu})"
	    + "|" + "([\\p{Punct}])"
	    + "|" + "([\\S])"
	    + ")"
	    ;
	    */
    }

    


}



