package com.xsensio.nfcsensorcomm.mainactivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Button;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.ReducedMeasurement;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DataHistoryScreen extends Fragment {

    private OnFragmentInteractionListener mListener;

    private Button backButton;
    private ArrayList<ReducedMeasurement> graphData;
    private LineChart graph1;
    private LineChart graph2;
    private LineChart graph3;

    public DataHistoryScreen() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_data_history_screen, container, false);
        backButton=view.findViewById(R.id.hs_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        graphData=((MainActivity)getActivity()).getMeasurements();

        graph1=view.findViewById(R.id.hs_graph1);
        graph2=view.findViewById(R.id.hs_graph2);
        graph3=view.findViewById(R.id.hs_graph3);

        initGraph(graph1);
        initGraph(graph2);
        initGraph(graph3);
        plot();

        return view;
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
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new ValueFormatter() {

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

    public void plot(){
        ArrayList<Entry> values1 = new ArrayList<>();
        ArrayList<Entry> values2 = new ArrayList<>();
        ArrayList<Entry> values3 = new ArrayList<>();
        for (ReducedMeasurement graphDatum : graphData) {
            values1.add(new Entry(
                    graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                    (float)graphDatum.getSodiumVal()
            ));
            values2.add(new Entry(
                    graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                    (float)graphDatum.getPhVal()
            ));
            values3.add(new Entry(
                    graphDatum.getDateTime().toEpochSecond(ZoneOffset.UTC),
                    (float)graphDatum.getTemperatureVal()
            ));
        }
        plotLine(graph1,values1);
        plotLine(graph2,values2);
        plotLine(graph3,values3);
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
        graph.getLegend().setEnabled(false);
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
