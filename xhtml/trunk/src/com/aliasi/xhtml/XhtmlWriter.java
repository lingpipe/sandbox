package com.aliasi.xhtml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An <code>XhtmlWriter</code> writes an XHTML document to an output
 * stream using a specified character set.  
 *
 * <p><b>Headers</b>
 * 
 * <p>The writer starts the output with the XML 1.0 declaration
 * with a specified character set; for UTF-8, this looks like:
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;</pre>
 *
 * This is followed by the XHTML strict document type:
 * declaration:
 *
 * <pre>
 * &lt;!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;</pre>
 * 
 * <p><b>Character Sets</b>
 *
 * <p>A character set must be specified when the writer is
 * constructed.  This character set is used for encoding character
 * content and attributes.  If the character set does not support a
 * character that is found, it will write a question mark
 * (<code>?</code>).  The UTF-8 and UTF-16 character sets are general
 * enough to print out arbitrary unicode characters; UTF-8 is more
 * compact if the characters are mostly ASCII, and UTF-16 is more
 * compact otherwise.  
 *
 * <p>If this class is used to write XHTML to a servlet, the character
 * set should match that specified by the servlet response method
 * <code>javax.servlet.ServletResponse.setCharacterEncoding(String)</code>.
 *
 * <p>Characters in attributes and text content are escaped as
 * entities where necessary.  For XHTML, the characters which must be
 * escaped are less-than (<code>&lt;</code>), greater-than
 * (<code>&gt;</code>), double quote (<code>&quot;</code>) and
 * ampersand (<code>&amp;</code>.
 *
 * <p><b>Closing Streams</b>
 *
 * <p>The {@link #endDocument()} method flushes the underlying print
 * writer, but does not close it.  It is the responsibility of any
 * client programs to close their streams.  For instance, writing to
 * a file should take the following defensive step to ensure file
 * references are not left open:
 *
 * <pre>
 * Document doc = ...;
 * FileOutputStream out = null;
 * try {
 *     out = new FileOutputStream(file);
 *     XhtmlWriter writer = new XhtmlWriter(out,"UTF-8");
 *     doc.writeTo(writer);
 * } finally {
 *    if (out != null) 
 *         try { out.close(); } 
 *         catch (IOException e) {  } // no recovery possible
 * }</pre>
 *
 * <p><b>Print Format</b>
 * 
 * <p>All elements are printed in full open/close form,
 * e.g. <code>&lt;br&gt;&lt;/br&gt;</code>.  
 *
 * <p><b>Name Spaces</b>
 * 
 * <p>Only qualified names are printed for elements and
 * attributes.  Any namespace declarations must be made
 * explicitly through attributes and all tags and attributes
 * must be provided in qualified form.
 *
 * <p><b>Thread Safety and Reuse</b>
 *
 * <p>Like the streams in <code>java.io</code>, instances
 * are meant for one-time use.  The methods in this class are
 * <b>not</b> in any way synchronized on the underlying stream.
 *
 * @author Bob Carpenter
 * @version 1.0
 */
public class XhtmlWriter extends DefaultHandler {

    private final PrintWriter mPrinter;
    private final String mCharsetName;

    /**
     * Construct an XHTML writer which writes to the specified
     * output stream using the specified character set to encode
     * characters.
     *
     * <p>See {@link #checkError()} to check for I/O errors.
     *
     * @param out Output stream to which XHTML is written.
     * @param charsetName Name of character set.
     * @throws UnsupportedEncodingException If the character set is
     * not supported by the virtual machine (JVM) configuration.
     */
    public XhtmlWriter(OutputStream out, String charsetName) 
        throws UnsupportedEncodingException {

        this(new OutputStreamWriter(out,charsetName),charsetName);
    }

    private XhtmlWriter(Writer writer, String charsetName) {
        mPrinter = new PrintWriter(writer);
        mCharsetName = charsetName;
    }

    /**
     * Returns the name of the character set used by this
     * writer.
     *
     * @return The name of the character set.
     */
    public String charsetName() {
        return mCharsetName;
    }

    /**
     * Returns <code>true</code> if there has been an I/O error during
     * printing.  This is simply the value of the underlying print
     * writer's {@link PrintWriter#checkError()} flag; see that method's
     * documentation for more information.
     *
     * @return <code>true</code> if there was an I/O error printing.
     */
    public boolean checkError() {
        return mPrinter.checkError();
    }

    /**
     * End the document, flushing any prints that have been buffered
     * by this writer.
     *
     * <p>See {@link #checkError()} to check for I/O errors.
     */
    public void endDocument() {
        mPrinter.flush();
    }

    /**
     * Prints the XML declaration and document type declaration to
     * the underlying printer.
     *
     * <p>See {@link #checkError()} to check for I/O errors.
     */
    public void startDocument() {
        // print XML declaration
        print("<?xml");
        printAttribute("version","1.0");
        printAttribute("encoding",mCharsetName);
        print("?>\n");

        // print DTD
        print(Document.DOCTYPE);
        print('\n');
    }

    /**
     * Prints the characters, escaping to entities where necessary.
     *
     * <p>See {@link #checkError()} to check for I/O errors.
     *
     * @param cs Underlying character array.
     * @param start Index of first character to print.
     * @param length Number of characters to print.
     *
     * <p>See {@link #checkError()} to check for I/O errors.
     */
    public void characters(char[] cs, int start, int length) {
        printEscaped(cs,start,length);
    }

    /**
     * Print the start element for the specified qualified name
     * and attributes; the namespace URI and base name are ignored.
     * 
     * <p>See {@link #checkError()} to check for I/O errors.
     *
     * @param uri Namespace URI (ignored).
     * @param name Base name (ignored).
     * @param qName Qualified name to print.
     * @param atts Attributes to print.
     */
    public void startElement(String uri, String name, String qName,
                             Attributes atts) {
        print('<');
        printEscaped(qName);
        for (int i = 0; i < atts.getLength(); ++i)
            printAttribute(atts.getQName(i), atts.getValue(i));
        print('>');
    }

    /**
     * Prints the end element for the specified qualified name;
     * the namespace URI and base name are ignored.
     *
     * <p>See {@link #checkError()} to check for I/O errors.
     *
     * @param uri Namespace URI (ignored).
     * @param name Base name (ignored).
     * @param qName Qualified name to print.
     */
    public void endElement(String uri, String name, String qName) {
        print("</");
        printEscaped(qName);
        print('>');
    }

    private void print(char c) {
        mPrinter.print(c);
    }

    private void print(String s) {
        mPrinter.print(s);
    }

    private void printAttribute(String att, String val) {
            print(' ');
            printEscaped(att);
            print('=');
            print('"');
            printEscaped(val);
            print('"');
    }

    private void printEscaped(String s) {
        for (int i = 0; i < s.length(); ++i)
            printEscaped(s.charAt(i));
    }

    private void printEscaped(char[] ch, int start, int length) {
        for (int i = start; i < start+length; ++i)
            printEscaped(ch[i]);
    }

    private void printEscaped(char c) {
        switch (c) {
        case '<':  { mPrinter.print("&lt;"); break; }
        case '>':  { mPrinter.print("&gt;"); break; }
        case '&':  { mPrinter.print("&amp;"); break; }
        case '"':  { mPrinter.print("&quot;"); break; }
        default:   { mPrinter.print(c); }
        }
    }

}
