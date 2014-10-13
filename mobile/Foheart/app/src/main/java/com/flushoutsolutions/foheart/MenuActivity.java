package com.flushoutsolutions.foheart;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.flushoutsolutions.foheart.appDataBase.DataBaseDebug;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.communication.Connection;
import com.flushoutsolutions.foheart.communication.Sync;
import com.flushoutsolutions.foheart.communication.SyncMaster;
import com.flushoutsolutions.foheart.communication.SyncTransaction;
import com.flushoutsolutions.foheart.data.ApplicationData;
import com.flushoutsolutions.foheart.data.ViewData;
import com.flushoutsolutions.foheart.design.ButtonMenuAdapter;
import com.flushoutsolutions.foheart.design.Color;
import com.flushoutsolutions.foheart.globals.Config;
import com.flushoutsolutions.foheart.globals.Screens;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.json.JSONParser;
import com.flushoutsolutions.foheart.logic.Procedures;
import com.flushoutsolutions.foheart.models.ApplicationModel;
import com.flushoutsolutions.foheart.models.ViewModel;
import com.flushoutsolutions.foheart.slidingmenu.adapter.NavDrawerListAdapter;
import com.flushoutsolutions.foheart.slidingmenu.fragment.MenuFragment;
import com.flushoutsolutions.foheart.slidingmenu.model.NavDrawerItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MenuActivity extends ActionBarActivity {

    public ScheduledFuture<?> scheduledFuture;
    public ScheduledExecutorService scheduledExecutorService;
    public static MenuActivity instance;

    boolean firstLoad = true;
    ProgressDialog progressDialog;

    public static MenuActivity getInstance() {
        return instance;
    }


    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    File imgFile;
    Bitmap bmpLogo;
    Resources res;
    BitmapDrawable icon;
    List<ApplicationData> lstAppData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        instance = this;

        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        imgFile = new File(FoHeart.getAppContext().getApplicationInfo().dataDir + "/apps/app" + settings.getString("idApplication", "") + "/app/logo.png");
        bmpLogo = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        res = getResources();
        icon = new BitmapDrawable(res, bmpLogo);

        Screens.currentCtx = MenuActivity.this;
        Screens.currentInstance = this;

        // Initiate application vars
        initVars();

        //Sync Information
        //syncInformation();
        //syncMaster();
        syncTransaction();
        /*
         0 - Initialize database
         1 - Create menu
         2 - doMainProcedure
         3 - doOnShowMenuProcedure
          */

        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        lstAppData = ApplicationModel.get_model().list();
        for (int i = 0; i < lstAppData.size(); i++) {
            ApplicationData applicationData = lstAppData.get(i);
            navDrawerItems.add(new NavDrawerItem(applicationData.description));
        }

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                getSupportActionBar().setIcon(icon);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                getActionBar().setIcon(R.drawable.ic_launcher);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            ApplicationData applicationData = ApplicationModel.get_model().get_data(settings.getString("idApplication", ""));
            displayView(applicationData.description);
        }
    }

    public synchronized void doMainProcedure()
    {
        try
        {
            Procedures mainProcedure = new Procedures();
            mainProcedure.initialize("main", null);
            mainProcedure.execute();
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
        }
    }

    public synchronized void doShowMenuProcedure()
    {
        try
        {
            Procedures mainProcedure = new Procedures();
            mainProcedure.initialize("_onShowMenu", null);
            mainProcedure.execute();

        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
        }
    }

    private void initVars() {
        clearVars();
        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);

        if (Variables.get("__user_id") == null)
            Variables.add("__user_id", "int", settings.getInt("user_id", 0));
        if (Variables.get("__gps_lat") == null)
            Variables.add("__gps_lat", "float", 0);
        if (Variables.get("__gps_lon") == null)
            Variables.add("__gps_lon", "float", 0);
        if (Variables.get("__gps_speed") == null)
            Variables.add("__gps_speed", "float", 0);
        if (Variables.get("__gps_accuracy") == null)
            Variables.add("__gps_accuracy", "int", 0);
    }

    private void clearVars() {
        Variables.removeAll();
    }

    private void syncInformation() {
        scheduledExecutorService = Executors.newScheduledThreadPool(5);
        final Sync sync = new Sync();
        scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        if (Connection.sync_locked == false) {
                            sync.run();
                        }
                    }
                }, 5, 5, TimeUnit.SECONDS
        );
    }

    private void syncMaster() {
        final SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        ApplicationData applicationData = ApplicationModel.get_model().get_data(settings.getString("idApplication", ""));

        scheduledExecutorService = Executors.newScheduledThreadPool(5);
        final SyncMaster sync = new SyncMaster();
        scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        if (Connection.sync_locked == false) {
                            sync.run();
                        }
                    }
                }, 60, 60, TimeUnit.SECONDS
        );
    }

    private void syncTransaction() {
        final SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        ApplicationData applicationData = ApplicationModel.get_model().get_data(settings.getString("idApplication", ""));

        scheduledExecutorService = Executors.newScheduledThreadPool(5);
        final SyncTransaction sync = new SyncTransaction();
        scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        if (Connection.sync_locked == false) {
                            sync.run();
                        }
                    }
                }, 120, 120, TimeUnit.SECONDS
        );
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            NavDrawerItem navDrawerItem = (NavDrawerItem)mDrawerList.getItemAtPosition(position);
            displayView(navDrawerItem.getTitle());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_menu:
                return true;
            case R.id.menu_logout:
                doLogout();
                return true;
            case R.id.menu_backup:
                backupDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void doLogout()
    {
        final SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);

        if (settings.getBoolean("locked_logout", false))
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Logout do aplicativo");
            alert.setMessage("Digite o código de desbloqueio");

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    // Do something with value!


                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet httpget = new HttpGet(settings.getString("restURL", "http://rest.airclic.net.br/")+
                            "get_hash/coduser/"+settings.getInt("id_user", 0)+
                            "/codapp/"+settings.getString("idApplication", "")+
                            "/hash/"+value);


                    try
                    {
                        HttpResponse response = httpclient.execute(httpget);
                        HttpEntity entity = response.getEntity();

                        String responseString = EntityUtils.toString(entity, "UTF-8");

                        JSONObject responseJSON = new JSONObject(responseString);
                        System.out.println(responseJSON);
                        if (responseJSON.getBoolean("status"))
                        {
                            proceedLogout();
                        }
                        else
                        {

                        }

                    }
                    catch (ClientProtocolException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            });

            alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to exit the application?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            proceedLogout();
                        }
                    })
                    .setNegativeButton("No", null).show();
        }
    }

    private void proceedLogout()
    {
        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        SharedPreferences.Editor editor = settings.edit();

        String stringUrl = settings.getString("REST_AUTH", Connection.REST_AUTH)+"logout/email/"+settings.getString("user_email", "");
        String responseString = Connection.get("logout", stringUrl);
        try
        {
            if (null != responseString)
            {
                JSONObject responseJSON = new JSONObject(responseString);
                if (null!=responseJSON)
                {
                    if (responseJSON.getBoolean("status"))
                    {
                        editor.putBoolean("keepLogged", false);
                        editor.putInt("id_user", 0);
                        editor.putString("idApplication", "");
                        editor.commit();

                        if (DownloadActivity.instance!=null) DownloadActivity.instance.finish();
                        //if (LicenceActivity.getInstance()!=null) LicenceActivity.getInstance().finish();
                        finish();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("error");
        }
    }

    public void backupDB()
    {
        System.out.println("inicando backup");
        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        String dbname = "db"+ settings.getString("idApplication","")+".db";
        String dbname2 = "Foheart.db";

        if (DataBaseDebug.exportDatabase(dbname))
            Toast.makeText(getBaseContext(), "Saved on the Memory Card", Toast.LENGTH_LONG).show();
        else
            System.out.println("Unsaved");

        if (DataBaseDebug.exportDatabase(dbname2))
            Toast.makeText(getBaseContext(), "Saved on the Memory Card", Toast.LENGTH_LONG).show();
        else
            System.out.println("Unsaved");

    }

    /* *
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_menu).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed()
    {
    }


    @Override
    public void onResume() {
        super.onResume();
        if (Config.user_id == 0)
            Config.user_id = 1;

        initVars();
        Screens.currentCtx = MenuActivity.this;
        Screens.current = "__mainmenu";
        if (firstLoad)
            firstLoad = false;
        else
            new MenuActivityAsyncTask().execute(3);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView(String description) {
        ApplicationData applicationData = ApplicationModel.get_model().get_data_by_desc(description);
        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("idApplication", applicationData.code);
        editor.commit();

        int countListView = mDrawerList.getAdapter().getCount();
        Fragment fragment = null;

        for (int i = 0; i < countListView; i++) {
            NavDrawerItem navDrawerItem = (NavDrawerItem) mDrawerList.getItemAtPosition(i);
            if (navDrawerItem.getTitle().equals(applicationData.description)) {
                fragment = new MenuFragment();
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment).commit();
                    mDrawerList.setItemChecked(i, true);
                    mDrawerList.setSelection(i);
                    setTitle(navDrawerItem.getTitle());
                    mDrawerLayout.closeDrawer(mDrawerList);
                    break;
                } else {
                    Log.e("MenuActivity", "Error in creating fragment");
                }
            }
        }

        new MenuActivityAsyncTask().execute(0);
        new MenuActivityAsyncTask().execute(1);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public class MenuActivityAsyncTask extends AsyncTask<Integer, Integer, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... params)
        {
            return params[0];
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Integer result)
        {
            if (result == 1)
            {
                createMenu();
                new MenuActivityAsyncTask().execute(2);
            }
            else if (result == 2)
            {
                doMainProcedure();
                new MenuActivityAsyncTask().execute(3);
            }
            else if (result == 3)
            {
                doShowMenuProcedure();
                if (null != progressDialog) progressDialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute()
        {
            if (null == progressDialog)
            {
                progressDialog = ProgressDialog.show(MenuActivity.getInstance(), "Wait", "Starting the application...");
                progressDialog.setCancelable(false);
            }
        }
    }
    public void createMenu()
    {
        GridView grdView = (GridView) findViewById(R.id.menuGridView);
        getSupportActionBar().setIcon(icon);


        try {
            grdView.setAdapter(new ButtonMenuAdapter(this));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        grdView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                try {
                    openScreen(position);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openScreen(int i) throws IOException, JSONException
    {
        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        String codeApp = settings.getString("idApplication", "");

        JSONParser menuJson = new JSONParser("mainmenu.json");
        JSONArray jsonArray = menuJson.get_json_array();
        JSONObject jsonObject = jsonArray.getJSONObject(i);

        ApplicationModel appModel = ApplicationModel.get_model();
        int fk_app = appModel.get_data(codeApp)._id;

        ViewData viewData = ViewModel.get_model().get_data(fk_app, jsonObject.getString("view"));

        if (viewData == null)
            System.out.println("View not found");
        else
        {
            Color.set_active_theme(jsonObject.getString("color"));

            Intent intent = new Intent(this, ScreenActivity.class);
            intent.putExtra("viewName", jsonObject.getString("view"));
            startActivity(intent);
        }

    }

}
