package com.flushoutsolutions.foheart.appDataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.TableData;
import com.flushoutsolutions.foheart.data.TableFieldData;
import com.flushoutsolutions.foheart.models.TableFieldModel;
import com.flushoutsolutions.foheart.models.TableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuel on 09/08/2014.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static AppDatabaseHelper instance;

    public static synchronized AppDatabaseHelper getHelper()
    {
        return instance;
    }

    public AppDatabaseHelper(Context context, String appCode, int versionDB)
    {
        super(context, "db" + appCode + ".db", null, versionDB+1);
        instance = this;
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        List<TableData> arrTableData = TableModel.get_model().list();
        int listSize = arrTableData.size();
        for (int x=0; x<listSize; x++)
        {
            TableData tableData = arrTableData.get(x);
            List<TableFieldData> lstField = TableFieldModel.get_model().list(tableData._id);
            String query = "CREATE TABLE " + tableData.name + " (";
            int numFields = lstField.size();
            for (int y = 0; y<numFields; y++)
            {
                TableFieldData fieldLine = lstField.get(y);

                query += fieldLine.name+" "+fieldLine.type;

                if (fieldLine.primary_key == 1)
                    query += " PRIMARY KEY";
                if (y<numFields-1) query += ", ";
            }
            query += ");";

            db.execSQL(query);
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        List<TableData> arrTableData = TableModel.get_model().list();
        int listSize = arrTableData.size();
        for (int x=0; x<listSize; x++)
        {
            TableData tableData = arrTableData.get(x);
            db.execSQL("DROP TABLE IF EXISTS "+tableData.name);
        }
        onCreate(db);
    }
    /*public synchronized List<ContentValues> execQuery(SQLiteDatabase db,String selectQuery)
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
    }*/
}
