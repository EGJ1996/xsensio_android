package com.xsensio.nfcsensorcomm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.xsensio.nfcsensorcomm.calibration.CalibrationProfileManager;
import com.xsensio.nfcsensorcomm.mainactivity.MainActivity;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinition;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorDefinitionCase2;

import java.io.IOException;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainActivityIntent=new Intent(WelcomeActivity.this,MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        },1000);
        createDefaultCalibrationProfile();
    }

    public void createDefaultCalibrationProfile(){
        CalibrationProfile sensor1=CalibrationProfile.createCalibrationProfile(
                "sensor1-default",
                VirtualSensorDefinitionCase2.SENSOR_1,
                "0 1 5 25",
                "0 0.000001 0.00001 0.0001"
        );
        CalibrationProfile sensor2=CalibrationProfile.createCalibrationProfile(
                "sensor2-default",
                VirtualSensorDefinitionCase2.SENSOR_2,
                "0 1 5 25",
                "0 0.001 0.01 0.1"
        );
        try {
            CalibrationProfileManager.saveCalibrationProfileInFile(this,sensor1);
            CalibrationProfileManager.saveCalibrationProfileInFile(this,sensor2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}
