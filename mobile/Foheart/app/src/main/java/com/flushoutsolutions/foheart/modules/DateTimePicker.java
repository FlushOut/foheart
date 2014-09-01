package com.flushoutsolutions.foheart.modules;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.flushoutsolutions.foheart.R;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.font.Font;
import com.flushoutsolutions.foheart.globals.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by daigomatsuoka on 09/05/14.
 */
public class DateTimePicker
{
    private Context parentActivityContext;
    private FragmentManager fragmentManager;
    private String name;
    private String container;
    private String hint="";
    private int mode = 0;
    private int day = -1;
    private int month = -1;
    private int year = -1;
    private int hour = -1;
    private int minute = -1;

    private Button dateTimePickerButton;
    private ViewGroup layout;

    private String ev_on_change = null;

    public DateTimePicker(String name, String container, ViewGroup layout, FragmentManager fragmentManager)
    {
        this.name = name;
        this.layout = layout;
        this.container = container;
        this.fragmentManager = fragmentManager;
    }

    public DateTimePicker(String name, String container, ViewGroup layout, FragmentManager fragmentManager, String properties, String events) throws JSONException
    {
        this.name = name;
        this.layout = layout;
        this.container = container;
        this.fragmentManager = fragmentManager;

        JSONObject jsonProperties = new JSONObject(properties);

        if (!jsonProperties.isNull("mode"))
            this.mode = jsonProperties.getInt("mode");

        if (!jsonProperties.isNull("hint"))
            this.hint = jsonProperties.getString("hint");

        if (!jsonProperties.isNull("day"))
            this.day = jsonProperties.getInt("day");

        if (!jsonProperties.isNull("month"))
            this.month = jsonProperties.getInt("month");

        if (!jsonProperties.isNull("year"))
            this.year = jsonProperties.getInt("year");

        if (!jsonProperties.isNull("hour"))
            this.hour = jsonProperties.getInt("hour");

        if (!jsonProperties.isNull("minute"))
            this.minute = jsonProperties.getInt("minute");

        JSONObject jsonEvents = new JSONObject(events);

        if (!jsonEvents.isNull("onChange"))
            this.ev_on_change = jsonEvents.getString("onChange");
    }

    public void set_parent_activity_context(Context parentActivityContext)
    {
        this.parentActivityContext = parentActivityContext;
    }

    private String zeroInFront(int number)
    {
        if (number<10) return "0"+number;
        else  return ""+number;

    }

    public void render() throws JSONException
    {
        dateTimePickerButton = new Button(FoHeart.getAppContext());
        dateTimePickerButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        dateTimePickerButton.setTypeface(Font.get_font("medium"));
        dateTimePickerButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        dateTimePickerButton.setGravity(Gravity.LEFT);
        dateTimePickerButton.setGravity(Gravity.CENTER_VERTICAL);

        switch (this.mode)
        {
            case 0:
                dateTimePickerButton.setCompoundDrawablesWithIntrinsicBounds(null, null, FoHeart.getAppContext().getResources().getDrawable(R.drawable.picker_date), null);
                break;
            case 1:
                dateTimePickerButton.setCompoundDrawablesWithIntrinsicBounds(null, null, FoHeart.getAppContext().getResources().getDrawable(R.drawable.picker_time), null);
                break;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 10, 0, 10);
        dateTimePickerButton.setLayoutParams(params);


        final int modo = this.mode;
        final DateTimePicker parent = this;

        dateTimePickerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                switch (modo)
                {
                    case 0:
                        DatePickerFragment newFragmentD = new DatePickerFragment();
                        newFragmentD.setDate(day,month,year);
                        newFragmentD.setParent(parent);
                        newFragmentD.show(fragmentManager, "datePicker");
                        break;
                    case 1:
                        TimePickerFragment newFragmentT = new TimePickerFragment();
                        newFragmentT.setTime(hour,minute);
                        newFragmentT.setParent(parent);
                        newFragmentT.show(fragmentManager, "timePicker");
                        break;
                }

            }
        });
        setText();

        set_properties();
        this.layout.addView(dateTimePickerButton);

    }

    public void setTime(int h, int m)
    {
        this.hour = h;
        this.minute = m;
        setText();
    }

    public void setDate(int d, int m, int y)
    {
        this.day = d;
        this.month = m;
        this.year = y;
        setText();
    }

    public void setText()
    {
        String text = this.hint;

        switch (this.mode)
        {
            case 0:
                if (year>-1 && month>-1 && day>-1)
                    text = zeroInFront(day)+"/"+zeroInFront(month)+"/"+year;
                break;
            case 1:
                if (hour>-1 && minute>-1)
                    text = zeroInFront(hour)+":"+zeroInFront(minute);
                break;
        }

        dateTimePickerButton.setText(text);
        set_properties();
    }


    private Context getActivity()
    {
        return this.parentActivityContext;
    }

    public String get_name_addressed()
    {
        return this.container + "__"+this.name;
    }

    public void set_properties()
    {
        String pattern = this.get_name_addressed()+"__";
        Variables.add(pattern + "mode", "int", this.mode);
        Variables.add(pattern+"hint", "string", this.hint);
        Variables.add(pattern+"hour", "int", zeroInFront(this.hour));
        Variables.add(pattern+"minute", "int", zeroInFront(this.minute));
        Variables.add(pattern+"day", "int", zeroInFront(this.day));
        Variables.add(pattern+"month", "int", zeroInFront(this.month));
        Variables.add(pattern+"year", "int", this.year);
    }

    public void set_property (String p, String v)
    {
        if (p.toLowerCase().equals("hint"))
        {
            this.hint = v;
        }
        else if (p.toLowerCase().equals("hour"))
        {
            this.hour = Integer.valueOf(v);
        }
        else if (p.toLowerCase().equals("minute"))
        {
            this.minute = Integer.valueOf(v);
        }
        else if (p.toLowerCase().equals("day"))
        {
            this.day = Integer.valueOf(v);
        }
        else if (p.toLowerCase().equals("month"))
        {
            this.month = Integer.valueOf(v);
        }
        else if (p.toLowerCase().equals("year"))
        {
            this.year = Integer.valueOf(v);
        }

        this.set_properties();
        setText();
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        int hour;
        int minute;
        DateTimePicker parent;

        public void setParent(DateTimePicker parent)
        {
            this.parent = parent;
        }

        public void setTime(int hour, int minute)
        {
            this.hour = hour;
            this.minute = minute;
        }


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();

            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            if (this.hour >-1 && this.minute >-1)
            {
                hour = this.hour;
                minute = this.minute;
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));


        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            parent.setTime(hourOfDay, minute);
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        int day;
        int month;
        int year;
        DateTimePicker parent;

        public void setParent(DateTimePicker parent)
        {
            this.parent = parent;
        }

        public void setDate(int day, int month, int year)
        {
            this.day = day;
            this.month = month;
            this.year = year;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            if (this.day >-1 && this.month>-1 && this.year>-1)
            {
                day = this.day;
                month = this.month-1;
                year= this.year;
            }

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day)
        {
            // Do something with the date chosen by the user
            parent.setDate(day, month+1, year);
        }
    }
}
