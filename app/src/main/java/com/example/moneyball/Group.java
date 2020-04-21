package com.example.moneyball;

public class Group {
    String id, heading, description, groupCreator, picUri;
    public Group(String id, String heading, String description, String groupCreator, String picUri){
        this.id = id;
        this.heading = heading;
        this.description = description;
        this.groupCreator = groupCreator;
        this.picUri = picUri;
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
}

