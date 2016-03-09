package com.weibangong.xform.fileserver;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * Created by guanxinquan on 16/2/25.
 */
public class FileServerTest {

    public static void main(String[] args) throws IOException, URISyntaxException {

        URI uri = new URIBuilder("http://localhost:12306/upload")
//                .addParameter("watermark","张三 2016-03-08 15:36")
                .addParameter("fileName","test.png")
                .addParameter("timStamp",new Date().getTime() + "")
                .build();

        HttpPost post = new HttpPost(uri);
        FileInputStream file = new FileInputStream("/Users/haizhi/Desktop/test.png");
        InputStreamEntity inputStreamEntity = new InputStreamEntity(file,ContentType.APPLICATION_OCTET_STREAM);
        post.setEntity(inputStreamEntity);

        ClientConnectionPool clientConnectionPool = new ClientConnectionPool();
        String result = clientConnectionPool.execute(post);

        System.out.println(result);


    }


}
