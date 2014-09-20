package com.flushoutsolutions.foheart.modules;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.logic.Procedures;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListCheckScreen 
{
	private String name;
	private String container;
	private int style = 1;
	private String list = null;
	private boolean multiselect = true;
	private List<ListCheckScreenItem> listOptions;
	private List<Boolean> listOptionsSelected;
	private ViewGroup layout;
    private String checkedItem;
	
	private String ev_on_select_cell = null;
	
	public ListCheckScreen(String name, String container, ViewGroup layout)
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
	}
	
	public ListCheckScreen(String name, String container, ViewGroup layout, String properties, String events) throws JSONException
	{
		this.name = name;
		this.layout = layout;
		this.container = container;

        System.out.println("debug: ListCheckScreen");
		
		listOptions = new ArrayList<ListCheckScreenItem>();
		listOptionsSelected = new ArrayList<Boolean>();
		
		JSONObject jsonProperties = new JSONObject(properties);

        System.out.println("jsonproperties: " + jsonProperties);
		
		if (!jsonProperties.isNull("style"))
			this.style = jsonProperties.getInt("style");
		
		if (!jsonProperties.isNull("list"))
			this.list = jsonProperties.getString("list");
		
		if (!jsonProperties.isNull("multiselect"))
			this.multiselect = jsonProperties.getBoolean("multiselect");

        if(!jsonProperties.isNull("checkedItem"))
            this.checkedItem = Variables.parse_vars(jsonProperties.getString("checkedItem"), false);
		
		JSONObject jsonEvents = new JSONObject(events);

		if (!jsonEvents.isNull("onSelectCell"))
			this.ev_on_select_cell = jsonEvents.getString("onSelectCell");
	}

	public void set_style(int style)
	{
		this.style = style;
	}
	
	public void set_multiselect(boolean multiselect)
	{
		this.multiselect = multiselect;
	}
	
	public void set_list(String list)
	{
		this.list = list;
	}
	
	public void render() throws JSONException, IOException
	{	
		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 60, FoHeart.getAppContext().getResources().getDisplayMetrics());
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 4, FoHeart.getAppContext().getResources().getDisplayMetrics());

		if (this.list.startsWith("["))
		{
			JSONArray arrayList = new JSONArray(this.list);
			
			for (int x=0; x<arrayList.length(); x++)
			{
				JSONObject objectLine = arrayList.getJSONObject(x);
				
				String icon = "";
				
				if (!objectLine.isNull("icon"))
					icon = Variables.parse_vars(objectLine.getString("icon"), false);

                ListCheckScreenItem listItem;
                //validando item como checked
                if(objectLine.getString("value").equals(this.checkedItem)){
                    listItem = new ListCheckScreenItem(FoHeart.getAppContext(), this, x, false, this.style, objectLine.getString("value"), objectLine.getString("title"), objectLine.getString("subtitle"), icon, true);
                }else {
                    listItem = new ListCheckScreenItem(FoHeart.getAppContext(), this, x, false, this.style, objectLine.getString("value"), objectLine.getString("title"), objectLine.getString("subtitle"), icon, false);
                }

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, height);
				params.setMargins(0, 0, 0, margin);
				listItem.setLayoutParams(params);
				
				listOptions.add(listItem);
				listOptionsSelected.add(false);
				this.layout.addView(listItem);	
			}	
		}
		else
		{
			JSONObject props = new JSONObject(this.list);
			JSONArray arrayList = new JSONArray(Variables.get(props.getString("objectList")).toString());

			for (int x=0; x<arrayList.length(); x++)
			{
				JSONObject objectLine = arrayList.getJSONObject(x);

				String value = objectLine.getString(Variables.parse_vars(props.getString("index"), false));
				String title = objectLine.getString(Variables.parse_vars(props.getString("title"), false));

				String subtitle = "";
				
				if (!props.isNull("subtitle"))
                    if (!"".equals(props.getString("subtitle")))
					    subtitle = objectLine.getString(Variables.parse_vars(props.getString("subtitle"), false));

				String icon = "";
				
				if (!props.isNull("icon"))
                    if (!"".equals(props.getString("icon")))
					    icon = objectLine.getString(Variables.parse_vars(props.getString("icon"), false));

                ListCheckScreenItem listItem;
                //validando item como checked
                if(value.equals(this.checkedItem)) {
                    listItem = new ListCheckScreenItem(FoHeart.getAppContext(), this, x, false, this.style, value, title, subtitle, icon, true);
                }else{
                    listItem = new ListCheckScreenItem(FoHeart.getAppContext(), this, x, false, this.style, value, title, subtitle, icon, false);
                }

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, height);
				params.setMargins(0, 0, 0, margin);
				listItem.setLayoutParams(params);
				System.out.println("listItem " + listItem);
				listOptions.add(listItem);
				listOptionsSelected.add(false);
				this.layout.addView(listItem);
			}
		}
		
		set_properties();
	}
	
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void event_on_select(int position)
	{
		if (!this.multiselect)
		{
			for (int x =0; x<listOptions.size(); x++)
			{
				listOptions.get(x).set_unselected();
				listOptionsSelected.set(x, false);
			}
			
			listOptions.get(position).set_selected();
			listOptionsSelected.set(position, true);
		}
        set_properties();

		if (this.ev_on_select_cell != null && !this.ev_on_select_cell.equals(""))
		{
			try 
	     	{
				Procedures mainProcedure = new Procedures();
				mainProcedure.initialize(this.ev_on_select_cell, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
	     	} 
	     	catch (JSONException e1)
	     	{
	     		e1.printStackTrace();
	     	}
		}

	}
	
	public void set_properties()
	{
        String valueString = "";

		String pattern = this.get_name_addressed()+"__";

		String jsonSelectedIndexes = "[";

		for (int x =0; x<listOptions.size(); x++)
		{
			if (listOptions.get(x).get_state())
				jsonSelectedIndexes += "{\"index\":"+x+"}, ";
		}

		if (jsonSelectedIndexes.endsWith(", ")) jsonSelectedIndexes = jsonSelectedIndexes.substring(0, jsonSelectedIndexes.length()-2);

		jsonSelectedIndexes += "]";


		String jsonSelectedValues = "[";

		for (int x =0; x<listOptions.size(); x++)
		{
			if (listOptions.get(x).get_state())
            {
				jsonSelectedValues += "{\"value\":\""+listOptions.get(x).get_value()+"\"}, ";
                //valueString = valueString + listOptions.get(x).get_value()+";";
                valueString = valueString + listOptions.get(x).get_value()+",";
            }

		}

		if (jsonSelectedValues.endsWith(", ")) jsonSelectedValues = jsonSelectedValues.substring(0, jsonSelectedValues.length()-2);

		jsonSelectedValues += "]";


		String jsonSelectedTitles = "[";

		for (int x =0; x<listOptions.size(); x++)
		{
			if (listOptions.get(x).get_state())
				jsonSelectedTitles += "{\"title\":\""+listOptions.get(x).get_title()+"\"}, ";
		}
		if (jsonSelectedTitles.endsWith(", ")) jsonSelectedTitles = jsonSelectedTitles.substring(0, jsonSelectedTitles.length()-2);

		jsonSelectedTitles += "]";

        if (valueString.length() > 0) valueString = valueString.substring(0, valueString.length()-1);

		Variables.add(pattern+"selectedIndexes", "array.int", jsonSelectedIndexes);
		Variables.add(pattern+"selectedValues", "array.string", jsonSelectedValues);
		Variables.add(pattern+"selectedTitles", "array.string", jsonSelectedTitles);
        Variables.add(pattern+"selectedValuesString", "string", valueString);
	}

    public void method_clear()
    {
        int count = this.layout.getChildCount();

        for (int x=count-1; x>=0; x--)
        {
            //	System.out.println("dbg "+this.layout.getChildAt(x).getClass());
            if (this.layout.getChildAt(x)!=null)
            {
                Class<? extends View> cls = this.layout.getChildAt(x).getClass();
                if (cls.getName().endsWith("ListCheckScreenItem"))
                {
                    ListCheckScreenItem item = (ListCheckScreenItem)this.layout.getChildAt(x);
                    if (item.parent == this)
                        this.layout.removeViewAt(x);
                }
            }
        }
    }
}
