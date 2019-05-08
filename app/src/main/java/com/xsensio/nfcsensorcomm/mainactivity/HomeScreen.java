package com.xsensio.nfcsensorcomm.mainactivity;

import android.content.Context;
import android.content.Intent;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.api.view.ButtonModeView;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.calibration.CalibrationActivity;
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
    private ImageView phone;

    public HomeScreen() {
        // Required empty public constructor
    }

    public static HomeScreen newInstance() {
        HomeScreen fragment = new HomeScreen();
        return fragment;
    }

    private Button mReadSensorsButton;

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
        Button calibrate = view.findViewById(R.id.hm_calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CalibrationActivity.class);
                startActivity(intent);
            }
        });
        Button history = view.findViewById(R.id.hm_history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).changeFragment("historyScreen");
            }
        });
        mReadSensorsButton=view.findViewById(R.id.hm_readSensors);
        mReadSensorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).readSensors();
            }
        });

        phone=view.findViewById(R.id.hs_phone);
        Animation animation= AnimationUtils.loadAnimation(getActivity(),R.anim.home_screen_phone_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                phone.startAnimation(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        phone.startAnimation(animation);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
