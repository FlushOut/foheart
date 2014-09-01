package com.flushoutsolutions.foheart.data;

/**
 * Created by Manuel on 09/08/2014.
 */
public class TableFieldData {

    public int _id;
    public int fk_table;
    public String name;
    public String type;
    public int size;
    public String def;
    public int required;
    public int auto_increment;
    public int primary_key;

    public TableFieldData(int _id, int fk_table, String name, String type, int size, String def, int required, int auto_increment, int primary_key)
    {
        this._id = _id;
        this.fk_table = fk_table;
        this.name = name;
        this.type = type;
        this.size = size;
        this.def = def;
        this.required = required;
        this.auto_increment = auto_increment;
        this.primary_key = primary_key;
    }

    public TableFieldData(int fk_table, String name, String type, int size, String def, int required, int auto_increment, int primary_key)
    {
        this.fk_table = fk_table;
        this.name = name;
        this.type = type;
        this.size = size;
        this.def = def;
        this.required = required;
        this.auto_increment = auto_increment;
        this.primary_key = primary_key;
    }
}
