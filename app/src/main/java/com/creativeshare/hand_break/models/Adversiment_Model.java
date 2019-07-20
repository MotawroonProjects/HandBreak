package com.creativeshare.hand_break.models;

import android.net.Uri;

import java.io.Serializable;
import java.util.List;

public class Adversiment_Model implements Serializable {
    private String city_id;
    private String cat_id;
    private String sub_id;
    private String model_id;
    private List<Uri> uris;

    public void setCity_id(String city_id) {
        this.city_id = city_id;
    }

    public void setCat_id(String cat_id) {
        this.cat_id = cat_id;
    }

    public void setSub_id(String sub_id) {
        this.sub_id = sub_id;
    }

    public void setModel_id(String model_id) {
        this.model_id = model_id;
    }

    public void setUris(List<Uri> uris) {
        this.uris = uris;
    }

    public String getCity_id() {
        return city_id;
    }

    public String getCat_id() {
        return cat_id;
    }

    public String getSub_id() {
        return sub_id;
    }

    public String getModel_id() {
        return model_id;
    }

    public List<Uri> getUris() {
        return uris;
    }
}
