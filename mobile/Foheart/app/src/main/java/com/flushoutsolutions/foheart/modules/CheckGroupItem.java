package com.flushoutsolutions.foheart.modules;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.R;
import com.flushoutsolutions.foheart.font.Font;

import java.io.IOException;

@SuppressLint("ViewConstructor")
public class CheckGroupItem extends RelativeLayout
{
	private boolean state;
	private CheckGroup parent;
	private int index;
	public String value;
	private String title;
	private ImageView imgCheck;
	private boolean multiselect;
	
	public CheckGroupItem(Context context, CheckGroup parent, int index, boolean state, String value, String title, boolean multiselect) throws IOException
	{
		super(context);
		
		this.state = state;
		this.parent = parent;
		this.index = index;
		this.value= value;
		this.multiselect = multiselect;
		this.title = title;
		
		int pad10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 10, FoHeart.getAppContext().getResources().getDisplayMetrics());
		int padTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 4, FoHeart.getAppContext().getResources().getDisplayMetrics());
		int padBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 5, FoHeart.getAppContext().getResources().getDisplayMetrics());
		
		// Checkbox
		RelativeLayout.LayoutParams checkLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		checkLayout.addRule(RelativeLayout.CENTER_VERTICAL);
		checkLayout.setMargins(0, 0, pad10, 0);

		imgCheck = new ImageView(context);
		imgCheck.setId(9994);
		imgCheck.setLayoutParams(checkLayout);

		if (this.multiselect)
		{
			if (state)
				imgCheck.setImageDrawable(context.getResources().getDrawable(R.drawable.checkbox_on));
			else
				imgCheck.setImageDrawable(context.getResources().getDrawable(R.drawable.checkbox_off));
		}
		else
		{
			if (state)
				imgCheck.setImageDrawable(context.getResources().getDrawable(R.drawable.radio_on));
			else
				imgCheck.setImageDrawable(context.getResources().getDrawable(R.drawable.radio_off));
		}

		this.addView(imgCheck);

		// Title
		RelativeLayout.LayoutParams titleLayout = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		titleLayout.addRule(RelativeLayout.CENTER_VERTICAL);
		titleLayout.addRule(RelativeLayout.RIGHT_OF, imgCheck.getId());

		TextView lblTitle = new TextView(context);
		lblTitle.setText(title);
		lblTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
		lblTitle.setTypeface(Font.get_font("condensed"));
		lblTitle.setSingleLine();
		lblTitle.setEllipsize(TruncateAt.END);
		lblTitle.setLayoutParams(titleLayout);
		lblTitle.setId(9991);
		this.addView(lblTitle);

		this.setPadding(pad10, padTop, pad10, padBottom);

		this.setOnTouchListener(new View.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            //gesture detector to detect swipe.
	        	
	        	switch (event.getAction())
	             {
	             case MotionEvent.ACTION_UP:
	            	 if (get_state())
	            		 set_unselected();
	            	 else
	            		 set_selected();
	            	 
	            	 get_parent().event_on_select(get_this());
	            	 
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
	
	public CheckGroupItem get_this()
	{
		return this;
	}
	
	public CheckGroup get_parent()
	{
		return this.parent;
	}
	
	public String get_title()
	{
		return this.title;
	}
	
	public int get_index()
	{
		return this.index;
	}
	
	public boolean get_state()
	{
		return this.state;
	}
	
	public void set_selected()
	{
		this.state = true;
		if (this.multiselect)
			imgCheck.setImageDrawable(FoHeart.getAppContext().getResources().getDrawable(R.drawable.checkbox_on));
		else
			imgCheck.setImageDrawable(FoHeart.getAppContext().getResources().getDrawable(R.drawable.radio_on));
	}
	
	public void set_unselected()
	{
		this.state = false;
		if (this.multiselect)
			imgCheck.setImageDrawable(FoHeart.getAppContext().getResources().getDrawable(R.drawable.checkbox_off));
		else
			imgCheck.setImageDrawable(FoHeart.getAppContext().getResources().getDrawable(R.drawable.radio_off));
	}
	
	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
     }
}
