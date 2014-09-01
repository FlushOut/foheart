package com.flushoutsolutions.foheart.track;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.data.LocationData;
import com.flushoutsolutions.foheart.models.LocationModel;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Manuel on 09/08/2014.
 */
public class BreadCrumbs {
    private static String signal;

    private BreadCrumbs()
    {

    }

    public static void record()
    {
        // Carrier information
        TelephonyManager manager = (TelephonyManager) FoHeart.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();

        // Battery info
        Intent batteryIntent = FoHeart.getAppContext().getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int rawlevel = batteryIntent.getIntExtra("level", -1);
        double scale = batteryIntent.getIntExtra("scale", -1);
        double level = -1;
        if (rawlevel >= 0 && scale > 0)
            level = rawlevel / scale;

        // Signal strength
        PhoneListener phoneListener = new PhoneListener();
        manager.listen(phoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


        // get now's date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDateTime = sdf.format(new Date());

        SimpleDateFormat sdf_tz = new SimpleDateFormat("Z");
        String tzone = sdf_tz.format(new Date());

        // insert into database
        LocationData locationData = new LocationData(String.valueOf(Tracker.latitude),String.valueOf(Tracker.longitude), String.valueOf(Tracker.speed), "",String.valueOf(Tracker.bearing),String.valueOf(Tracker.accuracy), String.valueOf(level), BreadCrumbs.signal, carrierName, currentDateTime);
        LocationModel.get_model().save(locationData);
    }

    private static class PhoneListener extends PhoneStateListener
    {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            BreadCrumbs.signal = String.valueOf(signalStrength.getGsmSignalStrength());
            if (BreadCrumbs.signal.trim()=="") BreadCrumbs.signal = "-1";
        }

    }
}
