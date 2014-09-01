package com.flushoutsolutions.foheart.design;

import com.flushoutsolutions.foheart.R;

/**
 * Created by Manuel on 10/08/2014.
 */
public class Color {

    private static String activeTheme;
    private static String baseColor = "#";

    private Color()
    {

    }

    public static int get_color(String code)
    {
        return android.graphics.Color.parseColor(baseColor + code);
    }

    public static int parse_color(String code)
    {
        return android.graphics.Color.parseColor(baseColor + code);
    }

    public static int get_button_color(int idCode)
    {
        return android.graphics.Color.parseColor(baseColor +idCode);
    }

    public static void set_active_theme(String the)
    {
        Color.activeTheme = the;
    }

    public static String get_active_theme()
    {
        return Color.activeTheme;
    }

    public static int get_actual_color_60()
    {
        return android.graphics.Color.parseColor(baseColor +Color.activeTheme);
    }
}
