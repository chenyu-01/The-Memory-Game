package com.team01.thememorygame.model;
public class CardBean {
    public String imgName_first;
    public String imgName_second;

    private String localImagePathFirst;
    private String localImagePathSecond;
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