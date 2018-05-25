package com.ape.backtouch.model;

/**
 * Created by linzhiqin on 9/8/16.
 */
public class ActionModel {
    public String type; //down or up
    public long time;
    public int count;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
