package com.example.moneyball;

public class Messages {
    private String name,message,date,time;

    public Messages(){

    }

    public Messages(String name,String message,String date,String time){
        this.name = name;
        this.message =message;
        this.date =date;
        this.time= time;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
