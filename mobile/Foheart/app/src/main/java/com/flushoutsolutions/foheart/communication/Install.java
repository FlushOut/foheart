package com.flushoutsolutions.foheart.communication;

import android.content.SharedPreferences;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.ApplicationData;
import com.flushoutsolutions.foheart.data.ProcedureData;
import com.flushoutsolutions.foheart.data.TableData;
import com.flushoutsolutions.foheart.data.TableFieldData;
import com.flushoutsolutions.foheart.data.TableMastersData;
import com.flushoutsolutions.foheart.data.ViewData;
import com.flushoutsolutions.foheart.data.ViewModuleData;
import com.flushoutsolutions.foheart.json.ConfigJson;
import com.flushoutsolutions.foheart.json.DatamodelJson;
import com.flushoutsolutions.foheart.json.ProceduresJson;
import com.flushoutsolutions.foheart.json.ViewsJson;
import com.flushoutsolutions.foheart.models.ApplicationModel;
import com.flushoutsolutions.foheart.models.ProcedureModel;
import com.flushoutsolutions.foheart.models.TableFieldModel;
import com.flushoutsolutions.foheart.models.TableMastersModel;
import com.flushoutsolutions.foheart.models.TableModel;
import com.flushoutsolutions.foheart.models.TableTransactionsModel;
import com.flushoutsolutions.foheart.models.ViewModel;
import com.flushoutsolutions.foheart.models.ViewModuleModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Manuel on 10/08/2014.
 */
public class Install {

    public Install() {

    }

    public boolean install(ArrayList<String> appsCode) throws IOException, JSONException{
        deleteAllApps();
        deleteAllTables();
        deleteAllViews();
        deleteAllProcs();
        int idApp;

        for (int i = 0; i < appsCode.size(); i++) {
            idApp = createApp(appsCode.get(i));
            createTables(appsCode.get(i),idApp);
            createViews(appsCode.get(i),idApp);
            createProcs(appsCode.get(i),idApp);
        }
        return true;
    }

    private void deleteAllApps() {
        ApplicationModel.get_model().delete_all();
    }

    private void deleteAllTables() {
        TableModel.get_model().delete_all();
        TableMastersModel.get_model().delete_all();
        TableTransactionsModel.get_model().delete_all();
        TableFieldModel.get_model().delete_all();
    }

    private void deleteAllViews() {
        ViewModel.get_model().delete_all();
        ViewModuleModel.get_model().delete_all();
    }

    private void deleteAllProcs() {
        ProcedureModel.get_model().delete_all();
    }


    private int createApp(String appCode) throws IOException, JSONException {
        ConfigJson config = new ConfigJson(appCode);
        ApplicationData application_data = new ApplicationData(config.get_json_object(), appCode);
        ApplicationModel application_model = ApplicationModel.get_model();
        application_model.save(application_data);
        return application_model.get_data(appCode)._id;
    }

    private void createTables(String appCode, int idApp) throws IOException, JSONException {
        DatamodelJson datamodelJson = new DatamodelJson(appCode);
        JSONArray tablesJsonArray = datamodelJson.get_json_array_tables();
        TableModel entityModel = TableModel.get_model();
        TableMastersModel entityMasterModel = TableMastersModel.get_model();

        int model_version = datamodelJson.version();
        int version_server = 1;

        for (int x=0; x<tablesJsonArray.length(); x++)
        {
            JSONObject jsonObject = tablesJsonArray.getJSONObject(x);

            String name = jsonObject.getString("name");
            int auto_sync = jsonObject.getInt("autoSync");
            String requestParams = jsonObject.getString("requestParams");
            String key = "_id";
            if (!jsonObject.isNull("key")) key = jsonObject.getString("key");

            TableData entityData = new TableData(idApp,model_version,name,auto_sync, key,requestParams);
            entityModel.save(entityData);

            int id_table = entityModel.get_data(idApp, name)._id;

            if(auto_sync == 0){
                TableMastersData entityMasterData = new TableMastersData(id_table,version_server);
                entityMasterModel.save(entityMasterData);
            }

            JSONArray jsonArrayFields = new JSONArray(jsonObject.getString("fields"));
            TableFieldModel entityFieldModel = TableFieldModel.get_model();

            TableFieldData entityFieldData_id = new TableFieldData (id_table, "_id", "INTEGER", 11, "", 1, 1, 1);
            entityFieldModel.save(entityFieldData_id);

            if(auto_sync == 1){

                TableFieldData entityFieldData_versionLocal = new TableFieldData (id_table, "_version_local", "INTEGER", 11, "", 1, 0, 0);
                entityFieldModel.save(entityFieldData_versionLocal);

                TableFieldData entityFieldData_versionServer = new TableFieldData (id_table, "_version_server", "INTEGER", 11, "", 1, 0, 0);
                entityFieldModel.save(entityFieldData_versionServer);

                TableFieldData entityFieldData_sync = new TableFieldData (id_table, "_sync", "INTEGER", 11, "", 1, 0, 0);
                entityFieldModel.save(entityFieldData_sync);

                TableFieldData entityFieldData_create = new TableFieldData (id_table, "_date_sync", "text", 255, "", 1, 0, 0);
                entityFieldModel.save(entityFieldData_create);

                TableFieldData entityFieldData_updated = new TableFieldData (id_table, "_date_mobile", "text", 255, "", 1, 0, 0);
                entityFieldModel.save(entityFieldData_updated);
            }

            for (int y=0; y<jsonArrayFields.length(); y++)
            {
                JSONObject fieldJsonObject = jsonArrayFields.getJSONObject(y);

                String name_field = fieldJsonObject.getString("name");
                String type_field = fieldJsonObject.getString("type");
                String default_field = "";

                if (!fieldJsonObject.isNull("default"))
                    default_field = fieldJsonObject.getString("default");

                int size_field = 0;

                if (!fieldJsonObject.isNull("size"))
                    size_field = fieldJsonObject.getInt("size");

                int required_field = 0;

                if (!fieldJsonObject.isNull("required"))
                {
                    if(fieldJsonObject.getBoolean("required"))
                        required_field = 1;
                }

                int autoincrement_field = 0;

                if (!fieldJsonObject.isNull("autoincrement"))
                {
                    if (!fieldJsonObject.isNull("autoincrement"))
                    {
                        if(fieldJsonObject.getBoolean("autoincrement"))
                            autoincrement_field = 1;
                    }
                }
                int primary_key_field = 0;
                if (!fieldJsonObject.isNull("primaryKey"))
                {
                    if (!fieldJsonObject.isNull("primaryKey"))
                    {
                        if(fieldJsonObject.getBoolean("primaryKey"))
                            primary_key_field = 1;
                    }
                }
                TableFieldData entityFieldData = new TableFieldData (id_table, name_field, type_field, size_field, default_field, required_field, autoincrement_field, primary_key_field);
                entityFieldModel.save(entityFieldData);
            }
        }
    }

    private void createViews(String appCode, int idApp) throws IOException, JSONException {
        ViewModel viewModel = ViewModel.get_model();
        ViewsJson viewsJson = new ViewsJson(appCode);
        JSONArray viewsJsonArray = viewsJson.get_json_array();

        for (int x=0; x<viewsJsonArray.length(); x++)
        {
            JSONObject jsonObject = viewsJsonArray.getJSONObject(x);
            String name = jsonObject.getString("name");
            String title = jsonObject.getString("title");
            String layout = jsonObject.getString("layout");

            JSONObject actionJsonObject = new JSONObject(jsonObject.getString("actionButton"));
            String button_title=null;
            String button_action=null;
            String view_events = null;

            int back_locked = 0;
            if (!jsonObject.isNull("backLocked"))
            {
                if (jsonObject.getBoolean("backLocked"))
                    back_locked = 1;
            }
            if (!actionJsonObject.isNull("title"))
                button_title = actionJsonObject.getString("title");

            if (!actionJsonObject.isNull("action"))
                button_action = actionJsonObject.getString("action");

            if (!jsonObject.isNull("events"))
                view_events = jsonObject.getString("events");

            ViewData viewData = new ViewData(idApp, name, title, layout, button_title, button_action, back_locked, view_events);
            viewModel.save(viewData);

            int id_view = viewModel.get_data(idApp,name)._id;

            JSONArray jsonArrayModules = new JSONArray(jsonObject.getString("modules"));
            ViewModuleModel viewModuleModel = ViewModuleModel.get_model();

            for (int y=0; y<jsonArrayModules.length(); y++)
            {
                JSONObject moduleJsonObject = jsonArrayModules.getJSONObject(y);

                String module = moduleJsonObject.getString("module");
                String name_module = moduleJsonObject.getString("name");
                String properties_module = moduleJsonObject.getString("properties");
                String events_module = moduleJsonObject.getString("events");
                ViewModuleData viewModuleData = new ViewModuleData(id_view, module, name_module, properties_module, events_module);
                viewModuleModel.save(viewModuleData);
            }
        }

    }

    private void createProcs(String appCode, int idApp) throws IOException, JSONException {
        ProcedureModel procedureModel = ProcedureModel.get_model();
        ProceduresJson proceduresJson = new ProceduresJson(appCode);
        JSONArray proceduresJsonArray = proceduresJson.get_json_array();

        for (int x=0; x<proceduresJsonArray.length(); x++)
        {
            JSONObject jsonObject = proceduresJsonArray.getJSONObject(x);

            String name = jsonObject.getString("name");
            String parameters = jsonObject.getString("params");
            String code = jsonObject.getString("code");
            String return_var = "var";

            ProcedureData procedureData = new ProcedureData(idApp, name, parameters, code, return_var);
            procedureModel.save(procedureData);
        }
    }
}