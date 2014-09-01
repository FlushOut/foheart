package com.flushoutsolutions.foheart.design;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.flushoutsolutions.foheart.globals.Menu;
import com.flushoutsolutions.foheart.json.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Manuel on 10/08/2014.
 */
public class ButtonMenuAdapter extends BaseAdapter{
    private Context mContext;
    private MenuButton[] btns = new MenuButton[90];
    private JSONArray jsonArray;

    public ButtonMenuAdapter(Context c) throws IOException, JSONException {
        mContext = c;

        JSONParser menuJson = new JSONParser("mainmenu.json");

        jsonArray = new JSONArray();
        jsonArray = menuJson.get_json_array();

        for (int x=0; x<jsonArray.length(); x++)
        {
            JSONObject jsonObject = jsonArray.getJSONObject(x);
            int notif = 0;
            if (!jsonObject.isNull("notifications")) notif = jsonObject.getInt("notifications");


            btns[x]= new MenuButton(mContext, jsonObject.get("title").toString(), jsonObject.get("icon").toString(), jsonObject.get("color").toString(), notif);
            Menu.add(jsonObject.get("name").toString(), btns[x]);
        }

    }

    public MenuButton[] get_menu_buttons()
    {
        return this.btns;
    }

    public int getCount() {
        return jsonArray.length();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return btns[position];
    }
}
