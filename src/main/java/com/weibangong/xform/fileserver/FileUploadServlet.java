package com.weibangong.xform.fileserver;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.csource.common.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Created by guanxinquan on 16/2/25.
 */
public class FileUploadServlet extends HttpServlet {

    private FastdfsFileService fileService;

    private static final Integer MAX_MEMORY_SIZE = 1024 * 1024 * 10;//10M

    private static final Integer MAX_SIZE = 1024 * 1024 * 20;//20M

    private ServletFileUpload upload;

    private static final Logger logger = LoggerFactory.getLogger(FileUploadServlet.class);

    private static final DateFormat format = new SimpleDateFormat("yy年MM月dd日 HH:mm:ss");


    @Override
    public void init() throws ServletException {
        super.init();
        try {
            fileService = new FastdfsFileService();
            DiskFileItemFactory factory = new DiskFileItemFactory();

            factory.setSizeThreshold(MAX_MEMORY_SIZE);
            factory.setRepository((File) getServletContext().getAttribute("javax.servlet.context.tempdir"));
            upload = new ServletFileUpload(factory);
            upload.setSizeMax(MAX_SIZE);
        } catch (Exception e) {
            logger.error("start file service error", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.doPost(req, resp);
        String fileName = null; //文件名
        String watermark = null; //水印
        InputStream inputStream = null; //文件流
        boolean base64 = false; //是否采用base64上传
        long timeStamp = 0;
        int locationShot = 0;

        String url = null;

        //客户端用multipart的形式上传文件
        if (ServletFileUpload.isMultipartContent(req)) {
            List<FileItem> items = null;
            try {
                items = upload.parseRequest(req);
            } catch (FileUploadException e) {
                logger.error("解析multipart请求失败", e);
            }
            Iterator<FileItem> iter = items.iterator();

            while (iter.hasNext()) {
                FileItem item = iter.next();

                if (item.isFormField()) {
                    if (item.getFieldName() != null && item.getFieldName().equals("fileName")) {
                        //获取fileName
                        fileName = item.getString();
                    } else if (item.getFieldName() != null && item.getFieldName().equals("watermark")) {
                        //获取watermark
                        watermark = item.getString();
                    } else if (item.getFieldName() != null && item.getFieldName().equals("base64")) {
                        base64 = Boolean.parseBoolean(item.getString());
                    } else if (item.getFieldName() != null && item.getFieldName().equals("timeStamp")) {
                        timeStamp = item.getString() != null ? Long.parseLong(item.getString()) : 0;
                    } else if (item.getFieldName() != null && item.getFieldName().equals("locationShot")) {
                        locationShot = item.getString() != null ? Integer.parseInt(item.getString()) : 0;
                    }
                } else {
                    inputStream = item.getInputStream();
                }
            }

        } else {//客户端直接用binary的形式上传文件
            inputStream = req.getInputStream();
            fileName = req.getParameter("fileName");
            watermark = req.getParameter("watermark");
            base64 = Boolean.parseBoolean(req.getParameter("base64"));
            timeStamp = req.getParameter("timeStamp") != null ? Long.parseLong(req.getParameter("timeStamp")) : 0;
            locationShot = req.getParameter("locationShot") != null ? Integer.parseInt(req.getParameter("locationShot")) : 0;
        }
        //所有文件信息获取完再上传
        url = fileService.saveFile(fileName, watermark, timeStamp, locationShot, inputStream, base64);

        resp.getOutputStream().write(url.getBytes());
        resp.getOutputStream().close();

    }
}
