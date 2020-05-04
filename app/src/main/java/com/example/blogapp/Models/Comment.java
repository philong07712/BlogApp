package com.example.blogapp.Models;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class Comment {
    private String id, name, img, content;
    private Object timeStamp;
    private String key;

    public Comment(String id, String name, String img, String content) {
        this.id = id;
        this.name = name;
        this.img = img;
        this.content = content;
        timeStamp = ServerValue.TIMESTAMP;
    }

    public Comment(Comment comment)
    {
        this.id = comment.getId();
        this.name = comment.getName();
        this.img = comment.getImg();
        this.content = comment.getContent();
        this.timeStamp = comment.getTimeStamp();
        this.key = comment.getKey();
    }

    public Comment() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }
}
