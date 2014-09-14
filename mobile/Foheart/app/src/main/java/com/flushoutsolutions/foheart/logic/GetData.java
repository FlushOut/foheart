package com.flushoutsolutions.foheart.logic;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.flushoutsolutions.foheart.appDataBase.AppDatabaseHelper;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.communication.Connection;
import com.flushoutsolutions.foheart.data.ApplicationData;
import com.flushoutsolutions.foheart.data.InternetStatus;
import com.flushoutsolutions.foheart.globals.Screens;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.models.ApplicationModel;

import org.json.JSONException;

import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Manuel on 09/08/2014.
 */
public class GetData {

    private AppDatabaseHelper dbHelper = AppDatabaseHelper.getHelper();

    private String table_name;
    private String request_type;
    private String request_params="null";
    private String update_set="";
    private String block_success="";
    private String user_exclusive;
    private String clear_all;
    private String if_repeats="ignore";

    public boolean displayTimeOutDialog;
    public String timeOutDialogTitle;
    public String timeOutMessage;
    public String timeOutDialogButton;
    public String onTimeOut;

    String rest_url;

    private ProgressDialog dialog;

    public String loadingMessage;

    private static GetData instance = null;

    public GetData()
    {
        instance = this;
    }

    public static GetData getInstance()
    {
        return instance;
    }


    public void initialize(String table_name, String request_type, String request_params, String update_set, String user_exclusive, String clear_all, String block_success, String loadingMessage, boolean displayTimeOutDialog, String timeOutDialogTitle, String timeOutMessage, String timeOutDialogButton, String onTimeOut, String ifRepeats)
    {
        this.table_name = table_name;
        this.request_type = request_type;
        if (!request_params.equals(""))
            this.request_params = Variables.parse_vars(request_params, false);

        this.update_set = Variables.parse_vars(update_set, false);
        this.user_exclusive = user_exclusive;
        this.clear_all = clear_all;
        this.block_success = block_success;
        this.loadingMessage = loadingMessage;
        this.if_repeats= ifRepeats;

        this.displayTimeOutDialog =displayTimeOutDialog;
        this.timeOutDialogTitle =timeOutDialogTitle;
        this.timeOutMessage = timeOutMessage;
        this.timeOutDialogButton = timeOutDialogButton;
        this.onTimeOut = onTimeOut;

        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        rest_url = settings.getString("REST_APPS", Connection.REST_APPS);
    }

    public String getIfRepeats()
    {
        return this.if_repeats;
    }

    public String getClearAll()
    {
        return this.clear_all;
    }

    public String getUserExclusive()
    {
        return this.user_exclusive;
    }

    public String getBlockSuccess()
    {
        return this.block_success;
    }

    public int getNumRows(SQLiteDatabase db,String table, int id)
    {
        List<ContentValues> list = dbHelper.execQuery(db,"select * from " + table + " WHERE _id=" + id);
        return list.size();
    }



    public void executeQuery()
    {
        String request_params_64 = URLEncoder.encode(this.request_params);

        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        ApplicationData applicationData = ApplicationModel.get_model().get_data(settings.getString("idApplication", ""));

        String stringUrl = rest_url+"get_data/coduser/"+settings.getInt("user_id", 0)+"/password/"+settings.getString("user_pass", "")+"/codapp/"+settings.getString("idApplication", "")+"/tablename/"+this.table_name+"/captureversion/1.0.1/appversion/1.0.0/requesttype/"+this.request_type+"/requestparam/"+request_params_64+"/userexclusive/"+String.valueOf(this.getUserExclusive())+"/appName/" + applicationData.description + "/user/" + applicationData.db_user + "/pass/" + applicationData.db_pass;

        String update_set_64 = URLEncoder.encode(this.update_set);

        if (this.request_type.equals("u"))
        {
            stringUrl += "/updateset/"+update_set_64;
        }

        System.out.println("stringUrl "+stringUrl);

        if (InternetStatus.isOnline())
        {
            if (!"".equals(this.loadingMessage))
            {
                dialog = ProgressDialog.show(Screens.currentCtx, "", this.loadingMessage);
                dialog.setCancelable(false);
            }
            Connection.sync_locked = true;
            Connection.get("get_data", stringUrl);
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
            return "Connection error";
        else
            return this.timeOutDialogTitle;
    }
    public String getTimeOutMessage()
    {
        if ("".equals(this.timeOutMessage))
            return "Connection time expired";
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

    public void onRestError()
    {
        Screens.currentInstance.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog alertDialog = new AlertDialog.Builder(Screens.currentCtx).create();
                alertDialog.setTitle("Communication error.");
                alertDialog.setMessage("There was an error communicating with the server.");
                alertDialog.setButton("Close", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                alertDialog.show();

            }

        });
    }
}
