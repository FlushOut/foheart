package com.flushoutsolutions.foheart.logic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.flushoutsolutions.foheart.ScreenActivity;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.ProcedureData;
import com.flushoutsolutions.foheart.data.ViewData;
import com.flushoutsolutions.foheart.globals.Components;
import com.flushoutsolutions.foheart.globals.Menu;
import com.flushoutsolutions.foheart.globals.Screens;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.models.ApplicationModel;
import com.flushoutsolutions.foheart.models.ProcedureModel;
import com.flushoutsolutions.foheart.models.ViewModel;
import com.flushoutsolutions.foheart.modules.ActivityIndicator;
import com.flushoutsolutions.foheart.modules.CmdButton;
import com.flushoutsolutions.foheart.modules.DateTimePicker;
import com.flushoutsolutions.foheart.modules.GroupedViews;
import com.flushoutsolutions.foheart.modules.Label;
import com.flushoutsolutions.foheart.modules.ListCheckScreen;
import com.flushoutsolutions.foheart.modules.ListScreen;
import com.flushoutsolutions.foheart.modules.Progress;
import com.flushoutsolutions.foheart.modules.ReverseGeocode;
import com.flushoutsolutions.foheart.modules.Textfield;
import com.flushoutsolutions.foheart.modules.Map;
import com.flushoutsolutions.foheart.track.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;

/**
 * Created by Manuel on 09/08/2014.
 */
public class Procedures {

    public ProcedureData procedureData;
    public String the_code;
    public JSONArray the_params;
    public String procedure_name;
    public JSONObject params_values;

    static Context appContext = FoHeart.getAppContext();
    static SharedPreferences settings = appContext.getSharedPreferences("userconfigs", 0);
    static String codeApp = settings.getString("idApplication", "");
    static int fk_app = ApplicationModel.get_model().get_data(codeApp)._id;

    public Procedures()
    {

    }

    public void initialize (String procedurename, String args) throws JSONException
    {
        ProcedureModel procedureModel = ProcedureModel.get_model();
        procedureData = procedureModel.get_data(this.fk_app, procedurename);

        if (procedureData!=null)
        {
            this.procedure_name = procedurename;
            this.the_code = procedureData.code;
            this.the_params = new JSONArray(procedureData.parameters);

            if (args!=null)
            {
                this.params_values = new JSONObject(args);

                for (int p=0; p<this.the_params.length(); p++)
                {
                    JSONObject paramObject = this.the_params.getJSONObject(p);

                    // make the var name
                    String param_var_name = procedurename+"__"+paramObject.getString("name");
                    Variables.add(param_var_name, paramObject.getString("type"), this.params_values.getString(paramObject.getString("name")));
                }
            }
        }

    }

    public void execute() throws JSONException
    {
        if (this.the_code!=null)
            exec(this.the_code);
        else
            System.out.println("no such function '" + this.procedure_name + "'");
    }

    public void exec(String strLines) throws JSONException
    {
        JSONArray lines = new JSONArray(strLines);
        TableHandler tHandler = new TableHandler();

        int numlines = lines.length();

        for (int v=0; v<numlines; v++)
        {
            JSONObject code_line = new JSONObject (lines.getString(v));

            if (!code_line.isNull("setVariable"))
            {
                JSONObject method = new JSONObject(code_line.getString("setVariable"));

                String varname = method.getString("name");
                String type="";
                if (!method.isNull("type")) type=method.getString("type");
                String value = Variables.parse_vars(method.getString("value"), false);

                if ("component".equals(type))
                    Variables.add(varname, type, value);
                else
                {
                    String var = Variables.parse_vars(value.toString(), false);
                    if (var.startsWith("'")) var = var.substring(1, var.length()-1);

                    Variables.add(varname, type, Variables.parse_vars(var, false));
                }
            }

            else if (!code_line.isNull("setNotificationsNumber"))
            {
                JSONObject method = new JSONObject(code_line.getString("setNotificationsNumber"));
                String notifNumber = Variables.parse_vars(method.getString("number"), false);

                Menu.get(method.getString("menuName")).setNotification(Integer.valueOf(notifNumber));
            }

            else if (!code_line.isNull("log"))
            {
                JSONObject method = new JSONObject (code_line.getString("log"));
                Log.i("FOHEART", Variables.parse_vars(method.getString("message"), false));
            }

            else if (!code_line.isNull("if"))
            {
                JSONObject method = new JSONObject(code_line.getString("if"));
                String expression = method.getString("condition");

                Variables.scope_if ++;

                if (Variables.check_condition(expression))
                {
                    exec(method.getString("block"));
                }
                else
                {
                    if (!method.isNull("else"))
                    {
                        exec(method.getString("else"));
                    }
                }
                Variables.scope_if--;
            }

            else if (!code_line.isNull("call"))
            {
                JSONObject method = new JSONObject(code_line.getString("call"));
                String methodName = method.getString("method");

                Procedures proc = new Procedures();
                proc.initialize(methodName, null);
                proc.execute();
            }


            else if (!code_line.isNull("timer"))
            {
                final JSONObject method = new JSONObject(code_line.getString("timer"));

                int interval = method.getInt("interval");
                final boolean repeats = method.getBoolean("repeats");
                boolean startNow = method.getBoolean("startNow");

                int delay=0;
                if (!startNow) delay = interval;

                ScheduledExecutorService scheduledExecutorService =  Executors.newScheduledThreadPool(5);
                ScheduledFuture scheduledTsk = scheduledExecutorService.scheduleWithFixedDelay(
                        new Runnable()
                        {
                            @Override
                            public void run() {
                                Procedures proc = new Procedures();

                                try
                                {
                                    proc.exec(method.getString("block"));
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }, delay, interval, TimeUnit.MILLISECONDS);

                if (!repeats)
                {
                    scheduledExecutorService.shutdown();
                }
            }


            else if (!code_line.isNull("for"))
            {
                JSONObject method = new JSONObject(code_line.getString("for"));

                String variable = method.getString("variable");

                String var_initial = Variables.parse_vars(method.getString("initial"), false);
                String var_end = Variables.parse_vars(method.getString("final"), false);

                int initial = Integer.valueOf(var_initial);
                int end_value = Integer.valueOf(var_end);

                Variables.scope_for ++;
                for (int counter = initial; counter<end_value; counter++)
                {
                    Variables.add(variable, "int", Integer.valueOf(counter));
                    exec(method.getString("block"));
                }
                Variables.scope_for --;

                Variables.remove(variable);
            }

            else if (!code_line.isNull("while"))
            {
                JSONObject method = new JSONObject(code_line.getString("while"));
                String condition = method.getString("condition");
                Variables.scope_while ++;

                while (Variables.check_condition(condition))
                {
                    exec(method.getString("block"));
                }

                Variables.scope_while --;
            }

            else if (!code_line.isNull("do"))
            {
                JSONObject method = new JSONObject(code_line.getString("do"));
                String condition = method.getString("condition");

                Variables.scope_do++;

                do
                {
                    exec(method.getString("block"));
                }
                while (Variables.check_condition(condition));

                Variables.scope_do --;
            }


            else if (!code_line.isNull("switch"))
            {
                JSONObject method = new JSONObject(code_line.getString("switch"));

                String variable = method.getString("variable");
                int value_variable = Integer.parseInt(Variables.get(variable).toString());

                JSONArray options = new JSONArray(method.getString("options"));
                boolean anyExecuted = false;

                for (int x = 0; x<options.length(); x++)
                {
                    JSONObject opt = options.getJSONObject(x);

                    if (opt.getInt("value")==value_variable)
                    {
                        anyExecuted = true;
                        exec(opt.getString("block"));
                    }
                }

                if (!anyExecuted && !method.isNull("default"))
                {
                    exec(method.getString("default"));
                }
            }

            else if (!code_line.isNull("openScreen"))
            {
                JSONObject method = new JSONObject(code_line.getString("openScreen"));
                String viewName = method.getString("screenName");

                ViewData viewData = ViewModel.get_model().get_data(this.fk_app, viewName);

                if (viewData == null)
                {
                    // error
                    System.out.println("View '"+viewName+"' not found");
                }
                else
                {
                    Intent intent = new Intent(FoHeart.getAppContext(), ScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("viewName", viewName);
                    intent.putExtra("parent_screen", Screens.current);
                    FoHeart.getAppContext().startActivity(intent);
                }

            }

            else if (!code_line.isNull("closeScreen"))
            {
                JSONObject method = new JSONObject(code_line.getString("closeScreen"));
                String viewname = method.getString("screenName");

                String view;
                if ("".equals(viewname))
                    view = Screens.current;
                else
                    view = viewname;

                // if viewname is empty, close this window
                ScreenActivity scA = Screens.get(view);
                if (null!=scA) scA.finish();
            }

            else if (!code_line.isNull("closeAll"))
            {
                for (ScreenActivity window: Screens.openScreens)
                {
                    window.finish();
                }
            }

            //Toast
            else if (!code_line.isNull("toast"))
            {
                JSONObject method = new JSONObject(code_line.getString("toast"));

                String message = method.getString("message");
                int duration = method.getInt("duration");

                /* duration 0 = Lenght.short
                   duration 1 = Lenght.Long */

                if (duration == 0)
                {
                    Toast.makeText(Screens.currentCtx, message, Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(Screens.currentCtx, message, Toast.LENGTH_LONG);
                }

            }

            else if (!code_line.isNull("alertDialog"))
            {
                JSONObject method = new JSONObject(code_line.getString("alertDialog"));

                String title="";
                String message="";
                JSONObject closeButton = null;
                JSONObject secondButton = null;

                if (!method.isNull("title"))
                    title = method.getString("title");

                if (!method.isNull("message"))
                    message = method.getString("message");

                if (!method.isNull("closeButton"))
                    closeButton = method.getJSONObject("closeButton");

                if (!method.isNull("secondButton"))
                    secondButton = method.getJSONObject("secondButton");

                final JSONObject closeButtonOb = closeButton;
                final JSONObject secondButtonOb = secondButton;

                AlertDialog alertDialog = new AlertDialog.Builder(Screens.currentCtx).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(message);

                try {

                    alertDialog.setButton(closeButton.getString("title"), new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            try {
                                if (!closeButtonOb.isNull("onClick") && !closeButtonOb.getString("onClick").equals(""))
                                {
                                    try {

                                        Procedures mainProcedure = new Procedures();
                                        mainProcedure.initialize(closeButtonOb.getString("onClick"), null);
                                        mainProcedure.execute();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    if (null!=secondButton)
                    {
                        alertDialog.setButton2(secondButton.getString("title"), new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                try {
                                    if (!secondButtonOb.isNull("onClick") && !secondButtonOb.getString("onClick").equals(""))
                                    {
                                        try {

                                            Procedures mainProcedure = new Procedures();
                                            mainProcedure.initialize(secondButtonOb.getString("onClick"), null);
                                            mainProcedure.execute();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                alertDialog.show();
            }

            else if (!code_line.isNull("setProperty"))
            {
                JSONObject method = new JSONObject(code_line.getString("setProperty"));

                String component = method.getString("componentName");
                String property = method.getString("property");

                String parsedValue = Variables.parse_vars(method.getString("value"), false);
                String type = Components.get_type(component);

                if (type.equals("label"))
                {
                    Label co = (Label)Components.get(component);
                    co.set_property(property, parsedValue);
                }
                else if (type.equals("button"))
                {
                    CmdButton co = (CmdButton)Components.get(component);
                    co.set_property(property, parsedValue);
                }
                else if (type.equals("textfield"))
                {
                    Textfield co = (Textfield)Components.get(component);
                    co.set_property(property, parsedValue);
                }
                else if (type.equals("spinner"))
                {
                    ActivityIndicator co = (ActivityIndicator)Components.get(component);
                    co.set_property(property, parsedValue);
                }
                else if (type.equals("progress"))
                {
                    Progress co = (Progress)Components.get(component);
                    co.set_property(property, parsedValue);
                }
                else if (type.equals("datetimepicker"))
                {
                    DateTimePicker co = (DateTimePicker)Components.get(component);
                    co.set_property(property, parsedValue);
                }

                else if (type.equals("datetimepicker"))
                {
                    DateTimePicker co = (DateTimePicker)Components.get(component);
                    co.set_property(property, parsedValue);
                }
                else if (type.equals("map"))
                {
                   Map co = (Map)Components.get(component);
                    co.set_property(property, parsedValue);
                }
            }

            else if (!code_line.isNull("setMethod"))
            {
                JSONObject method = new JSONObject(code_line.getString("setMethod"));

                String theMethod = method.getString("method");
                String component = method.getString("componentName");
                String type = Components.get_type(component);

                if (type.equals("reversegeocode"))
                {
                    ReverseGeocode co = (ReverseGeocode)Components.get(component);

                    // save data
                    if (theMethod.equals("refresh"))
                    {
                        co.method_refresh();
                    }
                }
                else if (type.equals("listscreen"))
                {
                    ListScreen co = (ListScreen)Components.get(component);

                    // save data
                    if (theMethod.equals("refresh"))
                    {
                        if (co != null)
                            co.method_refresh();


                    }
                }
                else if (type.equals("groupedviews"))
                {
                    GroupedViews co = (GroupedViews)Components.get(component);

                    // save data
                    if (theMethod.equals("refresh"))
                    {
                        if (co != null)
                            co.method_refresh();


                    }
                }

                else if (type.equals("listcheckscreen"))
                {
                    ListCheckScreen co = (ListCheckScreen)Components.get(component);

                    if (theMethod.equals("clear"))
                    {
                        if (co != null)
                            co.method_clear();


                    }
                }

            }

			/*
			 * Notifications
			 */

            else if (!code_line.isNull("playAlert"))
            {
                JSONObject method = new JSONObject(code_line.getString("playAlert"));
                Notifications.playAlert(method.getBoolean("sound"), method.getInt("vibration"));
            }

			/*
			 * Sync functions
			 */

            else if (!code_line.isNull("getData"))
            {
                JSONObject method = new JSONObject(code_line.getString("getData"));

                String user_exclusive = "";
                String clear_all = "";
                String block_success = "";
                String update_set = "";
                String loadingMessage = "";

                boolean displayTimeOutDialog =false;
                String timeOutDialogTitle ="";
                String timeOutMessage ="";
                String timeOutDialogButton ="";
                String onTimeOut = "";
                String ifRepeats = "ignore";

                if (!method.isNull("userExclusive")) user_exclusive = method.getString("userExclusive");
                if (!method.isNull("clearAll")) clear_all = method.getString("clearAll");
                if (!method.isNull("blockSuccess")) block_success = method.getString("blockSuccess");
                if (!method.isNull("updateSet")) update_set = method.getString("updateSet");
                if (!method.isNull("loadingMessage")) loadingMessage = method.getString("loadingMessage");

                if (!method.isNull("displayTimeOutDialog")) displayTimeOutDialog = method.getBoolean("displayTimeOutDialog");
                if (!method.isNull("timeOutDialogTitle")) timeOutDialogTitle = method.getString("timeOutDialogTitle");
                if (!method.isNull("timeOutDialogButton")) timeOutDialogButton = method.getString("timeOutDialogButton");
                if (!method.isNull("timeOutMessage")) timeOutMessage = method.getString("timeOutMessage");
                if (!method.isNull("onTimeOut")) onTimeOut = method.getString("onTimeOut");

                if (!method.isNull("ifRepeats")) ifRepeats = method.getString("ifRepeats");

                GetData getData = new GetData();
                getData.initialize(method.getString("tableName"), method.getString("requestType"), method.getString("requestParams"), update_set, user_exclusive, clear_all, block_success, loadingMessage, displayTimeOutDialog,timeOutDialogTitle,timeOutMessage, timeOutDialogButton, onTimeOut, ifRepeats);
                getData.executeQuery();
            }

            else if (!code_line.isNull("postData"))
            {
                JSONObject method = new JSONObject(code_line.getString("postData"));

                boolean user_exclusive = false;
                String block_success = "";
                String insert_params = "";
                String loadingMessage = "";
                String var_id_return = "";


                boolean displayTimeOutDialog =false;
                String timeOutDialogTitle ="";
                String timeOutMessage ="";
                String timeOutDialogButton ="";
                String onTimeOut = "";

                if (!method.isNull("userExclusive")) user_exclusive = method.getBoolean("userExclusive");
                if (!method.isNull("blockSuccess")) block_success = method.getString("blockSuccess");
                if (!method.isNull("insertParams")) insert_params = method.getString("insertParams");
                if (!method.isNull("loadingMessage")) loadingMessage = method.getString("loadingMessage");
                if (!method.isNull("varIdReturn")) var_id_return = method.getString("varIdReturn");

                if (!method.isNull("displayTimeOutDialog")) displayTimeOutDialog = method.getBoolean("displayTimeOutDialog");
                if (!method.isNull("timeOutDialogTitle")) timeOutDialogTitle = method.getString("timeOutDialogTitle");
                if (!method.isNull("timeOutDialogButton")) timeOutDialogButton = method.getString("timeOutDialogButton");
                if (!method.isNull("timeOutMessage")) timeOutMessage = method.getString("timeOutMessage");
                if (!method.isNull("onTimeOut")) onTimeOut = method.getString("onTimeOut");

                PostData postData = new PostData();
                postData.initialize(method.getString("tableName"), insert_params, user_exclusive, var_id_return, block_success, loadingMessage, displayTimeOutDialog,timeOutDialogTitle,timeOutMessage, timeOutDialogButton, onTimeOut);
                postData.executeQuery();
            }

			/*
			 * Database functions
			 */


            else if (!code_line.isNull("tableInsert"))
            {
                JSONObject method = new JSONObject(code_line.getString("tableInsert"));
                tHandler.tableInsert(method.getString("tableName"), method.getString("values"));
            }

            else if (!code_line.isNull("tableUpdate"))
            {
                JSONObject method = new JSONObject(code_line.getString("tableUpdate"));
                tHandler.tableUpdate(method.getString("tableName"), method.getString("values"));
            }

            else if (!code_line.isNull("tableDelete"))
            {
                JSONObject method = new JSONObject(code_line.getString("tableDelete"));
                tHandler.tableDelete(method.getString("tableName"), method.getString("field"), method.getString("value"));
            }

            else if (!code_line.isNull("query"))
            {
                JSONObject method = new JSONObject(code_line.getString("query"));
                tHandler.query(method.getString("statement"), method.getString("returnVar"));
            }

            else if (!code_line.isNull("queryNumRows"))
            {
                JSONObject method = new JSONObject(code_line.getString("queryNumRows"));
                tHandler.queryNumRows(method.getString("queryVar"), method.getString("returnVar"));
            }

            else if (!code_line.isNull("queryValue"))
            {
                JSONObject method = new JSONObject(code_line.getString("queryValue"));

                String var_position = Variables.parse_vars(method.getString("position"), false);
                int position= Integer.valueOf(var_position);

                tHandler.queryValue(method.getString("queryVar"), method.getString("fieldName"), position, method.getString("returnVar"));
            }

            else if (!code_line.isNull("getDateTime"))
            {
                JSONObject method = new JSONObject(code_line.getString("getDateTime"));

                long miliseconds =0;

                if (!method.isNull("miliseconds"))
                {
                    miliseconds = Long.parseLong(Variables.parse_vars(method.getString("miliseconds"), false));

                    if (miliseconds < 1000) miliseconds = 100;
                }

                String strFormat = Variables.parse_vars(method.getString("format"), false);
                String returnVar = Variables.parse_vars(method.getString("returnVar"), false);

                SimpleDateFormat sdf = new SimpleDateFormat(strFormat, Locale.getDefault());
                String currentDateTime;

                if (miliseconds==0)
                    currentDateTime = sdf.format(new Date());
                else
                {
                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    sdf.setTimeZone(tz);
                    currentDateTime = sdf.format(new Date(miliseconds));

                }

                Variables.add(returnVar, "string", currentDateTime);
            }

            else if (!code_line.isNull("getDateTimeMiliseconds"))
            {
                JSONObject method = new JSONObject(code_line.getString("getDateTimeMiliseconds"));

                String strDate = Variables.parse_vars(method.getString("date"), false);
                String strFormat = Variables.parse_vars(method.getString("format"), false);
                String returnVar = Variables.parse_vars(method.getString("returnVar"), false);

                Date convertedDate = new Date();
                if (!strDate.equals(""))
                {
                    try
                    {
                        SimpleDateFormat df = new SimpleDateFormat(strFormat, Locale.getDefault());
                        convertedDate =  df.parse(strDate);
                    }
                    catch (ParseException pe)
                    {
                        pe.printStackTrace();
                    }
                }

                Variables.add(returnVar, "int", convertedDate.getTime());
            }

            else if (!code_line.isNull("geoDistance"))
            {
                JSONObject method = new JSONObject(code_line.getString("geoDistance"));

                String stLat1 = Variables.parse_vars(method.getString("lat1"), false);
                String stLon1 = Variables.parse_vars(method.getString("lon1"), false);
                String stLat2 = Variables.parse_vars(method.getString("lat2"), false);
                String stLon2 = Variables.parse_vars(method.getString("lon2"), false);

                // Values
                double lat1 = Double.valueOf(stLat1);
                double lon1 = Double.valueOf(stLon1);
                double lat2 = Double.valueOf(stLat2);
                double lon2 = Double.valueOf(stLon2);
                String returnVar = method.getString("returnVar");

                double meters = FoHeart.getDistanceInMeters(lat1, lon1, lat2, lon2);
                Variables.add(returnVar, "float", meters);
            }

            else if (!code_line.isNull("listObjectValue"))
            {
                JSONObject method = new JSONObject(code_line.getString("listObjectValue"));

                JSONArray object = new JSONArray(Variables.parse_vars(method.getString("object"), false));
                String returnVar = method.getString("returnVar");

                String var_index = Variables.parse_vars(method.getString("index"), false);
                int index = Integer.valueOf(var_index);

                String value = object.getJSONObject(index).getString(method.getString("field"));
                Variables.add(returnVar, "string", value);
            }

            else if (!code_line.isNull("listObjectCount"))
            {
                JSONObject method = new JSONObject(code_line.getString("listObjectCount"));

                JSONArray object = new JSONArray(Variables.parse_vars(method.getString("object"), false));
                String returnVar = method.getString("returnVar");

                String value = String.valueOf(object.length());
                Variables.add(returnVar, "string", value);
            }

			/*
			 * ARRAY
			 */

            // Register array
            else if (!code_line.isNull("array"))
            {
                JSONObject method = new JSONObject(code_line.getString("array"));
                ArrayList<String> arr = new ArrayList<String>();

                // Values
                JSONArray arrayValues = new JSONArray(method.getString("values"));

                for (int x=0; x<arrayValues.length(); x++)
                {
                    arr.add(arrayValues.getJSONObject(x).getString("value"));
                }

                Variables.add(method.getString("arrayVar"), "array."+method.getString("type"), arr);
            }

            // Push a value into an array
            else if (!code_line.isNull("arrayPush"))
            {
                JSONObject method = new JSONObject(code_line.getString("arrayPush"));

                ArrayList<String> arr = new ArrayList<String>();
                arr = (ArrayList<String>) Variables.get(method.getString("arrayVar"));

                String value = method.getString("value");
                arr.add(value);
            }

            else if (!code_line.isNull("arrayItem"))
            {
                JSONObject method = new JSONObject(code_line.getString("arrayItem"));

                ArrayList<String> arr = new ArrayList<String>();
                arr = (ArrayList<String>) Variables.get(method.getString("arrayVar"));

                String returnVar = method.getString("returnVar");
                int index = method.getInt("index");

                String value = arr.get(index);
                Variables.add(returnVar, "string", value);

            }

            else if (!code_line.isNull("calc"))
            {
                JSONObject method = new JSONObject(code_line.getString("calc"));

                String expression = Variables.parse_vars(method.getString("expression"), false);
                String returnVar = method.getString("returnVar");

                ExpressionBuilder builder = new ExpressionBuilder(expression);

                int decimals = 0;
                if (!method.isNull("decimals")) decimals = method.getInt("decimals");

                String formatDecimals= "";
                if (decimals > 0)
                {
                    formatDecimals= ".";
                    for (int x=0;x<decimals;x++)
                    {
                        formatDecimals += "0";
                    }
                }
                System.out.println("expressionexpression "+expression);

                try
                {
                    Calculable calc = builder.build();
                    double value = calc.calculate();

                    DecimalFormat df = new DecimalFormat("0"+formatDecimals);
                    String dx = df.format(value);

                    Variables.add(returnVar, "float", dx);
                }
                catch (UnknownFunctionException e)
                {
                    e.printStackTrace();
                }
                catch (UnparsableExpressionException e)
                {
                    e.printStackTrace();
                }
                catch (EmptyStackException e)
                {
                    e.printStackTrace();
                }
            }

            else if (!code_line.isNull("numberFormat"))
            {
                JSONObject method = new JSONObject(code_line.getString("numberFormat"));

                String number = Variables.parse_vars(method.getString("number"), false);
                String returnVar = method.getString("returnVar");

                int decimals = 0;
                if (!method.isNull("decimals")) decimals = method.getInt("decimals");

                String decimalSeparator = ".";

                if (decimals > 0)
                {
                    for (int x=0;x<decimals;x++)
                    {
                        decimalSeparator += "0";
                    }
                }

                DecimalFormat df = new DecimalFormat("#,###,##0"+decimalSeparator);
                String dx = df.format(Float.valueOf(number));
                Variables.add(returnVar, "float", dx);
            }

            // String functions

            else if (!code_line.isNull("strLength"))
            {
                JSONObject method = new JSONObject(code_line.getString("strLength"));

                String stringvar = Variables.parse_vars(method.getString("string"), false);
                String returnVar = method.getString("returnVar");

                Variables.add(returnVar, "int", stringvar.length());
            }

            else if (!code_line.isNull("strSubstring"))
            {
                JSONObject method = new JSONObject(code_line.getString("strSubstring"));

                String stringvar = Variables.parse_vars(method.getString("string"), false);
                int valStart = method.getInt("start");

                int valSize = 0;
                if (!method.isNull("size"))
                    valSize = method.getInt("size");

                String returnVar = method.getString("returnVar");

                String value =  "";

                int valEnd = valStart+valSize;

                if (valSize == 0 || valEnd >= stringvar.length())
                    value = stringvar.substring(valStart);
                else
                    value = stringvar.substring(valStart, valEnd);

                Variables.add(returnVar, "string", value);
            }

            else if ((!code_line.isNull("toDate")))
            {
                JSONObject method = new JSONObject(code_line.getString("toDate"));
                String var = method.getString("variable");
                Object variable = Variables.get(var);
                String type = Variables.get_type(var);

                String format = method.getString("format");

                if (type.equals("string"))
                {
                    try
                    {
                        SimpleDateFormat df = new SimpleDateFormat(format);
                        Date result =  df.parse(variable.toString());

                        Variables.variables.put(var, result);
                        Variables.type_variables.put(var, "date");
                    }
                    catch (ParseException pe)
                    {
                        pe.printStackTrace();
                    }
                }
            }

            else if (!code_line.isNull("toInt"))
            {
                JSONObject method = new JSONObject(code_line.getString("toInt"));

                String var = method.getString("variable");
                Object variable = Variables.get(var);
                String type = Variables.get_type(var);

                if ("float".equals(type))
                {
                    int valueVar = (int)Float.parseFloat(variable.toString());

                    Variables.variables.put(var, valueVar);
                    Variables.type_variables.put(var, "int");
                }
                else if ("string".equals(type))
                {
                    try
                    {
                        int valueVar = Integer.parseInt(variable.toString());
                        Variables.variables.put(var, valueVar);
                        Variables.type_variables.put(var, "int");
                    }
                    catch (NumberFormatException e)
                    {
                        System.out.println("ERROR: not a number");
                    }

                }
                else if ("int".equals(type))
                {
                    System.out.println("WARNING: already an int type");
                }
                else
                {
                    System.out.println("ERROR: type not parsable to integer");
                }
            }

            else if (!code_line.isNull("toFloat"))
            {
                JSONObject method = new JSONObject(code_line.getString("toFloat"));

                String var = method.getString("variable");
                Object variable = Variables.get(var);
                String type = Variables.get_type(var);


                if ("int".equals(type))
                {
                    float valueVar = Float.parseFloat(variable.toString());

                    Variables.variables.put(var, valueVar);
                    Variables.type_variables.put(var, "float");
                }
                else if ("string".equals(type))
                {
                    try
                    {
                        float valueVar = Float.parseFloat(variable.toString());
                        Variables.variables.put(var, valueVar);
                        Variables.type_variables.put(var, "float");

                    }
                    catch (NumberFormatException e)
                    {
                        System.out.println("ERROR: not a number");
                    }

                }
                else if ("float".equals(type))
                {
                    System.out.println("WARNING: already a float type");
                }
                else
                {
                    System.out.println("ERROR: type not parsable to float");
                }
            }

            else if (!code_line.isNull("setLocationProvider"))
            {
                JSONObject method = new JSONObject(code_line.getString("setLocationProvider"));
                int prov = method.getInt("provider");
                Tracker.theProvider = prov;
            }
        }

    }
}
