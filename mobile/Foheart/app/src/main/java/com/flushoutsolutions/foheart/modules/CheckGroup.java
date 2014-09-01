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

public class CheckGroup 
{
	private String name;
	private String container;
	private String list = null;
	private boolean multiselect = false;
	private List<CheckGroupItem> listOptions;
	private ViewGroup layout;
	
	private int num_selected = 0;
	
	private String ev_on_select = null;
	
	public CheckGroup(String name, String container, ViewGroup layout)
	{	
		this.name = name;
		this.layout = layout;
		this.container = container;
	}
	
	public CheckGroup(String name, String container, ViewGroup layout, String properties, String events) throws JSONException
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
		
		listOptions = new ArrayList<CheckGroupItem>();
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("list"))
			this.list = jsonProperties.getString("list");
		
		if (!jsonProperties.isNull("multiselect"))
			this.multiselect = jsonProperties.getBoolean("multiselect");
		
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
	
	public void set_multiselect(boolean multiselect)
	{
		this.multiselect = multiselect;
	}
	
	public void render() throws JSONException, IOException
	{	
		int pad10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10, FoHeart.getAppContext().getResources().getDisplayMetrics());
		JSONArray arrayList = new JSONArray(this.list);
		
		for (int x=0; x<arrayList.length(); x++)
		{
			JSONObject objectLine = arrayList.getJSONObject(x);
			
			CheckGroupItem listItem = new CheckGroupItem(FoHeart.getAppContext(), this, x, false, objectLine.getString("value"), objectLine.getString("title"), this.multiselect);
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(0, pad10, 0, 0);
			listItem.setLayoutParams(params);
			
			listOptions.add(listItem);
			
			this.layout.addView(listItem);		
		}
		
		set_properties();
	}
	
	public void event_on_select(CheckGroupItem item)
	{
		if (!this.multiselect)
		{
			for (int x =0; x<listOptions.size(); x++)
			{
				listOptions.get(x).set_unselected();
			}
			item.set_selected();
		}
		
		if (this.ev_on_select!= null && !this.ev_on_select.equals(""))
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
		
		set_properties();
	}
	
	public void set_properties()
	{	
		this.num_selected=0;
		String pattern = this.get_name_addressed()+"__";
		
		String jsonSelectedIndexes = "[";
		
		int size = listOptions.size();
		
		for (int x =0; x<size; x++)
		{
			if (listOptions.get(x).get_state())
			{
				jsonSelectedIndexes += "{'index':"+x+"}";
				this.num_selected++;
			}
			if (x<listOptions.size()-1)
				jsonSelectedIndexes += ", ";
		}
		
		jsonSelectedIndexes += "]";
		
		String jsonSelectedValues = "[";
		
		for (int x =0; x<listOptions.size(); x++)
		{
			if (listOptions.get(x).get_state())
				jsonSelectedValues += "{\"value\":\""+listOptions.get(x).value+"\"}, ";
		}
		
		if (jsonSelectedValues.endsWith(", ")) jsonSelectedValues = jsonSelectedValues.substring(0, jsonSelectedValues.length()-2);
		
		jsonSelectedValues += "]";
		
		Variables.add(pattern + "selectedIndexes", "array.int", jsonSelectedIndexes);
		Variables.add(pattern+"selectedValues", "array.string", jsonSelectedValues);
		Variables.add(pattern+"selectedCount", "int", this.num_selected);
	}
}
