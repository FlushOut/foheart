package com.flushoutsolutions.foheart.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.DatabaseContract;
import com.flushoutsolutions.foheart.appDataBase.DatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.ViewData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 10/08/2014.
 */
public class ViewModel {

    private static ViewModel instance = null;

    private DatabaseHelper dbHelper = DatabaseHelper.getHelper(FoHeart.getAppContext());

    public static ViewModel get_model()
    {
        if (instance==null)
            instance = new ViewModel();

        return instance;
    }

    private ViewModel()
    {

    }

    public synchronized ViewData get_data(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.ViewSchema.TABLE_NAME+" WHERE "+DatabaseContract.ViewSchema._ID+ "=" +id, null);

        ViewData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            String name = curApp.getString(2);
            String title = curApp.getString(3);
            String layout = curApp.getString(4);
            String button_title = curApp.getString(5);
            String button_action = curApp.getString(6);
            int back_locked = curApp.getInt(7);
            String events = curApp.getString(8);

            appData = new ViewData(
                    _id,
                    fk_application,
                    name,
                    title,
                    layout,
                    button_title,
                    button_action,
                    back_locked,
                    events
            );
        }
        curApp.close();

        return appData;
    }

    public synchronized ViewData get_data(int fk_app, String name)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+DatabaseContract.ViewSchema.TABLE_NAME+
                                    " WHERE "+DatabaseContract.ViewSchema.COLUMN_NAME_FK_APPLICATION+ "=" +fk_app+
                                    " AND "+DatabaseContract.ViewSchema.COLUMN_NAME_NAME+"='"+name+"'", null);

        ViewData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            String name_ = curApp.getString(2);
            String title = curApp.getString(3);
            String layout = curApp.getString(4);
            String button_title = curApp.getString(5);
            String button_action = curApp.getString(6);
            int back_locked = curApp.getInt(7);
            String events = curApp.getString(8);

            appData = new ViewData(
                    _id,
                    fk_application,
                    name_,
                    title,
                    layout,
                    button_title,
                    button_action,
                    back_locked,
                    events
            );
        }
        curApp.close();

        return appData;
    }

    public synchronized long add(ViewData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_FK_APPLICATION, String.valueOf(data.fk_application));
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_TITLE, data.title);
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_LAYOUT, data.layout);
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_BUTTON_TITLE, data.button_title);
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_BUTTON_ACTION, data.button_action);
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_BACK_LOCKED, String.valueOf(data.back_locked));
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_EVENTS, data.events);

            lastRowId = db.insert(DatabaseContract.ViewSchema.TABLE_NAME, null, values);
        }
        return lastRowId;
    }

    public synchronized int update(ViewData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_FK_APPLICATION, String.valueOf(data.fk_application));
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_TITLE, data.title);
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_LAYOUT, data.layout);
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_BUTTON_TITLE, data.button_title);
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_BUTTON_ACTION, data.button_action);
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_BACK_LOCKED, String.valueOf(data.back_locked));
            values.put(DatabaseContract.ViewSchema.COLUMN_NAME_EVENTS, data.events);

            rowsAffected = db.update(DatabaseContract.ViewSchema.TABLE_NAME, values, DatabaseContract.ViewSchema._ID + "=" + data._id, null);
        }
        return rowsAffected;
    }

    public synchronized long save(ViewData data)
    {
        ViewData record = get_data(data._id);

        long result;
        if (record==null)
            result = add(data);
        else
            result = update(data);

        return result;
    }

    public synchronized List<ViewData> list()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<ViewData> list = new ArrayList<ViewData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.ViewSchema.TABLE_NAME;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                ViewData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();

        return list;
    }

    public synchronized void delete(ViewData data)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try
        {
            db.delete(DatabaseContract.ViewSchema.TABLE_NAME, DatabaseContract.ViewSchema._ID +"="+data._id, null);
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
            db.delete(DatabaseContract.ViewSchema.TABLE_NAME, null, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
