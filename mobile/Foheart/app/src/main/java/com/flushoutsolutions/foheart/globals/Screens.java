package com.flushoutsolutions.foheart.globals;

import android.app.Activity;
import android.content.Context;

import com.flushoutsolutions.foheart.ScreenActivity;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Manuel on 09/08/2014.
 */
public class Screens {

    public static String current ="";
    public static Context currentCtx;
    public static Activity currentInstance;
    public static final ConcurrentHashMap<String, ScreenActivity> screens = new ConcurrentHashMap<String, ScreenActivity>();
    public static final ArrayList<ScreenActivity> openScreens = new ArrayList<ScreenActivity>();

    private Screens() {}

    public static void add(String name, ScreenActivity obj)
    {
        screens.put(name, obj);
        openScreens.add(obj);
    }

    public static void remove(String name)
    {
        screens.remove(name);
    }

    public static ScreenActivity get(String name)
    {
        return screens.get(name);
    }
}
