package com.flushoutsolutions.foheart;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.communication.Connection;
import com.flushoutsolutions.foheart.data.InternetStatus;
import com.flushoutsolutions.foheart.globals.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    EditText txt_user_email;
    EditText txt_user_pass;
    CheckBox chk_keep_logged;
    Button btn_login;

    public static MainActivity instance = null;

    public static MainActivity getInstance()
    {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        SharedPreferences settings = getSharedPreferences("userconfigs", 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("REST_AUTH", Connection.REST_AUTH);
        editor.putString("REST_APPS", Connection.REST_APPS);
        editor.commit();

        if (settings.getBoolean("keepLogged", false) && !"".equals(settings.getString("idApplication", "")))
            goAutoLogin();

        setContentView(R.layout.activity_main);
        txt_user_email = (EditText) findViewById(R.id.txt_user_email);
        txt_user_pass = (EditText) findViewById(R.id.txt_user_pass);
        chk_keep_logged = (CheckBox) findViewById(R.id.chk_keep_logged);
        btn_login = (Button) findViewById(R.id.btn_login);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_main) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void goLogin(View view)
    {
        SharedPreferences settings = getSharedPreferences("userconfigs", 0);
        btn_login.setEnabled(false);

        String strEmail = txt_user_email.getText().toString();
        String strPass = txt_user_pass.getText().toString();

        if ("".equals(strEmail) || "".equals(strPass))
        {
            error();
        } else {
            if (InternetStatus.isOnline())
            {
                String stringUrl = settings.getString("REST_AUTH", Connection.REST_AUTH)+"login/email/"+strEmail.trim()+"/password/"+FoHeart.getMD5(strPass.trim());
                String responseString = Connection.get("login", stringUrl);
                try
                {
                    if (null != responseString)
                    {
                        JSONObject responseJSON = new JSONObject(responseString);
                        if (null!=responseJSON)
                        {
                            if (responseJSON.getBoolean("status"))
                            {
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putInt("user_id",Integer.valueOf(responseJSON.getString("userId")));
                                editor.putString("user_email", strEmail);
                                editor.putString("user_pass", strPass);
                                editor.commit();
                                Config.user_id = Integer.valueOf(responseJSON.getString("userId"));

                                // Create the directory
                                File dir = new File (FoHeart.getAppContext().getApplicationInfo().dataDir + "/apps");
                                if(dir.exists()==false)
                                    dir.mkdirs();

                                get_applications(responseJSON.getJSONArray("appcodes"));

                            }else
                                error();
                        }else
                            error_json();

                    }else
                        error_json();

                }catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }else
            {
                error_offline();
            }
        }
    }

    public void goAutoLogin()
    {
        Intent intent = new Intent(this, DownloadActivity.class);
        startActivity(intent);
    }

    public void get_applications(JSONArray appsCode)
    {
        ArrayList<String> lstAppsCode;
        lstAppsCode = new ArrayList<String>();
        SharedPreferences settings = getSharedPreferences("userconfigs", 0);
        SharedPreferences.Editor editor = settings.edit();
        try
        {
            for (int i = 0; i < appsCode.length(); i++)
            {
                JSONObject childJSONObject = appsCode.getJSONObject(i);
                lstAppsCode.add(childJSONObject.getString("code"));
                if (Integer.parseInt(childJSONObject.getString("start_app")) == 1){
                    editor.putString("idApplication",childJSONObject.getString("code"));
                    editor.commit();
                }

            }
        }catch (JSONException e)
        {
        e.printStackTrace();
        }

        Intent intentDownload = new Intent(this, DownloadActivity.class);
        intentDownload.putStringArrayListExtra("apps_code", lstAppsCode);
        startActivity(intentDownload);
        btn_login.setEnabled(true);
    }

    public void error()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Login Error");
        alertDialog.setMessage("Incorrect email or password.");
        alertDialog.setButton("close", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                btn_login.setEnabled(true);
            }
        });
        alertDialog.show();
    }

    public void error_offline()
    {
        btn_login.setEnabled(true);

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Conection Error");
        alertDialog.setMessage("Service Unavailable. Check your Internet connection and try again.");
        alertDialog.setButton("Close", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });
        alertDialog.show();
    }
    public void error_json()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Login Error");
        alertDialog.setMessage("Communication error, try again.");
        alertDialog.setButton("Close", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                btn_login.setEnabled(true);
            }
        });
        alertDialog.show();
    }

    public void onKeepLoggedClick(View view)
    {
        boolean checked = ((CheckBox) view).isChecked();

        switch(view.getId()) {
            case R.id.chk_keep_logged:
                SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
                SharedPreferences.Editor editor = settings.edit();
                if (checked)
                    editor.putBoolean("keepLogged", true);
                else
                    editor.putBoolean("keepLogged", false);
                editor.commit();
            break;
        }
    }
}
