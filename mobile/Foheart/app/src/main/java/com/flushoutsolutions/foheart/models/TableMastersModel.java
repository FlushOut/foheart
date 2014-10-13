package com.flushoutsolutions.foheart.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.DatabaseContract;
import com.flushoutsolutions.foheart.appDataBase.DatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.TableData;
import com.flushoutsolutions.foheart.data.TableMastersData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 10/11/2014.
 */
public class TableMastersModel {

    private static TableMastersModel instance = null;
    public SQLiteDatabase db = null;
    private DatabaseHelper dbHelper = DatabaseHelper.getHelper(FoHeart.getAppContext());

    public static TableMastersModel get_model()
    {
        if (instance==null)
            instance = new TableMastersModel();

        return instance;
    }

    private TableMastersModel()
    {

    }

    public synchronized TableMastersData get_data(int id)
    {
        openDB();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.TableMasterSchema.TABLE_NAME+" WHERE "+DatabaseContract.TableMasterSchema._ID+ "=" +id, null);

        TableMastersData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int _fk_table = curApp.getInt(1);
            int _version_server = curApp.getInt(2);

            appData = new TableMastersData(
                    _id,
                    _fk_table,
                    _version_server
            );
        }
        curApp.close();
        closeDB();

        return appData;
    }

    public synchronized TableMastersData getBy(int fk_table)
    {
        openDB();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.TableMasterSchema.TABLE_NAME+" WHERE "+DatabaseContract.TableMasterSchema.COLUMN_NAME_FK_TABLE+ "=" +fk_table, null);

        TableMastersData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int _fk_table = curApp.getInt(1);
            int _version_server = curApp.getInt(2);

            appData = new TableMastersData(
                    _id,
                    _fk_table,
                    _version_server
            );
        }
        curApp.close();
        closeDB();

        return appData;
    }

    public synchronized List<TableMastersData> list()
    {
        openDB();

        List<TableMastersData> list = new ArrayList<TableMastersData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.TableMasterSchema.TABLE_NAME;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                TableMastersData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();
        closeDB();

        return list;
    }

    public synchronized long add(TableMastersData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            openDB();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.TableMasterSchema.COLUMN_NAME_FK_TABLE, data.fk_table);
            values.put(DatabaseContract.TableMasterSchema.COLUMN_NAME_VERSION_SERVER, data.version_server);

            lastRowId = db.insert(DatabaseContract.TableMasterSchema.TABLE_NAME, null, values);
            closeDB();
        }
        return lastRowId;
    }

    public synchronized int update(TableMastersData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            openDB();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.TableMasterSchema.COLUMN_NAME_FK_TABLE, data.fk_table);
            values.put(DatabaseContract.TableMasterSchema.COLUMN_NAME_VERSION_SERVER, data.version_server);

            rowsAffected = db.update(DatabaseContract.TableMasterSchema.TABLE_NAME, values, DatabaseContract.TableMasterSchema._ID + "=" + data._id, null);
            closeDB();
        }
        return rowsAffected;
    }

    public synchronized long save(TableMastersData data)
    {
        TableMastersData record = get_data(data._id);

        long result;
        if (record==null)
            result = add(data);
        else
            result = update(data);

        return result;
    }

    public synchronized void delete(TableData data)
    {
        openDB();

        try
        {
            db.delete(DatabaseContract.TableMasterSchema.TABLE_NAME, DatabaseContract.TableMasterSchema._ID +"="+data._id, null);
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
            db.delete(DatabaseContract.TableMasterSchema.TABLE_NAME, null, null);
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
