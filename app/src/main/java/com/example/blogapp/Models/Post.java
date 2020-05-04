package com.example.blogapp.Models;

import com.google.firebase.database.ServerValue;

public class Post {
    private String key;
    private String title;
    private String description;
    private String picture;
    private String userId;
    private String name;
    private String userPhoto;
    private Object timeStamp;

    public Post() {
    }

    public Post(String title, String description, String picture, String userId, String userPhoto, String name) {
        this.title = title;
        this.description = description;
        this.picture = picture;
        this.userId = userId;
        this.userPhoto = userPhoto;
        this.name = name;
        this.timeStamp = ServerValue.TIMESTAMP;
    }

    public Post(Post post)
    {
        this.key = post.getKey();
        this.title = post.getTitle();
        this.description = post.getDescription();
        this.picture = post.getPicture();
        this.userId = post.getUserId();
        this.name = post.getName();
        this.userPhoto = post.getUserPhoto();
        this.timeStamp = post.getTimeStamp();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }
}
