package com.flushoutsolutions.foheart.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Manuel on 10/08/2014.
 */
public class ApplicationData {

    public int _id;
    public String code;
    public String description;
    public String app_version;
    public String base_version;

    public String db_user;
    public String db_pass;

    public int update_interval;
    public int debug_mode;


    public ApplicationData(String code, String description, String app_version, String base_version, String db_user, String db_pass, int update_interval, int debug_mode)
    {
        this.code = code;
        this.description = description;
        this.app_version = app_version;
        this.base_version = base_version;

        this.db_user = db_user;
        this.db_pass = db_pass;

        this.update_interval = update_interval;
        this.debug_mode = debug_mode;
    }

    public ApplicationData(int _id, String code, String description, String app_version, String base_version, String db_user, String db_pass, int update_interval, int debug_mode)
    {
        this._id = _id;
        this.code = code;
        this.description = description;
        this.app_version = app_version;
        this.base_version = base_version;

        this.db_user = db_user;
        this.db_pass = db_pass;

        this.update_interval = update_interval;
        this.debug_mode = debug_mode;
    }

    public ApplicationData(JSONObject jsonObj,String appCode) throws JSONException
    {
        this.code = appCode;
        this.description = jsonObj.get("description").toString();
        this.app_version = jsonObj.get("appVersion").toString();
        this.base_version = jsonObj.get("baseVersion").toString();

        this.db_user = jsonObj.get("dbuser").toString();
        this.db_pass = jsonObj.get("dbpass").toString();

        this.update_interval = jsonObj.getInt("updateInterval");
        this.debug_mode = jsonObj.getInt("debugMode");
    }
}
