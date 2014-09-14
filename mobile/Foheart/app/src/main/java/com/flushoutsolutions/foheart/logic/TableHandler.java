package com.flushoutsolutions.foheart.logic;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.AppDBModel;
import com.flushoutsolutions.foheart.appDataBase.AppDatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.TableData;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.models.ApplicationModel;
import com.flushoutsolutions.foheart.models.TableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Manuel on 09/08/2014.
 */
public class TableHandler {

    static Context appContext = FoHeart.getAppContext();
    static SharedPreferences settings = appContext.getSharedPreferences("userconfigs", 0);
    static String codeApp = settings.getString("idApplication", "");
    static int idApp = ApplicationModel.get_model().get_data(codeApp)._id;
    static int model_version = TableModel.get_model().get_model_version(idApp);

    private AppDatabaseHelper dbHelper = new AppDatabaseHelper(appContext, codeApp, model_version).getHelper();
    AppDBModel appDBModel = new AppDBModel(appContext, codeApp, model_version).get_model();
    public TableHandler()
    {

    }

    @SuppressWarnings("unchecked")
    public synchronized void tableInsert(String tableName, String values)
    {
        try
        {
            ContentValues contentValues = new ContentValues();
            JSONArray jsonValues = new JSONArray(values);
            for (int v=0; v<jsonValues.length(); v++)
            {
                JSONObject jsonValue = jsonValues.getJSONObject(v);
                Iterator<Object> keys = (Iterator)jsonValue.keys();

                while (keys.hasNext())
                {
                    String fieldName = keys.next().toString();
                    String record = Variables.parse_vars(jsonValue.getString(fieldName), false).replaceAll("\t", " ");

                    contentValues.put(fieldName, record.trim());
                }
            }
            appDBModel.setAppTableName(tableName);
            appDBModel.add(contentValues);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public synchronized void tableUpdate(String tableName, String values)
    {
        try
        {
            ContentValues contentValues = new ContentValues();
            JSONArray jsonValues = new JSONArray(values);
            for (int v=0; v<jsonValues.length(); v++)
            {
                JSONObject jsonValue = jsonValues.getJSONObject(v);
                @SuppressWarnings("unchecked")
                Iterator<Object> keys = (Iterator)jsonValue.keys();
                int id = Integer.parseInt(Variables.parse_vars(jsonValue.getString("_id"), false));
                while (keys.hasNext())
                {
                    String fieldName = keys.next().toString();
                    if (!fieldName.equals("_id"))
                    {
                        String record = Variables.parse_vars(jsonValue.getString(fieldName), false).replaceAll("\t", " ");
                        contentValues.put(fieldName, record.trim());
                    }

                }
                contentValues.put("_id", id);
                appDBModel.setAppTableName(tableName);
                appDBModel.save(contentValues);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public synchronized void tableDelete(String tableName, String field, String value)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] fields = field.split(";");
        String[] values = value.split(";");
        String[] conditions = new String[fields.length];
        for (int v=0; v<fields.length; v++)
        {
            conditions[v] = fields[v]+"='"+Variables.parse_vars(values[v], false)+"'";
        }
        String condition = FoHeart.combine(conditions, " and ");
        db.delete(tableName, condition, null);
    }

    public synchronized void query(String statement, String returnVar)
    {
        String queryResult = this.query(statement);
        Variables.add(returnVar, "objectList", queryResult);
    }

    public synchronized String query(String statement)
    {
        statement = Variables.parse_vars(statement, false);
        JSONArray arrReturn = new JSONArray();
        List<ContentValues> result = dbHelper.execQuery(appDBModel.db ,statement);

        for (int x=0; x< result.size(); x++)
        {
            ContentValues contentValues = result.get(x);
            JSONObject jsonObject = new JSONObject();
            ArrayList<String> listFields = new ArrayList<String>();
            Set<Map.Entry<String, Object>> values = contentValues.valueSet();
            for (Map.Entry<String, Object> entry : values)
            {
                String key = entry.getKey();
                listFields.add(key);
            }
            for (String field: listFields)
            {
                if(field != null)
                {
                    try
                    {
                        jsonObject.put(field, contentValues.get(field));
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            arrReturn.put(jsonObject);
        }

        return arrReturn.toString();
    }

    public synchronized void queryNumRows(String queryVar, String returnVar)
    {
        String strJSON = Variables.get_type(queryVar);

        if (strJSON.equals("objectList"))
        {
            try {
                JSONArray queryJSON = new JSONArray(Variables.get(queryVar).toString());
                Variables.add(returnVar, "int", queryJSON.length());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            // error
        }
    }

    public synchronized void queryValue(String queryVar, String fieldName, int position, String returnVar)
    {
        String strJSON = Variables.get_type(queryVar);
        if (strJSON.equals("objectList"))
        {
            try {
                if (Variables.get(queryVar) != null)
                {
                    JSONArray queryJSON = new JSONArray(Variables.get(queryVar).toString());

                    if (!queryJSON.isNull(position))
                    {
                        Variables.add(returnVar, "string", queryJSON.getJSONObject(position).get(fieldName));
                    }
                    else
                    {
                        System.out.println("ERROR: record at pos "+position+" not found");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            // error
        }
    }
}
