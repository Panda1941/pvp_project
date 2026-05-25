package com.example.accidentreportingapp.models;

import android.graphics.Bitmap;

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

    public Bitmap decodePhoto() {
        if (this.url == null) return null;

        String data = this.url;

        if (data.contains(",")) {
            data = data.split(",")[1];
        }

        byte[] decoded = android.util.Base64.decode(data, android.util.Base64.DEFAULT);
        return android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }
}
