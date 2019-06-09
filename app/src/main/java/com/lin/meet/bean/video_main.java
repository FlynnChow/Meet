package com.lin.meet.bean;

import cn.bmob.v3.BmobObject;

public class video_main extends BmobObject {
    private String id;
    private String uid;
    private String uri;
    private String tltle;

    private boolean isPicture = false;
    private String content;

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public video_main setId(String id) {
        this.id = id;
        return this;
    }

    public video_main setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getTltle() {
        return tltle;
    }



    public video_main setTltle(String tltle) {
        this.tltle = tltle;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public video_main setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getContent() {
        return content;
    }

    public video_main setContent(String content) {
        this.content = content;
        return this;
    }

    public boolean isPicture() {
        return isPicture;
    }

    public video_main setPicture(boolean picture) {
        isPicture = picture;
        return this;
    }
}