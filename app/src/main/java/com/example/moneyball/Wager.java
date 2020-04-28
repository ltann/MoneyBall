package com.example.moneyball;


import android.net.Uri;

import java.util.ArrayList;
import java.util.Comparator;

/*
WAGER CLASS:
ID: A unique key for the wager
HEADING: The title of the wager
DESCRIPTION: The description for the wager
PICTUREURI: The path to the image in firebase storage
GROUP: The ID of the group the wager is part of
WAGERCREATOR: The ID of the user who created the wager
WAGERRESULT: The result of the wager (either Y or N), the wager creator chooses this after the wager is closed
USERSLIST: Arraylist of user IDs of those who are part of the wager
CHALLENGELIST: Arraylist of dare ideas entered by users
USERVOTES: Arraylist of "Y" or "N" selected by users
OPENSTATUS: Boolean to tell if the wager is still open or if its been closed
BETVAL: the value of the bet for the wager
 */

public class Wager {
    String id, heading, description, pictureUri, group, wagerCreator, wagerResult;
    ArrayList<String> usersList, challengeList, userVotes;
    Boolean openStatus;
    double betVal;

    public Wager(String id, String heading, String group, String pictureUri, String description, String wagerCreator, ArrayList<String> usersList, Boolean openStatus, double betVal, ArrayList <String> challengeList, ArrayList <String> userVotes, String wagerResult){
        this.id = id;
        this.heading = heading;
        this.group = group;
        this.pictureUri = pictureUri;
        this.description = description;
        this.wagerCreator = wagerCreator;
        this.usersList = usersList;
        this.openStatus = openStatus;
        this.betVal = betVal;
        this.challengeList = challengeList;
        this.userVotes = userVotes;
        this.wagerResult = wagerResult;
    }

    //A function to add a user to the list of users who have entered a wager
    public void addUserToUsersList(String userID){
        this.usersList.add(userID);
    }

    public void setUsersList(ArrayList<String> usersList) {
        this.usersList = usersList;
    }

    public ArrayList<String> getUsersList() {
        return usersList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPicture() {
        return this.pictureUri;
    }

    public void setPicture(String picture) {
        this.pictureUri = picture;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWagerCreator() {
        return wagerCreator;
    }

    public void setWagerCreator(String wagerCreator) {
        this.wagerCreator = wagerCreator;
    }

    public Boolean getOpenStatus() {
        return openStatus;
    }

    public void setOpenStatus(Boolean openStatus) {
        this.openStatus = openStatus;
    }

    public double getBetVal() {
        return betVal;
    }

    public void setBetVal(double betVal) {
        this.betVal = betVal;
    }

    public ArrayList<String> getChallengeList() {
        return challengeList;
    }

    public void setChallengeList(ArrayList<String> challengeList) {
        this.challengeList = challengeList;
    }

    public void setUserVotes(ArrayList<String> userVotes) {
        this.userVotes = userVotes;
    }

    public ArrayList<String> getUserVotes() {
        return userVotes;
    }

    public String getWagerResult() {
        return wagerResult;
    }

    public void setWagerResult(String wagerResult) {
        this.wagerResult = wagerResult;
    }
}
