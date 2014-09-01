package com.flushoutsolutions.foheart.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.flushoutsolutions.foheart.application.FoHeart;

/**
 * Created by Manuel on 09/08/2014.
 */
public class InternetStatus {

    public static boolean isOnline()
    {
        ConnectivityManager cm =
                (ConnectivityManager) FoHeart.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
