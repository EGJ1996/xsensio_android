package com.xsensio.nfcsensorcomm.files;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensorCase2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class FileManagerAdapter extends RecyclerView.Adapter<FileManagerAdapter.Holder>{

    public String[] filenames;
    private Context context;
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.files_result_row,parent,false);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.name.setText(filenames[position]);
    }

    @Override
    public int getItemCount() {
        return filenames.length;
    }

    public FileManagerAdapter(Context contexz,String[] names){
        context=contexz;
        filenames=names.clone();
    }
    public FileSensorsBuffer buffer;
    public class Holder extends RecyclerView.ViewHolder{
        public TextView name;
        public Button load,delete;
        public Holder(View itemView) {
            super(itemView);
            name=(TextView) itemView.findViewById(R.id.file_name);
            load=(Button) itemView.findViewById(R.id.file_load);
            load.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos=getAdapterPosition();
                    String fileName=filenames[pos];
                    try{
                        FileInputStream fis = context.openFileInput(fileName);
                        ObjectInputStream is = new ObjectInputStream(fis);
                        buffer = (FileSensorsBuffer) is.readObject();
                        is.close();
                        fis.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

//                    //SAVING to EXTERNAL
//                    File external= Environment.getExternalStorageDirectory();
//                    File myDir=new File(external,"/xsensio");
//                    if(!myDir.exists()){ myDir.mkdir(); }
//                    File file=new File(myDir,fileName);
//                    Gson gson=new Gson();
//                    String object=gson.toJson(buffer).toString();
//                    try {
//                        FileOutputStream fos =  new FileOutputStream(file);
//                        fos.write(object.getBytes());
//                        fos.close();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    int resultCode = 69;
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("sensors", buffer.virtualSensors);
                    ((FileManagerActivity)context).setResult(resultCode, resultIntent);
                    ((FileManagerActivity)context).finish();
                }
            });
            delete=(Button) itemView.findViewById(R.id.file_delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fileName=filenames[getAdapterPosition()];
                    File file = new File(context.getFilesDir(), fileName);
                    file.delete();
                    filenames=context.fileList();
                    notifyDataSetChanged();
                }
            });
        }
    }
}
