import com.aliasi.io.FileLineReader;

import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.*;

class FragmentsIo {

    void foo() throws IOException {
        File file = null;

        /*x FragmentsIo.1 */
        InputStream in = new FileInputStream(file);
        Reader reader = new InputStreamReader(in,Strings.UTF8);
        BufferedReader bufReader = new BufferedReader(reader);
        /*x*/

        /*x FragmentsIo.2 */
        OutputStream out = new FileOutputStream(file);
        Writer writer = new OutputStreamWriter(out,Strings.UTF8);
        BufferedWriter bufWriter = new BufferedWriter(writer);
        /*x*/
    }

    void bar() throws IOException {
        /*x FragmentsIo.3 */
        new File("c:\\lpb\\Foo").getCanonicalPath()
        /*x*/
            ;

        /*x FragmentsIo.4 */
        new File("foo").getCanonicalPath();
        /*x*/
            ;
    }

    void baz() {
        File file = null;
        InputStream in = null;
        /*x FragmentsIo.5*/
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) { 
            // do something
        } catch (IOException e) {
            // do something
        }
        /*x*/
        
    }

    void biz() throws IOException {
        /*x FragmentsIo.6 */
        InputStream in = null;
        File file = null;
        try {
            in = new FileInputStream(file);
            // do something
        } finally {
            if (in != null)
                in.close();
        }
        /*x*/
    }

    void boz() {
        InputStream in = null;
        /*x FragmentsIo.7 */
        try {
            in.close();
        } catch (IOException e) {
            /* ignore exception */
        }
        /*x*/
    }

    /*x FragmentsIo.8 */
    static void close(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException e) {
            /* ignore exception */
        }
    }
    /*x*/

    void buz() throws IOException {
        String encoding = null;
        InputStream in = null;
        /*x FragmentsIo.9 */
        Reader reader = new InputStreamReader(in,encoding);
        // do something
        reader.close();
        /*x*/
    }

    void byz() throws IOException {
        File file = null;
        /*x FragmentsIo.10 */
        InputStream in = new FileInputStream(file);
        InputStream bufIn = new BufferedInputStream(in);
        /*x*/
    }

    void a1() throws IOException {
        /*x FragmentsIo.11 */
        System.setOut(new PrintStream(System.out,true,Strings.UTF8));
        /*x*/

        /*x FragmentsIo.12 */
        System.setErr(new PrintStream("stdout.utf8.txt","UTF-16BE"));
        /*x*/
    }

    void a2() throws IOException {
        File file = null;
        /*x FragmentsIo.13 */
        PrintStream out 
            = (file != null)
            ? new PrintStream(new FileOutputStream(file))
            : System.out;
        /*x*/
    }


    void a3() throws IOException {
        File file = null;
        /*x FragmentsIo.14 */
        FileLineReader lines = new FileLineReader(file,"UTF-8");
        for (String line : lines) {
            // do something
        }
        lines.close();
        /*x*/
    }

    void a4() throws IOException {
        File fileIn = null;
        String encIn = null;
        File fileOut = null;
        String encOut = null;
        /*x FragmentsIo.15 */
        char[] cs = Files.readCharsFromFile(fileIn,encIn);
        Files.writeCharsToFile(cs,fileOut,encOut);
        /*x*/
    }


    void a5() throws IOException {
        /*x FragmentsIo.16 */

        /*x*/
    }

}