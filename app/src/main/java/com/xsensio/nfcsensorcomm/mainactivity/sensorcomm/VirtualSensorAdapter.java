package com.xsensio.nfcsensorcomm.mainactivity.sensorcomm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.virtualsensor.VirtualSensor;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Michael Heiniger on 19.07.17.
 */

public class VirtualSensorAdapter extends ArrayAdapter<VirtualSensor> {

    private final Context mContext;
    private final List<VirtualSensor> mValues;
    private final SensorCommContract.View mView;


    public VirtualSensorAdapter(Context context, List<VirtualSensor> values, SensorCommContract.View view) {
        super(context, -1, values.toArray(new VirtualSensor[values.size()]));

        mContext = context;
        if (values != null) {
            mValues = new ArrayList<VirtualSensor>(values);
        } else {
            mValues = new ArrayList<VirtualSensor>();
        }
        mView = view;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.sensor_result_row, parent, false);

        final VirtualSensor virtualSensor = mValues.get(position);

        TextView sensorNameTv = (TextView) rowView.findViewById(R.id.sensor_name_tv);

        double durationInSeconds = (double) virtualSensor.getReadDataDuration() / (double) 1000;
        NumberFormat formatter = new DecimalFormat("#0.00");
        String durationAsString = formatter.format(durationInSeconds);

        String displayedText = virtualSensor.getVirtualSensorDefinition().toUserFriendlyString() + " (" + durationAsString + " s)";
        sensorNameTv.setText(displayedText);

        return rowView;
    }
}
