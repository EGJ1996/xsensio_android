package com.xsensio.nfcsensorcomm.mainactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.calibration.CalibrationProfileManager;
import com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.SensorCommContract;
import com.xsensio.nfcsensorcomm.mainactivity.sensorcomm.VirtualSensorAdapter;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.PhoneMcuCommand;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase2;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

//Its just code to handle HomeScreen UI.
//For the NFC communication part, we are using SensorCommFragment functions to tunnel the data into here.
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
    private boolean[] sensorAccess={true,true,true};
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
        Button toMore=view.findViewById(R.id.hm_oldInterface);
        toMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideHomeScreen();
            }
        });
        mReadSensorsButton=view.findViewById(R.id.hm_readSensors);
        mReadSensorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<3;i++){
                    sensorProgs[i].setProgress(0);
                }
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                int numSamplesReadoutsCase1 = Integer.valueOf(settings.getString("num_samples_roc1", getString(R.string.num_samples_roc1_def_val)));
                int numSamplesReadoutsCase2 = Integer.valueOf(settings.getString("num_samples_roc2", getString(R.string.num_samples_roc2_def_val)));
                int numSamplesReadoutsCase3 = Integer.valueOf(settings.getString("num_samples_roc3", getString(R.string.num_samples_roc3_def_val)));
                int sensorSelect = Integer.valueOf(settings.getString("sensor_select","10"));
                int sampleRate = Integer.valueOf(settings.getString("sampling_frequency","5"));

                PhoneMcuCommand command = new PhoneMcuCommand(
                        getActivity().getApplicationContext(),
                        sensorAccess[0],
                        sensorAccess[1],
                        sensorAccess[2],
                        false,
                        numSamplesReadoutsCase1,
                        numSamplesReadoutsCase2,
                        numSamplesReadoutsCase3,
                        sensorSelect,
                        sampleRate
                );

                ((MainActivity)getActivity()).sensorCommFragment.mPresenter.readSensors(command);
                ((MainActivity)getActivity()).changeFragment("loadingScreen");
            }
        });
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
