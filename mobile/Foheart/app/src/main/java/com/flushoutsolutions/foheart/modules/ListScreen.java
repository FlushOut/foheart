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

public class ListScreen 
{
	private String name;
	private String container;
	private int style = 1;
	private String list = null;
	private boolean disclosured = true;
	
	private int index = -1;
	private String selected_value = "";
	private String selected_title = "";
	
	private String ev_on_select_cell = null;
	
	private ViewGroup layout;
	
	
	public ListScreen(String name, String container, ViewGroup layout)
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
	}
	
	public ListScreen(String name, String container, ViewGroup layout, String properties, String events) throws JSONException
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("style"))
			this.style = jsonProperties.getInt("style");
		
		if (!jsonProperties.isNull("list"))
			this.list = jsonProperties.getString("list");
		
		if (!jsonProperties.isNull("disclosured"))
			this.disclosured = jsonProperties.getBoolean("disclosured");
		
		JSONObject jsonEvents = new JSONObject(events);

		if (!jsonEvents.isNull("onSelectCell"))
			this.ev_on_select_cell = jsonEvents.getString("onSelectCell");
		
	}
	
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void set_style(int style)
	{
		this.style = style;
	}
	
	public void set_disclosured(boolean disclosured)
	{
		this.disclosured = disclosured;
	}
	
	public void set_list(String list)
	{
		this.list = list;
	}
	
	public void render() throws JSONException, IOException
	{
		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 60, FoHeart.getAppContext().getResources().getDisplayMetrics());
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 4, FoHeart.getAppContext().getResources().getDisplayMetrics());
		
		if (this.list != null)
		{
			System.out.println("debug: " + this.list);
			
			if (this.list.startsWith("["))
			{
				JSONArray arrayList = new JSONArray(this.list);
				
				for (int x=0; x<arrayList.length(); x++)
				{
					JSONObject objectLine = arrayList.getJSONObject(x);
					
					String value = "";
					if (!objectLine.isNull("value"))
						value = objectLine.getString("value");
						
					String subtitle = "";
					if (!"".equals(objectLine.getString("subtitle")) && !objectLine.isNull("subtitle"))
						subtitle = objectLine.getString("subtitle");
					
					ListScreenItem listItem = new ListScreenItem(FoHeart.getAppContext(), this, x, this.style, value, objectLine.getString("title"), subtitle, objectLine.getString("icon"), this.disclosured);
					
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, height);
					params.setMargins(0, 0, 0, margin);
					listItem.setLayoutParams(params);
					
					this.layout.addView(listItem);
				}
			}
			else
			{
				
				JSONObject props = new JSONObject(this.list);
				
				if (Variables.get(props.getString("objectList")) != null)
				{
					JSONArray arrayList = new JSONArray(Variables.get(props.getString("objectList")).toString());

					for (int x=0; x<arrayList.length(); x++)
					{
						JSONObject objectLine = arrayList.getJSONObject(x);

						String value = objectLine.getString(Variables.parse_vars(props.getString("index"), false));
						String title = objectLine.getString(Variables.parse_vars(props.getString("title"), false));
						
						String subtitle = "";
						
						if (!props.isNull("subtitle") && !"".equals(props.getString("subtitle")))
							subtitle = objectLine.getString(Variables.parse_vars(props.getString("subtitle"), false));
					
						String icon = "";
						
						if (!props.isNull("icon"))
							icon = objectLine.getString(Variables.parse_vars(props.getString("icon"), false));
						
						ListScreenItem listItem = new ListScreenItem(FoHeart.getAppContext(), this, x, this.style, value, title, subtitle, icon, this.disclosured);
						
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, height);
						params.setMargins(0, 0, 0, margin);
						listItem.setLayoutParams(params);
						
						this.layout.addView(listItem);
					}
				}
			}	
		}
		this.set_properties();
	}
	
	public void event_on_select(ListScreenItem item)
	{
		this.index = item.get_index();
		this.selected_value = item.value;
		this.selected_title = item.title;
		
		this.set_properties();
		
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
		String pattern = this.get_name_addressed()+"__";
		
		Variables.add(pattern+"selectedIndex", "int", this.index);

		Variables.add(pattern+"selectedValue", "string", this.selected_value);
		Variables.add(pattern+"selectedTitle", "string", this.selected_title);
	}

	public void method_refresh() 
	{
		int count = this.layout.getChildCount();
			
		System.out.println("count "+count);
		for (int x=count-1; x>=0; x--)
		{
		//	System.out.println("dbg "+this.layout.getChildAt(x).getClass());
			if (this.layout.getChildAt(x)!=null)
			{
				Class<? extends View> cls = this.layout.getChildAt(x).getClass();
				if (cls.getName().endsWith("ListScreenItem"))
				{
					ListScreenItem item = (ListScreenItem)this.layout.getChildAt(x);
					if (item.parent == this)
						this.layout.removeViewAt(x);
				}
			}
		}
		
		try {
			render();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



}
