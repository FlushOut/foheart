package com.flushoutsolutions.foheart.modules;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;


import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.design.Color;
import com.flushoutsolutions.foheart.font.Font;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.logic.Procedures;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

@SuppressLint("DefaultLocale")
public class CmdButton 
{
	private Button button;
	
	private String name;
	private String container;
	private String text;
	private int color=-1;
	private int transform=1;
	private float font_size = 16;
	private String font_style = "bold condensed";
	private boolean enabled = true;
	private boolean visible = true;
	private int alignment = 0;
	
	private ViewGroup layout;
	
	private String ev_on_click = null;
	
	public CmdButton(String name, String container, ViewGroup layout)
	{	
		this.name = name;
		this.layout = layout;
		this.container = container;
	}
	
	public CmdButton(String name, String container, ViewGroup layout, String properties, String events) throws JSONException
	{	
		this.name = name;
		this.layout = layout;
		this.container = container;
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("text"))
			this.text = jsonProperties.getString("text");
		
		if (!jsonProperties.isNull("color"))
			this.color = jsonProperties.getInt("color");
		
		if (!jsonProperties.isNull("transform"))
			this.transform = jsonProperties.getInt("transform");
		
		if (!jsonProperties.isNull("font-size"))
			this.font_size = Float.parseFloat(jsonProperties.get("font-size").toString());
		
		if (!jsonProperties.isNull("font-style"))
			this.font_style = jsonProperties.getString("font-style");
		
		if (!jsonProperties.isNull("enabled"))
			this.enabled= jsonProperties.getBoolean("enabled");
		
		if (!jsonProperties.isNull("visible"))
			this.visible= jsonProperties.getBoolean("visible");
		
		if (!jsonProperties.isNull("alignment"))
			this.alignment= jsonProperties.getInt("alignment");
		
		
		JSONObject jsonEvents = new JSONObject(events);

		if (!jsonEvents.isNull("onClick"))
			this.ev_on_click = jsonEvents.getString("onClick");
	}
	
	
	public void set_text(String text)
	{
		this.text = text;
		button.setText(this.text);
	}
	
	public void set_color(int color)
	{
		this.color= color;
	}
	
	public void set_font_size(float size)
	{
		this.font_size = size;
	}
	
	public void set_font_style(String text)
	{
		this.font_style = text;
	}
	
	public void set_transform(int transform)
	{
		this.transform = transform;
	}
	
	public void set_enabled(boolean enabled)
	{
		this.enabled = enabled;
		
		if (this.enabled)
			button.setEnabled(true);
		else
			button.setEnabled(false);
	}
	
	public void set_visible(boolean visible)
	{
		this.visible = visible;
		
		if (this.visible)
			button.setVisibility(View.VISIBLE);
		else
			button.setVisibility(View.GONE);
	}
	
	public void set_alignment(int alignment)
	{
		this.alignment= alignment;
	}
	
	public void render()
	{
		button = new Button(FoHeart.getAppContext());
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
		);
		
		// get alignment
		/*
		 * 0 - left
		 * 1 - center
		 * 2 - right
		 */
			
		switch (this.alignment)
		{
		case 0:
			params.gravity = Gravity.LEFT;
			break;
		case 1:
			params.gravity = Gravity.CENTER_HORIZONTAL;
			break;
		case 2:
			params.gravity = Gravity.RIGHT;
			break;
		}
		
		
		
		params.setMargins(0, 10, 0, 0);
		button.setLayoutParams(params);
		button.setTextColor(0xffffffff);
		button.setShadowLayer(1, -1, -1, 0xff000000);

		if (color == -1)
			button.setBackgroundColor(Color.parse_color(Color.get_active_theme()));
		
		switch (this.transform)
		{
		case 1:
			this.text = this.text.toUpperCase(Locale.getDefault());
			break;
		case 2:
			this.text = this.text.toLowerCase(Locale.getDefault());
			break;
		}
		
		button.setText(this.text);
		button.setTextSize(this.font_size);

		button.setTypeface(Font.get_font(this.font_style));
		
		if (!this.visible) button.setVisibility(View.GONE);
		if (!this.enabled) button.setEnabled(false);
		
		this.layout.addView(button);
		
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{			
				button.setEnabled(false);
				on_click(v);
				button.setEnabled(true);
			}
		});
		
		set_properties();
	}
	
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void on_click(View view)
	{
		if (this.ev_on_click!=null && !this.ev_on_click.equals(""))
		{
			try {
				Procedures mainProcedure = new Procedures();
				mainProcedure.initialize(this.ev_on_click, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		set_properties();
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
		else if (p.toLowerCase().equals("enabled"))
		{
			set_enabled(Boolean.valueOf(v));
		}
		set_properties();
	}
}
