package com.imperial.biap;

import java.util.ArrayList;
import java.util.List;

import com.imperial.biap.SettingsFragment.OnHeadlineSelectedListener;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ChangeColor extends Fragment {
	
	OnHeadlineSelectedListener mCallbackC;
	
	private static final String ARG_SECTION_NUMBER = "section_number";
		
/*	private static final int ALARM_FRAGMENT = 2;
	private static AlarmFragment alarmFragment = AlarmFragment.newInstance(ALARM_FRAGMENT);*/
	
    /** Called when the activity is first created. */
    ListView list;
    private List<String> List_file;
    
    // Container Activity must implement this interface
    public interface OnHeadlineSelectedListener {
        public void onArticleSelectedColor(int position);
    }
    

    
	public static ChangeColor newInstance(int sectionNumber) {
		ChangeColor fragment = new ChangeColor();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.change_color, container, false);
		
		List_file = new ArrayList<String>();
        list = (ListView) rootView.findViewById(R.id.listview_color);
        
        CreateListView();
        
		return rootView;

	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallbackC = (OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
	
    public void CreateListView()
    {
         List_file.add("Red");
         List_file.add("Blue");
         List_file.add("Green");
         List_file.add("Yellow");
         List_file.add("Magenta");
         //Create an adapter for the listView and add the ArrayList to the adapter.
         list.setAdapter(new ArrayAdapter<String>(this.getActivity(), R.layout.list_white_text, R.id.list_content, List_file));
         //list.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1,List_file));
         
         list.setOnItemClickListener(new OnItemClickListener()
           {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3)
                {
                    //args2 is the listViews Selected index
                	
                	// Send the event to the host activity
                    mCallbackC.onArticleSelectedColor(arg2);
                    
                }
           });
    }
}
