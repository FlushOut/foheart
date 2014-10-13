package com.flushoutsolutions.foheart.data;

/**
 * Created by John on 10/11/2014.
 */
public class TableTransactionsData {

    public int _id;
    public int fk_table;
    public int version_local;
    public int version_server;
    public int fk_request;
    public int fk_response;

    public TableTransactionsData(int _id, int fk_table, int version_local, int version_server, int fk_request, int fk_response)
    {
        this._id = _id;
        this.fk_table = fk_table;
        this.version_local = version_local;
        this.version_server = version_server;
        this.fk_request = fk_request;
        this.fk_response = fk_response;
    }

    public TableTransactionsData(int fk_table, int version_local, int version_server, int fk_request, int fk_response)
    {
        this.fk_table = fk_table;
        this.version_local = version_local;
        this.version_server = version_server;
        this.fk_request = fk_request;
        this.fk_response = fk_response;
    }
}
