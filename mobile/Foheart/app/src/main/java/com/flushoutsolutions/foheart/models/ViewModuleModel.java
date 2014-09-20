package com.flushoutsolutions.foheart.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.DatabaseContract;
import com.flushoutsolutions.foheart.appDataBase.DatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.ViewModuleData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 10/08/2014.
 */
public class ViewModuleModel {
    private static ViewModuleModel instance = null;

    private DatabaseHelper dbHelper = DatabaseHelper.getHelper(FoHeart.getAppContext());

    public static ViewModuleModel get_model()
    {
        if (instance==null)
            instance = new ViewModuleModel();

        return instance;
    }

    private ViewModuleModel()
    {

    }

    public synchronized ViewModuleData get_data(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.ViewModuleSchema.TABLE_NAME+" WHERE "+DatabaseContract.ViewModuleSchema._ID+ "=" +id, null);

        ViewModuleData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_view = curApp.getInt(1);
            String module = curApp.getString(2);
            String name = curApp.getString(3);
            String properties = curApp.getString(4);
            String events = curApp.getString(5);

            appData = new ViewModuleData(
                    _id,
                    fk_view,
                    module,
                    name,
                    properties,
                    events
            );
        }
        curApp.close();
        db.close();

        return appData;
    }


    public synchronized long add(ViewModuleData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.ViewModuleSchema.COLUMN_NAME_FK_VIEW, String.valueOf(data.fk_view));
            values.put(DatabaseContract.ViewModuleSchema.COLUMN_NAME_MODULE, data.module);
            values.put(DatabaseContract.ViewModuleSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.ViewModuleSchema.COLUMN_NAME_PROPERTIES, data.properties);
            values.put(DatabaseContract.ViewModuleSchema.COLUMN_NAME_EVENTS, data.events);

            lastRowId = db.insert(DatabaseContract.ViewModuleSchema.TABLE_NAME, null, values);
            db.close();
        }
        return lastRowId;
    }

    public synchronized int update(ViewModuleData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.ViewModuleSchema.COLUMN_NAME_FK_VIEW, String.valueOf(data.fk_view));
            values.put(DatabaseContract.ViewModuleSchema.COLUMN_NAME_MODULE, data.module);
            values.put(DatabaseContract.ViewModuleSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.ViewModuleSchema.COLUMN_NAME_PROPERTIES, data.properties);
            values.put(DatabaseContract.ViewModuleSchema.COLUMN_NAME_EVENTS, data.events);

            rowsAffected = db.update(DatabaseContract.ViewModuleSchema.TABLE_NAME, values, DatabaseContract.ViewModuleSchema._ID + "=" + data._id, null);
            db.close();
        }
        return rowsAffected;
    }

    public synchronized long save(ViewModuleData data)
    {
        ViewModuleData record = get_data(data._id);

        long result;
        if (record==null)
            result = add(data);
        else
            result = update(data);

        return result;
    }

    public synchronized List<ViewModuleData> list(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<ViewModuleData> list = new ArrayList<ViewModuleData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.ViewModuleSchema.TABLE_NAME + " WHERE "+DatabaseContract.ViewModuleSchema.COLUMN_NAME_FK_VIEW+"="+id;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                ViewModuleData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();
        db.close();

        return list;
    }

    public synchronized void delete(ViewModuleData data)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try
        {
            db.delete(DatabaseContract.ViewModuleSchema.TABLE_NAME, DatabaseContract.ViewModuleSchema._ID +"="+data._id, null);
            db.close();
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
            db.delete(DatabaseContract.ViewModuleSchema.TABLE_NAME, null, null);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
