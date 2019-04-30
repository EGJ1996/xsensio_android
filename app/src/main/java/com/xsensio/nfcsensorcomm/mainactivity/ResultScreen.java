package com.xsensio.nfcsensorcomm.mainactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.calibration.CalibrationProfileManager;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase2;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultScreen extends Fragment {
    private OnFragmentInteractionListener mListener;

    private TextView phValue;
    private TextView sodiumValue;
    private TextView temperatureValue;

    private Button history;
    private Button newMeasurement;

    public ResultScreen() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result_screen, container, false);
        phValue=view.findViewById(R.id.rs_ph);
        sodiumValue=view.findViewById(R.id.rs_sodium);
        temperatureValue=view.findViewById(R.id.rs_temperature);
        phValue.setText("0");
        sodiumValue.setText("0");
        temperatureValue.setText("0");
        history=view.findViewById(R.id.rs_history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).changeFragment("historyScreen");
            }
        });
        newMeasurement=view.findViewById(R.id.rs_newMeasurement);
        newMeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).changeFragment("homeScreen");
            }
        });
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

    List<VirtualSensor> sensorResults=new ArrayList<>();
    public void updateSensorResult(List<VirtualSensor> sensorResult) {
        sensorResults=sensorResult;
        updateGui();
    }

    private CalibrationProfile getProfile(VirtualSensorDefinitionCase2 def) {

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

        return profiles.get(profiles.size()-1);
    }

    private void updateGui(){
        if(sensorResults.size()==0){
            phValue.setText("0");
            sodiumValue.setText("0");
            temperatureValue.setText("0");
        } else {
            for (int i = 0; i < sensorResults.size(); i++) {
                VirtualSensorCase2 sensor = (VirtualSensorCase2) sensorResults.get(i);
                //Just formatting average values
                VirtualSensorDefinitionCase2 definitionCase2 = (VirtualSensorDefinitionCase2) sensor.getVirtualSensorDefinition();
                int sensorNumber = Integer.valueOf(definitionCase2.getSensorName().replace("Sensor ",""))-1;
                String average="";
                if(sensorNumber==2){
                    //Todo 1: in order to display temperature instead of K+ concentration, 1st step: uncomment these 4 lines, and comment the following 4 lines, by junrui
                    VirtualSensorCase2.DataContainer data = sensor.getDataContainer(getContext(), null);
                    NumberFormat formatter = new DecimalFormat("###.###");
                    average =  formatter.format(data.getAverageDerivative());
                    average+="°C";
//                    VirtualSensorCase2.DataContainer data = sensor.getDataContainer(getContext(), selectedProfiles[sensorNumber]);
//                    NumberFormat formatter = new DecimalFormat("###.###");
//                    average =  formatter.format(Math.pow(10,data.getAverageMappedData())*1000);
//                    average = average + definitionCase2.getMappedDataPlotMetadata().getYAxisUnitLabel();
                    temperatureValue.setText(average);
                } else if(sensorNumber==1){
                    //sensorNumber==1, display as concentration, by junrui
                    CalibrationProfile profile=getProfile(VirtualSensorDefinitionCase2.SENSOR_2);
                    VirtualSensorCase2.DataContainer data = sensor.getDataContainer(getContext(), profile);
                    NumberFormat formatter = new DecimalFormat("###.###");
                    average =  formatter.format(Math.pow(10,data.getAverageMappedData())*1000);
                    average = average + definitionCase2.getMappedDataPlotMetadata().getYAxisUnitLabel();
                    sodiumValue.setText(average);
                } else {
                    //sensorNumber==0, display as pH, by junrui
                    CalibrationProfile profile=getProfile(VirtualSensorDefinitionCase2.SENSOR_1);
                    VirtualSensorCase2.DataContainer data = sensor.getDataContainer(getContext(), profile);
                    NumberFormat formatter = new DecimalFormat("###.###");
                    average =  formatter.format(-data.getAverageMappedData());
                    average = average + definitionCase2.getMappedDataPlotMetadata().getYAxisUnitLabel();
                    temperatureValue.setText(average);
                }
            }
        }
    }
}
