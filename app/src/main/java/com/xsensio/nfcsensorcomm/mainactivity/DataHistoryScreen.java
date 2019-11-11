package com.xsensio.nfcsensorcomm.mainactivity;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.ReducedMeasurement;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DataHistoryScreen extends Fragment {

    private HomeScreen.OnFragmentInteractionListener mListener;

    private ArrayList<ReducedMeasurement> graphData=new ArrayList<>();

    private View global_view;
    private ArrayList<Entry> values1;
    private  ArrayList<Entry>values2;
    private  ArrayList<Entry> values3;
    private Button backButton;
    private LineChart graph1;
    private LineChart graph2;
    private LineChart graph3;
    private int index = 10;


    void read_old(){
        values1 = new ArrayList<>();
        values2 = new ArrayList<>();
        values3 = new ArrayList<>();

        graphData=((MainActivity)getActivity()).getMeasurements();

        for (ReducedMeasurement graphDatum : graphData) {
            if(graphDatum.isSodiumValid()){
                values1.add(new Entry(
                        graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                        (float)graphDatum.getSodiumVal()
                ));
            }
            if(graphDatum.isPhValid()){
                values2.add(new Entry(
                        graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                        (float)graphDatum.getPhVal()
                ));
            }
            if(graphDatum.isTemperatureValid()){
                values3.add(new Entry(
                        graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                        (float)graphDatum.getTemperatureVal()
                ));
            }
        }
    }

    public DataHistoryScreen () {
        // Required empty public constructor
    }

    private DonutProgress progress;
    private TextView text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("Tag","Called onCreate\n");
        super.onCreate(savedInstanceState);
        Global.nfc_set = false;
//        read_old();
        ((MainActivity)getActivity()).clearMeasurements();
    }


    private void initGraph(LineChart graph){
        graph.getDescription().setEnabled(false);
        graph.setDragEnabled(true);
        graph.setScaleEnabled(true);
        graph.setBackgroundColor(Color.WHITE);
        graph.animateXY(500,500);

        XAxis xAxis = graph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#888888"));
        xAxis.setGridColor(Color.parseColor("#888888"));
        xAxis.setValueFormatter(new ValueFormatter() {

            //private final DateTimeFormatter mFormatPrecise =  DateTimeFormatter.ofPattern("MM/dd-HH");
            private final DateTimeFormatter mFormat =  DateTimeFormatter.ofPattern("MM/dd");

            @Override
            public String getFormattedValue(float value) {
                LocalDateTime dateTime=LocalDateTime.ofEpochSecond((long)value,0,ZoneOffset.UTC);
                return dateTime.format(mFormat);
            }
        });

        YAxis leftAxis = graph.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(Color.parseColor("#888888"));
        leftAxis.setGridColor(Color.parseColor("#888888"));

        YAxis rightAxis = graph.getAxisRight();
        rightAxis.setEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_data_history_screen, container, false);
        backButton=view.findViewById(R.id.hs_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

       Button clearButton=view.findViewById(R.id.hs_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).clearMeasurements();
                graph1.invalidate();
                graph2.invalidate();
                graph3.invalidate();
                plot();
            }
        });

//        graphData=((MainActivity)getActivity()).getMeasurements();

        graph1=view.findViewById(R.id.hs_graph1);
        graph2=view.findViewById(R.id.hs_graph2);
        graph3=view.findViewById(R.id.hs_graph3);

        initGraph(graph1);
        initGraph(graph2);
        initGraph(graph3);
        Log.d("Tag","Finished initializing the graph\n");

        return view;
    }

    public void read_sensors(View final_view){

        final Handler handler = new Handler();

        class MyRunnable implements Runnable {
            private Handler handler;
            private View view1;
            public MyRunnable(Handler handler, View view) {
                this.handler = handler;
                this.view1 = view;
            }
            @Override
            public void run() {

                Log.d("Tag","Inside run method\n");
//                for(int i=0;i<1000;i++);

                this.handler.postDelayed(this, 1000);


//                ReducedMeasurement reduced = new ReducedMeasurement(LocalDateTime.now(),1.,2.,3.);

//                Log.d("Tag","Data transmission succesful\n");


//                if(graphData.size() > 0)
//                    graphData.remove(0);
//
//                graphData.add(reduced);

//                Log.d("Tag","Global.data_read = "+Global.data_read+"\n");
                Log.d("Tag","Global.nfc_set = "+Global.nfc_set+"\n");
//                Log.d("Tag","Activity = "+getActivity()+"\n");
                Log.d("Tag","Before calling perform click\n");
//                mReadSensorsButton.performClick();
//                Global.global_button.performClick();
//                ((MainActivity)(new HomeScreen()).getActivity()).readSensors();
                ((MainActivity)getActivity()).readSensors();
                Log.d("Tag","After calling perform click\n");

                if(Global.data_read && Global.nfc_set) {
                    graphData=((MainActivity)getActivity()).getMeasurements();
                    for (ReducedMeasurement graphDatum : graphData) {
                        if(graphDatum.isSodiumValid()){
                            values1.add(new Entry(
                                    graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                                    (float)graphDatum.getSodiumVal()
                            ));
                        }
                        if(graphDatum.isPhValid()){
                            values2.add(new Entry(
                                    graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                                    (float)graphDatum.getPhVal()
                            ));
                        }
                        if(graphDatum.isTemperatureValid()){
                            values3.add(new Entry(
                                    graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                                    (float)graphDatum.getTemperatureVal()
                            ));
                        }
                    }
//                    Global.global_button.performClick();
//                    ((MainActivity)getActivity()).readSensors();
//                    mReadSensorsButton.performClick();
//                    ((MainActivity)HomeScreen.newInstance().getActivity()).readSensors();

                    Log.d("Tag","Checkpoint 8\n");
                    plot();
                    Log.d("Tag","Checkpoint 9\n");
                }

//                try {
//                    write_to_file(graphData);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }
        handler.post(new MyRunnable(handler, final_view));
        Log.d("Tag","After running thread\n");

    }

    public void plot(){


        Log.d("Tag","Before array initialization\n");
//        initialize_arrays();
        Log.d("Tag","Before array initialization\n");
        plotLine(graph1,values1);
        plotLine(graph2,values2);
        plotLine(graph3,values3);
        Log.d("Tag","Finished plotting\n");
    }

    public void add_data(View final_view){
        Log.d("Tag","Called update view");
        final Handler handler = new Handler();

        class MyRunnable implements Runnable {
            private Handler handler;
            private View view1;
            public MyRunnable(Handler handler, View view) {
                this.handler = handler;
                this.view1 = view;
            }
            @Override
            public void run() {
                Log.d("Tag","Inside run method\n");
//                for(int i=0;i<1000;i++);

                this.handler.postDelayed(this, 3000);


//                ReducedMeasurement reduced = new ReducedMeasurement(LocalDateTime.now(),1.,2.,3.);

//                Log.d("Tag","Data transmission succesful\n");


//                if(graphData.size() > 0)
//                    graphData.remove(0);
//
//                graphData.add(reduced);
                ++index;
                values1.add(new Entry(
                        index,
                        2*index
                ));
                values2.add(new Entry(
                        index,
                        5*index
                ));
                values3.add(new Entry(
                        index,
                        10*index
                ));
                Log.d("Tag","Checkpoint 8\n");
                plot();
                Log.d("Tag","Checkpoint 9\n");
//                try {
//                    write_to_file(graphData);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }
        handler.post(new MyRunnable(handler, final_view));
        Log.d("Tag","After running thread\n");

    }

    private void plotLine(LineChart graph, ArrayList<Entry> values){
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, " ");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(Color.parseColor("#ffc755"));
        set1.setLineWidth(3f);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setFillAlpha(150);
        set1.setFillColor(Color.parseColor("#ffdd99"));
        set1.setHighLightColor(Color.parseColor("#ffc755"));
        set1.setCircleColor(Color.parseColor("#ffc755"));
        set1.setDrawCircleHole(false);
        set1.setDrawFilled(true);
        set1.setDrawCircles(true);
        set1.setCircleRadius(6f);
        LineData data = new LineData(set1);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);
        graph.setData(data);
        graph.getXAxis().setAxisMaximum(set1.getXMax());
        graph.getXAxis().setAxisMinimum(set1.getXMin());
        graph.getLegend().setEnabled(false);
    }




    public void resetStats(){
        if(progress!=null){
            progress.setProgress(0);
        }
        if(text!=null){
            text.setText("Connecting to circuit");
        }
    }

    public void updateReadSensorProgress(String taskDescription, int completionRatio) {
//        switch (taskDescription) {
//            case "Receiving data for Sensor 1, Case 2":
//                progress.setProgress(completionRatio);
//                text.setText("Receiving data for Sensor 1/3");
//                break;
//            case "Receiving data for Sensor 2, Case 2":
//                progress.setProgress(completionRatio);
//                text.setText("Receiving data for Sensor 2/3");
//                break;
//            case "Receiving data for Sensor 3, Case 2":
//                progress.setProgress(completionRatio);
//                text.setText("Receiving data for Sensor 3/3");
//                if(completionRatio==100){
//                    text.setText("Finished Recieving Data");
//                    ((MainActivity)getActivity()).changeFragment("resultScreen");
//                    Global.data_read = true;
//                }
//                break;
//        }

        Log.d("Tag","Called update function in history screen with description = "+taskDescription+"\n");
        switch (taskDescription){
            case "Receiving data for Sensor1, Case2":
                values1.add(new Entry(completionRatio,2*completionRatio));
                break;

            case "Receiving data for Sensor2, Case 2":
                values2.add(new Entry(completionRatio,5*completionRatio));
                break;

            case "Receiving data for Sensor 3, Case 2":
                values3.add(new Entry(completionRatio,10*completionRatio));
                break;
        }
        plot();
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (HomeScreen.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}