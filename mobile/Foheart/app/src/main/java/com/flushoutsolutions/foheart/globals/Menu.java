package com.flushoutsolutions.foheart.globals;

import com.flushoutsolutions.foheart.design.MenuButton;

import java.util.HashMap;

/**
 * Created by Manuel on 10/08/2014.
 */
public class Menu {
    public static final HashMap<String, MenuButton> menus = new HashMap<String, MenuButton>();

    private Menu() {}

    public static void add(String name, MenuButton obj)
    {
        menus.put(name, obj);
    }

    public static MenuButton get(String name)
    {
        return menus.get(name);
    }
}
