package com.flushoutsolutions.foheart.design;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.util.Locale;

import com.flushoutsolutions.foheart.R;
import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.font.Font;

/**
 * Created by Manuel on 10/08/2014.
 */
public class MenuButton extends View{

    private String menuTitle = new String();
    private String menuTitleL1 = new String();
    private String menuTitleL2 = new String();

    private int menuColor = 0;
    private int notifications =0;
    private String iconFile;
    private Bitmap bitmapIcon;

    private Paint backgroundPaint;
    private Paint iconPaint;

    private Paint titlePaint;
    private Paint titleL2Paint;

    private Paint notificationPaint;
    private Bitmap bmpIcon;

    private int numlines = 1;

    // Elements dimensions
    private int maxSize; // Box size
    private int iconSize; // 50%
    private int iconXPos;
    private int iconYPos;
    public float fontsize;
    public float textYPos;
    public float notifXPos;
    public float notifYPos;

    private Canvas canv;

    public MenuButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MenuButton, 0, 0);

        try
        {
            menuTitle = a.getString(R.styleable.MenuButton_menuTitle);
            menuColor = a.getInteger(R.styleable.MenuButton_menuColor, 0);
            this.notifications = a.getInteger(R.styleable.MenuButton_notifications, 0);
            iconFile= a.getString(R.styleable.MenuButton_iconFile);
        }
        finally
        {
            a.recycle();
        }
    }


    public MenuButton(Context context)
    {
        super(context);
        init();
    }

    public MenuButton(Context context, String title, String iconFile, String color, int notf)
    {
        super(context);
        menuTitle = title;
        menuColor = Color.get_color(color);
        this.notifications = notf;
        this.iconFile = iconFile;

        init();
    }


    private void init ()
    {
        int numcols = 1;
        WindowManager wm = (WindowManager) FoHeart.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int width = display.getWidth();
        int rightMargin = 20;

        maxSize = Math.round((width/numcols))-rightMargin;

        iconSize = maxSize/6;
        iconXPos = Math.round(maxSize * 0.1f);
        iconYPos = Math.round(maxSize * 0.05f);
        fontsize = maxSize * 0.08f;

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTypeface(Font.get_font("bold condensed"));
        titlePaint.setTextSize(fontsize);
        titlePaint.setColor(0xffffffff);
        titlePaint.setTextAlign(Paint.Align.LEFT);

        menuTitle = menuTitle.toUpperCase(Locale.getDefault());
        float textWidth = titlePaint.measureText(menuTitle);
        if (textWidth>maxSize*0.9) numlines=2;

        if (numlines == 1)
            textYPos = maxSize*0.160f;
        else
            textYPos = maxSize*0.130f;

        notifXPos = maxSize*0.70f;

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(menuColor);

        iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        titleL2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titleL2Paint.setTypeface(Font.get_font("bold condensed"));
        titleL2Paint.setTextSize(fontsize);
        titleL2Paint.setColor(0xffffffff);
        titleL2Paint.setTextAlign(Paint.Align.CENTER);

        if (numlines==1)
            menuTitleL1 = menuTitle;
        else
        {
            String[] menuPieces = menuTitle.split(" ");

            if (menuPieces.length == 2)
            {
                menuTitleL1 = menuPieces[0];
                menuTitleL2 = menuPieces[1];
            }
            else
            {
                boolean newLine = false;

                for (int c=0; c<menuPieces.length; c++)
                {
                    if (!newLine)
                        menuTitleL1 += menuPieces[c]+" ";
                    else
                        menuTitleL2 += menuPieces[c]+" ";

                    float lineW = titlePaint.measureText(menuTitleL1.trim());
                    if (!newLine && lineW > textWidth * 0.4) newLine = true;
                }
                menuTitleL1 = menuTitleL1.trim();
                menuTitleL2 = menuTitleL2.trim();
            }
        }

        notificationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        notificationPaint.setTypeface(Font.get_font("bold condensed"));
        notificationPaint.setColor(0xffffffff);
        notificationPaint.setTextAlign(Paint.Align.LEFT);

        notificationPaint.setTextSize(16);
        float notifWidth = notificationPaint.measureText(Integer.toString(this.notifications));
        float notifWidthLimit = maxSize * 0.2f;
        float notifRate = notifWidthLimit/notifWidth;
        float notifFontSize = 16*notifRate;

        notifYPos = this.iconYPos + this.iconSize/2 + notifFontSize/2;

        notificationPaint.setTextSize(notifFontSize);

        SharedPreferences settings = FoHeart.getAppContext().getSharedPreferences("userconfigs", 0);

        File imgFile = new  File(FoHeart.getAppContext().getApplicationInfo().dataDir+"/apps/app"+settings.getString("idApplication", "")+"/app/"+this.iconFile);
        bmpIcon = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        bitmapIcon = Bitmap.createScaledBitmap( bmpIcon, iconSize, iconSize, true );
        bmpIcon.recycle();
    }


    public String getMenuTitle()
    {
        return menuTitle;
    }

    public void setMenuTitle(String title)
    {
        menuTitle=title;
        invalidate();
        requestLayout();
    }

    public int getMenuColor()
    {
        return menuColor;
    }


    public void setMenuColor(int idColor)
    {
        menuColor = idColor;
        invalidate();
        requestLayout();
    }

    public int getNotifications()
    {
        return this.notifications;
    }

    public void setNotification(int num)
    {
        this.notifications = num;

        invalidate();
        requestLayout();
    }

    public String getIconFile()
    {
        return iconFile;
    }

    public void setIconFile(String file)
    {
        iconFile=file;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
	/*	int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();

		int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));
		maxSize= w;
		System.out.println("debg  maxSize "+ maxSize);
		iconSize = w/2;
		*/
        setMeasuredDimension(maxSize, maxSize/4);
    }

    public void onDraw (Canvas canvas)
    {
        super.onDraw(canvas);
        canv = canvas;

        // Draw the background square
        int padding = 0;
        int squareSize = (maxSize-padding*2);
        int rectSizeBase = (maxSize-padding*2);
        int rectSizeHeight = (maxSize-padding*2)/2;
        //canvas.drawRect(padding, padding, squareSize, squareSize, backgroundPaint);
        canvas.drawRect(padding, padding, rectSizeBase, rectSizeHeight, backgroundPaint);

        canvas.drawText(menuTitleL1, squareSize/2, textYPos, titlePaint);

        if (numlines == 2)
            canvas.drawText(menuTitleL2, squareSize/2, textYPos+fontsize+maxSize*0.02f, titleL2Paint);

        // Place the icon on center

        if (this.notifications>0)
        {
            this.iconXPos = this.iconYPos;

            notificationPaint.setTextSize(16);
            float notifWidth = notificationPaint.measureText(Integer.toString(this.notifications));
            float notifWidthLimit = maxSize * 0.2f;
            float notifRate = notifWidthLimit/notifWidth;
            float notifFontSize = 16*notifRate;

            notifYPos = this.iconYPos + this.iconSize/2 + notifFontSize/2;

            notificationPaint.setTextSize(notifFontSize);

            canvas.drawText(Integer.toString(this.notifications), notifXPos, notifYPos, notificationPaint);

        }
        else
        {
            iconXPos = Math.round(maxSize * 0.25f);
        }

        canvas.drawBitmap(bitmapIcon, this.iconXPos, this.iconYPos, iconPaint);
    }
}
