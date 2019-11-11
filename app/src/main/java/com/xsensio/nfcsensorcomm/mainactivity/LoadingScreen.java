package com.xsensio.nfcsensorcomm.mainactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.common.util.ArrayUtils;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.calibration.CalibrationProfileManager;
import com.xsensio.nfcsensorcomm.files.FileSensorsBuffer;
import com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.VirtualSensorAdapter;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.ReducedMeasurement;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase1;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase2;
import com.xsensio.nfcsensorcomm.sensorresult.case1.VirtualSensorResultCase1Contract;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LoadingScreen extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ArrayList<ReducedMeasurement> graphData=new ArrayList<>();

    private View global_view;
    private ArrayList<Entry> values1;
    private  ArrayList<Entry>values2;
    private  ArrayList<Entry> values3;
    private Button backButton;
    private LineChart graph1;
    private LineChart graph2;
    private LineChart graph3;
    private int ind = 0;
    public ReducedMeasurement measurement;



    public LoadingScreen () {
        // Required empty public constructor
    }

    private DonutProgress progress;
    private TextView text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("Tag","Called onCreate of Loading Screen  \n");
        super.onCreate(savedInstanceState);
        Global.nfc_set = false;

//        Bundle b = getActivity().getIntent().getExtras();
//        ArrayList<VirtualSensor> tmp=b.getParcelableArrayList("sensors");
//        buffer=new FileSensorsBuffer(tmp);
//        Log.d("Tag","Buffer virtual sensors = "+buffer.virtualSensors+"\n");
//        Log.d("Tag","is buffer empty = "+buffer.isEmpty()+"\n");
//        read_old();
        delete_file();
//        ((MainActivity)getActivity()).clearMeasurements();
    }

    private CalibrationProfile getProfile(VirtualSensorDefinitionCase2 def) {

        List<CalibrationProfile> profiles = new ArrayList<>();

        try {

            // Load from the Settings the path of the folder containing the calibration profiles
            Log.d("Tag","Before calling getActivity\n");
            Context context = getActivity().getApplicationContext();
            Log.d("Tag","After calling getActivity\n");
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String profilesFolderPath = settings.getString("calibration_folder_path", context.getString(R.string.calibration_folder_path_def_val));

            // Load all available calibration profiles
            profiles = CalibrationProfileManager.loadCalibrationProfilesFromFiles(profilesFolderPath, def);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return profiles.get(profiles.size()-1);
    }

    public void updateGui(){
        Log.d("Tag","Called update GUI in result screen\n");
        if(sensorResults.size()==0){

        } else {
            double phVal=0;
            double sodiumVal=0;
            double temperatureVal=0;
            for (int i = 0; i < sensorResults.size(); i++) {
                VirtualSensorCase2 sensor = (VirtualSensorCase2) sensorResults.get(i);
                //Just formatting average values
                VirtualSensorDefinitionCase2 definitionCase2 = (VirtualSensorDefinitionCase2) sensor.getVirtualSensorDefinition();
                int sensorNumber = Integer.valueOf(definitionCase2.getSensorName().replace("Sensor ",""))-1;
                String average="";
                if(sensorNumber==2){
                    Log.d("Tag","Inside sensorNumber2\n");
                    //Todo 1: in order to display temperature instead of K+ concentration, 1st step: uncomment these 4 lines, and comment the following 4 lines, by junrui
                    VirtualSensorCase2.DataContainer data = sensor.getDataContainer(getContext(), null);
                    temperatureVal=data.getAverageDerivative();
//                    VirtualSensorCase2.DataContainer data = sensor.getDataContainer(getContext(), selectedProfiles[sensorNumber]);
//                    NumberFormat formatter = new DecimalFormat("###.###");
//                    average =  formatter.format(Math.pow(10,data.getAverageMappedData())*1000);
//                    average = average + definitionCase2.getMappedDataPlotMetadata().getYAxisUnitLabel()
                    Log.d("Tag","Insie sensorNumber2\n");
                } else if(sensorNumber==1){
                    Log.d("Tag","Inside sensorNumber1\n");

                    //sensorNumber==1, display as concentration, by junrui
                    CalibrationProfile profile=getProfile(VirtualSensorDefinitionCase2.SENSOR_2);
                    VirtualSensorCase2.DataContainer data = sensor.getDataContainer(getContext(), profile);
                    sodiumVal=Math.pow(10,data.getAverageMappedData())*1000;
                    Log.d("Tag","Inside sensorNumber1\n");
                } else {
                    Log.d("Tag","Sensor number = "+sensorNumber+"\n");
                    //sensorNumber==0, display as pH, by junrui
                    Log.d("Tag","Sensor definition = "+VirtualSensorDefinitionCase2.SENSOR_1+"\n");
                    CalibrationProfile profile=getProfile(VirtualSensorDefinitionCase2.SENSOR_1);
                    Log.d("Tag","Calling getProfile\n");
                    VirtualSensorCase2.DataContainer data = sensor.getDataContainer(getContext(), profile);
                    phVal=-data.getAverageMappedData();
                    Log.d("Tag","Inside sensorNumber3\n");
                }
            }
            measurement=new ReducedMeasurement(LocalDateTime.now(),phVal,sodiumVal,temperatureVal);
//            displayMeasurement();
            ((MainActivity)getActivity()).addMeasurement(measurement);
            Log.d("Tag","Finished updateGui function\n");
        }
    }

    List<VirtualSensor> sensorResults=new ArrayList<>();
    public void updateSensorResult(List<VirtualSensor> sensorResult) {
        Log.d("Tag","Inside update sensor result of result screen\n");
        Log.d("Tag","Size of sensorResult = "+sensorResult.size());
        sensorResults=sensorResult;
        updateGui();
    }
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
        Log.d("Tag","Size of values1 = "+values1.size()+"\n");
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

//                xAxis.setValueFormatter(new ValueFormatter() {
//
//            //private final DateTimeFormatter mFormatPrecise =  DateTimeFormatter.ofPattern("MM/dd-HH");
//            private final DateTimeFormatter mFormat =  DateTimeFormatter.ofPattern("MM/dd");
//
//            @Override
//            public String getFormattedValue(float value) {
//                LocalDateTime dateTime=LocalDateTime.ofEpochSecond((long)value,0,ZoneOffset.UTC);
//                return dateTime.format(mFormat);
//            }
//        });

        YAxis leftAxis = graph.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(Color.parseColor("#888888"));
        leftAxis.setGridColor(Color.parseColor("#888888"));

        YAxis rightAxis = graph.getAxisRight();
        rightAxis.setEnabled(false);
    }

//    private void initGraph(LineChart graph){
//        graph.getDescription().setEnabled(false);
//        graph.setDragEnabled(true);
//        graph.setScaleEnabled(true);
//        graph.setBackgroundColor(Color.WHITE);
////        xAxis.setGridColor(Color.parseColor("#888888"));
//
//        XAxis xAxis = graph.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        YAxis rightAxis = graph.getAxisRight();
//        rightAxis.setEnabled(false);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_loading_screen, container, false);
        Log.d("Tag","Called onnCreateView\n");
//        backButton=view.findViewById(R.id.hs_back);
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().onBackPressed();
//            }
//        });
//
//        Button clearButton=view.findViewById(R.id.hs_clear);
//        clearButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((MainActivity)getActivity()).clearMeasurements();
//                graph1.invalidate();
//                graph2.invalidate();
//                graph3.invalidate();
//                plot();
//            }
//        });

//        graphData=((MainActivity)getActivity()).getMeasurements();


        graph1=view.findViewById(R.id.hs_graph1);
        graph2=view.findViewById(R.id.hs_graph2);
        graph3=view.findViewById(R.id.hs_graph3);


//        values1 = new ArrayList<Entry>();
//        values2 = new ArrayList<Entry>();
//        values3 = new ArrayList<Entry>();

//        for(int i=0;i<10;i++){
//            values1.add(new Entry(i,2*i));
//            values2.add(new Entry(i,3*i));
//            values3.add(new Entry(i,5*i));
//        }
        initGraph(graph1);
        initGraph(graph2);
        initGraph(graph3);
        plot();
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

    void notify_chart_changed(LineChart g){
        g.notifyDataSetChanged();
        g.invalidate();
    }
    public void plot(){

//        Log.d("Tag","Size of values1 = "+values1.size()+"\n");
//        Log.d("Tag","Size of values2 = "+values2.size()+"\n");
//        Log.d("Tag","Size of values3 = "+values3.size()+"\n");
//
//        Log.d("Tag","Called plotting \n");
//        Log.d("Tag","Element of values1: \n");
//        for(int i=0;i<10;i++)
//            Log.d("Tag",values1.get(i).toString());
//
//        Log.d("Tag","Element of values2: \n");
//        for(int i=0;i<10;i++)
//            Log.d("Tag",values2.get(i).toString());
//
//        Log.d("Tag","Element of values3: \n");
//        for(int i=0;i<10;i++)
//            Log.d("Tag",values3.get(i).toString());
//
//        initGraph(graph1);
//        initGraph(graph2);
//        initGraph(graph3);
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
                ++ind;
                values1.add(new Entry(
                        ind,
                        2*ind
                ));
                values2.add(new Entry(
                        ind,
                        5*ind
                ));
                values3.add(new Entry(
                        ind,
                        10*ind
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

    public void run_delay(long ms){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
            }
        }, ms);
    }

    public void delete_file(){
        Log.d("Tag","Called add measurement\n");
        File external= Environment.getExternalStorageDirectory();
        File myDir=new File(external,"xsensio");
        if(!myDir.exists()){
            myDir.mkdir(); }
        File file=new File(myDir,"measurements.csv");
        if(file.exists()){
            boolean deleted = file.delete();
            if(deleted)
                Log.d("Tag","File deleted succesfully\n");
        }
    }
    public void updateReadSensorProgress(String taskDescription, int completionRatio)
    {
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
//
//        Log.d("Tag","Called update function in loading screen with description = "+taskDescription+"\n");



//
//        Log.d("Tag","global sensors = "+Global.global_sensors.size()+"\n");

        switch (taskDescription){
            case "Receiving data for Sensor 1, Case 2":
                Log.d("Tag","Inside case 3\n");
                break;

            case "Receiving data for Sensor 2, Case 2":
                Log.d("Tag","Inside case 2 \n");

                break;

            case "Receiving data for Sensor 3, Case 2":
                Log.d("Tag","Inside case 3\n");
//
//                Log.d("Tag","Before calling change fragment\n");
//                ((MainActivity)getActivity()).changeFragment("resultScreen");
//                Log.d("Tag","After calling change fragment\n");
                if(completionRatio == 100) {
                    values1 = new ArrayList<Entry>();
                    values2 = new ArrayList<Entry>();
                    values3 = new ArrayList<Entry>();

                    graphData = ((MainActivity) getActivity()).getMeasurements();
                    int ind1 = graphData.size();
                    for (ReducedMeasurement graphDatum : graphData) {
                        ind1+=1;
                        ind+=1;
                        if(graphDatum.isSodiumValid()){
                            values1.add(new Entry(
//                                    graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                                    ind1,
                                    (float)graphDatum.getSodiumVal() +ind
                            ));
                        }
                        if(graphDatum.isPhValid()){
                            values2.add(new Entry(
//                                    graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                                    ind1,
                                    (float)graphDatum.getPhVal() + ind
                            ));
                        }
                        if(graphDatum.isTemperatureValid()){
                            values3.add(new Entry(
//                                    graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                                    ind1,
                                    (float)graphDatum.getTemperatureVal() +ind
                            ));
                        }

                    }
                    while(values1.size() > 10)
                        values1.remove(0);
                    while(values2.size() > 10)
                        values2.remove(0);
                    while (values3.size() > 10)
                        values3.remove(0);

                    Log.d("Tag","Values1 are \n");
                    for(Entry e:values1){
                        Log.d("Tag","value1_val = "+e.toString());
                    }
                    Log.d("Tag","Finished adding new data \n");
                    notify_chart_changed(graph1);
                    notify_chart_changed(graph2);
                    notify_chart_changed(graph3);
                    plot();
                    Log.d("Tag","Finished plotting first batch\n");
//                    run_delay(100000000);
//                    ((MainActivity) getActivity()).changeFragment("loadingScreen");
//                    (new ResultScreen()).updateGui();
                    ((MainActivity)getActivity()).readSensors();

                }
                break;
        }


//        Log.d("Tag","index = "+ind+"\n");
//        plot();
//        Log.d("Tag","Before fragment refresh\n");
//        refresh_fragment();
//        Log.d("Tag","After fragment refresh\n");
//        ((MainActivity)getActivity()).clearMeasurements();
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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