package com.example.accidentreportingapp.models;

import java.io.Serializable;

public class Photo implements Serializable {
    private String url;
    private String description;
    private Integer ord;

    public Photo() {
    }

    public Photo(String url, String description, Integer ord) {
        this.url = url;
        this.description = description;
        this.ord = ord;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrd() {
        return ord;
    }

    public void setOrd(Integer ord) {
        this.ord = ord;
    }
}
