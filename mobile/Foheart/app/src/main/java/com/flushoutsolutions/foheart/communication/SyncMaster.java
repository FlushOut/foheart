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
import com.flushoutsolutions.foheart.logic.GetData;
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
public class SyncMaster {

    SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
    private String codeApp = settings.getString("idApplication", "");

    Context appContext = FoHeart.getAppContext();
    private int idApp = ApplicationModel.get_model().get_data(settings.getString("idApplication", ""))._id;

    private String dbUser = ApplicationModel.get_model().get_data(idApp).db_user;
    private String dbPass = ApplicationModel.get_model().get_data(idApp).db_pass;
    private String dbAppName = ApplicationModel.get_model().get_data(idApp).description;

    private int versionDB = TableModel.get_model().get_model_version(idApp);
    AppDBModel appDBModel = new AppDBModel(appContext, codeApp, versionDB).get_model();
    TableModel entityModel = TableModel.get_model();

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

                    final String url = settings.getString("REST_APPS",Connection.REST_APPS)+"check_version/coduser/"+String.valueOf(settings.getInt("user_id", 0))+"/password/"+settings.getString("user_pass", "")+"/tablename/"+thisTable.name+"/version/"+thisTable.version_local+"/appName/" + dbAppName + "/user/" + dbUser + "/pass/" + dbPass;

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
                                    getData.initialize(thisTable.name, "s", thisTable.requestParams, update_set, user_exclusive, clear_all, block_success, loadingMessage, displayTimeOutDialog,timeOutDialogTitle,timeOutMessage, timeOutDialogButton, onTimeOut, ifRepeats);
                                    getData.loadingMessage = "";
                                    getData.executeQuery();

                                    /* Update record sync ok*/
                                    TableData entityData = new TableData(idApp,thisTable.model_version,thisTable.name,thisTable.auto_sync, thisTable.key,thisTable.version_local,responseJSON.getInt("version"),thisTable.requestParams);
                                    entityData._id = thisTable._id;
                                    entityModel.save(entityData);
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
