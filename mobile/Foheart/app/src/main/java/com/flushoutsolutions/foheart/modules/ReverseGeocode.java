package com.flushoutsolutions.foheart.modules;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.communication.Connection;
import com.flushoutsolutions.foheart.data.InternetStatus;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.logic.Procedures;
import com.flushoutsolutions.foheart.track.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReverseGeocode 
{
	private String name;
	private String container;
	
	private String latitude = "";
	private String longitude = "";
	private String initialLatitude = "";
	private String initialLongitude = "";
	
	private String route="";
	private String sublocality="";
	private String city="";
	private String country="";
	private String postal_code="";
	
	private String ev_on_retrieve = null;
	private String ev_on_location_error = null;
	private String ev_on_gps_error = null;
	private String ev_on_connection_error = null;
	
	private LocationManager lm;
	
	private static ReverseGeocode instance = null;
	
	public static ReverseGeocode getInstance()
	{
		return instance;
	}
	
	
	public ReverseGeocode(String name, String container)
	{
		this.name = name;
		this.container = container;
		
		instance = this;
	}
	
	public ReverseGeocode(String name, String container, String properties, String events) throws JSONException
	{
		this.name = name;
		this.container = container;
		
		instance = this;
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("latitude"))
		{
			this.latitude = jsonProperties.getString("latitude");
			this.initialLatitude = jsonProperties.getString("latitude");
		}
		
		if (!jsonProperties.isNull("longitude"))
		{
			this.longitude = jsonProperties.getString("longitude");
			this.initialLongitude = jsonProperties.getString("longitude");
		}
		
		JSONObject jsonEvents = new JSONObject(events);

		if (!jsonEvents.isNull("onRetrieve"))
			this.ev_on_retrieve = jsonEvents.getString("onRetrieve");
		
		if (!jsonEvents.isNull("onLocationError"))
			this.ev_on_location_error = jsonEvents.getString("onLocationError");
		
		if (!jsonEvents.isNull("onGPSError"))
			this.ev_on_gps_error = jsonEvents.getString("onGPSError");
		
		if (!jsonEvents.isNull("onConnectionError"))
			this.ev_on_connection_error = jsonEvents.getString("onConnectionError");
	}
	
	
	public void set_latitude(String lat)
	{
		this.latitude = lat;
	}
	
	public void set_longitude(String lon)
	{
		this.longitude = lon;
	}
	
	
	public void set_route(String route)
	{
		this.route = route;
	}
	
	public void set_sublocality(String sublocality)
	{
		this.sublocality = sublocality;
	}
	
	public void set_city(String city)
	{
		this.city= city;
	}
	
	public void set_country(String country)
	{
		this.country = country;
	}
	
	public void set_postal_code(String postal_code)
	{
		this.postal_code = postal_code;
	}
		
	public void render()
	{
		
		if (this.latitude=="" || this.longitude=="" )
		{
			if (Tracker.new_latitude != 0)
			{
				this.latitude = String.valueOf(Tracker.new_latitude);
				this.longitude = String.valueOf(Tracker.new_longitude);
				
				findAddress();
			}
			else
			{
				lm = (LocationManager) FoHeart.getAppContext().getSystemService(Context.LOCATION_SERVICE);
		    	
	    		boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	    		
	    		if (isNetworkEnabled && InternetStatus.isOnline())
	    		{
	    			LocationListener ll = new update_manager();
	    			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1, ll);
	    		}
	    		else
	    		{
	    			on_gps_error();
	    		}
			}
		}
		set_properties();
	}
	
	public void method_refresh()
	{
		this.latitude = this.initialLatitude;
		this.longitude = this.initialLongitude;
		render();
	}
	
	public void findAddress()
	{
		final String stringUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+this.latitude+","+this.longitude+"&sensor=true";
		
		if (InternetStatus.isOnline())
		{
			String responseString =  Connection.get("reversegeocode", stringUrl);

            try
            {
                JSONObject responseJSON = new JSONObject(responseString);

                if (responseJSON!=null)
                {
                    if (!responseJSON.isNull("results"))
                    {
                        JSONArray resultArray = responseJSON.getJSONArray("results");

                        int num_results = resultArray.length();

                        if (num_results==0)
                        {
                            ReverseGeocode.getInstance().on_location_error();
                        }
                        else
                        {
                            JSONObject resultLine0Json = resultArray.getJSONObject(0);

                            JSONArray addressDataArray = resultLine0Json.getJSONArray("address_components");

                            for (int x=0; x<addressDataArray.length(); x++)
                            {
                                JSONObject jsonOb = addressDataArray.getJSONObject(x);
                                if (jsonOb.getJSONArray("types").getString(0).equals("route"))
                                    ReverseGeocode.getInstance().set_route(jsonOb.getString("short_name"));
                                else if (jsonOb.getJSONArray("types").getString(0).equals("sublocality"))
                                    ReverseGeocode.getInstance().set_sublocality(jsonOb.getString("short_name"));
                                else if (jsonOb.getJSONArray("types").getString(0).equals("locality"))
                                    ReverseGeocode.getInstance().set_city(jsonOb.getString("short_name"));
                                else if (jsonOb.getJSONArray("types").getString(0).equals("country"))
                                    ReverseGeocode.getInstance().set_country(jsonOb.getString("short_name"));
                                else if (jsonOb.getJSONArray("types").getString(0).equals("postal_code"))
                                    ReverseGeocode.getInstance().set_postal_code(jsonOb.getString("short_name"));

                                ReverseGeocode.getInstance().set_properties();
                            }
                            ReverseGeocode.getInstance().on_retrieve();
                        }
                    }
                    else
                    {
                        ReverseGeocode.getInstance().on_location_error();
                    }
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
		else 
		{
        	on_connection_error();
        }
	}
		
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void on_retrieve()
	{
		if (this.ev_on_retrieve!=null && !this.ev_on_retrieve.equals(""))
		{
			try 
			{
				Procedures mainProcedure = new Procedures();
				mainProcedure.initialize(this.ev_on_retrieve, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		set_properties();
	}
	
	public void on_connection_error()
	{
		if (this.ev_on_connection_error!=null && !this.ev_on_connection_error.equals(""))
		{
			try 
			{
				Procedures mainProcedure = new Procedures(); 
				mainProcedure.initialize(this.ev_on_connection_error, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
			
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		set_properties();
	}
	
	public void on_gps_error()
	{
		if (this.ev_on_gps_error!=null && !this.ev_on_gps_error.equals(""))
		{
			try 
			{
				Procedures mainProcedure = new Procedures(); 
				mainProcedure.initialize(this.ev_on_gps_error, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		set_properties();
	}
	
	public void on_location_error()
	{
		if (this.ev_on_location_error!=null && !this.ev_on_location_error.equals(""))
		{
			try 
			{
				Procedures mainProcedure = new Procedures(); 
				mainProcedure.initialize(this.ev_on_location_error, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
			
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void set_properties()
	{	
		String pattern = this.get_name_addressed()+"__";
		Variables.add(pattern + "route", "string", this.route);
		Variables.add(pattern+"sublocality", "string", this.sublocality);
		Variables.add(pattern+"city", "string", this.city);
		Variables.add(pattern+"country", "string", this.country);
		Variables.add(pattern+"postalCode", "string", this.postal_code);
	}
	
	 private class update_manager implements LocationListener
	 {
		 @Override
		 public void onLocationChanged(Location location)
		 {
			 if (location != null) 
			 {
				 set_latitude(String.valueOf(location.getLatitude()));
				 set_longitude(String.valueOf(location.getLongitude()));
				 findAddress();
				 lm.removeUpdates(this);
			 }
		 }

		 @Override
		 public void onProviderDisabled(String provider)
		 {
		 }

		 @Override
		 public void onProviderEnabled(String provider)
		 {
		 }

		 @Override
		 public void onStatusChanged(String provider, int status, Bundle extras)
		 {
		 }
	 }
}

