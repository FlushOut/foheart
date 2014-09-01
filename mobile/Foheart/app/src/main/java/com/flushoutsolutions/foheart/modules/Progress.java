package com.flushoutsolutions.foheart.modules;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.design.Color;

import com.flushoutsolutions.foheart.R;
import com.flushoutsolutions.foheart.globals.Variables;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("DefaultLocale")
public class Progress 
{
	private ProgressBar progressBar;
	private String name;
	private String container;
	private int width = 400;
	private int color = -1;
	private int progress = 0;
		
	private ViewGroup layout;
	
	public Progress(String name, String container, ViewGroup layout)
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
	}
	
	public Progress(String name, String container, ViewGroup layout, String properties) throws JSONException
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("width"))
			this.width = jsonProperties.getInt("width");
		
		if (!jsonProperties.isNull("color"))
			this.color = jsonProperties.getInt("color");
		
		if (!jsonProperties.isNull("progress"))
			this.progress = jsonProperties.getInt("progress");
	}
	
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void set_width(int width)
	{
		this.width = width;
	}
	
	public void set_progress(int progress)
	{
		this.progress = progress;
		progressBar.setProgress(this.progress);
	}
	
	public void set_color(int color)
	{
		this.color = color;
	}
	
	public void render()
	{		
		progressBar = new ProgressBar(FoHeart.getAppContext(), null, android.R.attr.progressBarStyleHorizontal);
		progressBar.setLayoutParams(new LayoutParams(this.width, 12));
		progressBar.setMax(100);

        progressBar.setBackgroundColor(Color.parse_color(Color.get_active_theme()));

		Rect bounds = progressBar.getProgressDrawable().getBounds();
		progressBar.getProgressDrawable().setBounds(bounds);

		progressBar.setProgress(this.progress);
		
		this.layout.addView(progressBar);
		set_properties();
	}
	
	
	public void set_properties()
	{	
		String pattern = this.get_name_addressed()+"__";
		Variables.add(pattern + "progress", "int", this.progress);
	}
	
	public void set_property (String p, String v)
	{
		if (p.toLowerCase().equals("progress"))
		{
			set_progress(Integer.parseInt(v));
		}
		
		set_properties();
	}
}
