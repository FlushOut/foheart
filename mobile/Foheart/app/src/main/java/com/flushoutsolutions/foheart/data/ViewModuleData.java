package com.flushoutsolutions.foheart.data;

/**
 * Created by Manuel on 10/08/2014.
 */
public class ViewModuleData {

    public int _id;
    public int fk_view;
    public String module;
    public String name;
    public String properties;
    public String events;

    public ViewModuleData(int _id, int fk_view, String module, String name, String properties, String events)
    {
        this._id = _id;
        this.fk_view = fk_view;
        this.module= module;
        this.name = name;
        this.properties = properties;
        this.events = events;
    }

    public ViewModuleData(int fk_view, String module, String name, String properties, String events)
    {
        this.fk_view = fk_view;
        this.module= module;
        this.name = name;
        this.properties = properties;
        this.events = events;
    }
}
