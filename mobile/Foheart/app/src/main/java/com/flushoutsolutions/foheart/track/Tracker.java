package com.flushoutsolutions.foheart.track;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.logic.Procedures;

import org.json.JSONException;

/**
 * Created by Manuel on 09/08/2014.
 */
public class Tracker extends Service implements LocationListener {
    public static int theProvider = 0;

    String providerFine;
    String providerCoarse;
    String providerChosen;

    private final Context mContext;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    boolean canGetLocation = false;


    Location location; // location
    public static double latitude; // latitude
    public static double longitude; // longitude

    public static double new_latitude; // latitude
    public static double new_longitude; // longitude

    public static double speed; // speed
    public static double bearing; // bearing
    public static double accuracy; // accuracy

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 6000; // 1 sec

    // Declaring a Location Manager
    protected LocationManager locationManager;
    Location currentLocation;

    public Tracker(Context context)
    {
        this.mContext = context;
        getLocation();
    }

    public void setVariables()
    {
        if (Variables.get("__gps_lat") == null)
            Variables.add("__gps_lat", "float", Tracker.latitude);
        if (Variables.get("__gps_lon") == null)
            Variables.add("__gps_lon", "float", Tracker.longitude);
        if (Variables.get("__gps_speed") == null)
            Variables.add("__gps_speed", "float", Tracker.speed);
        if (Variables.get("__gps_accuracy") == null)
            Variables.add("__gps_accuracy", "int", Tracker.accuracy);
    }

    public void onGPSUpdate()
    {
        try
        {
            Procedures gpsProcedure = new Procedures();
            gpsProcedure.initialize("_onGPSUpdate", null);
            gpsProcedure.execute();
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
        }
    }

    public Location getLocation()
    {
        try
        {
            LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            if (Tracker.theProvider == 0)
            {
                Criteria criteria = new Criteria();
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(false);
                criteria.setPowerRequirement(Criteria.POWER_LOW);

                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                providerFine = manager.getBestProvider(criteria, true);

                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                providerCoarse = manager.getBestProvider(criteria, true);
            }
            else if (Tracker.theProvider == 1)
                providerFine = LocationManager.GPS_PROVIDER;
            else if (Tracker.theProvider == 2)
                providerCoarse = LocationManager.NETWORK_PROVIDER;



            if (providerCoarse != null) {
                providerChosen= providerCoarse;
                manager.requestLocationUpdates(providerCoarse,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            }
            if (providerFine != null) {
                providerChosen = providerFine;
                manager.requestLocationUpdates(providerFine,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            }

            if (null == providerChosen) providerChosen = LocationManager.GPS_PROVIDER;



            if (manager != null)
            {
                location = manager.getLastKnownLocation(providerChosen);

                if (location != null)
                {
                    Tracker.new_latitude = location.getLatitude();
                    Tracker.new_longitude = location.getLongitude();

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    speed = location.getSpeed();
                    bearing = location.getBearing();
                    accuracy = location.getAccuracy();
                    System.out.println("accuracy "+accuracy);


                    setVariables();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (isBetterLocation(location, currentLocation))
        {
            Tracker.new_latitude = location.getLatitude();
            Tracker.new_longitude = location.getLongitude();

            double meters = FoHeart.getDistanceInMeters(Tracker.latitude, Tracker.longitude, Tracker.new_latitude, Tracker.new_longitude);

            if (meters >=0)
            {
                Tracker.latitude = location.getLatitude();
                Tracker.longitude = location.getLongitude();
            }

            Tracker.speed = location.getSpeed();
            Tracker.bearing = location.getBearing();
            Tracker.accuracy = location.getAccuracy();

            setVariables();
            onGPSUpdate();

            SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
            if (settings.getInt("user_id", 0)>0 && Tracker.latitude!=0)
                BreadCrumbs.record();

            currentLocation= location;
        }
    }

    @Override
    public void onProviderDisabled(String provider)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    /*
    * Function to check if best network provider
    * @return boolean
    * */

    public boolean canGetLocation()
    {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * */

    public void showSettingsAlert()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog,int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


// GPS Accuracy fixes

    protected boolean isBetterLocation(Location location, Location currentBestLocation)
    {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());


        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2)
    {
        if (provider1 == null)
        {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
