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


import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.LineChartView;
import com.db.chart.animation.Animation;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.ReducedMeasurement;

import java.util.ArrayList;
import java.util.List;

public class DataHistoryScreen extends Fragment {

    private OnFragmentInteractionListener mListener;

    private Button backButton;
    private ArrayList<ReducedMeasurement> graphData;
    private LineChartView graph1;
    private LineChartView graph2;
    private LineChartView graph3;

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

        plot();

        return view;
    }

    public void plot(){
        String[] xlabels=new String[graphData.size()];
        float[] graph1Vals=new float[graphData.size()];
        float[] graph2Vals=new float[graphData.size()];
        float[] graph3Vals=new float[graphData.size()];
        for (int i = 0; i < graphData.size(); i++) {
            xlabels[i]=graphData.get(i).getDateTime().toLocalDate().toString().substring(5);
            graph1Vals[i]= (float) graphData.get(i).getSodiumVal();
            graph2Vals[i]= (float) graphData.get(i).getPhVal();
            graph3Vals[i]= (float) graphData.get(i).getTemperatureVal();
        }
        plotLine(graph1,xlabels,graph1Vals);
        plotLine(graph2,xlabels,graph2Vals);
        plotLine(graph3,xlabels,graph3Vals);
    }

    private void tooltip(){

    }

    private void plotLine(LineChartView graph, String[] xlabels, float[] yvalues){
        LineSet dataset = new LineSet(xlabels, yvalues);
        dataset.setColor(Color.parseColor("#ffc755"))
                .setFill(Color.parseColor("#ffdd99"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setThickness(2);
        graph.addData(dataset);

        float min=Float.MAX_VALUE;
        float max=Float.MIN_VALUE;
        for (float yvalue : yvalues) {
            min= (min > yvalue) ? yvalue:min;
            max= (max < yvalue) ? yvalue:max;
        }
        Paint paint=new Paint();
        paint.setColor(Color.parseColor("#ffdd99"));
        graph.setBackgroundColor(Color.parseColor("#ffffff"));
        graph.setAxisBorderValues(min, max)
                .setGrid(3,5,paint)
                .setYLabels(AxisRenderer.LabelPosition.OUTSIDE)
                .setAxisColor(Color.parseColor("#888888"))
                .show(new Animation().setInterpolator(new BounceInterpolator())
                        .fromAlpha(0));
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
