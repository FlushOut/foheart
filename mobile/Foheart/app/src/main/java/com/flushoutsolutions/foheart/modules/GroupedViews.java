package com.flushoutsolutions.foheart.modules;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.flushoutsolutions.foheart.ScreenActivity;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.globals.Components;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.logic.TableHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class GroupedViews 
{
	public String container;
	public String name;
	
	public String listSource;
    public String listSQL;
	public String modules;

    public ViewGroup viewGrouped = null;

	public ViewGroup layout;
	public ScreenActivity parent;

    ArrayList<String> vars;
	
	public void initialize(String view, String name, ViewGroup layout, String properties, String events) throws JSONException
	{
        vars = new ArrayList<String>();

		this.container = view;
		this.name = name;
		this.layout = layout;
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("listSource"))
			this.listSource = jsonProperties.getString("listSource");

        if (!jsonProperties.isNull("listSQL"))
            this.listSQL = jsonProperties.getString("listSQL");

		if (!jsonProperties.isNull("modules"))
			this.modules = jsonProperties.getString("modules");
	}
	
	public void render()
	{
		try
		{
			JSONArray jsonList;

            if (null == this.listSQL)
            {
                if (this.listSource.trim().startsWith("["))
                    jsonList = new JSONArray(listSource);
                else
                    jsonList = new JSONArray(Variables.get(Variables.parse_vars(listSource, false)).toString());
            }
            else
            {
                TableHandler tableHandler = new TableHandler();
                jsonList = new JSONArray(tableHandler.query(this.listSQL));
            }

			JSONArray arrayModules = new JSONArray(this.modules);

			int countList = jsonList.length();
			int countMods = arrayModules.length();
System.out.println("viewGrouped "+viewGrouped);
            if (null==viewGrouped)
            {
                LinearLayout LL = new LinearLayout(FoHeart.getAppContext());
                LL.setOrientation(LinearLayout.VERTICAL);

                viewGrouped = LL;
                viewGrouped.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

			this.parent.create_grouped_views(countList, arrayModules, this.container, viewGrouped);
			
			for (int v=0; v<countList; v++)
			{

                // Register temporary variables
                JSONObject theVars = jsonList.getJSONObject(v);

                Iterator<Object> keys = (Iterator)theVars.keys();
                while (keys.hasNext())
                {
                    String varName = keys.next().toString();
                    String varValue = theVars.getString(varName);

                    Variables.add(varName, "string", varValue);
                    vars.add(varName);
                }

				for (int w=0; w<countMods; w++)
				{
					JSONObject theModule = arrayModules.getJSONObject(w);
					String theComponent = this.container+"_"+theModule.getString("name")+"_"+v;
					String theComponentAddressed = this.name+"__"+theComponent;
					String typeComp = Components.get_type(theComponentAddressed);

					if ("label".equals(typeComp))
					{
						Label lblGRP = (Label)Components.get(theComponentAddressed);
						lblGRP.set_text(Variables.parse_vars(lblGRP.text, false));
					}
                    else if ("groupedviews".equals(typeComp))
                    {
                        GroupedViews grpGRP = (GroupedViews)Components.get(theComponentAddressed);

                        if (null != grpGRP.listSQL)
                        {
                            grpGRP.listSQL = Variables.parse_vars(grpGRP.listSQL, false);
                            grpGRP.render();
                        }
                    }
				}
			}
		} 
		catch (JSONException e)
		{
			e.printStackTrace();
		}

        cleanVars();
	}


	public void setProperties()
	{
	
	}

    public void method_refresh()
    {
        int count = this.viewGrouped.getChildCount();

        for (int x=count-1; x>=0; x--)
        {
            if (this.viewGrouped.getChildAt(x)!=null)
            {
                this.viewGrouped.removeViewAt(x);
            }
        }

        render();
    }

    public void cleanVars()
    {
        int v = vars.size();
        for (int x = 0; x <v; x++)
        {
            Variables.remove(vars.get(x));
        }
    }

	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
}
