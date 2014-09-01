package com.flushoutsolutions.foheart.globals;

import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Manuel on 09/08/2014.
 */
public class Variables {

    public static int scope_for=0;
    public static int scope_while=0;
    public static int scope_do=0;
    public static int scope_if=0;
    public static String scope_procedure="";

    public static final ConcurrentHashMap<String, Object> variables = new ConcurrentHashMap<String, Object>();
    public static final ConcurrentHashMap<String, String> type_variables = new ConcurrentHashMap<String, String>();

    private Variables() {}

    public static void add(String name, String type, Object obj)
    {
        // Increasing or decreasing values : ++ / --

        if (obj.equals("++") || obj.equals("--"))
        {
            // Check if the variable exists
            if (variables.get(name) != null)
            {
                // Only works if there is a variable set and with a numeric type (float or int)
                if (type_variables.get(name).equals("int") || type_variables.get(name).equals("float"))
                {
                    String mode = "";
                    float valF =0;
                    int valI =0;

                    if (type_variables.get(name).equals("int"))
                    {
                        valI = Integer.parseInt(variables.get(name).toString());
                        mode = "i";
                    }
                    else
                    {
                        valF = Float.parseFloat(variables.get(name).toString());
                        mode = "f";
                    }

                    // Increase value
                    if (obj.equals("++"))
                        if (mode.equals("i")) valI++; else valF++;
                    // Or decrease
                    if (obj.equals("--"))
                        if (mode.equals("i")) valI--; else valF--;

                    // set value
                    if (mode.equals("i"))
                        obj = Integer.toString(valI);
                    else
                        obj = Float.toString(valF);
                }
            }
        }

        variables.put(name, obj);

        if (type_variables.get(name) == null && (type == null || type.equals("")))
            Log.e("APP_ERROR", "no type defined to the variable " + name);
        else if (type_variables.get(name) !=null && !type_variables.get(name).equals(type) && !type.equals(""))
            Log.e("APP_ERROR", "trying to assign a new type to the variable "+name);
        else if (type_variables.get(name) == null)
            type_variables.put(name, type);

        System.out.println("add var "+name+"="+obj);
    }

    public static void remove (String name)
    {
        variables.remove(name);
        type_variables.remove(name);
    }

    public static Object get(String name)
    {
        return variables.get(name);
    }

    public static String get_type(String name)
    {
        return type_variables.get(name);
    }

    public static String list_variables()
    {
        String vars="";

        for (String key: variables.keySet())
        {
            if (!type_variables.get(key).equals("component"))
            {
                vars += "var "+key+" = ";

                if (type_variables.get(key).equals("string"))
                    vars += "'"+variables.get(key)+"'; ";
                else
                    vars += variables.get(key)+"; ";
            }

        }

        return vars;
    }

    public static ArrayList<String> list_string_variables()
    {
        ArrayList<String> strs = new ArrayList<String>();

        for (String key: variables.keySet())
        {

            if (type_variables.get(key).equals("string"))
            {
                strs.add(variables.get(key).toString());
            }
        }

        return strs;
    }

    public static String parse_vars(String value, boolean quoted)
    {
        String var_return = value;



        Pattern pattern = Pattern.compile("(?i)(\\{\\{)(.+?)(\\}\\})");
        Matcher matcher = pattern.matcher(value);

        while (matcher.find())
        {
            String rawVar = matcher.group();
            String theVar = rawVar.substring(2, rawVar.length()-2);
            String regVar = "\\{\\{"+theVar+"\\}\\}";

            Object theValue = Variables.variables.get(theVar);

            if (theValue!=null)
            {
                if (quoted)
                {
                    String type = Variables.get_type(theVar);
                    if ("string".equals(type)) theValue = "'"+theValue+"'";
                }
                var_return = var_return.replaceAll(regVar, theValue.toString());
            }
            else
            {
                System.out.println("Variable "+theVar+" doesn't exist");
            }
        }

        return var_return;
    }

    public static boolean check_condition(String expression)
    {
        expression = parse_vars(expression, true);

        Context cx = Context.enter();
        cx.setOptimizationLevel(-1);

        String var_return = "false";
        System.out.println("expression "+ expression);

        try
        {
            Scriptable scope = cx.initStandardObjects();
            ScriptableObject.putProperty(scope, "javaContext", Context.javaToJS(cx, scope));

            String s = "function check(){ if ("+expression+") return true; else return false;} check();";
            Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);

            var_return = Context.toString(result);

        } finally {
            Context.exit();
        }

        return Boolean.parseBoolean(var_return);
    }
}
