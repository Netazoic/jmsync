package com.netazoic.netamtp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/*
 * From Christian Fries
 * http://stackoverflow.com/questions/18520972/converting-java-file-url-to-file-path-platform-independent-including-u
 */

public class UNCPathTest {

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
        UNCPathTest upt = new UNCPathTest();

        //upt.testURL("file://Virgil/bin");  // Windows UNC Path
        //upt.testURL("file://Virgil/");
        //upt.testURL("file://Virgil/C$");
        //upt.testURL("file://Virgil\\XT907");
        upt.testURL("file://XT907");
        //upt.testURL("file://Virgil/XT907");
        //upt.testURL("file:////server/XT907/storage/sdcard1");  // Windows UNC Path
        //upt.testURL("file://server/dir/file.txt");  // Windows UNC Path

        //upt.testURL("file:///Z:/dir/file.txt");     // Windows drive letter path

        //upt.testURL("file:///dir/file.txt");        // Unix (absolute) path
    }

    private void testURL(String urlString) throws MalformedURLException, URISyntaxException {
        URL url = new URL(urlString);
        System.out.println("URL is: " + url.toString());

        URI uri = url.toURI();
        System.out.println("URI is: " + uri.toString());

        if(uri.getAuthority() != null && uri.getAuthority().length() > 0) {
            // Hack for UNC Path
            uri = (new URL("file://" + urlString.substring("file:".length()))).toURI();
        }

        File file = new File(uri);
        System.out.println("File is: " + file.toString());
        File[] fCol = file.listFiles();
        if(fCol!=null){
        for(File ff : fCol){
        	System.out.println(ff.getName());
        }
        }

        String parent = file.getParent();
        System.out.println("Parent is: " + parent);
        File p = new File(parent);
        fCol = p.listFiles();

        System.out.println("____________________________________________________________");
    }
}