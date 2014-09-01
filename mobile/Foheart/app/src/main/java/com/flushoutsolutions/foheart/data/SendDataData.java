package com.flushoutsolutions.foheart.data;

/**
 * Created by Manuel on 10/08/2014.
 */
public class SendDataData {
    public int _id;
    public int fk_application;
    public int fk_user;
    public String table_name;
    public int row_id;
    public String record;
    public int sent;
    public String datetime;

    public SendDataData(int _id, int fk_application, int fk_user, String table_name, int row_id, String record, int sent, String datetime)
    {
        this._id = _id;
        this.fk_application = fk_application;
        this.fk_user = fk_user;
        this.table_name = table_name;
        this.row_id = row_id;
        this.record = record;
        this.sent = sent;
        this.datetime = datetime;
    }

    public SendDataData(int fk_application, int fk_user, String table_name, int row_id, String record, int sent, String datetime)
    {
        this.fk_application = fk_application;
        this.fk_user = fk_user;
        this.table_name = table_name;
        this.row_id = row_id;
        this.record = record;
        this.sent = sent;
        this.datetime = datetime;
    }
}
