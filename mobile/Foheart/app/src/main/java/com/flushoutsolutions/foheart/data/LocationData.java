package com.flushoutsolutions.foheart.data;

/**
 * Created by Manuel on 09/08/2014.
 */
public class LocationData {

    public int _id;
    public String lat;
    public String lon;
    public String speed;
    public String phone_number;
    public String bearing;
    public String accuracy;
    public String battery_level;
    public String gms_strength;
    public String carrier;
    public String date_time;

    public LocationData(int _id, String lat, String lon, String speed, String phone_number, String bearing, String accuracy, String battery_level, String gms_strength, String carrier, String date_time)
    {
        this._id = _id;
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.phone_number = phone_number;
        this.bearing = bearing;
        this.accuracy = accuracy;
        this.battery_level = battery_level;
        this.gms_strength = gms_strength;
        this.carrier = carrier;
        this.date_time = date_time;
    }

    public LocationData(String lat, String lon, String speed, String phone_number, String bearing, String accuracy, String battery_level, String gms_strength, String carrier, String date_time)
    {
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.phone_number = phone_number;
        this.bearing = bearing;
        this.accuracy = accuracy;
        this.battery_level = battery_level;
        this.gms_strength = gms_strength;
        this.carrier = carrier;
        this.date_time = date_time;
    }
}
