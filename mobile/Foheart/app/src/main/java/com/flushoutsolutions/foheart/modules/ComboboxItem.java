package com.flushoutsolutions.foheart.modules;

public class ComboboxItem 
{
	private String value;
	private String text;
	
	public ComboboxItem(String value, String text)
	{
		this.value = value;
		this.text = text;
	}
	
	public void set_value(String value)
	{
		this.value = value;
	}
	
	public String get_value()
	{
		return this.value;
	}
	
	public void set_text(String text)
	{
		this.text = text;
	}
	
	public String get_text()
	{
		return this.text;
	}
}
