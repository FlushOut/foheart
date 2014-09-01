package com.flushoutsolutions.foheart.modules;

import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.globals.Variables;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityIndicator 
{
	private String name;
	private String container;
	private int size =1; // 0 small / 1 medium / 2 big
	private boolean hides_on_stop = true;
	private boolean start_animating = true;
	private boolean animating = false;
	private int alignment = 0;
	
	private ImageView spinner;
	private AnimationDrawable anim;
	
	private ViewGroup layout;
	
	public ActivityIndicator(String view, String container, String name, ViewGroup layout)
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
	}
	
	public ActivityIndicator(String view, String container, String name, ViewGroup layout, String properties) throws JSONException
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("size"))
			this.size = jsonProperties.getInt("size");
		
		if (!jsonProperties.isNull("hidesOnStop"))
			this.hides_on_stop = jsonProperties.getBoolean("hidesOnStop");
		
		if (!jsonProperties.isNull("startAnimating"))
			this.start_animating = jsonProperties.getBoolean("startAnimating");
		
		if (!jsonProperties.isNull("alignment"))
			this.alignment= jsonProperties.getInt("alignment");
	}
	
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void set_size (int size)
	{
		this.size= size;
	}
	
	public void set_hides_on_stop(boolean hides_on_stop)
	{
		this.hides_on_stop = hides_on_stop;
	}
	
	public void set_start_animating(boolean start_animating)
	{
		this.start_animating = start_animating;
	}
		
	public void render()
	{		
		spinner = new ImageView(FoHeart.getAppContext());
		
		int wh =0;
		
		switch (this.size)
		{
		case 0:
			//spinner.setBackgroundResource(R.drawable.spinner_small);
			wh=50;
			break;
		case 1:
			//spinner.setBackgroundResource(R.drawable.spinner_medium);
			wh=75;
			break;
		case 2:
			//spinner.setBackgroundResource(R.drawable.spinner_big);
			wh=100;
			break;
		}
			
		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(wh,wh);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
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
		
		spinner.setLayoutParams(parms);
		
		anim = (AnimationDrawable) spinner.getBackground();
		
		if (start_animating)
			start_animation();
		else
			stop_animation();
				
		this.layout.addView(spinner);	
		set_properties();
	}
	
	public void start_animation()
	{
		this.animating = true;
		
		if (this.hides_on_stop)
			spinner.setVisibility(View.VISIBLE);
			
		spinner.post(new Runnable() {
		    @Override
		    public void run() {
		        AnimationDrawable frameAnimation =
		            (AnimationDrawable) spinner.getBackground();
		        frameAnimation.start();
		    }
		});
		
		set_properties();
	}
	
	public void stop_animation()
	{
		this.animating = false;
		if (this.hides_on_stop)
			spinner.setVisibility(View.GONE);
		
		anim.stop();
	}
	
	public void set_properties()
	{	
		String pattern = this.get_name_addressed()+"__";
		Variables.add(pattern + "animating", "boolean", this.animating);
	}
	
	public void set_property (String p, String v)
	{
		if (p.toLowerCase().equals("animating"))
		{
			if (v.equals("true"))
				start_animation();
			else
				stop_animation();
		}
		
		set_properties();
	}
}
