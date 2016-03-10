package com.weibangong.xform.fileserver;

/**
 * Created by chenbo on 16/3/10.
 */
public class FileServerException extends Exception {
    private String message;
    private Exception ex;

    public FileServerException(String message){
        this.message = message;
        this.ex = new Exception(message);
    }

    public FileServerException(String message, Exception e){
        this.message = message;
        this.ex = e;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getEx() {
        return ex;
    }

    public void setEx(Exception ex) {
        this.ex = ex;
    }
}
