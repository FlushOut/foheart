package com.flushoutsolutions.foheart.communication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;

import com.flushoutsolutions.foheart.appDataBase.AppDBModel;
import com.flushoutsolutions.foheart.appDataBase.AppDatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.ApplicationData;
import com.flushoutsolutions.foheart.data.InternetStatus;
import com.flushoutsolutions.foheart.data.SendDataData;
import com.flushoutsolutions.foheart.data.TableData;
import com.flushoutsolutions.foheart.data.TableTransactionsData;
import com.flushoutsolutions.foheart.logic.GetData;
import com.flushoutsolutions.foheart.models.ApplicationModel;
import com.flushoutsolutions.foheart.models.SendDataModel;
import com.flushoutsolutions.foheart.models.TableModel;
import com.flushoutsolutions.foheart.models.TableTransactionsModel;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by John on 10/11/2014.
 */
@SuppressLint("SimpleDateFormat")
public class SyncTransaction {
    SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
    private String codeApp = settings.getString("idApplication", "");

    Context appContext = FoHeart.getAppContext();
    private int idApp = ApplicationModel.get_model().get_data(settings.getString("idApplication", ""))._id;

    private String dbUser = ApplicationModel.get_model().get_data(idApp).db_user;
    private String dbPass = ApplicationModel.get_model().get_data(idApp).db_pass;
    private String dbAppName = ApplicationModel.get_model().get_data(idApp).description;

    private int versionDB = TableModel.get_model().get_model_version(idApp);
    AppDBModel appDBModel = new AppDBModel(appContext, codeApp, versionDB).get_model();

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
                List<TableTransactionsData> listTables = TableTransactionsModel.get_model().list();

                for (int tt=0; tt<listTables.size(); tt++)
                {
                    TableTransactionsData thisTable = listTables.get(tt);
                    TableData td = TableModel.get_model().get_data(thisTable.fk_table);

                    final String url = settings.getString("REST_APPS",Connection.REST_APPS)+"check_version_transaction/coduser/"+String.valueOf(settings.getInt("user_id", 0))+"/password/"+settings.getString("user_pass", "")+"/tablename/"+td.name+"/version/"+thisTable.version_server+"/request/"+thisTable.fk_request+"/response/"+thisTable.fk_response+"/appName/" + dbAppName + "/user/" + dbUser + "/pass/" + dbPass;

                    System.out.println("url " + url);

                    if (InternetStatus.isOnline())
                    {
                        String responseString = Connection.get("check_version", url);
                        try
                        {
                            JSONObject responseJSON = new JSONObject(responseString);
                            if (!responseJSON.isNull("status"))
                            {
                                if (responseJSON.getBoolean("status"))
                                {
                                    JSONObject method = new JSONObject("{}");

                                    String user_exclusive = "";
                                    String clear_all = "true";
                                    String block_success = "";
                                    String update_set = "";
                                    String loadingMessage = "";

                                    boolean displayTimeOutDialog =false;
                                    String timeOutDialogTitle ="";
                                    String timeOutMessage ="";
                                    String timeOutDialogButton ="";
                                    String onTimeOut = "";
                                    String ifRepeats = "ignore";

                                    GetData getData = new GetData();
                                    getData.initialize(td.name, "s", td.requestParams, update_set, user_exclusive, clear_all, block_success, loadingMessage, displayTimeOutDialog,timeOutDialogTitle,timeOutMessage, timeOutDialogButton, onTimeOut, ifRepeats);
                                    getData.loadingMessage = "";

                                    getData.request = thisTable.fk_request;
                                    getData.response = thisTable.fk_response;

                                    getData.syncTransaction();
                                }else{
                                    List<ContentValues> result = appDBModel.execQuery("select * from " + td.name + " where fk_client = '" + thisTable.fk_request + "' and fk_user = '" + thisTable.fk_response + "' and _version_local > '" + thisTable.version_server + "'");
                                    List<ContentValues> listResultSynTransaction = new ArrayList<ContentValues>();
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
                                                    if (!field.equals("_sync") && !field.equals("_id") && !field.equals("_version_server") && !field.equals("_version_local") && !field.equals("_date_mobile") && !field.equals("_date_sync"))
                                                        jsonObject.put(field, data.get(field));

                                                    if (field.equals("_id"))
                                                        jsonObject.put("fk_mobile", data.get(field));
                                                    if (field.equals("_date_mobile"))
                                                        jsonObject.put("date_mobile", data.get(field));
                                                    if (field.equals("_version_local"))
                                                        jsonObject.put("version", data.get(field));
                                                }
                                            }
                                        } else {

                                            fields = data.keySet();
                                            for (String field: fields)
                                            {
                                                if (!field.equals("_sync") && !field.equals("_id") && !field.equals("_version_server") && !field.equals("_version_local") && !field.equals("_date_mobile") && !field.equals("_date_sync"))
                                                    jsonObject.put(field, data.get(field));

                                                if (field.equals("_id"))
                                                    jsonObject.put("fk_mobile", data.get(field));
                                                if (field.equals("_date_mobile"))
                                                    jsonObject.put("date_mobile", data.get(field));
                                                if (field.equals("_version_local"))
                                                    jsonObject.put("version", data.get(field));
                                            }
                                        }
                                        int _id = data.getAsInteger("_id");

/*                                        SendDataData sendData = new SendDataData(idApp, settings.getInt("user_id", 0), td.name, _id, jsonObject.toString(), 0, data.getAsString("_date_time_created"));
                                        SendDataModel.get_model().add(sendData);*/

                                        String thekeyAdd ="_id";

                                        if (!td.key.equals(""))
                                            thekeyAdd = td.key;

                                        final String urlSend = settings.getString("REST_APPS",Connection.REST_APPS)+"v2_send_data";
                                        System.out.println("url "+urlSend);
                                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                                        nameValuePairs.add(new BasicNameValuePair("coduser", String.valueOf(settings.getInt("user_id", 0))));
                                        nameValuePairs.add(new BasicNameValuePair("password", settings.getString("user_pass", "")));
                                        nameValuePairs.add(new BasicNameValuePair("codapp", codeApp));
                                        nameValuePairs.add(new BasicNameValuePair("rowid", String.valueOf(_id)));
                                        nameValuePairs.add(new BasicNameValuePair("tablename", td.name));
                                        nameValuePairs.add(new BasicNameValuePair("thekey", thekeyAdd));
                                        nameValuePairs.add(new BasicNameValuePair("request", String.valueOf(thisTable.fk_request)));
                                        nameValuePairs.add(new BasicNameValuePair("response", String.valueOf(thisTable.fk_response)));
                                        nameValuePairs.add(new BasicNameValuePair("record", jsonObject.toString()));

                                        //ApplicationData applicationData = ApplicationModel.get_model().get_data(settings.getString("idApplication", ""));
                                        nameValuePairs.add(new BasicNameValuePair("appName", dbAppName));
                                        nameValuePairs.add(new BasicNameValuePair("user", dbUser));
                                        nameValuePairs.add(new BasicNameValuePair("pass", dbPass));

                                        System.out.println("param-> "+"/coduser/"+ String.valueOf(settings.getInt("user_id", 0)) + "/password/" + settings.getString("user_pass", "") + "/codapp/" + codeApp + "/rowid/" + String.valueOf(_id) + "/tablename/" + td.name + "/thekey/" + thekeyAdd + "/request/" + String.valueOf(thisTable.fk_request) + "/response/" + String.valueOf(thisTable.fk_response) + "/record/" + URLEncoder.encode(jsonObject.toString()));

                                        if (InternetStatus.isOnline())
                                        {
                                            String responseStringSend = Connection.post(urlSend, nameValuePairs);
                                            try
                                            {
                                                JSONObject responseJSONSend = new JSONObject(responseStringSend);
                                                if (!responseJSONSend.isNull("status"))
                                                {
                                                    if (responseJSONSend.getBoolean("status"))
                                                    {
                                                        listResultSynTransaction.add(data);
                                                    }
                                                }
                                            }
                                            catch (JSONException e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    if(result.size() > 0){

                                        final String urlUpdateVersion = settings.getString("REST_APPS",Connection.REST_APPS)+"updateVersion";
                                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                                        nameValuePairs.add(new BasicNameValuePair("coduser", String.valueOf(settings.getInt("user_id", 0))));
                                        nameValuePairs.add(new BasicNameValuePair("password", settings.getString("user_pass", "")));
                                        nameValuePairs.add(new BasicNameValuePair("codapp", codeApp));
                                        nameValuePairs.add(new BasicNameValuePair("tablename", td.name));
                                        nameValuePairs.add(new BasicNameValuePair("request", String.valueOf(thisTable.fk_request)));
                                        nameValuePairs.add(new BasicNameValuePair("response", String.valueOf(thisTable.fk_response)));

                                        nameValuePairs.add(new BasicNameValuePair("appName", dbAppName));
                                        nameValuePairs.add(new BasicNameValuePair("user", dbUser));
                                        nameValuePairs.add(new BasicNameValuePair("pass", dbPass));

                                        String responseStringSend = Connection.post(urlUpdateVersion, nameValuePairs);
                                        try
                                        {
                                            JSONObject responseJSONSend2 = new JSONObject(responseStringSend);
                                            if (!responseJSONSend2.isNull("status"))
                                            {
                                                if (responseJSONSend2.getBoolean("status"))
                                                {
                                                    for (ContentValues resultSynTransaction: listResultSynTransaction) {
                                                        resultSynTransaction.put("_sync","1");
                                                        resultSynTransaction.put("_version_server",responseJSONSend2.getInt("version"));
                                                        appDBModel.setAppTableName(td.name);
                                                        appDBModel.save(resultSynTransaction);
                                                    }
                                                    thisTable.version_server = responseJSONSend2.getInt("version");
                                                    TableTransactionsModel.get_model().save(thisTable);
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
                        catch (JSONException e)
                        {
                            e.printStackTrace();
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
