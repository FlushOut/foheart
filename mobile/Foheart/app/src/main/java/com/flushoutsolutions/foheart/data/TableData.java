package com.flushoutsolutions.foheart.data;

/**
 * Created by Manuel on 09/08/2014.
 */
public class TableData {

    public int _id;
    public int fk_application;
    public int model_version;
    public String name;
    public int auto_sync;
    public String key;
    public String requestParams;

    public TableData(int _id, int fk_application, int model_version, String name, int auto_sync, String key, String requestParams)
    {
        this._id = _id;
        this.fk_application = fk_application;
        this.model_version = model_version;
        this.name = name;
        this.auto_sync = auto_sync;
        this.key = key;
        this.requestParams = requestParams;
    }

    public TableData(int fk_application, int model_version, String name, int auto_sync, String key, String requestParams)
    {
        this.fk_application = fk_application;
        this.model_version = model_version;
        this.name = name;
        this.auto_sync = auto_sync;
        this.key = key;
        this.requestParams = requestParams;
    }
}
