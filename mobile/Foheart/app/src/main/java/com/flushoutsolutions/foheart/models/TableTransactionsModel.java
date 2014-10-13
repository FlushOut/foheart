package com.flushoutsolutions.foheart.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.DatabaseContract;
import com.flushoutsolutions.foheart.appDataBase.DatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.TableData;
import com.flushoutsolutions.foheart.data.TableMastersData;
import com.flushoutsolutions.foheart.data.TableTransactionsData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 10/11/2014.
 */
public class TableTransactionsModel {

    private static TableTransactionsModel instance = null;
    public SQLiteDatabase db = null;
    private DatabaseHelper dbHelper = DatabaseHelper.getHelper(FoHeart.getAppContext());

    public static TableTransactionsModel get_model()
    {
        if (instance==null)
            instance = new TableTransactionsModel();

        return instance;
    }

    private TableTransactionsModel()
    {

    }

    public synchronized TableTransactionsData get_data(int id)
    {
        openDB();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.TableTransactionSchema.TABLE_NAME+" WHERE "+DatabaseContract.TableTransactionSchema._ID+ "=" +id, null);

        TableTransactionsData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int _fk_table = curApp.getInt(1);
            int version_local = curApp.getInt(2);
            int version_server = curApp.getInt(3);
            int fk_request = curApp.getInt(4);
            int fk_response = curApp.getInt(5);

            appData = new TableTransactionsData(
                    _id,
                    _fk_table,
                    version_local,
                    version_server,
                    fk_request,
                    fk_response
            );
        }
        curApp.close();
        closeDB();

        return appData;
    }


    public synchronized TableTransactionsData getBy(int fk_table,int fk_response)
    {
        openDB();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.TableTransactionSchema.TABLE_NAME+" WHERE "+DatabaseContract.TableTransactionSchema.COLUMN_NAME_FK_TABLE+ "=" +fk_table+" AND "+DatabaseContract.TableTransactionSchema.COLUMN_NAME_FK_RESPONSE+ "=" +fk_response, null);

        TableTransactionsData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int _fk_table = curApp.getInt(1);
            int _version_local = curApp.getInt(2);
            int _version_server = curApp.getInt(3);
            int _fk_request = curApp.getInt(4);
            int _fk_response = curApp.getInt(5);

            appData = new TableTransactionsData(
                    _id,
                    _fk_table,
                    _version_local,
                    _version_server,
                    _fk_request,
                    _fk_response
            );
        }
        curApp.close();
        closeDB();

        return appData;
    }

    public synchronized List<TableTransactionsData> list()
    {
        openDB();

        List<TableTransactionsData> list = new ArrayList<TableTransactionsData>();
        String selectQuery = "SELECT * FROM " + DatabaseContract.TableTransactionSchema.TABLE_NAME;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                TableTransactionsData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();
        closeDB();

        return list;
    }

    public synchronized long add(TableTransactionsData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            openDB();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.TableTransactionSchema.COLUMN_NAME_FK_TABLE, data.fk_table);
            values.put(DatabaseContract.TableTransactionSchema.COLUMN_NAME_VERSION_LOCAL, data.version_local);
            values.put(DatabaseContract.TableTransactionSchema.COLUMN_NAME_VERSION_SERVER, data.version_server);
            values.put(DatabaseContract.TableTransactionSchema.COLUMN_NAME_FK_REQUEST, data.fk_request);
            values.put(DatabaseContract.TableTransactionSchema.COLUMN_NAME_FK_RESPONSE, data.fk_response);

            lastRowId = db.insert(DatabaseContract.TableTransactionSchema.TABLE_NAME, null, values);
            closeDB();
        }
        return lastRowId;
    }

    public synchronized int update(TableTransactionsData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            openDB();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.TableTransactionSchema.COLUMN_NAME_FK_TABLE, data.fk_table);
            values.put(DatabaseContract.TableTransactionSchema.COLUMN_NAME_VERSION_LOCAL, data.version_local);
            values.put(DatabaseContract.TableTransactionSchema.COLUMN_NAME_VERSION_SERVER, data.version_server);
            values.put(DatabaseContract.TableTransactionSchema.COLUMN_NAME_FK_REQUEST, data.fk_request);
            values.put(DatabaseContract.TableTransactionSchema.COLUMN_NAME_FK_RESPONSE, data.fk_response);

            rowsAffected = db.update(DatabaseContract.TableTransactionSchema.TABLE_NAME, values, DatabaseContract.TableTransactionSchema._ID + "=" + data._id, null);
            closeDB();
        }
        return rowsAffected;
    }

    public synchronized long save(TableTransactionsData data)
    {
        TableTransactionsData record = get_data(data._id);

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
            db.delete(DatabaseContract.TableTransactionSchema.TABLE_NAME, null, null);
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
