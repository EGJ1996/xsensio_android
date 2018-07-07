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
    private EditText[] sensorVals=new EditText[3];
    private TextView[] sensorNames=new TextView[3];
    private boolean[] sensorAccess={true,true,true};
    private ProgressBar[] sensorProgs=new ProgressBar[3];
    private List<CalibrationProfile> profile1=new ArrayList<>();
    private List<CalibrationProfile> profile2=new ArrayList<>();
    private Spinner[] spinners=new Spinner[2];

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
        sensorProgs[0]=(ProgressBar)view.findViewById(R.id.hm_sensor1_prog);
        sensorProgs[1]=(ProgressBar)view.findViewById(R.id.hm_sensor2_prog);
        sensorProgs[2]=(ProgressBar)view.findViewById(R.id.hm_sensor3_prog);
        mReadSensorsButton=(Button)view.findViewById(R.id.hm_read);
        mReadSensorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSensorResult(new ArrayList<VirtualSensor>());
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
                mReadSensorsButton.setEnabled(false);
            }
        });

        sensorNames[0]=(TextView)view.findViewById(R.id.hm_sensor1_label);
        sensorNames[1]=(TextView)view.findViewById(R.id.hm_sensor2_label);
        sensorNames[2]=(TextView)view.findViewById(R.id.hm_sensor3_label);

        sensorVals[0]=(EditText)view.findViewById(R.id.hm_sensor1_val);
        sensorVals[1]=(EditText)view.findViewById(R.id.hm_sensor2_val);
        sensorVals[2]=(EditText)view.findViewById(R.id.hm_sensor3_val);
        sensorNames[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorAccess[0]= !sensorAccess[0];
                if(sensorAccess[0]){
                    sensorNames[0].setTypeface(null, Typeface.BOLD);
                } else {
                    sensorNames[0].setTypeface(null, Typeface.NORMAL);
                }
            }
        });
        sensorNames[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorAccess[1]= !sensorAccess[1];
                if(sensorAccess[1]){
                    sensorNames[1].setTypeface(null, Typeface.BOLD);
                } else {
                    sensorNames[1].setTypeface(null, Typeface.NORMAL);
                }
            }
        });
        sensorNames[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorAccess[2]= !sensorAccess[2];
                if(sensorAccess[2]){
                    sensorNames[2].setTypeface(null, Typeface.BOLD);
                } else {
                    sensorNames[2].setTypeface(null, Typeface.NORMAL);
                }
            }
        });

        //Calibration profile spinners
        spinners[0]=(Spinner)view.findViewById(R.id.hm_spinner1);
        spinners[1]=(Spinner)view.findViewById(R.id.hm_spinner2);

        loadProfiles();
        spinners[0].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<profile1.size()){
                    selectedProfiles[0]=profile1.get(position);
                    updateGui();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinners[1].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<profile2.size()){
                    selectedProfiles[1]=profile2.get(position);
                    updateGui();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    //Profile related parts
    CalibrationProfile[] selectedProfiles={null,null};
    private void updateGui(){
        if(sensorResults.size()==0){
            for (int i=sensorResults.size();i<3;i++){
                sensorVals[i].setText(Double.toString(0));
            }
        } else {
            for (int i = 0; i < sensorResults.size(); i++) {
                VirtualSensorCase2 sensor = (VirtualSensorCase2) sensorResults.get(i);
                //Just formatting average values
                VirtualSensorDefinitionCase2 definitionCase2 = (VirtualSensorDefinitionCase2) sensor.getVirtualSensorDefinition();
                int sensorNumber = Integer.valueOf(definitionCase2.getSensorName().replace("Sensor ",""))-1;
                String average="";
                if(sensorNumber==2){
                    VirtualSensorCase2.DataContainer data = sensor.getDataContainer(getContext(), null);
                    average=Double.toString(data.getAverageDerivative());
                } else {
                    VirtualSensorCase2.DataContainer data = sensor.getDataContainer(getContext(), selectedProfiles[sensorNumber]);
                    NumberFormat formatter = new DecimalFormat("###.###");
                    average =  "10^"+formatter.format(data.getAverageMappedData());
                }
                average = average + definitionCase2.getCalibrationPlotMetadata().getYAxisUnitLabel();
                sensorVals[sensorNumber].setText(average);
            }
        }
    }
    private List<CalibrationProfile> getProfiles(VirtualSensorDefinitionCase2 def) {

        List<CalibrationProfile> profiles = new ArrayList<>();

        try {

            // Load from the Settings the path of the folder containing the calibration profiles
            Context context = getActivity().getApplicationContext();
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String profilesFolderPath = settings.getString("calibration_folder_path", context.getString(R.string.calibration_folder_path_def_val));

            // Load all available calibration profiles
            profiles = CalibrationProfileManager.loadCalibrationProfilesFromFiles(profilesFolderPath, def);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return profiles;
    }

    private void loadProfiles(){
        profile1=getProfiles(VirtualSensorDefinitionCase2.SENSOR_1);
        ArrayAdapter mCalibrationProfileAdapter1 = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, profile1.toArray());
        mCalibrationProfileAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinners[0].setAdapter(mCalibrationProfileAdapter1);
        profile2=getProfiles(VirtualSensorDefinitionCase2.SENSOR_2);
        ArrayAdapter mCalibrationProfileAdapter2 = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, profile2.toArray());
        mCalibrationProfileAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinners[1].setAdapter(mCalibrationProfileAdapter2);

        if(profile1.size()>0){
            selectedProfiles[0]=profile1.get(0);
        }
        if(profile2.size()>0){
            selectedProfiles[1]=profile2.get(0);
        }
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
    List<VirtualSensor> sensorResults=new ArrayList<>();
    public void updateSensorResult(List<VirtualSensor> sensorResult) {
        sensorResults=sensorResult;
        updateGui();
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
