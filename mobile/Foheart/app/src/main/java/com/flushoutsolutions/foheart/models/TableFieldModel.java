package com.flushoutsolutions.foheart.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.DatabaseContract;
import com.flushoutsolutions.foheart.appDataBase.DatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.TableFieldData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 09/08/2014.
 */
public class TableFieldModel {

    private static TableFieldModel instance = null;

    private DatabaseHelper dbHelper = DatabaseHelper.getHelper(FoHeart.getAppContext());

    public static TableFieldModel get_model()
    {
        if (instance==null)
            instance = new TableFieldModel();

        return instance;
    }

    private TableFieldModel()
    {

    }

    public synchronized TableFieldData get_data(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.TableFieldSchema.TABLE_NAME+" WHERE "+DatabaseContract.TableFieldSchema._ID+ "=" +id, null);

        TableFieldData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_table = curApp.getInt(1);
            String name = curApp.getString(2);
            String type = curApp.getString(3);
            int size = curApp.getInt(4);
            String default_ = curApp.getString(5);
            int required = curApp.getInt(6);
            int auto_increment = curApp.getInt(7);
            int primary_key = curApp.getInt(8);

            appData = new TableFieldData(
                    _id,
                    fk_table,
                    name,
                    type,
                    size,
                    default_,
                    required,
                    auto_increment,
                    primary_key
            );
        }
        curApp.close();
        db.close();

        return appData;
    }

    public synchronized TableFieldData get_data(int fk_table, String fieldname)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+DatabaseContract.TableFieldSchema.TABLE_NAME+" WHERE fk_table="+fk_table+" AND "+DatabaseContract.TableFieldSchema.COLUMN_NAME_NAME+"='"+fieldname+"'", null);

        TableFieldData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_table_ = curApp.getInt(1);
            String name = curApp.getString(2);
            String type = curApp.getString(3);
            int size = curApp.getInt(4);
            String default_ = curApp.getString(5);
            int required = curApp.getInt(6);
            int auto_increment = curApp.getInt(7);
            int primary_key = curApp.getInt(8);

            appData = new TableFieldData(
                    _id,
                    fk_table_,
                    name,
                    type,
                    size,
                    default_,
                    required,
                    auto_increment,
                    primary_key
            );
        }
        curApp.close();
        db.close();

        return appData;
    }


    public synchronized long add(TableFieldData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_FK_TABLE, data.fk_table);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_TYPE, data.type);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_SIZE, data.size);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_REQUIRED, data.required);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_AUTO_INCREMENT, data.auto_increment);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_PRIMARY_KEY, data.primary_key);

            lastRowId = db.insert(DatabaseContract.TableFieldSchema.TABLE_NAME, null, values);
            db.close();
        }
        return lastRowId;
    }

    public synchronized int update(TableFieldData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_FK_TABLE, data.fk_table);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_TYPE, data.type);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_SIZE, data.size);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_REQUIRED, data.required);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_AUTO_INCREMENT, data.auto_increment);
            values.put(DatabaseContract.TableFieldSchema.COLUMN_NAME_PRIMARY_KEY, data.primary_key);

            rowsAffected = db.update(DatabaseContract.TableFieldSchema.TABLE_NAME, values, DatabaseContract.TableFieldSchema._ID + "=" + data._id, null);
            db.close();
        }
        return rowsAffected;
    }

    public synchronized long save(TableFieldData data)
    {
        TableFieldData record = get_data(data._id);

        long result;
        if (record==null)
            result = add(data);
        else
            result = update(data);

        return result;
    }

    public synchronized List<TableFieldData> list()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<TableFieldData> list = new ArrayList<TableFieldData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.TableFieldSchema.TABLE_NAME;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                TableFieldData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();
        db.close();

        return list;
    }

    public synchronized List<TableFieldData> list(int fk)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<TableFieldData> list = new ArrayList<TableFieldData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.TableFieldSchema.TABLE_NAME + " WHERE "+DatabaseContract.TableFieldSchema.COLUMN_NAME_FK_TABLE+"="+fk;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                TableFieldData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();
        db.close();

        return list;
    }

    public synchronized void delete(TableFieldData data)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try
        {
            db.delete(DatabaseContract.TableFieldSchema.TABLE_NAME, DatabaseContract.TableFieldSchema._ID +"="+data._id, null);
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
        try{
            db.delete(DatabaseContract.TableFieldSchema.TABLE_NAME, null, null);
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
