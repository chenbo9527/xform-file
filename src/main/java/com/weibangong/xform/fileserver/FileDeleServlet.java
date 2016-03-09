package com.weibangong.xform.fileserver;

import org.csource.common.MyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by chenbo on 16/3/4.
 */
public class FileDeleServlet extends HttpServlet {
    private FastdfsFileService fileService;

    private static final Logger logger = LoggerFactory.getLogger(FileDeleServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            fileService = new FastdfsFileService();
        } catch (Exception e) {
            logger.error("FileDeleServlet init failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fileName = req.getParameter("filename");

        fileService.deleteFile(fileName);
    }
}
