package com.github.pwittchen.neurosky.app.Model;

public class ListDetails {
    public String listId,title,description,url,situation,userId,selectedAccount;
    public int index;

    public ListDetails(){

    }

    public ListDetails(String listId, String title, String description, String url, String situation, String userId, String selectedAccount, int index) {
        this.listId = listId;
        this.title = title;
        this.description = description;
        this.url = url;
        this.index = index;
        this.situation = situation;
        this.userId = userId;
        this.selectedAccount = selectedAccount;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
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

    public String getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(String selectedAccount) {
        this.selectedAccount = selectedAccount;
    }
}
