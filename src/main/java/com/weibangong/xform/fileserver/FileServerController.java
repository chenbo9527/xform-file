package com.weibangong.xform.fileserver;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenbo on 16/3/9.
 */
@Controller
@RequestMapping("/file")
public class FileServerController {
    private Logger logger = LoggerFactory.getLogger(FileServerController.class);

    @Autowired
    private FastdfsFileService fileService;

    @Autowired
    private ServletFileUpload upload;

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    @ResponseBody
    public Response uploadFile(HttpServletRequest req) {
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
                return new RespBuilder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg("解析multipart请求失败").build();
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
                    try {
                        inputStream = item.getInputStream();
                    } catch (IOException e) {
                        logger.error("从multipart中获取文件流失败", e);
                        return new RespBuilder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg("从multipart中获取文件流失败").build();
                    }
                }
            }

        } else {//客户端直接用binary的形式上传文件
            try {
                inputStream = req.getInputStream();
            } catch (IOException e) {
                logger.error("从binary中获取文件里失败", e);
                return new RespBuilder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg("从binary中获取文件里失败").build();
            }
            fileName = req.getParameter("fileName");
            watermark = req.getParameter("watermark");
            base64 = Boolean.parseBoolean(req.getParameter("base64"));
            timeStamp = req.getParameter("timeStamp") != null ? Long.parseLong(req.getParameter("timeStamp")) : 0;
            locationShot = req.getParameter("locationShot") != null ? Integer.parseInt(req.getParameter("locationShot")) : 0;
        }

        if(fileName == null || fileName.equals("")){
            return new RespBuilder().status(HttpStatus.BAD_REQUEST.value()).msg("fileName不能为空").build();
        }

        try{
            //所有文件信息获取完再上传
            url = fileService.saveFile(fileName, watermark, timeStamp, locationShot, inputStream, base64);
            return new RespBuilder().status(HttpStatus.OK.value()).setData("url", url).build();
        }catch (FileServerException e){
            return new RespBuilder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg(e.getMessage()).build();
        }

    }

    @RequestMapping(method = RequestMethod.GET, value = "/delete")
    @ResponseBody
    public Response deleteFile(@RequestParam(value = "fileName", required = true) String fileName){
        if(fileName == null || fileName.equals("")){
            return new RespBuilder().status(HttpStatus.BAD_REQUEST.value()).msg("fileName不能为空").build();
        }

        try {
            fileService.deleteFile(fileName);
        } catch (FileServerException e) {
            return new RespBuilder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg(e.getMessage()).build();
        }

        return new RespBuilder().status(200).build();
    }
}
