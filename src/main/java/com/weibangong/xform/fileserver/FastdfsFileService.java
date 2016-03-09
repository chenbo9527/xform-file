package com.weibangong.xform.fileserver;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by guanxinquan on 16/2/25.
 */
public class FastdfsFileService {

    private static final Logger logger = LoggerFactory.getLogger(FastdfsFileService.class);

    TrackerServer trackerServer;

    private static String FDFS_GROUP_NAME = "group01";
    private static String urlPrefix = "http://127.0.0.1:80/";
    private static String FOLDER_PREFIX = System.getProperty("file.config.env");
    private static BASE64Decoder bs64Encoder = new BASE64Decoder();

    static {
        if(FOLDER_PREFIX == null || FOLDER_PREFIX.equals("")){
            FOLDER_PREFIX = "dev";
        }
        logger.warn("The file.config.env is " + FOLDER_PREFIX);
    }

    public FastdfsFileService() throws IOException, MyException {

        String propPath = this.getClass().getResource("/" + FOLDER_PREFIX + "/fdfs.properties").getFile();
        InputStream in = new FileInputStream(propPath);
        Properties prop = new Properties();
        prop.load(in);
        FDFS_GROUP_NAME = prop.getProperty("groupName");
        urlPrefix = "http://" + prop.getProperty("serverUri") + "/";
        in.close();

        String config = this.getClass().getResource("/" + FOLDER_PREFIX + "/fdfs_client.conf").getFile();
        ClientGlobal.init(config);
        TrackerClient client = new TrackerClient();
        trackerServer = client.getConnection();
    }

    /**
     * 保存文件到fastdfs
     *
     * @param fileName    文件名
     * @param watermark   水印，仅图片需要
     * @param inputStream 文件流
     * @return
     * @throws IOException
     * @throws MyException
     */
    public String saveFile(String fileName, String watermark, long timeStamp, int locationShot, InputStream inputStream, boolean base64) {

        byte[] imgData = translateToByteArray(inputStream, base64);
        String ext = getExtName(fileName);

        //判断是否是图片，添加水印
        if (!StringUtils.isEmpty(watermark) && isImage(ext)) {
            imgData = addWaterMark(imgData, watermark, fileName, timeStamp, locationShot);
        }

        StorageClient1 client1 = new StorageClient1(trackerServer, null);
        NameValuePair[] metaList = new NameValuePair[3];
        metaList[0] = new NameValuePair("fileName", fileName);
        metaList[1] = new NameValuePair("fileExtName", ext);
        metaList[2] = new NameValuePair("fileLength", String.valueOf(imgData.length));
        String path = null;
        try {
            path = client1.upload_file1(FDFS_GROUP_NAME, imgData, ext, metaList);
        } catch (Exception e) {
            logger.error("文件上传到fdfs失败", e);
        }
        return urlPrefix + path;
    }

    /**
     * 删除fastdfs里的文件
     *
     * @param filename
     * @return
     * @throws IOException
     * @throws MyException
     */
    public void deleteFile(String filename) {
        StorageClient1 client1 = new StorageClient1(trackerServer, null);

        if (filename != null || filename != "") {
            try {
                int result = client1.delete_file1(filename.replaceFirst("^/", ""));
                if (result != 0) {
                    logger.warn("文件删除失败, error code : " + result + "");
                }
            } catch (Exception e) {
                logger.error("文件删除异常", e);
            }
        }
    }

    /**
     * 获取文件的扩展名
     *
     * @param fileName
     * @return
     */
    private String getExtName(String fileName) {
        String[] splits = fileName.split("\\.");
        if (splits.length > 1) {
            return splits[splits.length - 1];
        } else {
            return "";
        }
    }

    /**
     * 根据扩展名判断是否图片
     *
     * @param ext
     * @return
     */
    private boolean isImage(String ext) {
        if (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("bmp")
                || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("gif")) {
            return true;
        } else
            return false;
    }

    /**
     * 将字节流转换为字符流
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private byte[] translateToByteArray(InputStream inputStream, boolean base64) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            IOUtils.copy(inputStream, out);
//            System.out.println(new String(out.toByteArray()));
        } catch (IOException e) {
            logger.error("流转换异常", e);
        }

        if (base64) {
            byte[] bytes = new byte[0];
            try {
                bytes = bs64Encoder.decodeBuffer(new String(out.toByteArray()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            try {
                IOUtils.copy(in, out2);
            } catch (IOException e) {
                logger.error("流转换异常", e);
            }
            return out2.toByteArray();
        }
        return out.toByteArray();
    }

    /**
     * @param data
     * @param markContent
     * @param shotTimestamp
     * @param filename      文件名
     * @param locationShot
     * @return
     */
    private byte[] addWaterMark(byte[] data, String markContent, String filename, Long shotTimestamp, Integer locationShot) {
        //获取扩展名
        String ext = getExtName(filename);
        if (StringUtils.isEmpty(ext)) {
            return data;
        }

        InputStream is = new ByteArrayInputStream(data);
        BufferedImage src = null;
        try {
            src = ImageIO.read(is);
            is.close();
        } catch (IOException e) {
            logger.error("", e);
            e.printStackTrace();
        }

        if (src == null) {
            return data;
        }

        int width = src.getWidth(null);
        int height = src.getHeight(null);
        Graphics2D g2d = (Graphics2D) src.getGraphics();

        BufferedImage watermark;

        if (shotTimestamp == 0) {
            shotTimestamp = System.currentTimeMillis();
        }
        if (locationShot == 1) {
            markContent += " 相册图片";
        } else if (locationShot == 2) {
            markContent += " 现场拍摄";
        }
        boolean isSmallPic = false;
        //判断上传的是否为小图并做相应设置
        watermark = getWatermarkBigBgBufImage();
        if (width - watermark.getWidth() < 0) {
            isSmallPic = true;
            watermark = getWatermarkSmallBgBufImage();
        }
        if (watermark == null) {
            return data;
        }

        Font font = new Font("黑体", Font.PLAIN, 24);
        if (isSmallPic) {
            font = new Font("黑体", Font.PLAIN, 9);
        }

        String shotTime = formatTime(shotTimestamp);
        FontRenderContext context = g2d.getFontRenderContext();
        Rectangle2D markContentRect = font.getStringBounds(markContent, context);
        Rectangle2D timeRect = font.getStringBounds(shotTime, context);


        ///////////////////////////////////////////////////////////////////////
        // 画水印底图
        ///////////////////////////////////////////////////////////////////////
        //int watermarkCordX = width - watermark.getWidth() - 20;
        int watermarkCordY = height - watermark.getHeight() + 5;
        //int watermarkWidth = watermark.getWidth() + 10;
        //一般情况下，采用时间的宽度加5
        int watermarkCordX = (int) (width - timeRect.getWidth() - 30);
        int watermarkWidth = (int) (timeRect.getWidth() + 20);
        //如果markContent大于宽度
        if ((markContentRect.getWidth() + 5) > watermark.getWidth()) {
            watermarkCordX = (int) (width - markContentRect.getWidth() - 30);
            watermarkWidth = (int) (markContentRect.getWidth() + 20);
        }

        g2d.drawImage(watermark, watermarkCordX, watermarkCordY, watermarkWidth, watermark.getHeight() - 15, null);

        ///////////////////////////////////////////////////////////////////////
        // 打水印
        ///////////////////////////////////////////////////////////////////////

        g2d.setColor(Color.WHITE);
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //画时间watermarkCordX + 5
        g2d.drawString(shotTime, (int) (width - timeRect.getWidth() - 20), (int) (watermarkCordY + 5 + timeRect.getHeight()));
        //画文字
        g2d.drawString(markContent, (int) (width - markContentRect.getWidth() - 20), (int) (watermarkCordY + 10 + timeRect.getHeight() + markContentRect.getHeight()));
        g2d.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean succeed = false;
        try {
            succeed = ImageIO.write(src, ext, out);
        } catch (IOException e) {
            logger.error("", e);
            e.printStackTrace();
        }

        if (succeed) {
            return out.toByteArray();
        } else {
            logger.error("no appropriate writer is found.");
            return data;
        }
    }

    private BufferedImage getWatermarkSmallBgBufImage() {
        BufferedImage image = getWatermarkBigBgBufImage();
        image = new BufferedImage(image.getWidth() / 2, image.getHeight() / 2, BufferedImage.TYPE_INT_BGR);
        return image;
    }

    private String formatTime(Long shotTimestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(new Date(shotTimestamp));
    }

    public BufferedImage getWatermarkBigBgBufImage() {
        String path = this.getClass().getResource("/image/watermark_big_bg.png").getFile();
        BufferedImage image = null;
        try {
            InputStream in = new FileInputStream(path);
            image = ImageIO.read(in);
        } catch (Exception e) {
            logger.error("加载水印背景图失败", e);
        }
        return image;
    }
}
