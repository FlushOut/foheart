package com.flushoutsolutions.foheart.appDataBase;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.ApplicationData;
import com.flushoutsolutions.foheart.data.TableFieldData;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.models.ApplicationModel;
import com.flushoutsolutions.foheart.models.TableFieldModel;
import com.flushoutsolutions.foheart.models.TableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 09/08/2014.
 */
public class AppDBModel {

    private static AppDBModel instance = null;
    private AppDatabaseHelper dbHelper = null;
    public SQLiteDatabase db = null;

    private String appTableName;
    public List<TableFieldData> fieldDatas;


    SQLiteStatement statement;
    int sizeFields;
    int idApp;

    public static AppDBModel get_model()
    {
        return instance;
    }

    public AppDBModel(Context context, String appCode, int versionDB)
    {
        dbHelper = new AppDatabaseHelper(context, appCode, versionDB).getHelper();

        idApp = ApplicationModel.get_model().get_data(appCode)._id;
        instance = this;
    }

    public AppDBModel(Context context, String appCode, int versionDB, String tableName)
    {
        dbHelper = new AppDatabaseHelper(context, appCode, versionDB).getHelper();

        this.appTableName = tableName;
        idApp = ApplicationModel.get_model().get_data(appCode)._id;
        fieldDatas = TableFieldModel.get_model().list(TableModel.get_model().get_data(idApp,tableName)._id);
        sizeFields = this.fieldDatas.size();

        instance = this;

    }
    public synchronized ContentValues get_data(int id)
    {
        ContentValues values = new ContentValues();
        try
        {
            db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "+ appTableName+" WHERE _ID="+id, null);
            if (cursor.moveToFirst())
            {
                do
                {
                    for (int i=0; i<cursor.getColumnCount(); i++)
                    {
                        values.put(cursor.getColumnName(i), cursor.getString(i));
                    }

                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        }
        catch (SQLiteException e)
        {
            e.printStackTrace();
        }
        return values;
    }
    public synchronized ContentValues get_data_transaction(int id)
    {
        ContentValues values = new ContentValues();
        try
        {
            Cursor cursor = db.rawQuery("SELECT * FROM "+ this.appTableName+" WHERE _ID="+id, null);
            if (cursor.moveToFirst())
            {
                do
                {
                    for (int i=0; i<cursor.getColumnCount(); i++)
                    {
                        values.put(cursor.getColumnName(i), cursor.getString(i));
                    }

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        catch (SQLiteException e)
        {
            e.printStackTrace();
        }
        return values;
    }

    public synchronized void beginTransaction()
    {
        db  = dbHelper.getWritableDatabase();
        db.beginTransaction();
        String sql = this.addStatement();

        try
        {
            statement = db.compileStatement(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db  = dbHelper.getWritableDatabase();
            statement = db.compileStatement(sql);
        }

        statement.clearBindings();
    }
    public synchronized void endTransaction()
    {
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
    }
    public synchronized void add(ContentValues values)
    {
        if (null!=values)
        {
            values.put("_date_time_created", FoHeart.dateTime());
            values.put("_date_time_updated", FoHeart.dateTime());
            db = dbHelper.getWritableDatabase();
            db.insert(this.appTableName, null, values);
            db.close();
        }
    }
    public synchronized void update(ContentValues values)
    {
        int id = values.getAsInteger("_id");

        if (null!=values && id > 0)
        {
            values.put("_date_time_updated", FoHeart.dateTime());
            db = dbHelper.getWritableDatabase();
            db.update(this.appTableName, values, "_id=" + id, null);
            db.close();
        }
    }
    public synchronized void save(ContentValues values)
    {
        ContentValues value = get_data(values.getAsInteger("_id"));

        if (value.size()==0)
            add(values);
        else
            update(values);
    }
    public  String addStatement()
    {
        String returnSQL = "INSERT INTO "+this.appTableName +" ";
        ArrayList<String> arrFields = new ArrayList<String>();
        ArrayList<Object> arrValues = new ArrayList<Object>();

        for (TableFieldData tableFieldData : this.fieldDatas)
        {
            arrFields.add(tableFieldData.name);
            arrValues.add("?");
        }
        String[] strArrFields = arrFields.toArray(new String[arrFields.size()]);
        String[] strArrValues = arrValues.toArray(new String[arrValues.size()]);
        String strFields = FoHeart.implode(strArrFields, ", ");
        String strValues = FoHeart.implode(strArrValues, ", ");
        returnSQL += " ("+strFields+") VALUES ("+strValues+")";

        return returnSQL;
    }
    public void addTransaction(ContentValues values)
    {
        for (int c=0; c<sizeFields; c++)
        {
            String fieldName = this.fieldDatas.get(c).name;
            String fieldType = this.fieldDatas.get(c).type.toLowerCase();
            Object value = values.get(fieldName);
            if (c==2 || c==3)
                value = FoHeart.dateTime();

            if (fieldType.equals("int") || fieldType.equals("integer"))
            {
                if (null == value || "".equals(value)) value = "0";
                statement.bindLong(c + 1, Long.parseLong(value.toString()));
            }
            if (fieldType.equals("real"))
            {
                if (null == value || "".equals(value)) value = "0";

                statement.bindDouble(c + 1, Double.parseDouble(value.toString()));
            }
            if (fieldType.equals("string") || fieldType.equals("text"))
            {
                if (null == value) value = "";
                statement.bindString(c+1, value.toString());
            }
        }
        try
        {
            statement.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void updateTransaction(ContentValues values)
    {
        ArrayList<String> arrFieldsValues = new ArrayList<String>();
        for (int c=3; c<sizeFields; c++)
        {
            String fieldName = this.fieldDatas.get(c).name;
            String fieldType = this.fieldDatas.get(c).type.toLowerCase();
            Object value = values.get(fieldName);

            if (c==3)
                value = FoHeart.dateTime();

            if (fieldType.equals("int") || fieldType.equals("integer"))
            {
                if (null == value || "".equals(value)) value = "0";
                arrFieldsValues.add(fieldName+"="+Long.parseLong(value.toString()));
            }

            if (fieldType.equals("real"))
            {
                if (null == value || "".equals(value)) value = "0";
                arrFieldsValues.add(fieldName+"="+Double.parseDouble(value.toString()));
            }
            if (fieldType.equals("string") || fieldType.equals("text"))
            {
                if (null == value) value = "";
                arrFieldsValues.add(fieldName+"='"+value.toString()+"'");
            }
        }
        String updateSQL = "UPDATE "+this.appTableName +" SET ";
        String[] strArrFields = arrFieldsValues.toArray(new String[arrFieldsValues.size()]);
        String strFields = FoHeart.implode(strArrFields, ", ");
        String whereSQL = " WHERE _id = "+values.getAsInteger("_id");
        String sql = updateSQL+strFields+whereSQL;
        SQLiteStatement statement = db.compileStatement(sql);

        try
        {
            statement.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public synchronized void saveTransaction(ContentValues values)
    {
        ContentValues value = get_data_transaction(values.getAsInteger("_id"));

        if (value.size()==0)
            addTransaction(values);
        else
            updateTransaction(values);
    }
    public synchronized List<ContentValues> list()
    {
        db = dbHelper.getWritableDatabase();
        List<ContentValues> list = new ArrayList<ContentValues>();
        String selectQuery = "SELECT  * FROM " + appTableName;

        Cursor curApp = db.rawQuery(selectQuery, null);

        if (curApp.moveToFirst())
        {
            do
            {
                ContentValues appData = get_data(curApp.getInt(0));
                list.add(appData);
            }
            while (curApp.moveToNext());
        }

        curApp.close();
        db.close();

        return list;
    }
    public synchronized void delete(int _id)
    {
        try
        {
            db = dbHelper.getWritableDatabase();
            db.delete(this.appTableName, "_id="+_id, null);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public synchronized void delete(String condition)
    {
        try
        {
            db = dbHelper.getWritableDatabase();
            db.delete(this.appTableName, condition, null);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public synchronized void deleteAll()
    {
        try
        {
            db = dbHelper.getWritableDatabase();
            db.delete(this.appTableName, null, null);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public synchronized List<ContentValues> execQuery(String selectQuery)
    {
        List<ContentValues> list = new ArrayList<ContentValues>();
        try
        {
            db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst())
            {
                do
                {
                    ContentValues values = new ContentValues();
                    for (int i=0; i<cursor.getColumnCount(); i++)
                    {
                        values.put(cursor.getColumnName(i), cursor.getString(i));
                    }

                    list.add(values);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        }
        catch (SQLiteException e)
        {
            e.printStackTrace();
        }
        return list;
    }
    public synchronized List<ContentValues> execQueryTrans(String selectQuery)
    {
        List<ContentValues> list = new ArrayList<ContentValues>();
        try
        {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst())
            {
                do
                {
                    ContentValues values = new ContentValues();
                    for (int i=0; i<cursor.getColumnCount(); i++)
                    {
                        values.put(cursor.getColumnName(i), cursor.getString(i));
                    }

                    list.add(values);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        catch (SQLiteException e)
        {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized int getNumRows(String table, int id)
    {
        List<ContentValues> list = this.execQuery("SELECT * FROM " + table + " WHERE _id=" + id);
        return list.size();
    }
    public synchronized int getNumRowsTrans(String table, int id)
    {
        List<ContentValues> list = this.execQueryTrans("SELECT * FROM " + table + " WHERE _id=" + id);
        return list.size();
    }

    public void setAppTableName(String tb)
    {
        this.appTableName = tb;
        fieldDatas = TableFieldModel.get_model().list(TableModel.get_model().get_data(idApp,tb)._id);
        sizeFields = this.fieldDatas.size();
    }
}
