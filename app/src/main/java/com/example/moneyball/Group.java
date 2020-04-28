package com.example.moneyball;

/*
GROUP CLASS:
ID: A unique key for the group
HEADING: The title of the group
DESCRIPTION: The description for the group
PICTUREURI: The path to the group image in firebase storage
GROUPCREATOR: The ID of the user who created the group
CHATID: ID for the group chat

 */

import java.util.Comparator;

public class Group {
    String id, heading, description, groupCreator, picUri,chatId;
    public Group(String id, String heading, String description, String groupCreator, String picUri, String chatId){
        this.id = id;
        this.heading = heading;
        this.description = description;
        this.groupCreator = groupCreator;
        this.picUri = picUri;
        this.chatId = chatId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getGroupCreator() {
        return groupCreator;
    }

    public void setGroupCreator(String groupCreator) {
        this.groupCreator = groupCreator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicUri() {
        return this.picUri;
    }

    public void setPicUri(String picUri) {
        this.picUri = picUri;
    }

    public String getChatId() {
        return this.chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}

