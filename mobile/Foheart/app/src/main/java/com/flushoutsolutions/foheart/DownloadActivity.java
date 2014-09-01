package com.flushoutsolutions.foheart;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;


import com.flushoutsolutions.foheart.R;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.communication.Connection;
import com.flushoutsolutions.foheart.communication.Install;
import com.flushoutsolutions.foheart.data.InternetStatus;
import com.flushoutsolutions.foheart.font.Font;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadActivity extends ActionBarActivity {

    public static DownloadActivity instance;
    ArrayList<String> lstAppsCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_download);

        final ImageView spin = (ImageView) findViewById(R.id.spinner);
        spin.setBackgroundResource(R.drawable.spinner);
        instance = this;

        spin.post(new Runnable() {
            @Override
            public void run() {
                AnimationDrawable frameAnimation =
                        (AnimationDrawable) spin.getBackground();
                frameAnimation.start();
            }
        });

        Intent intentMain = this.getIntent();
        lstAppsCode = intentMain.getStringArrayListExtra("apps_code");

        if (InternetStatus.isOnline()) {

            try {
                new DownloadFilesTask().execute(lstAppsCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            install_apps(lstAppsCode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.download, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_download) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DownloadFilesTask extends AsyncTask<ArrayList<String>, Integer, ArrayList<String>> {
        protected ArrayList<String> doInBackground(ArrayList<String>... appsCode) {
            ArrayList<String> passedAppsCode = appsCode[0];
            try {
                for (int i = 0; i < passedAppsCode.size(); i++) {
                    URL url = new URL(Connection.REST_REP + "app" + passedAppsCode.get(i) + ".zip");
                    downloadFile(url, passedAppsCode.get(i));
                    File dir = new File(FoHeart.getAppContext().getApplicationInfo().dataDir + "/apps/app" + passedAppsCode.get(i));
                    unpackZip(dir + "/", "/app.zip");
                    publishProgress((int) ((i / (float) passedAppsCode.size()) * 100));
                    if (isCancelled()) break;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return passedAppsCode;
        }

        // This is called each time you call publishProgress()
        protected void onProgressUpdate(Integer... progress) {
//	    	System.out.println(progress[0]);
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(ArrayList<String> passedAppsCode) {
            DownloadActivity.instance.install_apps(passedAppsCode);
        }
    }

    public long downloadFile(URL url, String appCode) {
        File dir = new File(FoHeart.getAppContext().getApplicationInfo().dataDir + "/apps/app" + appCode);
        if (dir.exists() == false)
            dir.mkdirs();

        File dirapp = new File(FoHeart.getAppContext().getApplicationInfo().dataDir + "/apps/app" + appCode + "/app");
        if (dirapp.exists() == false)
            dirapp.mkdirs();

        try {
            // Download application
            File file = new File(dir, "app.zip");

            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setConnectTimeout(120 * 1000);
            huc.setRequestMethod("GET");

            int responseCode = huc.getResponseCode();

            if (responseCode == -1) {
                System.out.println("Conection error.");
            } else {
                InputStream is = huc.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                ByteArrayBuffer baf = new ByteArrayBuffer(10000);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baf.toByteArray());
                fos.flush();
                fos.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void install_apps(ArrayList<String> passedAppsCode) {
        new InstallAsyncTask().execute(passedAppsCode);
    }

    private class InstallAsyncTask extends AsyncTask<ArrayList<String>, Integer, Void> {

        @Override
        protected Void doInBackground(ArrayList<String>... passedAppsCode) {
            ArrayList<String> appsCode = passedAppsCode[0];
            Install install = new Install();
            try {
                install.install(appsCode);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Intent intent = new Intent(DownloadActivity.instance, MenuActivity.class);
            startActivity(intent);
        }
    }

    private boolean unpackZip(String path, String zipname) {
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;
            path = path + "app/";
            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }
                FileOutputStream fout = new FileOutputStream(path + filename);
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }
                fout.close();
                zis.closeEntry();
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
