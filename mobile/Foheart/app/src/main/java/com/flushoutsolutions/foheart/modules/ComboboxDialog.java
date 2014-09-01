package com.flushoutsolutions.foheart.modules;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ComboboxDialog extends DialogFragment 
{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final CharSequence[] items = { "String 1", "String 2", "String 3" };

		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Titulo")
        	.setItems(items, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {
        			// The 'which' argument contains the index position
        			// of the selected item
        		}
        });
        
        
        return builder.create();
	}
}
