package com.xsensio.nfcsensorcomm.mainactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.SensorCommContract;
import com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.VirtualSensorAdapter;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase2;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class HomeScreen extends Fragment{

    private OnFragmentInteractionListener mListener;

    public HomeScreen() {
        // Required empty public constructor
    }

    public static HomeScreen newInstance() {
        HomeScreen fragment = new HomeScreen();
        return fragment;
    }

    private Button mReadSensorsButton;
    private EditText[] sensorVals=new EditText[3];
    private ProgressBar[] sensorProgs=new ProgressBar[3];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home_screen, container, false);
        ImageView toMore=(ImageView)view.findViewById(R.id.hm_more);
        toMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideHomeScreen();
            }
        });
        mReadSensorsButton=(Button)view.findViewById(R.id.hm_read);
        mReadSensorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSensorResult(new ArrayList<VirtualSensor>());

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                int numSamplesReadoutsCase1 = Integer.valueOf(settings.getString("num_samples_roc1", getString(R.string.num_samples_roc1_def_val)));
                int numSamplesReadoutsCase2 = Integer.valueOf(settings.getString("num_samples_roc2", getString(R.string.num_samples_roc2_def_val)));
                int numSamplesReadoutsCase3 = Integer.valueOf(settings.getString("num_samples_roc3", getString(R.string.num_samples_roc3_def_val)));
                int sensorSelect = Integer.valueOf(settings.getString("sensor_select","10"));
                int sampleRate = Integer.valueOf(settings.getString("sampling_frequency","5"));

                PhoneMcuCommand command = new PhoneMcuCommand(
                        getActivity().getApplicationContext(),
                        true,
                        true,
                        true,
                        false,
                        numSamplesReadoutsCase1,
                        numSamplesReadoutsCase2,
                        numSamplesReadoutsCase3,
                        sensorSelect,
                        sampleRate
                );

                ((MainActivity)getActivity()).sensorCommFragment.mPresenter.readSensors(command);
                mReadSensorsButton.setEnabled(false);
            }
        });

        sensorVals[0]=(EditText)view.findViewById(R.id.hm_sensor1_val);
        sensorVals[1]=(EditText)view.findViewById(R.id.hm_sensor2_val);
        sensorVals[2]=(EditText)view.findViewById(R.id.hm_sensor3_val);

        sensorProgs[0]=(ProgressBar)view.findViewById(R.id.hm_sensor1_prog);
        sensorProgs[1]=(ProgressBar)view.findViewById(R.id.hm_sensor2_prog);
        sensorProgs[2]=(ProgressBar)view.findViewById(R.id.hm_sensor3_prog);

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

    public void updateSensorResult(List<VirtualSensor> sensorResults) {
        for (int i = 0; i < sensorResults.size(); i++) {
            VirtualSensorCase2 sensor=(VirtualSensorCase2) sensorResults.get(i);
            VirtualSensorCase2.DataContainer data=sensor.getDataContainer(getContext(),null);
            NumberFormat formatter = new DecimalFormat("0.###E0");
            String average = formatter.format(data.getAverageDerivative());
            average=average.replace("E","*10^");
            sensorVals[i].setText(average);
        }
        for (int i=sensorResults.size();i<3;i++){
            sensorVals[i].setText(Double.toString(0));
        }
    }


    public void updateReadSensorProgress(String taskDescription, int completionRatio) {
        switch (taskDescription) {
            case "Receiving data for Sensor 1, Case 2":
                sensorProgs[0].setProgress(completionRatio);
                break;
            case "Receiving data for Sensor 2, Case 2":
                sensorProgs[1].setProgress(completionRatio);
                break;
            case "Receiving data for Sensor 3, Case 2":
                sensorProgs[2].setProgress(completionRatio);
                break;
        }
    }

    public void setReadSensorsButtonEnabled(boolean enabled) {
        mReadSensorsButton.setEnabled(enabled);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
