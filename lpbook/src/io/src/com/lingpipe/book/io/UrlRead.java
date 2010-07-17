package com.lingpipe.book.io;

import java.io.InputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlRead {

    public static void main(String[] args) 
        throws MalformedURLException, IOException {

        String url = args[0];
        /*x ReadUrl.1 */
        URL u = new URL(url);
        InputStream in = u.openStream();

        byte[] buf = new byte[8096];
        int n;
        while ((n = in.read(buf)) >= 0)
            System.out.write(buf,0,n);

        in.close();
        /*x*/
    }

}