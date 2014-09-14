package com.flushoutsolutions.foheart;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.communication.Connection;
import com.flushoutsolutions.foheart.communication.Sync;
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

    private void initVars() {
        if (Variables.get("__gps_lat") == null)
            Variables.add("__gps_lat", "float", 0);
        if (Variables.get("__gps_lon") == null)
            Variables.add("__gps_lon", "float", 0);
        if (Variables.get("__gps_speed") == null)
            Variables.add("__gps_speed", "float", 0);
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
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public void onResume() {
        super.onResume();
        if (Config.user_id == 0)
            Config.user_id = 1;

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
                //doShowMenuProcedure();
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
        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
        Variables.add("__user_id", "int", settings.getInt("user_id", 0));
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
