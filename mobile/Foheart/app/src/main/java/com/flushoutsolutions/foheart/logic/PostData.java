package com.flushoutsolutions.foheart.logic;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.TelephonyManager;

import com.flushoutsolutions.foheart.appDataBase.AppDatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.communication.Connection;
import com.flushoutsolutions.foheart.data.InternetStatus;
import com.flushoutsolutions.foheart.globals.Screens;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.models.ApplicationModel;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Manuel on 10/08/2014.
 */
public class PostData {

    private AppDatabaseHelper dbHelper = AppDatabaseHelper.getHelper();

    private String table_name;
    private String insert_params="null";
    private String block_success="";
    private boolean user_exclusive;
    private String var_id_return ="";

    public boolean displayTimeOutDialog;
    public String timeOutDialogTitle;
    public String timeOutMessage;
    public String timeOutDialogButton;
    public String onTimeOut;

    private ProgressDialog dialog;

    public String loadingMessage;

    private static PostData instance = null;

    public PostData()
    {
        instance = this;
    }

    public static PostData getInstance()
    {
        return instance;
    }

    public static String implode(Object[] ary, String delim)
    {
        String out = "";
        for(int i=0; i<ary.length; i++) {
            if(i!=0) { out += delim; }
            out += ary[i].toString();
        }
        return out;
    }

    public void initialize(String table_name, String insert_params, boolean user_exclusive, String var_id_return, String block_success, String loadingMessage, boolean displayTimeOutDialog, String timeOutDialogTitle, String timeOutMessage, String timeOutDialogButton, String onTimeOut)
    {
        this.table_name = table_name;
        if (!insert_params.equals(""))
            this.insert_params = Variables.parse_vars(insert_params, false);

        this.var_id_return= var_id_return;
        this.user_exclusive = user_exclusive;
        this.block_success = block_success;
        this.loadingMessage = loadingMessage;

        this.displayTimeOutDialog =displayTimeOutDialog;
        this.timeOutDialogTitle =timeOutDialogTitle;
        this.timeOutMessage = timeOutMessage;
        this.timeOutDialogButton = timeOutDialogButton;
        this.onTimeOut = onTimeOut;
    }

    public boolean getUserExclusive()
    {
        return this.user_exclusive;
    }

    public String getBlockSuccess()
    {
        return this.block_success;
    }

    /*public int getNumRows(SQLiteDatabase db,String table, int id)
    {
        List<ContentValues> list = dbHelper.execQuery(db,"select * from " + table + " WHERE _id=" + id);
        return list.size();
    }*/



    public void executeQuery()
    {
        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        String stringUrl = settings.getString("REST_APPS", Connection.REST_APPS)+"post_data";

        if (InternetStatus.isOnline())
        {
            if (!"".equals(this.loadingMessage))
            {
                dialog = ProgressDialog.show(Screens.currentCtx, "", this.loadingMessage);
                dialog.setCancelable(false);
            }

            TelephonyManager tm = (TelephonyManager)FoHeart.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair("__coduser", String.valueOf(settings.getInt("user_id", 0))));
            nameValuePairs.add(new BasicNameValuePair("__password", String.valueOf(settings.getInt("user_pass", 0))));
            nameValuePairs.add(new BasicNameValuePair("__tablename", this.table_name));
            nameValuePairs.add(new BasicNameValuePair("__user_exclusive", String.valueOf(this.user_exclusive)));
            nameValuePairs.add(new BasicNameValuePair("__imei", String.valueOf(tm.getDeviceId())));

            JSONObject jsonValue = null;
            try
            {
                jsonValue = new JSONObject(this.insert_params);
                Iterator<Object> keys = (Iterator)jsonValue.keys();

                while (keys.hasNext())
                {
                    String fieldName = keys.next().toString();
                    String record = Variables.parse_vars(jsonValue.getString(fieldName), false).replaceAll("\t", " ");

                    nameValuePairs.add(new BasicNameValuePair(fieldName.toString(), record.trim()));
                }

                Connection.sync_locked = true;
                String response = Connection.post(stringUrl, nameValuePairs);

                JSONObject jsonResponse = new JSONObject(response);
                if (!jsonResponse.isNull("status"))
                {
                    if (jsonResponse.getBoolean("status"))
                    {
                        Variables.add(this.var_id_return, "int", jsonResponse.getInt("rowid"));
                        Procedures proc = new Procedures();
                        proc.exec(PostData.getInstance().getBlockSuccess());
                    }
                    else
                    {
                        PostData.getInstance().onTimeOutEvent();
                    }
                }
                else
                {
                    PostData.getInstance().onTimeOutEvent();
                }

                Connection.sync_locked = false;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    public ProgressDialog get_alert()
    {
        return this.dialog;
    }


    public String getOnTimeOut()
    {
        return this.onTimeOut;
    }

    public String getTimeOutDialogTitle()
    {
        if ("".equals(this.timeOutDialogTitle))
            return "Connection error.";
        else
            return this.timeOutDialogTitle;
    }
    public String getTimeOutMessage()
    {
        if ("".equals(this.timeOutMessage))
            return "Connection time expired.";
        else
            return this.timeOutMessage;
    }
    public String getTimeOutDialogButton()
    {
        if ("".equals(this.timeOutDialogButton))
            return "Close";
        else
            return this.timeOutDialogButton;
    }

    private void timeOutEvent()
    {
        Connection.sync_locked = false;
        if (getOnTimeOut()!=null && !getOnTimeOut().equals(""))
        {
            try
            {
                Procedures mainProcedure = new Procedures();
                mainProcedure.initialize(getOnTimeOut(), null);
                mainProcedure.execute();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void onTimeOutEvent()
    {
        if (this.displayTimeOutDialog)
        {
            Screens.currentInstance.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(Screens.currentCtx).create();
                    alertDialog.setTitle(getTimeOutDialogTitle());
                    alertDialog.setMessage(getTimeOutMessage());
                    alertDialog.setButton(getTimeOutDialogButton(), new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            System.out.println("Timeout event.");
                            timeOutEvent();
                        }
                    });
                    alertDialog.show();

                }

            });
        }
        else
            timeOutEvent();
    }
}
