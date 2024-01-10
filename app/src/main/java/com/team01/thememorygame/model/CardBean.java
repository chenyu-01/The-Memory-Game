package com.team01.thememorygame.model;
public class CardBean {
    public String imgName_first; // The name of the first picture
    public String imgName_second; // The name of the second picture

    private String localImagePathFirst; // The path of the first picture
    private String localImagePathSecond; // The path of the second picture
    public String getLocalImagePathFirst() {
        return localImagePathFirst;
    }

    public void setLocalImagePathFirst(String path) {
        this.localImagePathFirst = path;
    }

    public String getLocalImagePathSecond() {
        return localImagePathSecond;
    }

    public void setLocalImagePathSecond(String path) {
        this.localImagePathSecond = path;
    }

}