package com.flushoutsolutions.foheart.modules;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.flushoutsolutions.foheart.application.FoHeart;
import com.flushoutsolutions.foheart.R;
import com.flushoutsolutions.foheart.globals.Components;
import com.flushoutsolutions.foheart.globals.Screens;
import com.flushoutsolutions.foheart.globals.Variables;
import com.flushoutsolutions.foheart.logic.Procedures;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by daigomatsuoka on 18/06/14.
 */
public class PhotoInput
{
    private ImageView imageView;

    private String name;
    private String container;

    private int mode=0;
    private int height = 0;
    private String remoteFolder = "";
    private String imageFile = "";
    private Object imageData;
    private boolean visible = true;

    private ViewGroup layout;

    private String ev_on_change = null;

    public PhotoInput(String name, String container, ViewGroup layout)
    {
        this.name = name;
        this.layout = layout;
        this.container = container;
    }

    public PhotoInput(String name, String container, ViewGroup layout, String properties, String events) throws JSONException
    {
        this.name = name;
        this.layout = layout;
        this.container = container;

        JSONObject jsonProperties = new JSONObject(properties);

        if (!jsonProperties.isNull("mode"))
            this.mode = jsonProperties.getInt("mode");

        if (!jsonProperties.isNull("height"))
            this.height = jsonProperties.getInt("height");

        if (!jsonProperties.isNull("remoteFolder"))
            this.remoteFolder = jsonProperties.getString("remoteFolder");

        if (!jsonProperties.isNull("imageFile"))
            this.imageFile = jsonProperties.getString("imageFile");

        if (!jsonProperties.isNull("visible"))
            this.visible= jsonProperties.getBoolean("visible");


        JSONObject jsonEvents = new JSONObject(events);

        if (!jsonEvents.isNull("onChange"))
            this.ev_on_change = jsonEvents.getString("onChange");
    }

    public void set_visible(boolean visible)
    {
        this.visible = visible;

        if (this.visible)
            imageView.setVisibility(View.VISIBLE);
        else
            imageView.setVisibility(View.GONE);
    }

    public void render()
    {
        imageView = new ImageView(FoHeart.getAppContext());

        LinearLayout.LayoutParams params;

        if (this.height>0)
            params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                this.height
            );
        else
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );


        imageView.setImageResource(R.drawable.noimage);
//        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundColor(0x11FFFFFF);
        //button.setText("Clique para inserir a imagem");

        if (!this.visible) imageView.setVisibility(View.GONE);

//        params.setMargins(5, 5, 5, 5);
        imageView.setLayoutParams(params);
//        button.setTextColor(0xffffffff);

        this.layout.addView(imageView);

        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                imageView.setEnabled(false);
                Components.selectedImageView = imageView;

                switch (mode)
                {
                    case 0:
                        pick_menu_choose();
                        break;

                    case 1:
                        pick_galery();
                        break;

                    case 2:
                        pick_camera();
                        break;
                }



                imageView.setEnabled(true);
            }
        });

        set_properties();
    }

    public String get_name_addressed()
    {
        return this.container + "__"+this.name;
    }

    private void pick_menu_choose()
    {
        String[] listChoice = new String[2];
        listChoice[0] = "Da galeria";
        listChoice[1] = "Da câmera";

        AlertDialog.Builder builder = new AlertDialog.Builder(Screens.currentCtx);
        builder.setTitle("Escolher imagem...")
                .setItems(listChoice, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which)
                        {
                            case 0:
                                pick_galery();
                                break;
                            case 1:
                                pick_camera();
                                break;
                        }
                    }
                });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void pick_galery()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Screens.currentInstance.startActivityForResult(Intent.createChooser(intent, "Selecionar imagem"), 1);

    }

    private void pick_camera()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Screens.currentInstance.startActivityForResult(cameraIntent, 2);
    }


    public void on_change(View view)
    {
        if (this.ev_on_change!=null && !this.ev_on_change.equals(""))
        {
            try {
                Procedures mainProcedure = new Procedures();
                mainProcedure.initialize(this.ev_on_change, "{'sender':'"+get_name_addressed()+"'}");
                mainProcedure.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        set_properties();
    }


    public void set_properties()
    {
        String pattern = this.get_name_addressed()+"__";
        Variables.add(pattern + "imageFile", "string", this.imageFile);
    }


    public void set_property (String p, String v)
    {
        if (p.toLowerCase().equals("imageFile"))
        {
            this.imageFile = v;
        }
        else if (p.toLowerCase().equals("visible"))
        {
            set_visible(Boolean.valueOf(v));
        }

        set_properties();
    }
}
