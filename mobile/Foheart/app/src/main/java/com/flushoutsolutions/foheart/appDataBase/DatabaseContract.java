package com.flushoutsolutions.foheart.appDataBase;

import android.provider.BaseColumns;

/**
 * Created by Manuel on 09/08/2014.
 */
public class DatabaseContract {

    private DatabaseContract() {}

    public static abstract class ApplicationSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "application";
        public static final String COLUMN_NAME_APP_CODE = "code";
        public static final String COLUMN_NAME_APP_DESC = "description";
        public static final String COLUMN_NAME_APP_VERSION = "app_version";
        public static final String COLUMN_NAME_BASE_VERSION = "base_version";

        public static final String COLUMN_NAME_DB_USER = "db_user";
        public static final String COLUMN_NAME_DB_PASS = "db_pass";

        public static final String COLUMN_NAME_UPDATE_INTERVAL = "update_interval";
        public static final String COLUMN_NAME_DEBUG_MODE = "debug_mode";
        public static final String COLUMN_NAME_SYNC_MASTER = "syncMaster";
        public static final String COLUMN_NAME_SYNC_TRANSACTION = "syncTransaction";


    }

    public static abstract class StructureSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "structure";
        public static final String COLUMN_NAME_FK_APPLICATION = "fk_application";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_PARENT = "parent";
        public static final String COLUMN_NAME_PRESENTATION = "presentation";
    }

    public static abstract class ViewSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "view";
        public static final String COLUMN_NAME_FK_APPLICATION = "fk_application";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LAYOUT = "layout";
        public static final String COLUMN_NAME_BUTTON_TITLE = "button_title";
        public static final String COLUMN_NAME_BUTTON_ACTION = "button_action";
        public static final String COLUMN_NAME_BACK_LOCKED = "back_locked";
        public static final String COLUMN_NAME_EVENTS = "events";
    }

    public static abstract class ViewModuleSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "view_module";
        public static final String COLUMN_NAME_FK_VIEW = "fk_view";
        public static final String COLUMN_NAME_MODULE = "module";
        public static final String COLUMN_NAME_NAME= "name";
        public static final String COLUMN_NAME_PROPERTIES = "properties";
        public static final String COLUMN_NAME_EVENTS = "events";
    }

    public static abstract class ProcedureSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "procedure";
        public static final String COLUMN_NAME_FK_APPLICATION = "fk_application";
        public static final String COLUMN_NAME_NAME= "name";
        public static final String COLUMN_NAME_PARAMETERS = "parameters";
        public static final String COLUMN_NAME_VARIABLES = "variables";
        public static final String COLUMN_NAME_CODE = "code";
        public static final String COLUMN_NAME_RETURN = "return";
    }

    public static abstract class TableSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "tables";
        public static final String COLUMN_NAME_FK_APPLICATION = "fk_application";
        public static final String COLUMN_NAME_MODEL_VERSION = "model_version";
        public static final String COLUMN_NAME_NAME= "name";
        public static final String COLUMN_NAME_AUTO_SYNC = "auto_sync";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_REQUESTPARAMS = "requestParams";
    }

    public static abstract class TableMasterSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "table_masters";
        public static final String COLUMN_NAME_FK_TABLE = "fk_table";
        public static final String COLUMN_NAME_VERSION_SERVER = "version__server";
    }

    public static abstract class TableTransactionSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "table_transactions";
        public static final String COLUMN_NAME_FK_TABLE = "fk_table";
        public static final String COLUMN_NAME_VERSION_LOCAL = "version_local";
        public static final String COLUMN_NAME_VERSION_SERVER = "version__server";
        public static final String COLUMN_NAME_FK_REQUEST = "fk_request";
        public static final String COLUMN_NAME_FK_RESPONSE = "fk_response";
    }

    public static abstract class TableFieldSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "table_field";
        public static final String COLUMN_NAME_FK_TABLE = "fk_table";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_TYPE= "type";
        public static final String COLUMN_NAME_SIZE= "size";
        public static final String COLUMN_NAME_DEFAULT= "def";
        public static final String COLUMN_NAME_REQUIRED = "required";
        public static final String COLUMN_NAME_AUTO_INCREMENT = "auto_increment";
        public static final String COLUMN_NAME_PRIMARY_KEY= "primary_key";
    }

    public static abstract class SendDataSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "send_data";
        public static final String COLUMN_NAME_FK_APPLICATION = "fk_application";
        public static final String COLUMN_NAME_FK_USER= "fk_user";
        public static final String COLUMN_NAME_TABLE_NAME = "table_name";
        public static final String COLUMN_NAME_ROW_ID = "row_id";
        public static final String COLUMN_NAME_RECORD = "record";
        public static final String COLUMN_NAME_SENT = "sent";
        public static final String COLUMN_NAME_DATETIME = "datetime";
    }

    public static abstract class LocationSchema implements BaseColumns
    {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LON = "lon";
        public static final String COLUMN_NAME_SPEED = "speed";
        public static final String COLUMN_NAME_PHONE_NUMBER= "phonenumber";
        public static final String COLUMN_NAME_BEARING= "bearing";
        public static final String COLUMN_NAME_ACCURACY= "accuracy";
        public static final String COLUMN_NAME_BATTERY_LEVEL = "battery_level";
        public static final String COLUMN_NAME_GSM_STRENGTH= "gsmstrength";
        public static final String COLUMN_NAME_CARRIER= "carrier";
        public static final String COLUMN_NAME_DATETIME = "datetime";
    }
}
