package com.weibangong.xform.fileserver;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class RespBuilder {

    private Map<String, Object> map;
    private Set<String> filters;
    private Map<String, String> rename;

    @JsonFilter("filter")
    private Map<String, Object> data;

    public RespBuilder() {
        filters = new HashSet<String>();
        rename = new HashMap<String, String>();
        map = new HashMap<String, Object>();
        map.put("status", 0);
    }

    public RespBuilder status(int status) {
        map.put("status", status);
        return this;
    }

    public RespBuilder filter(String... name) {
        for (int i = 0; i < name.length; i++) {
            filters.add(name[i]);
        }
        return this;
    }

    public RespBuilder rename(String name, String newName) {
        rename.put(name, newName);
        return this;
    }

    public RespBuilder msg(String msg) {
        map.put("msg", msg);
        return this;
    }

    public RespBuilder msg(List<String> msg) {
        map.put("msg", msg);
        return this;
    }

    public RespBuilder setData(String name, Object value) {
        if (data == null) {
            data = new HashMap<String, Object>();
        }
        data.put(name, value);
        return this;
    }

    public RespBuilder setData(String name, Object... value) {
        if (data == null) {
            data = new HashMap<String, Object>();
        }
        data.put(name, value);
        return this;

    }

    public Response build() {
        Response response = new Response();
        response.setStatus((Integer) map.get("status"));
        if (map.get("msg") != null) {
            response.setMsg(map.get("msg").toString());
        }
        if (data != null && data.keySet() != null && data.keySet().size() > 0) {
            ObjectMapper mapper = new ObjectMapper();
            response.setData((Map<String, Object>) BeanUtils.filter(mapper.convertValue(data, HashMap.class), filters, rename));
        }
        return response;
    }

}
