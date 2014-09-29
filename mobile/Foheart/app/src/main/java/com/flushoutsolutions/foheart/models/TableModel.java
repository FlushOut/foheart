package com.flushoutsolutions.foheart.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.DatabaseContract;
import com.flushoutsolutions.foheart.appDataBase.DatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.TableData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 09/08/2014.
 */
public class TableModel {

    private static TableModel instance = null;

    private DatabaseHelper dbHelper = DatabaseHelper.getHelper(FoHeart.getAppContext());

    public static TableModel get_model()
    {
        if (instance==null)
            instance = new TableModel();

        return instance;
    }

    private TableModel()
    {

    }

    public synchronized TableData get_data(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.TableSchema.TABLE_NAME+" WHERE "+DatabaseContract.TableSchema._ID+ "=" +id, null);

        TableData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            int model_version = curApp.getInt(2);
            String name = curApp.getString(3);
            int auto_sync = curApp.getInt(4);
            String key = curApp.getString(5);
            int version_local = curApp.getInt(6);
            int version_server = curApp.getInt(7);
            String requestParams = curApp.getString(8);

            appData = new TableData(
                    _id,
                    fk_application,
                    model_version,
                    name,
                    auto_sync,
                    key,
                    version_local,
                    version_server,
                    requestParams
            );
        }
        curApp.close();
        db.close();

        return appData;
    }


    public synchronized TableData get_data(int fk_app, String name)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+DatabaseContract.TableSchema.TABLE_NAME+
                                    " WHERE " + DatabaseContract.TableSchema.COLUMN_NAME_FK_APPLICATION+ "=" +fk_app+
                                    " AND " + DatabaseContract.TableSchema.COLUMN_NAME_NAME+"='"+name+"'", null);

        TableData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            int model_version = curApp.getInt(2);
            String name_ = curApp.getString(3);
            int auto_sync = curApp.getInt(4);
            String key = curApp.getString(5);
            int version_local = curApp.getInt(6);
            int version_server = curApp.getInt(7);
            String requestParams = curApp.getString(8);

            appData = new TableData(
                    _id,
                    fk_application,
                    model_version,
                    name_,
                    auto_sync,
                    key,
                    version_local,
                    version_server,
                    requestParams
            );
        }
        curApp.close();
        db.close();

        return appData;
    }

    public synchronized int get_model_version(int fk_app)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.TableSchema.TABLE_NAME+" WHERE "+DatabaseContract.TableSchema.COLUMN_NAME_FK_APPLICATION+ "=" +fk_app + " LIMIT 1", null);

        TableData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            int model_version = curApp.getInt(2);
            String name = curApp.getString(3);
            int auto_sync = curApp.getInt(4);
            String key = curApp.getString(5);
            int version_local = curApp.getInt(6);
            int version_server = curApp.getInt(7);
            String requestParams = curApp.getString(8);

            appData = new TableData(
                    _id,
                    fk_application,
                    model_version,
                    name,
                    auto_sync,
                    key,
                    version_local,
                    version_server,
                    requestParams
            );
        }
        curApp.close();
        db.close();

        return appData.model_version;
    }

    public synchronized long add(TableData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_FK_APPLICATION, data.fk_application);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_MODEL_VERSION, data.model_version);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_AUTO_SYNC, String.valueOf(data.auto_sync));
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_KEY, data.key);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_VERSION_LOCAL, data.version_local);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_VERSION_SERVER, data.version_server);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_REQUESTPARAMS, data.requestParams);

            lastRowId = db.insert(DatabaseContract.TableSchema.TABLE_NAME, null, values);
            db.close();
        }
        return lastRowId;
    }

    public synchronized int update(TableData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_FK_APPLICATION, data.fk_application);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_MODEL_VERSION, data.model_version);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_AUTO_SYNC, String.valueOf(data.auto_sync));
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_KEY, data.key);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_VERSION_LOCAL, data.version_local);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_VERSION_SERVER, data.version_server);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_REQUESTPARAMS, data.requestParams);

            rowsAffected = db.update(DatabaseContract.TableSchema.TABLE_NAME, values, DatabaseContract.TableSchema._ID + "=" + data._id, null);
            db.close();
        }
        return rowsAffected;
    }

    public synchronized long save(TableData data)
    {
        TableData record = get_data(data._id);

        long result;
        if (record==null)
            result = add(data);
        else
            result = update(data);

        return result;
    }

    public synchronized List<TableData> list()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<TableData> list = new ArrayList<TableData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.TableSchema.TABLE_NAME;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                TableData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();
        db.close();

        return list;
    }

    public synchronized List<TableData> listSyncable()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<TableData> list = new ArrayList<TableData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.TableSchema.TABLE_NAME + " WHERE "+DatabaseContract.TableSchema.COLUMN_NAME_AUTO_SYNC+"=1";

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                TableData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();
        db.close();

        return list;
    }

    public synchronized void delete(TableData data)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try
        {
            db.delete(DatabaseContract.TableSchema.TABLE_NAME, DatabaseContract.TableSchema._ID +"="+data._id, null);
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
            db.delete(DatabaseContract.TableSchema.TABLE_NAME, null, null);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
