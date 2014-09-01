package com.flushoutsolutions.foheart.modules;

import android.view.View;
import android.view.ViewGroup;

import com.flushoutsolutions.foheart.application.FoHeart;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by daigomatsuoka on 17/03/14.
 */
public class Line
{
    public View viSpace;
    private String container;
    public String name;
    public int size = 1;

    public ViewGroup layout;

    public void initialize(String view, String name, ViewGroup layout, String properties) throws JSONException
    {
        this.container = view;
        this.name = name;
        this.layout = layout;

        JSONObject jsonProperties = new JSONObject(properties);

        if (!jsonProperties.isNull("size"))
            this.size = jsonProperties.getInt("size");
    }


    public void render()
    {
        viSpace = new View(FoHeart.getAppContext());
        viSpace.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, size));
        viSpace.setBackgroundColor(0xFFFFFFFF);
        this.layout.addView(viSpace);
    }
}
