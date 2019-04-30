package com.xsensio.nfcsensorcomm.mainactivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.ReducedMeasurement;

import java.util.ArrayList;
import java.util.List;

public class DataHistoryScreen extends Fragment {

    private OnFragmentInteractionListener mListener;

    private Button backButton;
    private ArrayList<ReducedMeasurement> graphData;

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
        
        AnyChartView chartView=view.findViewById(R.id.hs_chart);
        List<DataEntry> data=new ArrayList<>();
        for (ReducedMeasurement graphDatum : graphData) {
            //data.add()
        }

        return view;
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
