package example;

import com.aliasi.xhtml.A;
import com.aliasi.xhtml.B;
import com.aliasi.xhtml.I;

public class Exceptions {

    public static void main(String[] args) {

	// cyclicity b(1) contains b(1)
	try { 
	    B b = new B();
	    b.add(b);
	} catch (IllegalArgumentException e) {
	    System.out.println();
	    e.printStackTrace(System.out);
	}

	// cyclicity b(1) contains i contains b(1)
	try { 
	    B b = new B();
	    I i = new I();
	    i.add(b);
	    b.add(i);
	} catch (IllegalArgumentException e) {
	    System.out.println();
	    e.printStackTrace(System.out);
	}

	// prohibition
	try {
	    A a1 = new A();
	    // a1.setHref("http://hello.world.com");
	    B b = new B();
	    a1.add(b);
	    A a = new A();
	    // a.setHref("http://goodbye.world.com");
	    b.add(a);
	} catch (IllegalArgumentException e) {
	    System.out.println();
	    e.printStackTrace(System.out);
	}

	    
    }

}