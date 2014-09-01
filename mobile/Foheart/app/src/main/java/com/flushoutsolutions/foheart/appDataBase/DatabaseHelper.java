package com.flushoutsolutions.foheart.appDataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Manuel on 09/08/2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "Foheart.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getHelper(Context context)
    {
        if (instance == null)
            instance = new DatabaseHelper(context);

        return instance;
    }


    private static final String SQL_CREATE_ENTRIES_APPLICATION =
            "CREATE TABLE " + DatabaseContract.ApplicationSchema.TABLE_NAME + " (" +
                    DatabaseContract.ApplicationSchema._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_CODE + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_DESC + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_APP_VERSION + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_BASE_VERSION + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_USER + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_PASS + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_HOST + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_DB_PORT + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_UPDATE_INTERVAL + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ApplicationSchema.COLUMN_NAME_DEBUG_MODE + INT_TYPE +
                    " )";

    private static final String SQL_CREATE_ENTRIES_STRUCTURE =
            "CREATE TABLE " + DatabaseContract.StructureSchema.TABLE_NAME + " (" +
                    DatabaseContract.StructureSchema._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.StructureSchema.COLUMN_NAME_FK_APPLICATION + INT_TYPE + COMMA_SEP +
                    DatabaseContract.StructureSchema.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.StructureSchema.COLUMN_NAME_PARENT + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.StructureSchema.COLUMN_NAME_PRESENTATION + INT_TYPE +
                    " )";

    private static final String SQL_CREATE_ENTRIES_VIEW =
            "CREATE TABLE " + DatabaseContract.ViewSchema.TABLE_NAME + " (" +
                    DatabaseContract.ViewSchema._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.ViewSchema.COLUMN_NAME_FK_APPLICATION + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewSchema.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewSchema.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewSchema.COLUMN_NAME_LAYOUT + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewSchema.COLUMN_NAME_BUTTON_TITLE + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewSchema.COLUMN_NAME_BUTTON_ACTION + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewSchema.COLUMN_NAME_BACK_LOCKED + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewSchema.COLUMN_NAME_EVENTS+ TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_ENTRIES_VIEW_MODULE =
            "CREATE TABLE " + DatabaseContract.ViewModuleSchema.TABLE_NAME + " (" +
                    DatabaseContract.ViewModuleSchema._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.ViewModuleSchema.COLUMN_NAME_FK_VIEW + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewModuleSchema.COLUMN_NAME_MODULE + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewModuleSchema.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewModuleSchema.COLUMN_NAME_PROPERTIES + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ViewModuleSchema.COLUMN_NAME_EVENTS+ TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_ENTRIES_PROCEDURE =
            "CREATE TABLE " + DatabaseContract.ProcedureSchema.TABLE_NAME + " (" +
                    DatabaseContract.ProcedureSchema._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.ProcedureSchema.COLUMN_NAME_FK_APPLICATION + INT_TYPE + COMMA_SEP +
                    DatabaseContract.ProcedureSchema.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ProcedureSchema.COLUMN_NAME_PARAMETERS + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ProcedureSchema.COLUMN_NAME_CODE+ TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.ProcedureSchema.COLUMN_NAME_RETURN+ TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_ENTRIES_TABLE =
            "CREATE TABLE " + DatabaseContract.TableSchema.TABLE_NAME + " (" +
                    DatabaseContract.TableSchema._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.TableSchema.COLUMN_NAME_FK_APPLICATION + INT_TYPE + COMMA_SEP +
                    DatabaseContract.TableSchema.COLUMN_NAME_MODEL_VERSION + INT_TYPE + COMMA_SEP +
                    DatabaseContract.TableSchema.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.TableSchema.COLUMN_NAME_KEY + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.TableSchema.COLUMN_NAME_AUTO_SYNC + INT_TYPE +
                    " )";

    private static final String SQL_CREATE_ENTRIES_TABLE_FIELD =
            "CREATE TABLE " + DatabaseContract.TableFieldSchema.TABLE_NAME + " (" +
                    DatabaseContract.TableFieldSchema._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.TableFieldSchema.COLUMN_NAME_FK_TABLE + INT_TYPE + COMMA_SEP +
                    DatabaseContract.TableFieldSchema.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.TableFieldSchema.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.TableFieldSchema.COLUMN_NAME_SIZE + INT_TYPE + COMMA_SEP +
                    DatabaseContract.TableFieldSchema.COLUMN_NAME_DEFAULT + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.TableFieldSchema.COLUMN_NAME_REQUIRED + INT_TYPE + COMMA_SEP +
                    DatabaseContract.TableFieldSchema.COLUMN_NAME_AUTO_INCREMENT + INT_TYPE + COMMA_SEP +
                    DatabaseContract.TableFieldSchema.COLUMN_NAME_PRIMARY_KEY + INT_TYPE +
                    " )";

    private static final String SQL_CREATE_ENTRIES_SEND_DATA =
            "CREATE TABLE " + DatabaseContract.SendDataSchema.TABLE_NAME + " (" +
                    DatabaseContract.SendDataSchema._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.SendDataSchema.COLUMN_NAME_FK_APPLICATION + INT_TYPE + COMMA_SEP +
                    DatabaseContract.SendDataSchema.COLUMN_NAME_FK_USER + INT_TYPE + COMMA_SEP +
                    DatabaseContract.SendDataSchema.COLUMN_NAME_TABLE_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.SendDataSchema.COLUMN_NAME_ROW_ID + INT_TYPE + COMMA_SEP +
                    DatabaseContract.SendDataSchema.COLUMN_NAME_RECORD + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.SendDataSchema.COLUMN_NAME_SENT + INT_TYPE + COMMA_SEP +
                    DatabaseContract.SendDataSchema.COLUMN_NAME_DATETIME + TEXT_TYPE +
                    " )";



    private static final String SQL_CREATE_ENTRIES_LOCATION =
            "CREATE TABLE " + DatabaseContract.LocationSchema.TABLE_NAME + " (" +
                    DatabaseContract.LocationSchema._ID + " "+INT_TYPE+" PRIMARY KEY," +
                    DatabaseContract.LocationSchema.COLUMN_NAME_LAT + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.LocationSchema.COLUMN_NAME_LON + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.LocationSchema.COLUMN_NAME_SPEED + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.LocationSchema.COLUMN_NAME_ACCURACY + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.LocationSchema.COLUMN_NAME_BATTERY_LEVEL + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.LocationSchema.COLUMN_NAME_BEARING + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.LocationSchema.COLUMN_NAME_CARRIER + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.LocationSchema.COLUMN_NAME_GSM_STRENGTH + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.LocationSchema.COLUMN_NAME_PHONE_NUMBER + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.LocationSchema.COLUMN_NAME_DATETIME + TEXT_TYPE +
                    " )";


    private static final String SQL_DELETE_ENTRIES_APPLICATION = "DROP TABLE IF EXISTS " + DatabaseContract.ApplicationSchema.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_STRUCTURE = "DROP TABLE IF EXISTS " + DatabaseContract.StructureSchema.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_VIEW = "DROP TABLE IF EXISTS " + DatabaseContract.ViewSchema.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_VIEW_MODULE = "DROP TABLE IF EXISTS " + DatabaseContract.ViewModuleSchema.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_PROCEDURE = "DROP TABLE IF EXISTS " + DatabaseContract.ProcedureSchema.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_TABLE = "DROP TABLE IF EXISTS " + DatabaseContract.TableSchema.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_TABLE_FIELD = "DROP TABLE IF EXISTS " + DatabaseContract.TableFieldSchema.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_SEND_DATA = "DROP TABLE IF EXISTS " + DatabaseContract.SendDataSchema.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_LOCATION = "DROP TABLE IF EXISTS " + DatabaseContract.LocationSchema.TABLE_NAME;

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_ENTRIES_APPLICATION);
        db.execSQL(SQL_CREATE_ENTRIES_STRUCTURE);
        db.execSQL(SQL_CREATE_ENTRIES_VIEW);
        db.execSQL(SQL_CREATE_ENTRIES_VIEW_MODULE);
        db.execSQL(SQL_CREATE_ENTRIES_PROCEDURE);
        db.execSQL(SQL_CREATE_ENTRIES_TABLE);
        db.execSQL(SQL_CREATE_ENTRIES_TABLE_FIELD);
        db.execSQL(SQL_CREATE_ENTRIES_SEND_DATA);
        db.execSQL(SQL_CREATE_ENTRIES_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_ENTRIES_APPLICATION);
        db.execSQL(SQL_DELETE_ENTRIES_STRUCTURE);
        db.execSQL(SQL_DELETE_ENTRIES_VIEW);
        db.execSQL(SQL_DELETE_ENTRIES_VIEW_MODULE);
        db.execSQL(SQL_DELETE_ENTRIES_PROCEDURE);
        db.execSQL(SQL_DELETE_ENTRIES_TABLE);
        db.execSQL(SQL_DELETE_ENTRIES_TABLE_FIELD);
        db.execSQL(SQL_DELETE_ENTRIES_SEND_DATA);
        db.execSQL(SQL_DELETE_ENTRIES_LOCATION);
        onCreate(db);
    }
}
