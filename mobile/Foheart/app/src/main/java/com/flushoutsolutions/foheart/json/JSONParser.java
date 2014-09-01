package com.flushoutsolutions.foheart.json;

import android.content.SharedPreferences;

import com.flushoutsolutions.foheart.application.FoHeart;

import org.json.JSONArray;
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
public class JSONParser {
    JSONArray jsonArray;
    JSONObject jsonObject;

    public JSONParser(String filename) throws IOException, JSONException
    {
        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);

        File file = new File(FoHeart.getAppContext().getApplicationInfo().dataDir+"/apps/app"+settings.getString("idApplication", "")+"/app/"+filename);
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
            }
            finally
            {
                is.close();
            }

            String jsonString = wr.toString();
            jsonArray = new JSONArray(jsonString);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }

    }

    public JSONArray get_json_array()
    {
        return jsonArray;
    }
}
