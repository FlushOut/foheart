package com.flushoutsolutions.foheart;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.ViewData;
import com.flushoutsolutions.foheart.data.ViewModuleData;
import com.flushoutsolutions.foheart.design.Color;
import com.flushoutsolutions.foheart.font.Font;
import com.flushoutsolutions.foheart.globals.Components;
import com.flushoutsolutions.foheart.globals.Config;
import com.flushoutsolutions.foheart.globals.Screens;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.logic.Procedures;
import com.flushoutsolutions.foheart.models.ApplicationModel;
import com.flushoutsolutions.foheart.models.ViewModel;
import com.flushoutsolutions.foheart.models.ViewModuleModel;
import com.flushoutsolutions.foheart.modules.ActivityIndicator;
import com.flushoutsolutions.foheart.modules.CheckGroup;
import com.flushoutsolutions.foheart.modules.CmdButton;
import com.flushoutsolutions.foheart.modules.Combobox;
import com.flushoutsolutions.foheart.modules.DateTimePicker;
import com.flushoutsolutions.foheart.modules.Geolocation;
import com.flushoutsolutions.foheart.modules.GroupedViews;
import com.flushoutsolutions.foheart.modules.Label;
import com.flushoutsolutions.foheart.modules.Line;
import com.flushoutsolutions.foheart.modules.ListCheckScreen;
import com.flushoutsolutions.foheart.modules.ListOptions;
import com.flushoutsolutions.foheart.modules.ListScreen;
import com.flushoutsolutions.foheart.modules.Map;
import com.flushoutsolutions.foheart.modules.PhotoInput;
import com.flushoutsolutions.foheart.modules.Progress;
import com.flushoutsolutions.foheart.modules.ReverseGeocode;
import com.flushoutsolutions.foheart.modules.Spacer;
import com.flushoutsolutions.foheart.modules.Textfield;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScreenActivity extends FragmentActivity {

    private ViewGroup layout;
    private ViewData viewData;
    private String parent_screen;
    private String name;

    public boolean back_locked = false;

    private String ev_on_load = null;
    private String ev_on_show = null;
    private String ev_on_stop = null;
    private String ev_on_action = null;
    private String ev_on_back = null;

    private boolean actionBtn = false;
    private String titleActionBtn = "";
    private int fk_app = 0;

    public FragmentManager fragmentManager;

    File imgFile;
    Bitmap bmpLogo;
    Resources res;
    BitmapDrawable icon;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);

        String codeApp = settings.getString("idApplication", "");

        ApplicationModel appModel = ApplicationModel.get_model();
        this.fk_app = appModel.get_data(codeApp)._id;

        Intent intent = getIntent();
        layout = (ViewGroup) findViewById(R.id.linear);
        ViewModel viewModel = ViewModel.get_model();
        viewData = viewModel.get_data(this.fk_app, intent.getStringExtra("viewName"));
        this.parent_screen  =  intent.getStringExtra("parent_screen");
        this.name = intent.getStringExtra("viewName");

        if (null!=viewData)
        {
            fragmentManager = getSupportFragmentManager();

            if (viewData.back_locked == 1) this.back_locked = true;

            //Icon Image
            imgFile = new File(FoHeart.getAppContext().getApplicationInfo().dataDir + "/apps/app" + settings.getString("idApplication", "") + "/app/logo.png");
            bmpLogo = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            res = getResources();
            icon = new BitmapDrawable(res, bmpLogo);
            getActionBar().setIcon(icon);

            // Title
            getActionBar().setTitle(viewData.title.toUpperCase(Locale.getDefault()));
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parse_color(Color.get_active_theme())));
            int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
            TextView abTitle = (TextView) findViewById(titleId);
            abTitle.setTypeface(Font.get_font("bold condensed"));
            abTitle.setTextSize(25);
            abTitle.setTextColor(getResources().getColor(R.color.title_action_bar));

            // Back button
            getActionBar().setDisplayHomeAsUpEnabled(true);

            // Action button
            if (viewData.button_title== null)
            {
                actionBtn = false;
                titleActionBtn = "";
            }
            else
            {
                actionBtn = true;
                titleActionBtn = viewData.button_title.toUpperCase();
                this.ev_on_action = viewData.button_action;
            }

            Screens.add(this.name, this);

            if (viewData.events!= null)
            {
                try {
                    JSONObject jsonEvents = new JSONObject(viewData.events);

                    if (!jsonEvents.isNull("onLoad"))
                        this.ev_on_load = jsonEvents.getString("onLoad");

                    if (!jsonEvents.isNull("onShow"))
                        this.ev_on_show = jsonEvents.getString("onShow");

                    if (!jsonEvents.isNull("onStop"))
                        this.ev_on_stop = jsonEvents.getString("onStop");

                    if (!jsonEvents.isNull("onBack"))
                        this.ev_on_back = jsonEvents.getString("onBack");

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            on_load();

            ViewModuleModel viewModuleModel = ViewModuleModel.get_model();
            List<ViewModuleData> list = viewModuleModel.list(viewData._id);

            try {
                create_components(list, null);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (Config.user_id == 0)
            finish();

        Screens.current = this.name;
        Screens.currentCtx = ScreenActivity.this;
        Screens.currentInstance = this;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        on_show();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (this.ev_on_stop!=null && !this.ev_on_stop.equals(""))
        {
            try {
                Procedures mainProcedure = new Procedures();
                mainProcedure.initialize(this.ev_on_stop, "{'sender':'"+this.name+"'}");
                mainProcedure.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.screen, menu);
        MenuItem item = menu.findItem(R.id.action_screen);

        if(actionBtn)
            item.setVisible(true);
        else
        item.setVisible(false);
        item.setTitle(titleActionBtn);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                break;
            case R.id.action_screen:
                goAction();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && null != data)
        {
            Uri selectedImage = data.getData();
            Components.selectedImageView.setImageBitmap(BitmapFactory.decodeFile(getPath(selectedImage)));
        }
        else if (requestCode == 2 && resultCode == RESULT_OK && null != data)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    500
            );
            Components.selectedImageView.setLayoutParams(params);
            Components.selectedImageView.setImageBitmap(photo);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Uri uri)
    {
        Context context = FoHeart.getAppContext();

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme()))
        {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public void create_grouped_views(int countList, JSONArray arrayModules, String gv_name, ViewGroup viewGrouped)
    {
        try
        {
            this.layout.addView(viewGrouped);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            int qtd = arrayModules.length();

            for (int v=0; v<countList; v++)
            {
                List<ViewModuleData> listCp = new ArrayList<ViewModuleData>();
                for (int x=0; x<qtd; x++)
                {
                    JSONObject jsonModule = arrayModules.getJSONObject(x);
                    ViewModuleData vmData = new ViewModuleData(viewData._id, jsonModule.getString("module"), gv_name+"_"+jsonModule.getString("name")+"_"+v, jsonModule.getString("properties"), jsonModule.getString("events"));


                    listCp.add(vmData);
                }

                this.create_components(listCp, viewGrouped);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void create_components(List<ViewModuleData> list, ViewGroup vgroup) throws JSONException, IOException
    {
        if (vgroup == null) vgroup = layout;

        for (int x =0; x<list.size(); x++)
        {
            ViewModuleData vmData = list.get(x);

            // Add spacer
            if (vmData.module.toString().equals("spacer"))
            {
                Spacer spc = new Spacer();
                spc.initialize(viewData.name, vmData.name, vgroup, vmData.properties);
                spc.render();

                Components.add(viewData.name + "__" + vmData.name, "spacer", spc);
            }
            // Add separator line
            else if (vmData.module.toString().equals("line"))
            {
                Line lineSep = new Line();
                lineSep.initialize(viewData.name, vmData.name, vgroup, vmData.properties);
                lineSep.render();

                Components.add(viewData.name+"__"+vmData.name, "line", lineSep);
            }

            // Add Label
            else if (vmData.module.toString().equals("label"))
            {
                Label label = new Label();
                label.initialize(viewData.name, vmData.name, vgroup, vmData.properties);
                label.render();

                Components.add(viewData.name + "__" + vmData.name, "label", label);
            }
            // Add a Textfield
            else if (vmData.module.toString().equals("textfield"))
            {
                Textfield textfield = new Textfield(vmData.name, this.name, vgroup, vmData.properties, vmData.events);
                textfield.render();
                Components.add(viewData.name+"__"+vmData.name, "textfield", textfield);
            }
            // Add a Button
            else if (vmData.module.toString().equals("button"))
            {
                CmdButton button = new CmdButton(vmData.name, this.name, vgroup, vmData.properties, vmData.events);
                button.render();
                Components.add(viewData.name+"__"+vmData.name, "button", button);
            }
            // Add a Spinner
            else if (vmData.module.toString().equals("spinner"))
            {
                ActivityIndicator spinner = new ActivityIndicator(viewData.name, this.name, vmData.name, vgroup, vmData.properties);
                spinner.render();
                Components.add(viewData.name+"__"+vmData.name, "spinner", spinner);
            }
            // Add a Progress bar
            else if (vmData.module.toString().equals("progress"))
            {
                Progress progress = new Progress(vmData.name, this.name, vgroup, vmData.properties);
                progress.render();
                Components.add(viewData.name+"__"+vmData.name, "progress", progress);
            }
            // Add a List Full screen
            else if (vmData.module.toString().equals("listscreen"))
            {
                ListScreen listscreen= new ListScreen(vmData.name, this.name, vgroup, vmData.properties, vmData.events);
                listscreen.render();
                Components.add(viewData.name+"__"+vmData.name, "listscreen", listscreen);
            }
            // Add a List with checks in Full screen
            else if (vmData.module.toString().equals("listcheckscreen"))
            {
                ListCheckScreen listcheckscreen= new ListCheckScreen(vmData.name, this.name, vgroup, vmData.properties, vmData.events);
                listcheckscreen.render();
                Components.add(viewData.name+"__"+vmData.name, "listcheckscreen", listcheckscreen);
            }
            // Add a List options
            else if (vmData.module.toString().equals("listoptions"))
            {
                ListOptions listoption= new ListOptions(vmData.name, this.name, vgroup, vmData.properties, vmData.events);
                listoption.render();
                Components.add(viewData.name+"__"+vmData.name, "listoptions", listoption);
            }
            // Add a checkgroup
            else if (vmData.module.toString().equals("checkgroup"))
            {
                CheckGroup checkgroup= new CheckGroup(vmData.name, this.name, vgroup, vmData.properties, vmData.events);
                checkgroup.render();
                Components.add(viewData.name+"__"+vmData.name, "checkgroup", checkgroup);
            }
            // Add a combobox
            else if (vmData.module.toString().equals("combobox"))
            {
                Combobox combobox= new Combobox(vmData.name, this.name, vgroup, vmData.properties, vmData.events);
                combobox.set_parent_activity_context(ScreenActivity.this);
                combobox.render();
                Components.add(viewData.name+"__"+vmData.name, "combobox", combobox);
            }

            // Add a date time picker
            else if (vmData.module.toString().equals("datetimepicker"))
            {
                DateTimePicker dateTimePicker = new DateTimePicker(vmData.name, this.name, vgroup, fragmentManager, vmData.properties, vmData.events);
                dateTimePicker.set_parent_activity_context(ScreenActivity.this);
                dateTimePicker.render();
                Components.add(viewData.name+"__"+vmData.name, "datetimepicker", dateTimePicker);
            }

            // Add a geolocation Object
            else if (vmData.module.toString().equals("geolocation"))
            {
                Geolocation geolocation = new Geolocation(vmData.name, this.name, vmData.properties, vmData.events);
                geolocation.render();
                Components.add(viewData.name+"__"+vmData.name, "geolocation", geolocation);
            }
            // Add a map object
            else if (vmData.module.toString().equals("map"))
            {
                SupportMapFragment supportMapFragment = Map.newInstance(viewData.name, vmData.name, vgroup, vmData.properties);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.linear, supportMapFragment, "supportMapFragment");
                fragmentTransaction.commit();

                Components.add(viewData.name+"__"+vmData.name, "map", supportMapFragment);
            }

            // Add a reverseGeocode object
            else if (vmData.module.toString().equals("reverseGeocode"))
            {
                ReverseGeocode reverseGeocode = new ReverseGeocode(vmData.name, this.name, vmData.properties, vmData.events);
                reverseGeocode.render();
                Components.add(viewData.name+"__"+vmData.name, "reversegeocode", reverseGeocode);
            }
            else if (vmData.module.toString().equals("groupedViews"))
            {
                GroupedViews groupedViews = new GroupedViews();
                groupedViews.initialize(vmData.name, this.name, vgroup, vmData.properties, vmData.events);
                groupedViews.parent = this;
                groupedViews.render();
                Components.add(viewData.name+"__"+vmData.name, "groupedviews", groupedViews);
            }
            // Photo Input
            else if (vmData.module.toString().equals("photoInput"))
            {
                PhotoInput photoInput = new PhotoInput(vmData.name, this.name, vgroup, vmData.properties, vmData.events);
                photoInput.render();
                Components.add(viewData.name+"__"+vmData.name, "photoinput", photoInput);
            }
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        int cc = Debug.getThreadAllocCount();
    }

    public void on_load()
    {
        if (this.ev_on_load!=null && !this.ev_on_load.equals(""))
        {
            try
            {
                Procedures mainProcedure = new Procedures();
                mainProcedure.initialize(this.ev_on_load, "{'sender':'"+this.name+"'}");
                mainProcedure.execute();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void on_show()
    {
        if (this.ev_on_show!=null && !this.ev_on_show.equals(""))
        {
            try {
                Procedures mainProcedure = new Procedures();
                mainProcedure.initialize(this.ev_on_show, "{'sender':'"+this.name+"'}");
                mainProcedure.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void goBack()
    {
        if (!this.back_locked)
        {
            Screens.remove(this.name);
            Screens.openScreens.remove(this);
            super.onBackPressed();
        }
        on_back();
    }

    public void on_back()
    {
        if (this.ev_on_back!=null && !this.ev_on_back.equals(""))
        {
            try {
                Procedures mainProcedure = new Procedures();
                mainProcedure.initialize(this.ev_on_back, "{'sender':'"+this.name+"'}");
                mainProcedure.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void goAction()
    {
        if (this.ev_on_action!=null && !this.ev_on_action.equals(""))
        {
            try
            {
                Procedures mainProcedure = new Procedures();
                mainProcedure.initialize(this.ev_on_action, "{'sender':'"+this.name+"__actionButton'}");
                mainProcedure.execute();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void set_properties()
    {
        Variables.add(this.name + "__locked", "boolean", this.back_locked);
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
