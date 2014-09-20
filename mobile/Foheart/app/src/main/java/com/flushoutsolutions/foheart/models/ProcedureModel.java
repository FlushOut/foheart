package com.flushoutsolutions.foheart.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.DatabaseContract;
import com.flushoutsolutions.foheart.appDataBase.DatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.ProcedureData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 09/08/2014.
 */
public class ProcedureModel {
    private static ProcedureModel instance = null;

    private DatabaseHelper dbHelper = DatabaseHelper.getHelper(FoHeart.getAppContext());

    public static ProcedureModel get_model()
    {
        if (instance==null)
            instance = new ProcedureModel();

        return instance;
    }

    private ProcedureModel()
    {

    }

    public synchronized ProcedureData get_data(int id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+ DatabaseContract.ProcedureSchema.TABLE_NAME+" WHERE "+DatabaseContract.ProcedureSchema._ID+ "=" +id, null);

        ProcedureData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            String p_name = curApp.getString(2);
            String parameters = curApp.getString(3);
            String code = curApp.getString(4);
            String return_str = curApp.getString(5);

            appData = new ProcedureData(
                    _id,
                    fk_application,
                    p_name,
                    parameters,
                    code,
                    return_str
            );
        }
        curApp.close();
        db.close();

        return appData;
    }

    public synchronized ProcedureData get_data(int fk_app, String name)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor curApp = db.rawQuery("SELECT * FROM "+DatabaseContract.ProcedureSchema.TABLE_NAME+
                                    " WHERE "+DatabaseContract.ProcedureSchema.COLUMN_NAME_FK_APPLICATION+ "=" +fk_app +
                                    " AND "+DatabaseContract.ProcedureSchema.COLUMN_NAME_NAME+ "='" +name+"'", null);

        ProcedureData appData = null;

        if (curApp.moveToFirst())
        {
            int _id = curApp.getInt(0);
            int fk_application = curApp.getInt(1);
            String p_name = curApp.getString(2);
            String parameters = curApp.getString(3);
            String code = curApp.getString(4);
            String return_str = curApp.getString(5);

            appData = new ProcedureData(
                    _id,
                    fk_application,
                    p_name,
                    parameters,
                    code,
                    return_str
            );
        }
        curApp.close();
        db.close();

        return appData;
    }

    public synchronized long add(ProcedureData data)
    {
        long lastRowId = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.ProcedureSchema.COLUMN_NAME_FK_APPLICATION, data.fk_application);
            values.put(DatabaseContract.ProcedureSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.ProcedureSchema.COLUMN_NAME_PARAMETERS, data.parameters);
            values.put(DatabaseContract.ProcedureSchema.COLUMN_NAME_CODE, data.code);
            values.put(DatabaseContract.ProcedureSchema.COLUMN_NAME_RETURN, data.retur);

            lastRowId = db.insert(DatabaseContract.ProcedureSchema.TABLE_NAME, null, values);
            db.close();
        }
        return lastRowId;
    }

    public synchronized int update(ProcedureData data)
    {
        int rowsAffected = 0;
        if (null!=data)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.ProcedureSchema.COLUMN_NAME_FK_APPLICATION, data.fk_application);
            values.put(DatabaseContract.ProcedureSchema.COLUMN_NAME_NAME, data.name);
            values.put(DatabaseContract.ProcedureSchema.COLUMN_NAME_PARAMETERS, data.parameters);
            values.put(DatabaseContract.ProcedureSchema.COLUMN_NAME_CODE, data.code);
            values.put(DatabaseContract.ProcedureSchema.COLUMN_NAME_RETURN, data.retur);

            rowsAffected = db.update(DatabaseContract.ProcedureSchema.TABLE_NAME, values, DatabaseContract.ProcedureSchema._ID + "=" + data._id, null);
            db.close();
        }
        return rowsAffected;
    }

    public synchronized long save(ProcedureData data)
    {
        ProcedureData record = get_data(data._id);

        long result;
        if (record==null)
            result = add(data);
        else
            result = update(data);

        return result;
    }

    public synchronized List<ProcedureData> list()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<ProcedureData> list = new ArrayList<ProcedureData>();
        String selectQuery = "SELECT  * FROM " + DatabaseContract.ProcedureSchema.TABLE_NAME;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                ProcedureData appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();
        db.close();

        return list;
    }

    public synchronized void delete(ProcedureData data)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try
        {
            db.delete(DatabaseContract.ProcedureSchema.TABLE_NAME, DatabaseContract.ProcedureSchema._ID +"="+data._id, null);
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
            db.delete(DatabaseContract.ProcedureSchema.TABLE_NAME, null, null);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
