package com.imperial.biap;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData; 
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
//import com.jjoe64.graphview.Viewport;
/*import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview_demos.MainActivity;
import com.jjoe64.graphview_demos.R;*/

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class GraphFragment extends Fragment {
	private static final String ARG_SECTION_NUMBER = "section_number";
	
	//Example Series
	private static final int EXAMPLE_SERIES = 0;
	private static final int EXAMPLE_SERIES_LOW = 1;
	private static final int EXAMPLE_SERIES_HIGH = 2;
	private static final int EXAMPLE_SERIES_2 = 3;
	
	private GraphView graphView;
	private GraphView graphView2;
	
	LinearLayout layout;
	LinearLayout layout2;
	
	//GraphViewData[] data = new GraphViewData[] {};
	GraphViewSeries exampleSeries = new GraphViewSeries(" ",new GraphViewSeriesStyle(Color.YELLOW, 3), new GraphViewData[] {}); 
	GraphViewSeries exampleSeriesLow = new GraphViewSeries(" ",new GraphViewSeriesStyle(Color.RED, 3), new GraphViewData[] {});
	GraphViewSeries exampleSeriesHigh = new GraphViewSeries(" ",new GraphViewSeriesStyle(Color.RED, 3), new GraphViewData[] {}); 
	GraphViewSeries exampleSeries2 = new GraphViewSeries(" ",new GraphViewSeriesStyle(Color.YELLOW, 3), new GraphViewData[] {}); 
    
	//Create test data to play with as handler only entered in BT
	public static GraphViewSeries testSeries = new GraphViewSeries(" ",new GraphViewSeriesStyle(Color.YELLOW, 3), new GraphViewData[] {
	          new GraphViewData(1, 2.0d)
	          , new GraphViewData(2, 1.5d)
	          , new GraphViewData(3, 2.5d)
	          , new GraphViewData(4, 1.0d)
	          , new GraphViewData(5, 2.0d)
	          , new GraphViewData(6, 2.5d)
	          , new GraphViewData(7, 2.5d)
	          , new GraphViewData(8, 1.0d)
	          , new GraphViewData(9, 2.0d)
	          , new GraphViewData(10, 5.0d)
	          , new GraphViewData(11, 2.5d)
	          , new GraphViewData(12, 1.0d)
	      	});
	
	public GraphViewSeries getTestSeries() {
		return testSeries;
	}

	public static void setTestSeries(GraphViewSeries testSeries) {
		GraphFragment.testSeries = testSeries;
	}

	public static GraphFragment newInstance(int sectionNumber) {
		GraphFragment fragment = new GraphFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
    	ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.graph_fragment, container, false);
    	 
    	//Initialise the glucose level graph
        graphView = new LineGraphView(this.getActivity(), "Blood Glucose (mmol/l)");  
        graphView.addSeries(exampleSeries);  
        graphView.addSeries(exampleSeriesLow);
        graphView.addSeries(exampleSeriesHigh);
        
        graphView.getGraphViewStyle().setGridColor(Color.WHITE);
        graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.WHITE);
        graphView.getGraphViewStyle().setVerticalLabelsColor(Color.WHITE);
        graphView.getGraphViewStyle().setVerticalLabelsWidth(40);
        //graphView.setHorizontalLabels(horlabels);
        graphView.setManualYAxisBounds(15,0);
        graphView.getGraphViewStyle().setTextSize(20);
//      graphView.getGraphViewStyle().setNumHorizontalLabels(5);
        graphView.getGraphViewStyle().setNumVerticalLabels(16);
        graphView.setScrollable(true); //Activate scaling and zooming
        graphView.setScalable(true);
        
        //graphView.setPadding(0, 50, 0, 50);
        
    	// * time as label formatter
    	 
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Date d = new Date((long) value);
                    return dateFormat.format(d);
                }
                return null; // let graphview generate Y-axis label for us
            }
        });
        
        //Add the test data
        //graphView.addSeries(testSeries);
        
        layout = (LinearLayout) rootView.findViewById(R.id.graph1);  
        layout.addView(graphView);
        
        
        
        //Initialise the insulin level graph
        graphView2 = new BarGraphView(this.getActivity(), "Insulin Units (U)");
        graphView2.addSeries(exampleSeries2);
        graphView2.getGraphViewStyle().setGridColor(Color.WHITE);
        graphView2.getGraphViewStyle().setHorizontalLabelsColor(Color.WHITE);
        graphView2.getGraphViewStyle().setVerticalLabelsColor(Color.WHITE);
        graphView2.getGraphViewStyle().setVerticalLabelsWidth(40);
        graphView2.setManualYAxisBounds(9,0);
        graphView2.getGraphViewStyle().setTextSize(20);
//        graphView2.getGraphViewStyle().setNumHorizontalLabels(5);
        graphView2.getGraphViewStyle().setNumVerticalLabels(10);
        graphView2.setScrollable(true);
        //OMA //Activate scaling and zooming
        graphView.setScalable(true);
        
        //graphView2.setPadding(0, 50, 0, 50);
        
        graphView2.setCustomLabelFormatter(new CustomLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Date d = new Date((long) value);
                    return dateFormat.format(d);
                }
                return null; // let graphview generate Y-axis label for us
            }
        });
        
        layout2 = (LinearLayout) rootView.findViewById(R.id.graph2);  
        layout2.addView(graphView2); 
        
       
        return rootView;
    }
    
    public GraphView getGraphView() {
		return graphView;
	}

	public void setGraphView(GraphView graphView) {
		this.graphView = graphView;
	}

	public GraphView getGraphView2() {
		return graphView2;
	}

	public void setGraphView2(GraphView graphView2) {
		this.graphView2 = graphView2;
	}

	public void setViewPort(int graph, long time, int value) {
    	switch(graph) {
	    case 1: graphView.setViewPort(time, value); 
	    		break;
	    case 2: graphView2.setViewPort(time, value); 
	    		break;
	    default: Log.e("GRAPH_FRAGMENT", "incorrect graph called: setViewPort(int, long, int)");
    	}
    }
    
    public void appendData(int EXAMPLE_ID, GraphViewData data, boolean outcome, int value) {
    	switch(EXAMPLE_ID) {
    	case EXAMPLE_SERIES: 	 exampleSeries.appendData(data, outcome, value);
    		break;
    	case EXAMPLE_SERIES_LOW: exampleSeriesLow.appendData(data, outcome, value);
			break;
    	case EXAMPLE_SERIES_HIGH:exampleSeriesHigh.appendData(data, outcome, value);
			break;
    	case EXAMPLE_SERIES_2:	 exampleSeries2.appendData(data, outcome, value);
			break;
		default: Log.e("GRAPH_FRAGMENT", "incorrect example series called: appendData(GraphViewData, boolean, int)");  
    	}
    }
    
    public int returnPosition(){
    	
    }
    
    public void setGridColor (int position){
    	if(position==0){
    		layout.removeView(graphView);
    		graphView.getGraphViewStyle().setGridColor(Color.RED);
    		layout.addView(graphView2);
    		graphView2.invalidate();
    	}
    	if(position==1){
    		//graphView = new LineGraphView(this.getActivity(), "Blood Glucose (mmol/l)");
    		layout.removeView(graphView);
    		graphView.getGraphViewStyle().setGridColor(Color.BLUE);
    		layout.addView(graphView);
    		graphView.invalidate();
    	}
    	if(position==2){
    		//graphView = new LineGraphView(this.getActivity(), "Blood Glucose (mmol/l)");
    		layout.removeView(graphView);
    		graphView.getGraphViewStyle().setGridColor(Color.GREEN);
    		layout.addView(graphView);
    		graphView.invalidate();
    	}
    }

    
}
