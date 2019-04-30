package com.xsensio.nfcsensorcomm.mainactivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.xsensio.nfcsensorcomm.R;

public class LoadingScreen extends Fragment {

    private OnFragmentInteractionListener mListener;

    public LoadingScreen() {
        // Required empty public constructor
    }

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading_screen, container, false);
        progressBar=view.findViewById(R.id.ls_progress_bar);
        progressBar.setProgress(0);
        return view;
    }

    public void updateReadSensorProgress(String taskDescription, int completionRatio) {
        switch (taskDescription) {
            case "Receiving data for Sensor 1, Case 2":
                progressBar.setProgress(completionRatio);
                break;
            case "Receiving data for Sensor 2, Case 2":
                progressBar.setProgress(completionRatio);
                break;
            case "Receiving data for Sensor 3, Case 2":
                progressBar.setProgress(completionRatio);
                break;
        }
        if(completionRatio==1){
            ((MainActivity)getActivity()).changeFragment("resultScreen");
        }
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
