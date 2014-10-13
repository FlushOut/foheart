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
    public SQLiteDatabase db = null;
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
        openDB();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.TableSchema.TABLE_NAME+" WHERE "+DatabaseContract.TableSchema._ID+ "=" +id, null);

        TableData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            int model_version = curApp.getInt(2);
            String name = curApp.getString(3);
            String key = curApp.getString(4);
            int auto_sync = curApp.getInt(5);
            String requestParams = curApp.getString(6);

            appData = new TableData(
                    _id,
                    fk_application,
                    model_version,
                    name,
                    auto_sync,
                    key,
                    requestParams
            );
        }
        curApp.close();
        closeDB();

        return appData;
    }


    public synchronized TableData get_data(int fk_app, String name)
    {
        openDB();
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
            String key = curApp.getString(4);
            int auto_sync = curApp.getInt(5);
            String requestParams = curApp.getString(6);

            appData = new TableData(
                    _id,
                    fk_application,
                    model_version,
                    name_,
                    auto_sync,
                    key,
                    requestParams
            );
        }
        curApp.close();
        closeDB();

        return appData;
    }

    public synchronized int get_model_version(int fk_app)
    {
        openDB();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.TableSchema.TABLE_NAME+" WHERE "+DatabaseContract.TableSchema.COLUMN_NAME_FK_APPLICATION+ "=" +fk_app + " LIMIT 1", null);

        TableData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            int model_version = curApp.getInt(2);
            String name = curApp.getString(3);
            String key = curApp.getString(4);
            int auto_sync = curApp.getInt(5);
            String requestParams = curApp.getString(6);

            appData = new TableData(
                    _id,
                    fk_application,
                    model_version,
                    name,
                    auto_sync,
                    key,
                    requestParams
            );
        }
        curApp.close();
        closeDB();

        return appData.model_version;
    }

    public synchronized long add(TableData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            openDB();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_FK_APPLICATION, data.fk_application);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_MODEL_VERSION, data.model_version);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_AUTO_SYNC, String.valueOf(data.auto_sync));
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_KEY, data.key);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_REQUESTPARAMS, data.requestParams);

            lastRowId = db.insert(DatabaseContract.TableSchema.TABLE_NAME, null, values);
            closeDB();
        }
        return lastRowId;
    }

    public synchronized int update(TableData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            openDB();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_FK_APPLICATION, data.fk_application);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_MODEL_VERSION, data.model_version);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_AUTO_SYNC, String.valueOf(data.auto_sync));
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_KEY, data.key);
            values.put(DatabaseContract.TableSchema.COLUMN_NAME_REQUESTPARAMS, data.requestParams);

            rowsAffected = db.update(DatabaseContract.TableSchema.TABLE_NAME, values, DatabaseContract.TableSchema._ID + "=" + data._id, null);
            closeDB();
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
        openDB();

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
        closeDB();

        return list;
    }

    public synchronized List<TableData> listSyncable()
    {
        openDB();

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
        closeDB();

        return list;
    }

    public synchronized List<TableData> listTableMaster()
    {
        openDB();

        List<TableData> list = new ArrayList<TableData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.TableSchema.TABLE_NAME + " WHERE "+DatabaseContract.TableSchema.COLUMN_NAME_AUTO_SYNC+"=0";

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
        closeDB();

        return list;
    }

    public synchronized void delete(TableData data)
    {
        openDB();

        try
        {
            db.delete(DatabaseContract.TableSchema.TABLE_NAME, DatabaseContract.TableSchema._ID +"="+data._id, null);
            closeDB();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public synchronized void delete_all()
    {
        openDB();

        try
        {
            db.delete(DatabaseContract.TableSchema.TABLE_NAME, null, null);
            closeDB();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void openDB(){
        if(db == null){
            db = dbHelper.getWritableDatabase();
        }else{
            if(!db.isOpen())
                db = dbHelper.getWritableDatabase();
        }
    }

    private void closeDB(){
        if(db != null && db.isOpen())
            db.close();
    }
}
