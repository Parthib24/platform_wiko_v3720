package com.ape.backtouch.model;

/**
 * Created by linzhiqin on 9/8/16.
 */
public class EventModel {
    public String eventType; //single long double
    public ActionModel downAction;
    public ActionModel upAction;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public ActionModel getDownAction() {
        return downAction;
    }

    public void setDownAction(ActionModel downAction) {
        this.downAction = downAction;
    }

    public ActionModel getUpAction() {
        return upAction;
    }

    public void setUpAction(ActionModel upAction) {
        this.upAction = upAction;
    }
}
