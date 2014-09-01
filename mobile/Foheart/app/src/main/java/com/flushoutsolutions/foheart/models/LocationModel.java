package com.flushoutsolutions.foheart.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.DatabaseContract;
import com.flushoutsolutions.foheart.appDataBase.DatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.LocationData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 09/08/2014.
 */
public class LocationModel {
    private DatabaseHelper dbHelper = DatabaseHelper.getHelper(FoHeart.getAppContext());
    private static LocationModel instance = null;

    public static LocationModel get_model()
    {
        if (instance==null)
            instance = new LocationModel();

        return instance;
    }

    private LocationModel()
    {

    }

    public synchronized LocationData get_data(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+DatabaseContract.LocationSchema.TABLE_NAME+" WHERE "+DatabaseContract.LocationSchema._ID + "=" +id, null);

        LocationData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            String latitude = curApp.getString(1);
            String longitude = curApp.getString(2);
            String speed = curApp.getString(3);
            String phone_number = curApp.getString(4);
            String bearing = curApp.getString(5);
            String accuracy = curApp.getString(6);
            String battery_level = curApp.getString(7);
            String gsm_strength = curApp.getString(8);
            String carrier = curApp.getString(9);
            String date_time = curApp.getString(10);

            appData = new LocationData(
                    _id,
                    latitude,
                    longitude,
                    speed,
                    phone_number,
                    bearing,
                    accuracy,
                    battery_level,
                    gsm_strength,
                    carrier,
                    date_time
            );
        }
        curApp.close();

        return appData;
    }

    public synchronized long add(LocationData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_LAT, data.lat);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_LON, data.lon);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_SPEED, data.speed);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_PHONE_NUMBER, data.phone_number);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_BEARING, data.bearing);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_ACCURACY, data.accuracy);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_BATTERY_LEVEL, data.battery_level);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_GSM_STRENGTH, data.gms_strength);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_CARRIER, data.carrier);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_DATETIME, data.date_time);

            lastRowId = db.insert(DatabaseContract.LocationSchema.TABLE_NAME, null, values);
        }
        return lastRowId;
    }


    public synchronized int update(LocationData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_LAT, data.lat);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_LON, data.lon);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_SPEED, data.speed);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_PHONE_NUMBER, data.phone_number);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_BEARING, data.bearing);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_ACCURACY, data.accuracy);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_BATTERY_LEVEL, data.battery_level);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_GSM_STRENGTH, data.gms_strength);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_CARRIER, data.carrier);
            values.put(DatabaseContract.LocationSchema.COLUMN_NAME_DATETIME, data.date_time);

            rowsAffected = db.update(DatabaseContract.LocationSchema.TABLE_NAME, values, DatabaseContract.LocationSchema._ID + "=" + data._id, null);
        }
        return rowsAffected;
    }


    public synchronized long save(LocationData data)
    {
        LocationData record = get_data(data._id);
        long result;

        if (record==null)
            result = add(data);
        else
            result = update(data);

        return result;
    }

    public synchronized List<LocationData> list()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<LocationData> list = new ArrayList<LocationData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.LocationSchema.TABLE_NAME;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                LocationData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();

        return list;
    }

    public synchronized void delete(LocationData data)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try
        {
            db.delete(DatabaseContract.LocationSchema.TABLE_NAME, DatabaseContract.LocationSchema._ID +"="+data._id, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
