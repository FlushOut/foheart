package com.flushoutsolutions.foheart.data;

/**
 * Created by John on 10/11/2014.
 */
public class TableMastersData {

    public int _id;
    public int fk_table;
    public int version_server;

    public TableMastersData(int _id, int fk_table, int version_server)
    {
        this._id = _id;
        this.fk_table = fk_table;
        this.version_server = version_server;
    }

    public TableMastersData(int fk_table, int version_server)
    {
        this.fk_table = fk_table;
        this.version_server = version_server;
    }
}
