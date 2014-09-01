package com.flushoutsolutions.foheart.modules;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.font.Font;
import com.flushoutsolutions.foheart.R;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.logic.Procedures;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Combobox 
{
	private Context parentActivityContext;
	private String name;
	private String container;
	private String selected_value = "";
	private String empty_value="";
	private ArrayList<String> listOptions;
	private ArrayList<String> listValues;
	private String list;
	private int index=-1;
	
	private Button comboboxButton;
	private ViewGroup layout;
	
	private String ev_on_change = null;
	
	public Combobox(String name, String container, ViewGroup layout)
	{	
		this.name = name;
		this.layout = layout;
		this.container = container;
	}
	
	public Combobox(String name, String container, ViewGroup layout, String properties, String events) throws JSONException
	{
		this.name = name;
		this.layout = layout;
		this.container = container;
		
		listOptions = new ArrayList<String>();
		listValues = new ArrayList<String>();
		
		JSONObject jsonProperties = new JSONObject(properties);
		
		if (!jsonProperties.isNull("emptyValue"))
			this.empty_value = jsonProperties.getString("emptyValue");
		
		if (!jsonProperties.isNull("selectedValue"))
			this.selected_value = jsonProperties.getString("selectedValue");
		
		if (!jsonProperties.isNull("list"))
			this.list = jsonProperties.getString("list");

		
		JSONObject jsonEvents = new JSONObject(events);

		if (!jsonEvents.isNull("onChange"))
			this.ev_on_change = jsonEvents.getString("onChange");
	}
	
	public void set_parent_activity_context(Context parentActivityContext)
	{
		this.parentActivityContext = parentActivityContext;
	}
	
	public void set_selected_value(String selected_value)
	{
		this.selected_value = selected_value;
	}
	
	
	public void render() throws JSONException
	{
		comboboxButton = new Button(FoHeart.getAppContext());
		comboboxButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		
		comboboxButton.setTypeface(Font.get_font("medium"));
		comboboxButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		comboboxButton.setGravity(Gravity.LEFT);
		comboboxButton.setGravity(Gravity.CENTER_VERTICAL);
		comboboxButton.setCompoundDrawablesWithIntrinsicBounds (null, null, FoHeart.getAppContext().getResources().getDrawable(R.drawable.dots), null);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
		);
		params.setMargins(0, 10, 0, 10);
		comboboxButton.setLayoutParams(params);
		
		if (this.list != null)
		{
			if (this.list.startsWith("["))
			{
				JSONArray arrayList = new JSONArray(this.list);
				
				for (int x=0; x<arrayList.length(); x++)
				{
					JSONObject objectLine = arrayList.getJSONObject(x);

					listOptions.add(objectLine.getString("text"));
					listValues.add(objectLine.getString("value"));
					
				}
			}
			else
			{
				JSONObject props = new JSONObject(this.list);
				if (props != null)
				{
					if (!props.isNull("objectList"))
					{
						if (Variables.get(props.getString("objectList"))!=null)
						{
							JSONArray arrayList = new JSONArray(Variables.get(props.getString("objectList")).toString());
							
							for (int x=0; x<arrayList.length(); x++)
							{
								JSONObject objectLine = arrayList.getJSONObject(x);

								String index = Variables.parse_vars(props.getString("index"), false);
								String title = Variables.parse_vars(props.getString("title"), false);
								
								listOptions.add(objectLine.getString(title));
								listValues.add(objectLine.getString(index));
							}
						}
					}
				}
			}
		}
		
		
		
		comboboxButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{			
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Selecione uma opção")
		        	.setItems(get_list_options(), new DialogInterface.OnClickListener() {
		        		public void onClick(DialogInterface dialog, int which) {
		        			// The 'which' argument contains the index position
		        			// of the selected item
		        			set_option_selected(which);
		        		}
		        });
		        
		        
	            AlertDialog alert = builder.create();
	            alert.show();
			}
		});
		
		if (this.selected_value==null || this.selected_value.equals(""))
			comboboxButton.setText(empty_value);
		else
		{
			this.index = listValues.indexOf(this.selected_value);
			comboboxButton.setText(listOptions.get(this.index));
		}
		
		set_properties();
		this.layout.addView(comboboxButton);

	}
	
	private Context getActivity()
	{
		return this.parentActivityContext;
	}
	
	private CharSequence[] get_list_options()
	{
		return this.listOptions.toArray(new CharSequence[listOptions.size()]);
	}
	
	public String get_name_addressed()
	{
		return this.container + "__"+this.name;
	}
	
	public void set_option_selected(int index)
	{
		comboboxButton.setText(listOptions.get(index));
		selected_value = listValues.get(index);
		this.index = index;
		
		if (ev_on_change!=null && !this.ev_on_change.equals(""))
		{
			try 
			{
				Procedures mainProcedure = new Procedures();
				mainProcedure.initialize(this.ev_on_change, "{'sender':'"+get_name_addressed()+"'}");
				mainProcedure.execute();
			} 
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		set_properties();
	}
	
	public void set_properties()
	{	
		String pattern = this.get_name_addressed()+"__";
		Variables.add(pattern+"selectedIndex", "int", this.index);
		Variables.add(pattern+"selectedValue", "string", this.selected_value);
	}
	
}
