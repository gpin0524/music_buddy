package com.github.pwittchen.neurosky.app.Model;

public class VideoDetails {

    public String videoId,title,description,url,listId,documentId,situation,userId;
    public int index;

    public VideoDetails(String videoId, String title, String description, String url,String listId, String documentId, String situation, int index, String userId) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.url = url;
        this.index = index;
        this.listId = listId;
        this.documentId = documentId;
        this.situation = situation;
        this.userId = userId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public  VideoDetails(){ }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSituation() {
        return situation;
    }

    public void setSituation(String situation) {
        this.situation = situation;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
