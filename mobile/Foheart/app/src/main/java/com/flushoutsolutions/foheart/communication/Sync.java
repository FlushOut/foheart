package com.flushoutsolutions.foheart.communication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Base64;

import com.flushoutsolutions.foheart.appDataBase.AppDBModel;
import com.flushoutsolutions.foheart.appDataBase.AppDatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.InternetStatus;
import com.flushoutsolutions.foheart.data.SendDataData;
import com.flushoutsolutions.foheart.data.TableData;
import com.flushoutsolutions.foheart.models.ApplicationModel;
import com.flushoutsolutions.foheart.models.SendDataModel;
import com.flushoutsolutions.foheart.models.TableModel;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Manuel on 10/08/2014.
 */
@SuppressLint("SimpleDateFormat")
public class Sync {

    SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
    private String codeApp = settings.getString("idApplication", "");

    Context appContext = FoHeart.getAppContext();
    private int idApp = ApplicationModel.get_model().get_data(settings.getString("idApplication", ""))._id;

    private String dbUser = ApplicationModel.get_model().get_data(idApp).db_user;
    private String dbPass = ApplicationModel.get_model().get_data(idApp).db_pass;
    private String dbHost = ApplicationModel.get_model().get_data(idApp).db_host;
    private String dbName = ApplicationModel.get_model().get_data(idApp).db_name;
    private String dbPort = ApplicationModel.get_model().get_data(idApp).db_port;
    private int versionDB = TableModel.get_model().get_model_version(idApp);

    private AppDatabaseHelper dbHelper = new AppDatabaseHelper(appContext,codeApp,versionDB).getHelper();

    public String toBase64fromString(String text)
    {
        return Base64.encodeToString(text.getBytes(), Base64.DEFAULT);
    }

    public synchronized void run()
    {
        if (InternetStatus.isOnline())
        {
            try
            {
                List<TableData> listTables = TableModel.get_model().listSyncable();

                for (int tt=0; tt<listTables.size(); tt++)
                {
                    TableData thisTable = listTables.get(tt);
                    List<ContentValues> result = dbHelper.execQuery("select * from " + thisTable.name + " where _sync='' OR _sync=0 OR _sync is null limit 5");
                    for (int x=0; x< result.size(); x++)
                    {
                        JSONObject jsonObject = new JSONObject();
                        ContentValues data = result.get(x);
                        Set<String> fields;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                            for (Map.Entry<String, Object> item : data.valueSet()) {
                                String field = item.getKey();
                                if(field != null)
                                {
                                    if (!field.equals("_sync") && !field.equals("_date_time_created") && !field.equals("_date_time_updated") && !field.equals("_id"))
                                    {
                                        jsonObject.put(field, data.get(field));
                                    }

                                    if (field.equals("_date_time_created"))
                                    {
                                        jsonObject.put("date_time", data.get(field));
                                    }
                                }
                            }
                        } else {

                            fields = data.keySet();
                            for (String field: fields)
                            {
                                if(field != null)
                                {
                                    if (!field.equals("_sync") && !field.equals("_date_time_created") && !field.equals("_date_time_updated") && !field.equals("_id"))
                                    {
                                        jsonObject.put(field, data.get(field));
                                    }

                                    if (field.equals("_date_time_created"))
                                    {
                                        jsonObject.put("date_time", data.get(field));
                                    }
                                }
                            }
                        }
                        int _id = data.getAsInteger("_id");
                        SendDataData sendData = new SendDataData(idApp, settings.getInt("user_id", 0), thisTable.name, _id, jsonObject.toString(), 0, data.getAsString("_date_time_created"));
                        SendDataModel.get_model().add(sendData);
                        String thekeyAdd ="_id";

                        if (!thisTable.key.equals(""))
                            thekeyAdd = thisTable.key;

                        final String url = settings.getString("REST_APPS",Connection.REST_APPS)+"v2_send_data";
                        System.out.println("url "+url);
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                        nameValuePairs.add(new BasicNameValuePair("coduser", String.valueOf(settings.getInt("user_id", 0))));
                        nameValuePairs.add(new BasicNameValuePair("password", settings.getString("user_pass", "")));
                        nameValuePairs.add(new BasicNameValuePair("codapp", codeApp));
                        nameValuePairs.add(new BasicNameValuePair("rowid", String.valueOf(sendData.row_id)));
                        nameValuePairs.add(new BasicNameValuePair("tablename", sendData.table_name));
                        nameValuePairs.add(new BasicNameValuePair("datetime", String.valueOf(sendData.datetime)));
                        nameValuePairs.add(new BasicNameValuePair("thekey", thekeyAdd));
                        nameValuePairs.add(new BasicNameValuePair("record", sendData.record));

                        /* DB parameters */
                        nameValuePairs.add(new BasicNameValuePair("dbUser", dbUser));
                        nameValuePairs.add(new BasicNameValuePair("dbPass", dbPass));
                        nameValuePairs.add(new BasicNameValuePair("dbHost", dbHost));
                        nameValuePairs.add(new BasicNameValuePair("dbName", dbName));
                        nameValuePairs.add(new BasicNameValuePair("dbPort", dbPort));

                        if (InternetStatus.isOnline())
                        {
                            String responseString = Connection.post(url, nameValuePairs);
                            try
                            {
                                JSONObject responseJSON = new JSONObject(responseString);
                                if (!responseJSON.isNull("status"))
                                {
                                    if (responseJSON.getBoolean("status"))
                                    {
                                        // Delete from table
                                        SendDataModel dataMod = SendDataModel.get_model();
                                        SendDataData dataCurrent = dataMod.get_data(responseJSON.getInt("rowid"), responseJSON.getString("table"));
                                        if (dataCurrent!=null) dataMod.delete(dataCurrent);

                                        /* Update record sync ok*/
                                        AppDBModel appDBModel = new AppDBModel(appContext, codeApp, versionDB, thisTable.name).get_model();
                                        ContentValues value = appDBModel.get_data(_id);
                                        value.put("_sync", "1");
                                        appDBModel.save(value);
                                    }
                                }
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
