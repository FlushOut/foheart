package com.flushoutsolutions.foheart.modules;

import android.util.TypedValue;
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

public class ListOptions 
{
	private String name;
	private String container;
	private String list = null;
	private List<ListOptionItem> listOptions;
	private ViewGroup layout;
	
	private int index = -1;
	private String selected_value = "";
	private String selected_title = "";
	
	private String ev_on_select = null;
	
	public ListOptions(String name, String container, ViewGroup layout)
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
	}
	
	public ListOptions(String name, String container, ViewGroup layout, String properties, String events) throws JSONException
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
		
		listOptions = new ArrayList<ListOptionItem>();
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("list"))
			this.list = jsonProperties.getString("list");
		
		JSONObject jsonEvents = new JSONObject(events);

		if (!jsonEvents.isNull("onSelect"))
			this.ev_on_select = jsonEvents.getString("onSelect");
	}
	
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void set_list(String list)
	{
		this.list = list;
	}

	
	public void render() throws JSONException, IOException
	{	
		int padBot = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 16, FoHeart.getAppContext().getResources().getDisplayMetrics());
		
		if (this.list.startsWith("["))
		{
			JSONArray arrayList = new JSONArray(this.list);
			
			for (int x=0; x<arrayList.length(); x++)
			{
				JSONObject objectLine = arrayList.getJSONObject(x);
				String icon = null;
				if (!objectLine.isNull("icon"))
					icon = objectLine.getString("icon");
				
				ListOptionItem listItem = new ListOptionItem(FoHeart.getAppContext(), this, x, objectLine.getString("value"), objectLine.getString("title"), icon);
				
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				params.setMargins(0, padBot, 0, 0);
				listItem.setLayoutParams(params);
				
				listOptions.add(listItem);
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
				
				String icon = "";
				
				if (!props.isNull("icon"))
					icon = objectLine.getString(Variables.parse_vars(props.getString("icon"), false));
				
				ListOptionItem listItem = new ListOptionItem(FoHeart.getAppContext(), this, x, value, title, icon);
				
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				params.setMargins(0, 10, 0, 0);
				listItem.setLayoutParams(params);
				
				listOptions.add(listItem);
				this.layout.addView(listItem);
			}
		}
		set_properties();
	}
	
	public void event_on_select(ListOptionItem item)
	{
		this.index = item.get_index();
		this.selected_value = item.get_value();
		this.selected_title = item.get_title();
		set_properties();
		
		if (this.ev_on_select != null && !this.ev_on_select.equals(""))
		{
			try 
	     	{
				Procedures mainProcedure = new Procedures();
				mainProcedure.initialize(this.ev_on_select, "{'sender':'"+get_name_addressed()+"'}");
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
		String pattern = this.get_name_addressed()+"__";
		Variables.add(pattern+"selectedIndex", "int", this.index);
		Variables.add(pattern+"selectedValue", "int", this.selected_value);
		Variables.add(pattern+"selectedTitle", "int", this.selected_title);
	}
}
