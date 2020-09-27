package com.imperial.biap;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.app.Fragment;

public class AlarmFragment extends Fragment {

	private static final String ARG_SECTION_NUMBER = "section_number";

	//private String buffer = String.valueOf(2.0F);

	private ImageView tLights;
	private ImageView arrows;
	private MediaPlayer mp;
	
/*	public ImageView gettLights() {
		return ttLights;
	}*/
	
/*	public void setBuffer(String buffer) {
		this.buffer = buffer;
	}
*/
	public static AlarmFragment newInstance(int sectionNumber) {
		AlarmFragment fragment = new AlarmFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.alarm_fragment, container, false);

		tLights = (ImageView) rootView.findViewById(R.id.trafficLights);
		arrows = (ImageView) rootView.findViewById(R.id.arrows);
		setYoda(R.raw.high_gl);
		//mp.reset();
		//WORKS
		//setLights(R.drawable.red_l);
		//setArrow(R.drawable.north);
		
		Log.e("ALARM ON CREATE", "OnCreateView");
		return rootView;
	}
	
	public void setLights(int resource) {
		tLights.setImageResource(resource);
	}
	
	public void setArrow(int resource) {
		arrows.setImageResource(resource);
	}
	
	public void setYoda(int resource){
		mp = MediaPlayer.create(this.getActivity(), resource);
		mp.start();
	}

}
