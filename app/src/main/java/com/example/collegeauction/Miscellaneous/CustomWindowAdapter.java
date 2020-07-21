package com.example.collegeauction.Miscellaneous;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.collegeauction.R;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class CustomWindowAdapter implements InfoWindowAdapter {
	LayoutInflater mInflater;
	
	public CustomWindowAdapter(LayoutInflater i){
		mInflater = i;
	}

	// This defines the contents within the info window based on the marker
	@Override
	public View getInfoContents(Marker marker) {
	    // Getting view from the layout file
	    View v = mInflater.inflate(R.layout.custom_info_window, null);
	    // Populate fields	
	    TextView title = v.findViewById(R.id.tv_info_window_title);
	    title.setText(marker.getTitle());

	    TextView description = v.findViewById(R.id.tv_info_window_description);
	    description.setText(marker.getSnippet());
	    // Return info window contents
	    return v;
	}
        
	// This changes the frame of the info window; returning null uses the default frame.
        // This is just the border and arrow surrounding the contents specified above
	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}
}