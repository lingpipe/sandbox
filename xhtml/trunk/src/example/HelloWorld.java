package example;

import com.aliasi.xhtml.Body;
import com.aliasi.xhtml.Document;
import com.aliasi.xhtml.Html;
import com.aliasi.xhtml.Head;
import com.aliasi.xhtml.Title;

import com.aliasi.xml.SAXWriter;

public class HelloWorld {

    static boolean XHTML_MODE = true;

    public static void main(String[] args) throws Exception {
	Title title = new Title("Hello World");
	Head head = new Head(title);
	Body body = new Body();
	Html html = new Html(head,body);        
	Document document = new Document(html);

	SAXWriter writer = new SAXWriter(System.out,"ISO-8859-1",XHTML_MODE);
	writer.setDTDString(Document.DOCTYPE);
	document.writeTo(writer);
    }

}