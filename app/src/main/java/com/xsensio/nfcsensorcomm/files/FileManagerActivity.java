package com.xsensio.nfcsensorcomm.files;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.calibration.CalibrationProfileManager;
import com.xsensio.nfcsensorcomm.model.CalibrationProfile;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase1;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase2;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase3;
import com.xsensio.nfcsensorcomm.sensorresult.case1.VirtualSensorResultCase1Presenter;
import com.xsensio.nfcsensorcomm.sensorresult.case2.VirtualSensorResultCase2Presenter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FileManagerAdapter mAdapter;
    private EditText saveFileName;
    private FileSensorsBuffer buffer;
    private Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        }

        Bundle b=this.getIntent().getExtras();
        ArrayList<VirtualSensor> tmp=b.getParcelableArrayList("sensors");
        buffer=new FileSensorsBuffer(tmp);

        recyclerView = (RecyclerView) findViewById(R.id.fileRecyclerView);

        mAdapter = new FileManagerAdapter(this,fileList());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        saveFileName=(EditText) findViewById(R.id.file_savename);
        Button Save=(Button) findViewById(R.id.file_save);
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buffer.isEmpty()){
                    toast=Toast.makeText(getApplicationContext(),"Nothing to Save",Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    String name=saveFileName.getText().toString();
                    if(name==""){ name="New File";}
                    try {
                        FileOutputStream fos = openFileOutput(name, Context.MODE_PRIVATE);
                        ObjectOutputStream os = new ObjectOutputStream(fos);
                        os.writeObject(buffer);
                        os.close();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mAdapter.filenames=fileList();
                    mAdapter.notifyDataSetChanged();
                    externalSaver(name);
                }
            }
        });
    }

    private class ExternalBuffer{
        ArrayList<VirtualSensorCase1.DataContainer> data1=new ArrayList<>();
        ArrayList<VirtualSensorCase2.DataContainer> data2=new ArrayList<>();
        ArrayList<VirtualSensorCase3.DataContainer> data3=new ArrayList<>();
    }

    private void externalSaver(String fileName){
        ExternalBuffer externalBuffer=new ExternalBuffer();
        if(!buffer.isEmpty()){
            ArrayList<VirtualSensor> sensors=buffer.virtualSensors;
            for (VirtualSensor sensor : sensors) {
                if(sensor instanceof VirtualSensorCase1){
                    externalBuffer.data1.add(((VirtualSensorCase1)sensor).getDataContainer(this,null));
                } else if(sensor instanceof VirtualSensorCase2){
                    externalBuffer.data2.add(((VirtualSensorCase2)sensor).getDataContainer(this,null));
                } else if(sensor instanceof VirtualSensorCase3){
                    externalBuffer.data3.add(((VirtualSensorCase3)sensor).getDataContainer(this,null));
                }
            }
        }
        //SAVING to EXTERNAL
        File external= Environment.getExternalStorageDirectory();
        File myDir=new File(external,"/xsensio");
        if(!myDir.exists()){
            myDir.mkdir(); }
        File file=new File(myDir,fileName);
        Gson gson=new Gson();
        String object=gson.toJson(externalBuffer).toString();
        try {
            FileOutputStream fos =  new FileOutputStream(file);
            fos.write(object.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
