package com.flushoutsolutions.foheart.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.DatabaseContract;
import com.flushoutsolutions.foheart.appDataBase.DatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.ApplicationData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 10/08/2014.
 */
public class ApplicationModel {

    private DatabaseHelper dbHelper = DatabaseHelper.getHelper(FoHeart.getAppContext());
    private static ApplicationModel instance = null;

    public static ApplicationModel get_model()
    {
        if (instance==null)
            instance = new ApplicationModel();

        return instance;
    }

    private ApplicationModel()
    {

    }

    public synchronized ApplicationData get_data(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.ApplicationSchema.TABLE_NAME+" WHERE "+DatabaseContract.ApplicationSchema._ID+ "=" +id, null);

        ApplicationData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            String code = curApp.getString(1);
            String description = curApp.getString(2);
            String appVersion = curApp.getString(3);
            String baseVersion = curApp.getString(4);

            String dbuser = curApp.getString(5);
            String dbpass = curApp.getString(6);
            String dbhost = curApp.getString(7);
            String dbname = curApp.getString(8);
            String dbport = curApp.getString(9);

            int updateInterval = curApp.getInt(10);
            int debugMode = curApp.getInt(11);


            appData = new ApplicationData(
                    _id,
                    code,
                    description,
                    appVersion,
                    baseVersion,
                    dbuser,
                    dbpass,
                    dbhost,
                    dbname,
                    dbport,
                    updateInterval,
                    debugMode
            );
        }
        curApp.close();

        return appData;
    }


    public synchronized long add(ApplicationData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_CODE, data.code);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_DESC, data.description);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_VERSION, data.app_version);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_BASE_VERSION, data.base_version);

            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_USER, data.db_user);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_PASS, data.db_pass);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_HOST, data.db_host);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_NAME, data.db_name);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_PORT, data.db_port);

            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_UPDATE_INTERVAL, data.update_interval);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DEBUG_MODE, data.debug_mode);

            lastRowId = db.insert(DatabaseContract.ApplicationSchema.TABLE_NAME, null, values);

        }
        return lastRowId;
    }

    public synchronized int update(ApplicationData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_CODE, data.code);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_DESC, data.description);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_VERSION, data.app_version);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_BASE_VERSION, data.base_version);

            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_USER, data.db_user);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_PASS, data.db_pass);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_HOST, data.db_host);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_NAME, data.db_name);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_PORT, data.db_port);

            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_UPDATE_INTERVAL, data.update_interval);
            values.put(DatabaseContract.ApplicationSchema.COLUMN_NAME_DEBUG_MODE, data.debug_mode);

            rowsAffected = db.update(DatabaseContract.ApplicationSchema.TABLE_NAME, values, DatabaseContract.ApplicationSchema._ID + "=" + data._id, null);
        }
        return rowsAffected;
    }

    public synchronized long save(ApplicationData data)
    {
        ApplicationData record = this.get_data(data._id);
        long result;
        if (record==null)
            result = add(data);
        else
            result = update(data);

        return result;
    }

    public synchronized List<ApplicationData> list()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<ApplicationData> list = new ArrayList<ApplicationData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.ApplicationSchema.TABLE_NAME;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                ApplicationData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();

        return list;
    }

    public synchronized void delete(ApplicationData data)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try
        {
            db.delete(DatabaseContract.ApplicationSchema.TABLE_NAME, DatabaseContract.ApplicationSchema._ID +"="+data._id, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public synchronized void delete_all()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try
        {
            db.delete(DatabaseContract.ApplicationSchema.TABLE_NAME, null, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public synchronized ApplicationData get_data(String appCode)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+DatabaseContract.ApplicationSchema.TABLE_NAME+" WHERE "+DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_CODE+"='"+appCode+"'", null);

        ApplicationData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            String code = curApp.getString(1);
            String description = curApp.getString(2);
            String app_version = curApp.getString(3);
            String base_version = curApp.getString(4);

            String db_user = curApp.getString(5);
            String db_pass = curApp.getString(6);
            String db_host = curApp.getString(7);
            String db_name = curApp.getString(8);
            String db_port = curApp.getString(9);

            int update_interval = curApp.getInt(10);
            int debug_mode = curApp.getInt(11);

            appData = new ApplicationData(
                    _id,
                    code,
                    description,
                    app_version,
                    base_version,
                    db_user,
                    db_pass,
                    db_host,
                    db_name,
                    db_port,
                    update_interval,
                    debug_mode
            );
        }
        curApp.close();

        return appData;
    }

    public synchronized ApplicationData get_data_by_desc(String desc)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+DatabaseContract.ApplicationSchema.TABLE_NAME+" WHERE "+DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_DESC+"='"+desc+"'", null);

        ApplicationData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            String code = curApp.getString(1);
            String description = curApp.getString(2);
            String app_version = curApp.getString(3);
            String base_version = curApp.getString(4);

            String db_user = curApp.getString(5);
            String db_pass = curApp.getString(6);
            String db_host = curApp.getString(7);
            String db_name = curApp.getString(8);
            String db_port = curApp.getString(9);

            int update_interval = curApp.getInt(10);
            int debug_mode = curApp.getInt(11);

            appData = new ApplicationData(
                    _id,
                    code,
                    description,
                    app_version,
                    base_version,
                    db_user,
                    db_pass,
                    db_host,
                    db_name,
                    db_port,
                    update_interval,
                    debug_mode
            );
        }
        curApp.close();

        return appData;
    }
}
