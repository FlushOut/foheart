package com.flushoutsolutions.foheart.modules;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.font.Font;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.logic.Procedures;

import org.json.JSONException;
import org.json.JSONObject;

public class Textfield 
{
	private final EditText textfield = new EditText(FoHeart.getAppContext());
	private String name;
	private String container;
	private int lines = 1;
	private boolean password = false;
	private String text;
	private float font_size = 14;
	private String font_style = "regular";
	private String hint;
	private int keyboard = 0;
	private boolean enabled = true;
	private boolean visible = true;

	private ViewGroup layout;
	
	private String ev_on_focus = null;
	private String ev_on_change = null;
	private String ev_on_blur = null;
	
	public Textfield(String name, String container, ViewGroup layout)
	{		
		this.name = name;
		this.layout = layout;
		this.container = container;
	}
	
	public Textfield(String name, String container, ViewGroup layout, String properties, String events) throws JSONException
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("lines"))
			this.lines = jsonProperties.getInt("lines");
		
		if (!jsonProperties.isNull("password"))
			this.password = jsonProperties.getBoolean("password");
		
		if (!jsonProperties.isNull("text"))
			this.text = jsonProperties.getString("text");

		if (!jsonProperties.isNull("font-size"))
			this.font_size = Float.parseFloat(jsonProperties.get("font-size").toString());
		
		if (!jsonProperties.isNull("font-style"))
			this.font_style = jsonProperties.getString("font-style");
		
		if (!jsonProperties.isNull("hint"))
			this.hint = jsonProperties.getString("hint");
		
		if (!jsonProperties.isNull("keyboard"))
			this.keyboard = jsonProperties.getInt("keyboard");
		
		if (!jsonProperties.isNull("enabled"))
			this.enabled= jsonProperties.getBoolean("enabled");
		
		if (!jsonProperties.isNull("visible"))
			this.visible= jsonProperties.getBoolean("visible");
		
		
		JSONObject jsonEvents = new JSONObject(events);
		
		if (!jsonEvents.isNull("onFocus"))
			this.ev_on_focus = jsonEvents.getString("onFocus");
		
		if (!jsonEvents.isNull("onChange"))
			this.ev_on_change = jsonEvents.getString("onChange");
		
		if (!jsonEvents.isNull("onBlur"))
			this.ev_on_blur = jsonEvents.getString("onBlur");

	}
	
	public String get_name()
	{
		return this.name;
	}
	
	public void set_lines(int lines)
	{
		this.lines = lines;
	}
	
	public void set_password(boolean password)
	{
		this.password = password;
	}
	
	public void set_text(String text)
	{
		this.text = Variables.parse_vars(text, false);
		textfield.setText(this.text);
	}
	
	public void set_font_size(float size)
	{
		this.font_size = size;
	}
	
	public void set_font_style(String text)
	{
		this.font_style = text;
	}
	
	public void set_hint(String hint)
	{
		this.hint = hint;
	}
	
	public void set_keyboard(int keyboard)
	{
		this.keyboard = keyboard;
	}
	
	public void set_enabled(boolean enabled)
	{
		this.enabled = enabled;
		
		if (this.enabled)
			textfield.setEnabled(true);
		else
			textfield.setEnabled(false);
	}
	
	public void set_visible(boolean visible)
	{
		this.visible = visible;
		
		if (this.visible)
			textfield.setVisibility(View.VISIBLE);
		else
			textfield.setVisibility(View.GONE);
	}
	
	public void render()
	{
        textfield.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		textfield.setGravity(Gravity.TOP);
		
		textfield.setText(Variables.parse_vars(this.text, false));
		textfield.setHint(this.hint);
		textfield.setLines(this.lines);
			
		if (this.lines<=1)
			textfield.setSingleLine();
		
		switch (this.keyboard)
		{
		case 1:
			textfield.setInputType(InputType.TYPE_CLASS_NUMBER);
			break;
		case 2:
			textfield.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			break;
		case 3:
			textfield.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			break;
		}
		
		if (this.password)
			textfield.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		
		if (!this.visible) textfield.setVisibility(View.GONE);
		if (!this.enabled) textfield.setEnabled(false);
		
		textfield.setMaxLines(this.lines);
		textfield.setTextSize(this.font_size);

		textfield.setTypeface(Font.get_font(this.font_style));
		
		textfield.setOnFocusChangeListener(new TextfieldChangeListener(textfield));
		
		textfield.addTextChangedListener(new TextWatcher() {
		    public void onTextChanged(CharSequence s, int start, int before, int count) {
		    	
		    }
		    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		    }

		    public void afterTextChanged(Editable s)
		    {
		        event_on_change(textfield);
		    }
		});
		
		this.layout.addView(textfield);
		set_properties();
	}
	
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void event_on_focus(View view)
	{
		if (this.ev_on_focus!=null && !this.ev_on_focus.equals(""))
		{
			try 
			{
				Procedures mainProcedure = new Procedures();
				mainProcedure.initialize(this.ev_on_focus, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
			} 
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		set_properties();
	}
	
	public void event_on_change(EditText edt)
	{
		this.text = edt.getText().toString();
		
		if (this.ev_on_change!=null && !this.ev_on_change.equals(""))
		{
			try 
			{
				Procedures mainProcedure = new Procedures(); 
				mainProcedure.initialize(this.ev_on_change, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		set_properties();
	}

	public void event_on_blur(View view)
	{
		if (this.ev_on_blur!=null && !this.ev_on_blur.equals(""))
		{
			try {
				Procedures mainProcedure = new Procedures(); 
				mainProcedure.initialize(this.ev_on_blur, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		set_properties();
	}
	
	private class TextfieldChangeListener implements OnFocusChangeListener
	{
		private EditText et;
		
	    public TextfieldChangeListener(EditText editText)
	    {
            System.out.println("focus "+editText);
	    	this.setEt(editText);
	    }

	    @Override
	    public void onFocusChange(View view, boolean isFocused)
	    {

            System.out.println("focus changed "+name);
	    	if (isFocused) 
	    	{
                System.out.println("focus changed focus"+name);
	    		event_on_focus(view);
	    	}
	    	else
	    	{
                System.out.println("focus changed blur "+name);
	    		event_on_blur(view);
	    	}
	    }
	    
		@SuppressWarnings("unused")
		public EditText getEt() {
			return et;
		}
		
		public void setEt(EditText et) {
			this.et = et;
		}
	}
	
	public void set_properties()
	{	
		String pattern = this.get_name_addressed()+"__";
		Variables.add(pattern+"text", "string", this.text);
	}
	
	
	@SuppressLint("DefaultLocale")
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
