package com.flushoutsolutions.foheart.slidingmenu.fragment;

import com.flushoutsolutions.foheart.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class MenuFragment extends Fragment {
	
	public MenuFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        GridView grdView = (GridView) rootView.findViewById(R.id.menuGridView);

        return rootView;
    }
}
