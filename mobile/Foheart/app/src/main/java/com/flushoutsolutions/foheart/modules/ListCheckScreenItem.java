
package com.flushoutsolutions.foheart.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flushoutsolutions.foheart.R;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.design.Color;
import com.flushoutsolutions.foheart.font.Font;

import java.io.File;
import java.io.IOException;

public class ListCheckScreenItem extends RelativeLayout
{
	private boolean state;
	private ImageView imgDisclosure;
	public ListCheckScreen parent;
	private int index;
	public String value;
	private String title;
	private String subtitle;

	public ListCheckScreenItem(Context context, ListCheckScreen parent, int index, boolean state, int style, String value, String title, String subtitle, String icon, boolean checked) throws IOException
	{
		super(context);

        System.out.println("debug: ListCheckScreenItem");

		this.state = state;
		this.parent = parent;
		this.index = index;
		this.value = value;

		int pad10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10, FoHeart.getAppContext().getResources().getDisplayMetrics());
		
		// Icon
		RelativeLayout.LayoutParams iconLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		iconLayout.addRule(RelativeLayout.CENTER_VERTICAL);
		iconLayout.setMargins(0, 0, pad10, 0);

		int ico24 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 24, FoHeart.getAppContext().getResources().getDisplayMetrics());

		SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);
		Bitmap bitmapIcon = null;
		if (!icon.equals(""))
		{
			File imgFile = new File(FoHeart.getAppContext().getApplicationInfo().dataDir+"/app"+settings.getString("idApplication", "")+"/app/"+icon);
			Bitmap bmpIcon = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			bitmapIcon = Bitmap.createScaledBitmap(bmpIcon, ico24, ico24, true);
		}
		ImageView imgIcon = new ImageView(context);
		if (bitmapIcon!=null)
		{
			imgIcon.setImageBitmap(bitmapIcon);
		}
		imgIcon.setId(9993);
		imgIcon.setLayoutParams(iconLayout);
		this.addView(imgIcon);

		int icoW = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 18, FoHeart.getAppContext().getResources().getDisplayMetrics());
		int icoH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 22, FoHeart.getAppContext().getResources().getDisplayMetrics());

		// Checkbox
		RelativeLayout.LayoutParams disclosureLayout = new RelativeLayout.LayoutParams(icoW, icoH);
		disclosureLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		disclosureLayout.addRule(RelativeLayout.CENTER_VERTICAL);
		disclosureLayout.setMargins(pad10, 0, 0, 0);

		imgDisclosure = new ImageView(context);
		imgDisclosure.setId(9994);
		imgDisclosure.setLayoutParams(disclosureLayout);

		if (state)
			imgDisclosure.setImageDrawable(context.getResources().getDrawable(R.drawable.checkbox_on));
		else
			imgDisclosure.setImageDrawable(context.getResources().getDrawable(R.drawable.checkbox_off));

		this.addView(imgDisclosure);

		// Title
		RelativeLayout.LayoutParams titleLayout = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		titleLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		titleLayout.addRule(RelativeLayout.RIGHT_OF, imgIcon.getId());
		titleLayout.addRule(RelativeLayout.LEFT_OF, imgDisclosure.getId());

		TextView lblTitle = new TextView(context);
		lblTitle.setText(title);
		lblTitle.setTypeface(Font.get_font("bold condensed"));
		lblTitle.setSingleLine();
		lblTitle.setEllipsize(TruncateAt.END);
		lblTitle.setLayoutParams(titleLayout);
		lblTitle.setId(9991);
		this.addView(lblTitle);

		this.title = title;

		// Subtitle
		RelativeLayout.LayoutParams subtitleLayout = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		subtitleLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		subtitleLayout.addRule(RelativeLayout.RIGHT_OF, imgIcon.getId());
		subtitleLayout.addRule(RelativeLayout.LEFT_OF, imgDisclosure.getId());

		TextView lblSubTitle = new TextView(context);
		lblSubTitle.setText(subtitle.toUpperCase());
		lblSubTitle.setSingleLine();
		lblSubTitle.setEllipsize(TruncateAt.END);
		lblSubTitle.setTextColor(0x66ffffff);
		lblSubTitle.setLayoutParams(subtitleLayout);
		lblSubTitle.setId(9992);
		this.addView(lblSubTitle);
		this.subtitle = subtitle;

		int padTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 4, FoHeart.getAppContext().getResources().getDisplayMetrics());
		int padBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 5, FoHeart.getAppContext().getResources().getDisplayMetrics());

		this.setBackgroundColor(0x55000000);
		this.setPadding(pad10, padTop, pad10, padBottom);

		int padTop1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 8, FoHeart.getAppContext().getResources().getDisplayMetrics());

		switch(style)
		{
		case 0:
			lblTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
			titleLayout.setMargins(0, padTop1, 0, 0);
			lblSubTitle.setVisibility(View.GONE);
			imgIcon.setVisibility(View.GONE);
			break;
		case 1:
			lblTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
			lblSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			imgIcon.setVisibility(View.GONE);
			break;
		case 2:
			lblTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
			titleLayout.setMargins(0, padTop1, 0, 0);
			lblSubTitle.setVisibility(View.GONE);
			break;
		case 3:
			lblTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
			lblSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			break;
		}

        //selecionando quando tiver falor default
        if(checked)
            set_selected();

		this.setOnTouchListener(new View.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            //gesture detector to detect swipe.
	        	
	        	 switch (event.getAction())
	             {
	        	 case MotionEvent.ACTION_DOWN:
	        		 set_pressed();
	        		 break;
	             case MotionEvent.ACTION_UP:
	            	 if (get_state())
	            		 set_unselected();
	            	 else
	            		 set_selected();
	            	 
	            	 get_parent().event_on_select(get_index());
	            	 
	             	break;
	             case MotionEvent.ACTION_CANCEL:
	            	 if (!get_state())
	            		 set_unselected();
	            	 else
	            		 set_selected();
	             	break;
	             }
	            return true;//always return true to consume event
	        }
	    });
	}
	
	public ListCheckScreen get_parent()
	{
		return this.parent;
	}
	
	public int get_index()
	{
		return this.index;
	}
	
	public boolean get_state()
	{
		return this.state;
	}
	
	public String get_value()
	{
		return this.value;
	}
	
	public String get_title()
	{
		return this.title;
	}
	
	public String get_subtitle()
	{
		return this.subtitle;
	}
	
	public void set_pressed()
	{
		this.setBackgroundColor(0x99000000);
	}
	
	public void set_selected()
	{
		this.state = true;
		this.setBackgroundColor(Color.get_actual_color_60());
		imgDisclosure.setImageDrawable(FoHeart.getAppContext().getResources().getDrawable(R.drawable.checkbox_on));
	}
	
	public void set_unselected()
	{
		this.state = false;
		this.setBackgroundColor(0x55000000);
		imgDisclosure.setImageDrawable(FoHeart.getAppContext().getResources().getDrawable(R.drawable.checkbox_off));
	}
	
	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
     }
	
}
