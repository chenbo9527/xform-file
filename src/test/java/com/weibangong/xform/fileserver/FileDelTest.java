package com.weibangong.xform.fileserver;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by chenbo on 16/3/4.
 */
public class FileDelTest {
    private static Logger logger = LoggerFactory.getLogger(FileDelTest.class);
    public static void main(String[] args) throws URISyntaxException, IOException {
        URI uri = new URIBuilder("http://localhost:12306/delete")
                .addParameter("filename", "/image/M00/00/00/wKhjZFbZXIKATZvvAHoq2ghzN7I179.log".replaceFirst("^/", ""))
                .build();

        HttpPost post = new HttpPost(uri);
        ClientConnectionPool clientConnectionPool = new ClientConnectionPool();
        String result = clientConnectionPool.execute(post);

       // System.out.println("/image/M00/00/00/wKhjZFbZXIKATZvvAHoq2ghzN7I179.log".replaceFirst("^/", ""));
    }
}
