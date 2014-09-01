package com.flushoutsolutions.foheart.json;

import android.content.SharedPreferences;

import com.flushoutsolutions.foheart.application.FoHeart;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Manuel on 10/08/2014.
 */
public class ConfigJson {
    JSONObject jsonObject;

    public ConfigJson(String appCode) throws IOException, JSONException
    {
        File file = new File(FoHeart.getAppContext().getApplicationInfo().dataDir+"/apps/app"+appCode+"/app/config.json");
        InputStream is = null;
        try
        {
            is = new BufferedInputStream(new FileInputStream(file));
            int size = is.available();
            Writer wr = new StringWriter();
            char[] buffer = new char[size];

            try
            {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n=reader.read(buffer)) != -1)
                {
                    wr.write(buffer,0,n);
                }
                reader.close();
            }
            finally
            {
                is.close();
            }

            String jsonString = wr.toString();

            jsonObject = new JSONObject(jsonString);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }
    public JSONObject get_json_object()
    {
        return jsonObject;
    }
}
