package com.flushoutsolutions.foheart.modules;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.track.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by daigomatsuoka on 20/05/14.
 */
public class Map extends SupportMapFragment implements GoogleMap.OnMapLongClickListener
{
    public String container;
    public String name;

    public int height;
    public boolean allowUserInteraction = true;
    public boolean makeDirections = false;
    public LatLng centerAt;
    public boolean setCenterAt = false;
    public int zoom = 16;
    public JSONArray points;

    int heightFull;
    int widthFull;

    public ViewGroup layout;

    private GoogleMap map;

    public Map() {
        super();

    }

    public static Map newInstance(String view, String name, ViewGroup layout, String properties)
    {
        Map frag = new Map();

        frag.container = view;
        frag.name = name;
        frag.layout = layout;

        JSONObject jsonProperties = null;

        try
        {
            jsonProperties = new JSONObject(properties);

            if (!jsonProperties.isNull("height"))
                frag.height = jsonProperties.getInt("height");

            if (!jsonProperties.isNull("allowUserInteraction"))
                frag.allowUserInteraction = jsonProperties.getBoolean("allowUserInteraction");

            if (!jsonProperties.isNull("makeDirections"))
                frag.makeDirections = jsonProperties.getBoolean("makeDirections");

            if (!jsonProperties.isNull("centerAt"))
            {
                JSONObject jsonCenterAt = jsonProperties.getJSONObject("centerAt");
                double lat = jsonCenterAt.getDouble("latitude");
                double lon = jsonCenterAt.getDouble("longitude");

                frag.centerAt = new LatLng(lat,lon);
                frag.setCenterAt = true;
            }

            if (!jsonProperties.isNull("zoom"))
                frag.zoom = jsonProperties.getInt("zoom");

            if (!jsonProperties.isNull("points"))
                frag.points = jsonProperties.getJSONArray("points");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2)
    {
        View v = super.onCreateView(arg0, arg1, arg2);

        WindowManager wm = (WindowManager) FoHeart.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int heightActionbar  = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 110, FoHeart.getAppContext().getResources().getDisplayMetrics());

        heightFull = display.getHeight()-heightActionbar;
        widthFull = display.getWidth();

        int layoutH  = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) height, FoHeart.getAppContext().getResources().getDisplayMetrics());
        if (height>0)
            v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, layoutH));
        else
            v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, heightFull));



        initMap();
        return v;
    }

    private void initMap()
    {
        UiSettings settings = getMap().getUiSettings();
        settings.setAllGesturesEnabled(allowUserInteraction);
        settings.setMyLocationButtonEnabled(false);

        map = getMap();


        map.setMyLocationEnabled(true);

        LatLngBounds.Builder bounds;
        bounds = new LatLngBounds.Builder();

        PolylineOptions polyLine = new PolylineOptions().geodesic(true);

        for (int c = 0; c< this.points.length(); c++)
        {
            try
            {
                JSONObject object = this.points.getJSONObject(c);

                double lat = object.getDouble("latitude");
                double lon = object.getDouble("longitude");

                LatLng pos = new LatLng(lat,lon);

                map.addMarker(new MarkerOptions().title(object.getString("labelTitle")).snippet(object.getString("labelDescription")).position(pos));
                polyLine.add(pos);

                bounds.include(pos);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        if (this.makeDirections) map.addPolyline(polyLine);

        if (!setCenterAt)
        {
            if (this.points.length() > 0) {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), widthFull, heightFull, 30));
                System.out.println("center bounds");
            }
            else
            {
                System.out.println("center center");
                centerAt = new LatLng(Tracker.latitude, Tracker.longitude);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(centerAt, zoom));
            }
        }
        else {
            System.out.println("center centerAt "+centerAt);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(centerAt, zoom));
        }

        map.setOnMapLongClickListener(this);
    }


    public String get_name_addressed()
    {
        return this.container + "__"+this.name;
    }


    public void set_properties()
    {
        String pattern = this.get_name_addressed()+"__";
        Variables.add(pattern + "points", "string", this.points);
    }


    public void set_property (String p, String v)
    {
     if (p.toLowerCase().equals("points"))
        {
            try
            {
                this.points = new JSONArray(v);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        this.set_properties();
        initMap();
    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        LatLng pointStart = new LatLng(Tracker.latitude, Tracker.longitude);
        LatLng pointEnd = new LatLng(Tracker.latitude, Tracker.longitude);

        if (this.points.length()>0)
        {
            try
            {
                JSONObject jsonStart = this.points.getJSONObject(0);
                pointStart = new LatLng(jsonStart.getDouble("latitude"),jsonStart.getDouble("longitude"));

                JSONObject jsonEnd = this.points.getJSONObject(this.points.length()-1);
                pointEnd = new LatLng(jsonEnd.getDouble("latitude"),jsonStart.getDouble("longitude"));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }


        final LatLng fPointStart = pointStart;
        final LatLng fPointEnd = pointEnd;

        String[] listChoice = new String[2];
        listChoice[0] = "Google Maps";
        listChoice[1] = "Waze";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Abrir com...")
                .setItems(listChoice, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which)
                        {
                            case 0:
                                String uri = String.format(Locale.getDefault(), "geo:%f,%f", fPointStart.latitude, fPointStart.longitude);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                startActivity(intent);
                                break;
                            case 1:
                                try
                                {
                                    String url = "waze://?ll="+fPointStart.latitude+","+fPointStart.longitude;
                                    Intent intentWaze = new Intent( Intent.ACTION_VIEW, Uri.parse(url) );
                                    startActivity( intentWaze );
                                }
                                catch ( ActivityNotFoundException ex  )
                                {
                                    Intent intentWaze = new Intent( Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze") );
                                    startActivity(intentWaze);
                                }
                                break;
                        }
                    }
                });


        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
