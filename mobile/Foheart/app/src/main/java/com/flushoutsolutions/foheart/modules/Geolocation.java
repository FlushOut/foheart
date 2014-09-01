package com.flushoutsolutions.foheart.modules;

import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.logic.Procedures;
import com.flushoutsolutions.foheart.track.Tracker;

import org.json.JSONException;
import org.json.JSONObject;

public class Geolocation 
{
	public String name;
	public String container;
	public double latitude;
	public double longitude;
	public double altitude;
	public double speed;
	
	private String ev_on_update = null;
	
	
	public Geolocation(String name, String container)
	{
		this.name = name;
		this.container = container;
	}
	
	public Geolocation(String name, String container, String properties, String events) throws JSONException
	{
		this.name = name;
		this.container = container;
		
		JSONObject jsonEvents = new JSONObject(events);

		if (!jsonEvents.isNull("onUpdate"))
			this.ev_on_update = jsonEvents.getString("onUpdate");
	}
	
	
	public void render()
	{
		this.latitude = Tracker.latitude;
		this.longitude = Tracker.longitude;
		this.speed = Tracker.speed;
		
		this.set_properties();
	}
	
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void on_update()
	{
		if (this.ev_on_update!=null && !this.ev_on_update.equals(""))
		{
			try 
			{
				this.set_properties();
				
				Procedures mainProcedure = new Procedures();
				mainProcedure.initialize(this.ev_on_update, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void set_properties()
	{	
		String pattern = this.get_name_addressed()+"__";
		
		Variables.add(pattern + "latitude", "int", Tracker.latitude);
		Variables.add(pattern+"longitude", "string", Tracker.longitude);
		Variables.add(pattern+"speed", "string", Tracker.speed);
	}

}
