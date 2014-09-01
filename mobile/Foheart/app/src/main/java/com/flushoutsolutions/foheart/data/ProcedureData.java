package com.flushoutsolutions.foheart.data;

/**
 * Created by Manuel on 09/08/2014.
 */
public class ProcedureData {

    public int _id;
    public int fk_application;
    public String name;
    public String parameters;
    public String code;
    public String retur;

    public ProcedureData(int _id, int fk_application, String name, String parameters, String code, String retur)
    {
        this._id = _id;
        this.fk_application = fk_application;
        this.name = name;
        this.parameters= parameters;
        this.code = code;
        this.retur = retur;
    }

    public ProcedureData(int fk_application, String name, String parameters, String code, String retur)
    {
        this.fk_application = fk_application;
        this.name = name;
        this.parameters= parameters;
        this.code = code;
        this.retur = retur;
    }
}
