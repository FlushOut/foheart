package com.flushoutsolutions.foheart.application;

import android.app.Application;
import android.content.Context;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Manuel on 09/08/2014.
 */
public class FoHeart extends Application {

    private static Context context;

    public void onCreate()
    {
        super.onCreate();
        FoHeart.context = getApplicationContext();
    }

    public static Context getAppContext()
    {
        return FoHeart.context;
    }

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static double getDistanceInMeters(double lat1, double lon1, double lat2, double lon2)
    {
        double raioDaTerraEmKm = 6371; // km
        double dLat = (lat2-lat1)*  Math.PI / 180;
        double dLon = (lon2-lon1)* Math.PI / 180;
        double l1 = lat1 * Math.PI / 180;
        double l2 = lat2 * Math.PI / 180;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(l1) * Math.cos(l2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = raioDaTerraEmKm * c;

        return d*1000;
    }

    public static String combine(String[] s, String glue)
    {
        int k=s.length;
        if (k==0)
            return null;

        StringBuilder out=new StringBuilder();
        out.append(s[0]);

        for (int x=1;x<k;++x)
            out.append(glue).append(s[x]);

        return out.toString();
    }

    public static String implode(Object[] ary, String delim)
    {
        String out = "";
        for(int i=0; i<ary.length; i++) {
            if(i!=0) { out += delim; }
            out += ary[i].toString();
        }
        return out;
    }

    public static String dateTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("gmt"));
        String currentDateTime = sdf.format(new Date());

        return currentDateTime;
    }
}
