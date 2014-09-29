package com.flushoutsolutions.foheart.communication;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.flushoutsolutions.foheart.appDataBase.AppDBModel;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.TableData;
import com.flushoutsolutions.foheart.data.TableFieldData;
import com.flushoutsolutions.foheart.logic.GetData;
import com.flushoutsolutions.foheart.logic.PostData;
import com.flushoutsolutions.foheart.logic.Procedures;
import com.flushoutsolutions.foheart.models.ApplicationModel;
import com.flushoutsolutions.foheart.models.TableFieldModel;
import com.flushoutsolutions.foheart.models.TableModel;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Manuel on 09/08/2014.
 */
public class Connection {

    public static boolean sync_locked = false;
    public static final String REST_AUTH = "http://r-auth-heart.flushoutsolutions.com/index.php?";
    public static final String REST_REP = "http://r-auth-heart.flushoutsolutions.com/repository/";
    public static final String REST_APPS = "http://r-apps-heart.flushoutsolutions.com/index.php?";


    public static String post(String url, List<NameValuePair> values)
    {
        PostDataAsync postDataAsync = new PostDataAsync();
        postDataAsync.postData = values;
        postDataAsync.execute(url);

        try {
            return postDataAsync.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String get(String type, String url)
    {
        GetDataAsync getDataAsync = new GetDataAsync(type);
        getDataAsync.execute(url);

        try {
            return getDataAsync.get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private static class GetDataAsync extends AsyncTask<String, Void, String>
    {
        String type;

        public GetDataAsync(String type)
        {
            this.type = type;
        }

        protected String doInBackground(String... urls)
        {
            String url = urls[0];
            HttpGet get = new HttpGet(url);

            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 30 * 1000);
            HttpConnectionParams.setSoTimeout(httpParameters, 45*1000);

            DefaultHttpClient client = new DefaultHttpClient(httpParameters);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            try
            {
                HttpResponse response = client.execute(get);

                String strReturn = responseHandler.handleResponse(response);

                return strReturn;
            }
            catch (ClientProtocolException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                if ("get_data".equals(type))
                {
                    if (null != GetData.getInstance().get_alert())
                        GetData.getInstance().get_alert().dismiss();

                    GetData.getInstance().onTimeOutEvent();
                }

                e.printStackTrace();
            }

            return null;
        }

        protected synchronized void onPostExecute(String result)
        {
            if (this.type.equals("get_data"))
            {
                try
                {
                    Context appContext = FoHeart.getAppContext();
                    SharedPreferences settings = appContext.getSharedPreferences("userconfigs", 0);
                    String codeApp = settings.getString("idApplication", "");
                    int idApp = ApplicationModel.get_model().get_data(codeApp)._id;

                    if (null!=result)
                    {
                        JSONObject responseJSON = new JSONObject(result);

                        if (responseJSON!=null)
                        {
                            if (responseJSON.getBoolean("status"))
                            {
                                if (!responseJSON.isNull("results"))
                                {
                                    Connection.sync_locked = true;
                                    JSONArray resultArray = responseJSON.getJSONArray("results");

                                    int num_results = resultArray.length();

                                    String[] clearAll = GetData.getInstance().getClearAll().split(";;");

                                    if (num_results>0)
                                    {

                                        for (int y=0; y<num_results; y++)
                                        {
                                            JSONObject obj = resultArray.getJSONObject(y);
                                            Iterator<Object> keys = (Iterator)obj.keys();

                                            String tablename = String.valueOf(keys.next());

                                            JSONArray rows = obj.getJSONArray(tablename);
                                            TableData tbData = TableModel.get_model().get_data(idApp, tablename);

                                            AppDBModel appDBModel = new AppDBModel(appContext, codeApp, tbData.model_version,tablename).get_model();

                                            ArrayList<String> arrLocalFieldsFinal = new ArrayList<String>();

                                            List<TableFieldData> tbFieldData = TableFieldModel.get_model().list(tbData._id);

                                            for (int v = 0; v<tbFieldData.size(); v++)
                                            {
                                                arrLocalFieldsFinal.add(tbFieldData.get(v).name);
                                            }

                                            if (clearAll[y].equals("true"))
                                            {
                                                appDBModel.deleteAll();
                                            }


                                            appDBModel.beginTransaction();
                                            for (int w=0; w<rows.length(); w++)
                                            {
                                                appDBModel.setAppTableName(tablename);
                                                int _id = rows.getJSONObject(w).getInt("_id");
                                                //int count = GetData.getInstance().getNumRows(appDBModel.db,tablename, _id);
                                                int count = appDBModel.getNumRowsTrans(tablename, _id);

                                                ContentValues values = new ContentValues();

                                                Iterator<String> fields= rows.getJSONObject(w).keys();
                                                values.put("_id", rows.getJSONObject(w).getInt("_id"));

                                                String ifRepeats = GetData.getInstance().getIfRepeats();

                                                while (fields.hasNext())
                                                {
                                                    String fieldName = fields.next().toString();

                                                    if (arrLocalFieldsFinal.contains(fieldName))
                                                    {
                                                        String record = rows.getJSONObject(w).getString(fieldName);
                                                        if (record == null || record.equals("null")) record = "";
                                                        values.put(fieldName, record);
                                                    }
                                                }

                                                if (values.getAsInteger("_sync") == null)
                                                    values.put("_sync", "0");
                                                else
                                                    values.put("_sync", values.getAsInteger("_sync"));

                                                if ("ignore".equals(ifRepeats) && count > 0)
                                                {
                                                    System.out.println("ignore in "+tablename+" at "+_id);
                                                }
                                                else
                                                {
                                                    appDBModel.saveTransaction(values);
                                                }
                                            }
                                            appDBModel.endTransaction();
                                        }
                                    }

                                    ProgressDialog progressDialog = GetData.getInstance().get_alert();
                                    if (!GetData.getInstance().getBlockSuccess().equals(""))
                                    {
                                        Procedures proc = new Procedures();
                                        proc.exec(GetData.getInstance().getBlockSuccess());

                                    }
                                    if (null != progressDialog) {
                                        progressDialog.dismiss();
                                    }
                                    Connection.sync_locked = false;
                                }
                            }
                        }
                        else
                            GetData.getInstance().onTimeOutEvent();
                    }
                    else
                        GetData.getInstance().onRestError();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }else if (this.type.equals("sync_master")) {
                try
                {
                    Context appContext = FoHeart.getAppContext();
                    SharedPreferences settings = appContext.getSharedPreferences("userconfigs", 0);
                    String codeApp = settings.getString("idApplication", "");
                    int idApp = ApplicationModel.get_model().get_data(codeApp)._id;

                    if (null!=result)
                    {
                        JSONObject responseJSON = new JSONObject(result);

                        if (responseJSON!=null)
                        {
                            if (responseJSON.getBoolean("status"))
                            {
                                if (!responseJSON.isNull("results"))
                                {
                                    Connection.sync_locked = true;
                                    JSONArray resultArray = responseJSON.getJSONArray("results");

                                    int num_results = resultArray.length();

                                    String[] clearAll = GetData.getInstance().getClearAll().split(";;");

                                    if (num_results>0)
                                    {
                                        for (int y=0; y<num_results; y++)
                                        {
                                            JSONObject obj = resultArray.getJSONObject(y);
                                            Iterator<Object> keys = (Iterator)obj.keys();

                                            String tablename = String.valueOf(keys.next());

                                            JSONArray rows = obj.getJSONArray(tablename);
                                            TableData tbData = TableModel.get_model().get_data(idApp, tablename);

                                            AppDBModel appDBModel = new AppDBModel(appContext, codeApp, tbData.model_version,tablename).get_model();

                                            ArrayList<String> arrLocalFieldsFinal = new ArrayList<String>();

                                            List<TableFieldData> tbFieldData = TableFieldModel.get_model().list(tbData._id);

                                            for (int v = 0; v<tbFieldData.size(); v++)
                                            {
                                                arrLocalFieldsFinal.add(tbFieldData.get(v).name);
                                            }

                                            if (clearAll[y].equals("true"))
                                            {
                                                appDBModel.deleteAll();
                                            }

                                            appDBModel.beginTransaction();
                                            for (int w=0; w<rows.length(); w++)
                                            {
                                                appDBModel.setAppTableName(tablename);
                                                int _id = rows.getJSONObject(w).getInt("_id");
                                                //int count = GetData.getInstance().getNumRows(appDBModel.db,tablename, _id);
                                                int count = appDBModel.getNumRowsTrans(tablename, _id);

                                                ContentValues values = new ContentValues();

                                                Iterator<String> fields= rows.getJSONObject(w).keys();
                                                values.put("_id", rows.getJSONObject(w).getInt("_id"));

                                                String ifRepeats = GetData.getInstance().getIfRepeats();

                                                while (fields.hasNext())
                                                {
                                                    String fieldName = fields.next().toString();

                                                    if (arrLocalFieldsFinal.contains(fieldName))
                                                    {
                                                        String record = rows.getJSONObject(w).getString(fieldName);
                                                        if (record == null || record.equals("null")) record = "";
                                                        values.put(fieldName, record);
                                                    }
                                                }

                                                if ("ignore".equals(ifRepeats) && count > 0)
                                                {
                                                    System.out.println("ignore in "+tablename+" at "+_id);
                                                }
                                                else
                                                {
                                                    appDBModel.saveTransaction(values);
                                                }
                                            }
                                            appDBModel.endTransaction();
                                        }
                                    }

                                    ProgressDialog progressDialog = GetData.getInstance().get_alert();
                                    if (!GetData.getInstance().getBlockSuccess().equals(""))
                                    {
                                        Procedures proc = new Procedures();
                                        proc.exec(GetData.getInstance().getBlockSuccess());

                                    }
                                    if (null != progressDialog) {
                                        progressDialog.dismiss();
                                    }
                                    Connection.sync_locked = false;
                                }
                            }
                        }
                        else
                            GetData.getInstance().onTimeOutEvent();
                    }
                    else
                        GetData.getInstance().onRestError();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }

    private static  class PostDataAsync extends AsyncTask<String, Void, String>
    {
        private Exception exception;
        public List<NameValuePair> postData;

        protected String doInBackground(String... urls)
        {
            String url = urls[0];
            HttpPost httppost = new HttpPost(url);

            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 30*1000);
            HttpConnectionParams.setSoTimeout(httpParameters, 45*1000);

            DefaultHttpClient client = new DefaultHttpClient(httpParameters);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            try
            {
                httppost.setEntity(new UrlEncodedFormEntity(postData));
                HttpResponse response = client.execute(httppost);

                String strReturn = responseHandler.handleResponse(response);

                if (null != PostData.getInstance())
                {
                    if (null != PostData.getInstance().get_alert())
                        PostData.getInstance().get_alert().dismiss();
                }


                return strReturn;
            }
            catch (ClientProtocolException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                if (null != PostData.getInstance().get_alert())
                    PostData.getInstance().get_alert().dismiss();

                PostData.getInstance().onTimeOutEvent();

                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute()
        {
        }
    }
}
