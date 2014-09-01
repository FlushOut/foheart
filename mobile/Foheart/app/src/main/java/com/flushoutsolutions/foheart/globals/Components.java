package com.flushoutsolutions.foheart.globals;

import android.widget.ImageView;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Manuel on 10/08/2014.
 */
public class Components {

    public static final ConcurrentHashMap<String, Object> components = new ConcurrentHashMap<String, Object>();
    public static final ConcurrentHashMap<String, String> type_components = new ConcurrentHashMap<String, String>();

    public static ImageView selectedImageView;

    private Components() {}

    public static void add(String name, String type, Object obj)
    {
        components.put(name, obj);
        type_components.put(name, type);
    }

    public static Object get(String name)
    {
        return components.get(name);
    }

    public static String get_type(String name)
    {
        return type_components.get(name);
    }
}
