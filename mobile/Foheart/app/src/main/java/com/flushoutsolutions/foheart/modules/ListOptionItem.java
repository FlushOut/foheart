package com.flushoutsolutions.foheart.modules;

import android.annotation.SuppressLint;
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

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.font.Font;

import java.io.File;
import java.io.IOException;

@SuppressLint("ViewConstructor")
public class ListOptionItem extends RelativeLayout
{
	private boolean state;
	private ListOptions parent;
	private int index;
	private String value;
	private String title;
	
	public ListOptionItem(Context context, ListOptions parent, int index, String value, String title, String icon) throws IOException
	{
		super(context);
		
		this.parent = parent;
		this.index = index;
		this.value = value;
		this.title = title;
		
		int pad10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10, FoHeart.getAppContext().getResources().getDisplayMetrics());
		
		// Icon
		RelativeLayout.LayoutParams iconLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		iconLayout.addRule(RelativeLayout.CENTER_VERTICAL);
		iconLayout.setMargins(0, 0, pad10, 0);

		SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);

		ImageView imgIcon = new ImageView(context);

		if (null != icon)
		{
			if (!"".equals(icon.trim()))
			{
				try
				{
					int ico24 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 24, FoHeart.getAppContext().getResources().getDisplayMetrics());

					File imgFile = new File(FoHeart.getAppContext().getApplicationInfo().dataDir+"/app"+settings.getString("idApplication", "")+"/app/"+icon);
					Bitmap bmpIcon = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
					Bitmap bitmapIcon = Bitmap.createScaledBitmap(bmpIcon, ico24, ico24, true);

					imgIcon.setImageBitmap(bitmapIcon);
					imgIcon.setId(9993);
					imgIcon.setLayoutParams(iconLayout);
				}
				catch(Exception e)
				{
					//TODO: Send webservice
					e.printStackTrace();
				}
			}
		}
		this.addView(imgIcon);


		// Title
		RelativeLayout.LayoutParams titleLayout = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		titleLayout.addRule(RelativeLayout.CENTER_VERTICAL);
		titleLayout.addRule(RelativeLayout.RIGHT_OF, imgIcon.getId());

		TextView lblTitle = new TextView(context);
		lblTitle.setText(title);
		lblTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
		lblTitle.setTypeface(Font.get_font("condensed"));
		lblTitle.setSingleLine();
		lblTitle.setEllipsize(TruncateAt.END);
		lblTitle.setLayoutParams(titleLayout);
		lblTitle.setId(9991);
		this.addView(lblTitle);



		this.setOnTouchListener(new View.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            //gesture detector to detect swipe.
	        	
	        	 switch (event.getAction())
	             {
	        	case MotionEvent.ACTION_UP:
	            	 get_parent().event_on_select(get_this());
	            	 
	             	break;
	             }
	            return true;//always return true to consume event
	        }
	    });
	}
	
	public ListOptionItem get_this()
	{
		return this;
	}
	
	public ListOptions get_parent()
	{
		return this.parent;
	}
	
	public int get_index()
	{
		return this.index;
	}
	
	public String get_value()
	{
		return this.value;
	}
	
	public boolean get_state()
	{
		return this.state;
	}
	
	public String get_title()
	{
		return this.title;
	}
	
	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
     }
}
