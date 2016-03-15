package com.weibangong.xform.fileserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    @ResponseBody
    public Response uploadFile(@RequestParam(value = "fileName", required = true) String fileName,
                               @RequestParam(value = "watermark", required = false) String watermark,
                               @RequestParam(value = "base64", required = false, defaultValue = "false") boolean base64,
                               @RequestParam(value = "timeStamp", required = false, defaultValue = "0") Long timeStamp,
                               @RequestParam(value = "locationShot", required = false, defaultValue = "0") Integer locationShot,
                               HttpServletRequest req) throws UnsupportedEncodingException {

        String url = null;
        //客户端用multipart的形式上传文件
        if (req instanceof DefaultMultipartHttpServletRequest) {
            List<MultipartFile> fileList = ((DefaultMultipartHttpServletRequest) req).getMultiFileMap().get("file");

            MultipartFile file = null;
            if (fileList != null && fileList.size() > 0) {
                file = fileList.get(0);
            }
            try {
                byte[] data = file.getBytes();
                url = fileService.saveFile(fileName, watermark, timeStamp, locationShot, data, base64);
                return new RespBuilder().status(HttpStatus.OK.value()).setData("url", url).build();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FileServerException e) {
                return new RespBuilder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg(e.getMessage()).build();
            }
            return new RespBuilder().status(HttpStatus.OK.value()).build();
        } else {
            //客户端采用二进制的方式上传
            fileName = new String(fileName.getBytes("ISO-8859-1"),"UTF-8");
            if(watermark != null){
                watermark = new String(watermark.getBytes("ISO-8859-1"),"UTF-8");
            }
            try {
                InputStream inputStream = req.getInputStream();
                url = fileService.saveFile(fileName, watermark, timeStamp, locationShot, inputStream, base64);
                return new RespBuilder().status(HttpStatus.OK.value()).setData("url", url).build();
            } catch (IOException e) {
                logger.error("从binary中获取文件里失败", e);
                return new RespBuilder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg("从binary中获取文件里失败").build();
            } catch (FileServerException e) {
                return new RespBuilder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg(e.getMessage()).build();
            }
        }

    }

    @RequestMapping(method = RequestMethod.GET, value = "/delete")
    @ResponseBody
    public Response deleteFile(@RequestParam(value = "fileName", required = true) String fileName) {
        if (fileName == null || fileName.equals("")) {
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
