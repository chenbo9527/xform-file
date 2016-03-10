package com.weibangong.xform.fileserver;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Created by chenbo on 16/3/10.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private String msg;
    private int status;
    private Map<String, Object> data;

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getMsg() {

        return msg;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, Object> getData() {
        return data;
    }

}
