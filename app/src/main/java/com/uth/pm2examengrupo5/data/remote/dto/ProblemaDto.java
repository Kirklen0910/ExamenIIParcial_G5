package com.uth.pm2examengrupo5.data.remote.dto;

import java.util.List;
import java.util.Map;

public class ProblemaDto {
    private String title;
    private Integer status;
    private String detail;
    private Map<String, List<String>> errors;

    public String getTitle() { return title; }
    public Integer getStatus() { return status; }
    public String getDetail() { return detail; }
    public Map<String, List<String>> getErrors() { return errors; }
}
