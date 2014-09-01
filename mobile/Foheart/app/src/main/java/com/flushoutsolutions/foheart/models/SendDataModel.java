package com.flushoutsolutions.foheart.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.DatabaseContract;
import com.flushoutsolutions.foheart.appDataBase.DatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.SendDataData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 10/08/2014.
 */
public class SendDataModel {

    private static SendDataModel instance = null;

    private DatabaseHelper dbHelper = DatabaseHelper.getHelper(FoHeart.getAppContext());

    public static SendDataModel get_model()
    {
        if (instance==null)
            instance = new SendDataModel();

        return instance;
    }

    private SendDataModel()
    {

    }

    public synchronized SendDataData get_data(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.SendDataSchema.TABLE_NAME+" WHERE "+DatabaseContract.SendDataSchema._ID+ "=" +id, null);

        SendDataData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            int fk_user = curApp.getInt(2);
            String table_name = curApp.getString(3);
            int row_id = curApp.getInt(4);
            String record = curApp.getString(5);
            int sent = curApp.getInt(6);
            String datetime = curApp.getString(7);

            appData = new SendDataData(
                    _id,
                    fk_application,
                    fk_user,
                    table_name,
                    row_id,
                    record,
                    sent,
                    datetime
            );
        }
        curApp.close();

        return appData;
    }

    public synchronized SendDataData get_data(int rowid, String tablename)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+DatabaseContract.SendDataSchema.TABLE_NAME+" WHERE "+DatabaseContract.SendDataSchema._ID+ "=" +rowid+" AND "+DatabaseContract.SendDataSchema.COLUMN_NAME_TABLE_NAME+"='"+tablename+"'",null);

        SendDataData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            int fk_user = curApp.getInt(2);
            String table_name = curApp.getString(3);
            int row_id = curApp.getInt(4);
            String record = curApp.getString(5);
            int sent = curApp.getInt(6);
            String datetime = curApp.getString(7);

            appData = new SendDataData(
                    _id,
                    fk_application,
                    fk_user,
                    table_name,
                    row_id,
                    record,
                    sent,
                    datetime
            );
        }
        curApp.close();

        return appData;
    }

    public synchronized long add(SendDataData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_FK_APPLICATION, data.fk_application);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_FK_USER, data.fk_user);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_TABLE_NAME, data.table_name);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_ROW_ID, data.row_id);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_RECORD, data.record);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_SENT, data.sent);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_DATETIME, data.datetime);

            lastRowId = db.insert(DatabaseContract.SendDataSchema.TABLE_NAME, null, values);
        }
        return lastRowId;
    }


    public synchronized int update(SendDataData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_FK_APPLICATION, data.fk_application);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_FK_USER, data.fk_user);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_TABLE_NAME, data.table_name);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_ROW_ID, data.row_id);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_RECORD, data.record);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_SENT, data.sent);
            values.put(DatabaseContract.SendDataSchema.COLUMN_NAME_DATETIME, data.datetime);

            rowsAffected = db.update(DatabaseContract.SendDataSchema.TABLE_NAME, values, DatabaseContract.SendDataSchema._ID + "=" + data._id, null);
        }
        return rowsAffected;
    }


    public synchronized long save(SendDataData data)
    {
        SendDataData record = get_data(data._id);

        long result;
        if (record==null)
            result = add(data);
        else
            result = update(data);

        return result;
    }

    public synchronized List<SendDataData> list()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<SendDataData> list = new ArrayList<SendDataData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.SendDataSchema.TABLE_NAME;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                SendDataData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();

        return list;
    }

    public synchronized List<SendDataData> listUnsync()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<SendDataData> list = new ArrayList<SendDataData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.SendDataSchema.TABLE_NAME +" WHERE "+DatabaseContract.SendDataSchema.COLUMN_NAME_SENT+"=0";

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                SendDataData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();

        return list;
    }

    public synchronized void delete(SendDataData data)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try
        {
            db.delete(DatabaseContract.SendDataSchema.TABLE_NAME, DatabaseContract.SendDataSchema._ID +"="+data._id, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
