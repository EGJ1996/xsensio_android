package com.xsensio.nfcsensorcomm.mainactivity.phonemcucomm;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.xsensio.nfcsensorcomm.Utils;
import com.xsensio.nfcsensorcomm.files.FileManagerActivity;
import com.xsensio.nfcsensorcomm.mainactivity.CommContract;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.calibration.CalibrationActivity;
import com.xsensio.nfcsensorcomm.settings.SettingsActivity;

/**
 * Created by Michael Heiniger on 07.07.17.
 */

public class PhoneMcuCommFragment extends Fragment implements PhoneMcuCommContract.View {

    private static final String TAG = "PhoneMCUCommFragment";

    private PhoneMcuCommContract.Presenter mPresenter;

    private TextView mDataFromMcuTv;
    private Switch mExtendedModeSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_phone_mcu_comm, container, false);

        mDataFromMcuTv = (TextView)  getActivity().findViewById(R.id.ext_mode_received_data_tv);

        mExtendedModeSwitch = (Switch) view.findViewById(R.id.ext_mode_swt);
        mExtendedModeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch extendedModeSwitch = (Switch) v;
                boolean newStatus = extendedModeSwitch.isChecked();

                /**
                 *  We set the switch back to its old status. The writing of the tag config
                 *  will trigger a read config that will update the switch status.
                 *  This way, the switch has a correct status even in case of a writing failure
                 */
                extendedModeSwitch.setChecked(!newStatus);

                mPresenter.setExtendedMode(newStatus);
            }
        });

        Button readButton = (Button) view.findViewById(R.id.ext_mode_read_btn);
        readButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                mPresenter.readData();
            }
        });

        Button writeButton = (Button) view.findViewById(R.id.ext_mode_send_btn);
        writeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                String bytesToWrite = ((EditText) getActivity().findViewById(R.id.ext_mode_write_content_edt)).getText().toString();

                if (!"".equals(bytesToWrite)) {
                    // Get the hex characters to write
                    String[] bytesToWriteAsArray = bytesToWrite.split("\\s");

                    // Convert into bytes
                    byte[] data = Utils.stringArraytoByteArray(bytesToWriteAsArray);

                    // Write to MCU
                    mPresenter.writeData(data);
                } else {
                    showToast("Bytes to write cannot be empty !");
                }
            }
        });

        getActivity().invalidateOptionsMenu();

        return view;
    }

    @Override
    public void setPresenter(CommContract.Presenter presenter) {
        mPresenter = (PhoneMcuCommContract.Presenter) presenter;
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateExtendedMode(boolean isEnabled) {
        mExtendedModeSwitch.setChecked(isEnabled);
    }

    @Override
    public void setReceivedData(byte[] data) {
        mDataFromMcuTv.setText(Utils.bytesToHexString(data));
    }

    public void onResume() {
        super.onResume();

        if (mPresenter != null) {
            getActivity().registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(PhoneMcuCommIntentService.ACTION_READ));
            getActivity().registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(PhoneMcuCommIntentService.ACTION_WRITE));
        }
    }

    public void onPause() {
        super.onPause();

        if (mPresenter != null) {
            getActivity().unregisterReceiver((BroadcastReceiver) mPresenter);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_phonemcucomm, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_calibration:
                intent = new Intent(getActivity(), CalibrationActivity.class);
                startActivity(intent);

                return true;
            case R.id.action_files:
                intent = new Intent(getActivity(), FileManagerActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}