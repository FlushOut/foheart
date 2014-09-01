package com.flushoutsolutions.foheart.modules;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.font.Font;
import com.flushoutsolutions.foheart.globals.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

@SuppressLint("DefaultLocale")
public class Label 
{
	public TextView label;
	private String container;
	public String name;
	public String text;
	public int transform=0;
	public float font_size = 14;
	public String font_style = "regular";
	public boolean visible = true;
	
	public ViewGroup layout;
	
	public void initialize(String view, String name, ViewGroup layout, String properties) throws JSONException
	{
		this.container = view;
		this.name = name;
		this.layout = layout;
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("text"))
			this.text = jsonProperties.getString("text");
		
		if (!jsonProperties.isNull("transform"))
			this.transform = jsonProperties.getInt("transform");
		
		if (!jsonProperties.isNull("font-size"))
			this.font_size = Float.parseFloat(jsonProperties.get("font-size").toString());
		
		if (!jsonProperties.isNull("font-style"))
			this.font_style = jsonProperties.getString("font-style");
		
		if (!jsonProperties.isNull("visible"))
			this.visible= jsonProperties.getBoolean("visible");
	}
	
	public void set_text(String text)
	{
		this.text = text;
		this.label.setText(this.text);
	}
	
	public void set_visible(boolean visible)
	{
		this.visible = visible;
		
		if (this.visible)
			label.setVisibility(View.VISIBLE);
		else
			label.setVisibility(View.GONE);
	}
	
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void render()
	{		
		label = new TextView(FoHeart.getAppContext());
		label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		switch (this.transform)
		{
		case 1:
			this.text = this.text.toUpperCase(Locale.getDefault());
			break;
		case 2:
			this.text = this.text.toLowerCase(Locale.getDefault());
			break;
		}
		
		if (!this.visible) label.setVisibility(View.GONE);
		label.setText(this.text);
		label.setTextSize(this.font_size);

		label.setTypeface(Font.get_font(this.font_style));
		this.layout.addView(label);
		this.set_properties();
	}
	
	public void set_properties()
	{	
		String pattern = this.get_name_addressed()+"__";
		Variables.add(pattern + "text", "string", this.text);
	}
	
	
	public void set_property (String p, String v)
	{
		if (p.toLowerCase().equals("text"))
		{
			set_text(v);
		}
		else if (p.toLowerCase().equals("visible"))
		{
			set_visible(Boolean.valueOf(v));
		}
		
		this.set_properties();
	}
}
