package com.flushoutsolutions.foheart.data;

/**
 * Created by Manuel on 10/08/2014.
 */
public class ViewData {

    public int _id;
    public int fk_application;
    public String name;
    public String title;
    public String layout;
    public String button_title;
    public String button_action;
    public int back_locked;
    public String events;

    public ViewData(int _id, int fk_application, String name, String title, String layout, String button_title, String button_action, int back_locked, String events)
    {
        this._id = _id;
        this.fk_application = fk_application;
        this.name = name;
        this.title = title;
        this.layout = layout;
        this.button_title = button_title;
        this.button_action = button_action;
        this.back_locked = back_locked;
        this.events = events;
    }

    public ViewData(int fk_application, String name, String title, String layout, String button_title, String button_action, int back_locked, String events)
    {
        this.fk_application = fk_application;
        this.name = name;
        this.title = title;
        this.layout = layout;
        this.button_title = button_title;
        this.button_action = button_action;
        this.back_locked = back_locked;
        this.events = events;
    }
}
