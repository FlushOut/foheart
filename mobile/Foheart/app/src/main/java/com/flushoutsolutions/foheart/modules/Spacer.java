package com.flushoutsolutions.foheart.modules;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.flushoutsolutions.foheart.application.FoHeart;

import org.json.JSONException;
import org.json.JSONObject;

public class Spacer 
{
	public View viSpace;
	private String container;
	public String name;
	public int height = 10;
	
	public ViewGroup layout;
	
	public void initialize(String view, String name, ViewGroup layout, String properties) throws JSONException
	{
		this.container = view;
		this.name = name;
		this.layout = layout;
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("height"))
			this.height = jsonProperties.getInt("height");
	}
	
	
	public void render()
	{		
		viSpace = new View(FoHeart.getAppContext());
		viSpace.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, height));
		
		this.layout.addView(viSpace);
	}
}
