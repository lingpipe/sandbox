package com.aliasi.xhtml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

class AbstractElement extends AbstractContent implements Element {

    int mParentMask = 0;

    private final boolean mMutable;
    private final String mTag;
    final List mContentList;
    private final AttributesImpl mAttributes = new AttributesImpl();
    
    AbstractElement(String tag, boolean mutable) {
        this(tag,new ArrayList(),mutable);
    }

    AbstractElement(String tag, List contentList, boolean mutable) {
        mTag = tag;
        mContentList = contentList;
        mMutable = mutable;
    }

    public String tag() {
        return mTag;
    }

    public List contents() {
        return Collections.unmodifiableList(contentsInternal());
    }

    List contentsInternal() {
        return mContentList;
    }
                       
    public Attributes attributes() {
        return new UnmodifiableAttributes(mAttributes);
    }

    public final void writeTo(ContentHandler handler) throws SAXException {
        handler.startElement(null,null,mTag,attributes());  
        List contentList = contentsInternal();              
        for (int i = 0; i < contentList.size(); ++i) {
            Content content = (Content) contentList.get(i);
            content.writeTo(handler);
        }
        handler.endElement(null,null,mTag);
    }



    final void addContent(Element content) {
        throwUnsupportedExceptionIfNotMutable();
        mContentList.add(content); // add before tests
        AbstractContent absContent = (AbstractContent) content;
        if (absContent.isCyclic(new HashSet())) {
            String msg = "Attempt to add element to itself.";
            throw new IllegalArgumentException(msg);
        }
        absContent.propagateMask(mParentMask); // must test after cycles!
    }

    final void addContent(CharSequence text) {
        throwUnsupportedExceptionIfNotMutable();
        mContentList.add(new TextContent(text));
    }


    final void set(String qName, String value) {
        set(qName,value,"CDATA");
    }

    final void set(String qName, String value, String type) {
        throwUnsupportedExceptionIfNotMutable();
        int existingIndex = mAttributes.getIndex(qName);
        if (existingIndex == -1)
            mAttributes.addAttribute(null,null,qName,type,value);
        else
            mAttributes.setAttribute(existingIndex,null,null,qName,type,value);
    }

    void throwUnsupportedExceptionIfNotMutable() {
        if (mMutable) return;
        String msg = "Attempt to modify constant.";
        throw new UnsupportedOperationException(msg);
    }

    boolean isCyclic(Set visitedSet) {
        if (!visitedSet.add(this)) return true;
        List contentList = contentsInternal();
        for (int i = 0; i < contentList.size(); ++i) {
            AbstractContent absContent 
                = (AbstractContent) contentList.get(i);
            if (absContent.isCyclic(visitedSet)) 
                return true;
        }
        return false;
    }

    void propagateMask(int mask) {
        if (prohibitedAncestor(mask)) {
            String msg = "Element with tag=" + mTag
                + " violates the element prohibition."
                + " Ancestors include=" + maskToString(mask);
            throw new IllegalArgumentException(msg);
        }
        mParentMask |= mask;
        List contentList = contentsInternal();
        for (int i = 0; i < contentList.size(); ++i) {
            AbstractContent content = (AbstractContent) contentList.get(i);
            content.propagateMask(mask);
        }
    }

    boolean prohibitedAncestor(int mask) {
        return false;
    }

    String maskToString(int mask) {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        if ((mask & A_MASK) != 0) sb.append(" a");
        if ((mask & PRE_MASK) != 0) sb.append(" pre");
        if ((mask & BUTTON_MASK) != 0) sb.append(" button");
        if ((mask & LABEL_MASK) != 0) sb.append(" label");
        if ((mask & FORM_MASK) != 0) sb.append(" form");
        sb.append(" }");
        return sb.toString();
    }
        
    void matches(StringBuffer sb, String elt, int mask1, int mask2) {
        sb.append(" ");
        sb.append(elt);
        sb.append(((mask1 & mask2) != 0) ? '+' : '-');
    }

    static final int A_MASK = 1;
    static final int PRE_MASK = 2;
    static final int BUTTON_MASK = 4;
    static final int LABEL_MASK = 8;
    static final int FORM_MASK = 16;

    static final String ABBR_ATT = "abbr";
    static final String ACCEPT = "accept";
    static final String ACCEPT_CHARSET = "accept-charset";
    static final String ACCESSKEY = "accesskey";
    static final String ACTION = "action";
    static final String ALIGN = "align";
    static final String ALT = "alt";
    static final String ARCHIVE = "archive";
    static final String AXIS = "axis";
    static final String BORDER = "border";
    static final String CELLSPACING = "cellspacing";
    static final String CELLPADDING = "cellpadding";
    static final String CHAR = "char";
    static final String CHAROFF = "charoff";
    static final String CHARSET = "charset";
    static final String CHECKED = "checked";
    static final String CITE_ATT = "cite";
    static final String CLASS = "class";
    static final String CLASSID = "classid";
    static final String CODEBASE = "codebase";
    static final String CODETYPE = "codetype";
    static final String COLS = "cols";
    static final String COLSPAN = "colspan";
    static final String CONTENT = "content";
    static final String COORDS = "coords";
    static final String DATA = "data";
    static final String DATETIME = "datetime";
    static final String DECLARE = "declare";
    static final String DEFER = "defer";
    static final String DIR_ATT = "dir";
    static final String DISABLED = "disabled";
    static final String ENCTYPE = "enctype";
    static final String FOR = "for";
    static final String FRAME_ATT = "frame";
    static final String HEADERS = "headers";
    static final String HEIGHT = "height";
    static final String HREF = "href";
    static final String HREFLANG = "hreflang";
    static final String HTTP_EQUIV = "http-equiv";
    static final String ID = "id";
    static final String ISMAP = "ismap";
    static final String LABEL_ATT = "label";
    static final String LANG = "lang";
    static final String LONGDESC = "longdesc";
    static final String MAXLENGTH = "maxlength";
    static final String MEDIA = "media";
    static final String METHOD = "method";
    static final String MULTIPLE = "multiple";
    static final String NAME = "name";
    static final String NOHREF = "nohref";
    static final String ONBLUR = "onblur";
    static final String ONCHANGE = "onchange";
    static final String ONCLICK = "onclick";
    static final String ONDBLCLICK = "ondblclick";
    static final String ONFOCUS = "onfocus";
    static final String ONKEYDOWN = "onkeydown";
    static final String ONKEYPRESS = "onkeypress";
    static final String ONKEYUP = "onkeyup";
    static final String ONLOAD = "onload";
    static final String ONMOUSEDOWN = "onmousedown";
    static final String ONMOUSEMOVE = "onmousemove";
    static final String ONMOUSEOUT = "onmouseout";
    static final String ONMOUSEOVER = "onmouseover";
    static final String ONMOUSEUP = "onmouseup";
    static final String ONRESET = "onreset";
    static final String ONSELECT = "onselect";
    static final String ONSUBMIT = "onsubmit";
    static final String ONUNLOAD = "onunload";
    static final String PROFILE = "profile";
    static final String READONLY = "readonly";
    static final String REL = "rel";
    static final String REV = "rev";
    static final String ROWS = "rows";
    static final String ROWSPAN = "rowspan";
    static final String RULES = "rules";
    static final String SCHEME = "scheme";
    static final String SCOPE = "scope";
    static final String SELECTED = "selected";
    static final String SHAPE = "shape";
    static final String SIZE = "size";
    static final String SPAN_ATT = "span";
    static final String SRC = "src";
    static final String STANDBY = "standby";
    static final String STYLE_ATT = "style";
    static final String SUMMARY = "summary";
    static final String TABINDEX = "tabindex";
    static final String TITLE_ATT = "title";
    static final String TYPE = "type";
    static final String USEMAP = "usemap";
    static final String VALIGN = "valign";
    static final String VALUE = "value";
    static final String VALUETYPE = "valuetype";
    static final String WIDTH = "width";
    static final String XML_LANG = "xml:lang";
    static final String XML_SPACE = "xml:space";
    static final String XMLNS = "xmlns";
    static final String A = "a";
    static final String ABBR = "abbr";
    static final String ACRONYM = "acronym";
    static final String ADDRESS = "address";
    static final String APPLET = "applet";
    static final String AREA = "area";
    static final String B = "b";
    static final String BASE = "base";
    static final String BASEFONT = "basefont";
    static final String BDO = "bdo";
    static final String BIG = "big";
    static final String BLOCKQUOTE = "blockquote";
    static final String BODY = "body";
    static final String BR = "br";
    static final String BUTTON = "button";
    static final String CAPTION = "caption";
    static final String CENTER = "center";
    static final String CITE = "cite";
    static final String CODE = "code";
    static final String COL = "col";
    static final String COLGROUP = "colgroup";
    static final String DD = "dd";
    static final String DEL = "del";
    static final String DFN = "dfn";
    static final String DIR = "dir";
    static final String DIV = "div";
    static final String DL = "dl";
    static final String DT = "dt";
    static final String EM = "em";
    static final String FIELDSET = "fieldset";
    static final String FONT = "font";
    static final String FORM = "form";
    static final String FRAME = "frame";
    static final String FRAMESET = "frameset";
    static final String HEAD = "head";
    static final String H1 = "h1";
    static final String H2 = "h2";
    static final String H3 = "h3";
    static final String H4 = "h4";
    static final String H5 = "h5";
    static final String H6 = "h6";
    static final String HR = "hr";
    static final String HTML = "html";
    static final String I = "i";
    static final String IFRAME = "iframe";
    static final String IMG = "img";
    static final String INPUT = "input";
    static final String INS = "ins";
    static final String KBD = "kbd";
    static final String LABEL = "label";
    static final String LEGEND = "legend";
    static final String LI = "li";
    static final String LINK = "link";
    static final String MAP = "map";
    static final String MENU = "menu";
    static final String META = "meta";
    static final String NOFRAMES = "noframes";
    static final String NOSCRIPT = "noscript";
    static final String OBJECT = "object";
    static final String OL = "ol";
    static final String OPTGROUP = "optgroup";
    static final String OPTION = "option";
    static final String P = "p";
    static final String PARAM = "param";
    static final String PRE = "pre";
    static final String Q = "q";
    static final String S = "s";
    static final String SAMP = "samp";
    static final String SCRIPT = "script";
    static final String SELECT = "select";
    static final String SMALL = "small";
    static final String SPAN = "span";
    static final String STRIKE = "strike";
    static final String STRONG = "strong";
    static final String STYLE = "style";
    static final String SUB = "sub";
    static final String SUP = "sup";
    static final String TABLE = "table";
    static final String TBODY = "tbody";
    static final String TD = "td";
    static final String TEXTAREA = "textarea";
    static final String TFOOT = "tfoot";
    static final String TH = "th";
    static final String THEAD = "thead";
    static final String TITLE = "title";
    static final String TR = "tr";
    static final String TT = "tt";
    static final String U = "u";
    static final String UL = "ul";
    static final String VAR = "var";

    static final class UnmodifiableAttributes implements Attributes {
        private final Attributes mAtts;
        UnmodifiableAttributes(Attributes atts) {
            mAtts = atts;
        }
        public int getIndex(String qName) {
            return mAtts.getIndex(qName);
        }
        public int getIndex(String uri, String localName) {
            return mAtts.getIndex(uri,localName);
        }
        public int getLength() {
            return mAtts.getLength();
        }
        public String getLocalName(int index) {
            return mAtts.getLocalName(index);
        }
        public String getQName(int index) {
            return mAtts.getQName(index);
        }
        public String getType(int index) {
            return mAtts.getType(index);
        }
        public String getType(String qName) {
            return mAtts.getType(qName);
        }
        public String getType(String uri, String localName) {
            return mAtts.getType(uri,localName);
        }
        public String getURI(int index) {
            return mAtts.getURI(index);
        }
        public String getValue(int index) {
            return mAtts.getValue(index);
        }
        public String getValue(String qName) {
            return mAtts.getValue(qName);
        }
        public String getValue(String uri, String localName) {
            return mAtts.getValue(uri,localName);
        }
        public int hashCode() {
            return mAtts.hashCode();
        }
        public boolean equals(Object that) {
            return mAtts.equals(that);
        }
    }


}
