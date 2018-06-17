package com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xsensio.nfcsensorcomm.files.FileManagerActivity;
import com.xsensio.nfcsensorcomm.mainactivity.CommContract;
import com.xsensio.nfcsensorcomm.R;

import com.xsensio.nfcsensorcomm.calibration.CalibrationActivity;
import com.xsensio.nfcsensorcomm.model.MemoryBlock;
import com.xsensio.nfcsensorcomm.model.NfcTagConfiguration;
import com.xsensio.nfcsensorcomm.settings.SettingsActivity;

import java.util.List;
import java.util.TreeMap;


public class NfcTagConfigurationFragment extends Fragment implements NfcTagConfigurationContract.View {

    public static final String TAG = "NfcTagConfigFragment";

    private NfcTagConfigurationContract.Presenter mPresenter;

    private Spinner mPowerModeSp;
    TreeMap<Byte, ConfigSpinnerRow> mPowerModeMap;

    private Spinner mVoltageSp;
    TreeMap<Byte, ConfigSpinnerRow> mVoltageMap;

    private Spinner mOutputResistanceSp;
    TreeMap<Byte, ConfigSpinnerRow> mOutputResistanceMap;

    private RadioButton mExtendedModeEnabledRadio;
    private RadioButton mExtendedModeDisabledRadio;

    private TextView mConfigBlock1Tv;
    private TextView mConfigBlock2Tv;
    private TextView mConfigBlock3Tv;
    private TextView mConfigBlock4Tv;

    private ArrayAdapter powerModeAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_nfc_tag_configuration, container, false);

        // Power mode spinner
        mPowerModeSp = (Spinner) view.findViewById(R.id.power_mode_sp);
        mPowerModeMap = initializePowerModeMap();
        powerModeAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mPowerModeMap.values().toArray());
        powerModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPowerModeSp.setAdapter(powerModeAdapter);

        // Voltage spinner
        mVoltageSp = (Spinner) view.findViewById(R.id.voltage_sp);
        mVoltageMap = initializeVoltageMap();
        ArrayAdapter voltageAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mVoltageMap.values().toArray());
        voltageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mVoltageSp.setAdapter(voltageAdapter);

        // Output resistance spinner
        mOutputResistanceSp = (Spinner) view.findViewById(R.id.output_resistance_sp);
        mOutputResistanceMap = initializeOutputResistanceMap();
        ArrayAdapter outputResistanceAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mOutputResistanceMap.values().toArray());
        outputResistanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mOutputResistanceSp.setAdapter(outputResistanceAdapter);

        // Extended mode radio buttons
        mExtendedModeEnabledRadio = (RadioButton) view.findViewById(R.id.extended_mode_enabled_radio);
        mExtendedModeDisabledRadio = (RadioButton) view.findViewById(R.id.extended_mode_disabled_radio);

        // Configuration-block textviews
        mConfigBlock1Tv = (TextView) view.findViewById(R.id.config_block1_tv);
        mConfigBlock2Tv = (TextView) view.findViewById(R.id.config_block2_tv);
        mConfigBlock3Tv = (TextView) view.findViewById(R.id.config_block3_tv);
        mConfigBlock4Tv = (TextView) view.findViewById(R.id.config_block4_tv);

        // Update menu
        getActivity().invalidateOptionsMenu();

        return view;
    }

    @Override
    public void setTest(String bla) {
        mPowerModeSp.getSelectedItem();
    }

    @Override
    public void setExtendedMode(boolean isEnabled) {
        if (isEnabled) {
            mExtendedModeEnabledRadio.setChecked(true);
        } else {
            mExtendedModeDisabledRadio.setChecked(true);
        }
    }

    @Override
    public void setPowerMode(byte powerMode) {
        ConfigSpinnerRow row = mPowerModeMap.get(Byte.valueOf(powerMode));
        mPowerModeSp.setSelection(row.getPosition());

    }

    @Override
    public void setVoltage(byte voltage) {
        ConfigSpinnerRow row = mVoltageMap.get(Byte.valueOf(voltage));
        mVoltageSp.setSelection(row.getPosition());
    }

    @Override
    public void setOutputResistance(byte outputResistance) {

        ConfigSpinnerRow row = mOutputResistanceMap.get(Byte.valueOf(outputResistance));
        mOutputResistanceSp.setSelection(row.getPosition());
    }

    @Override
    public void setMemoryBlockConfiguration(List<MemoryBlock> blocks) {

        MemoryBlock block7c = blocks.get(NfcTagConfiguration.POS_BLOCK_7C);
        mConfigBlock1Tv.setText("Block " + block7c.getAddress() + ": " + block7c.getContent());

        MemoryBlock block7d = blocks.get(NfcTagConfiguration.POS_BLOCK_7D);
        mConfigBlock2Tv.setText("Block " + block7d.getAddress() + ": " + block7d.getContent());

        MemoryBlock block7e = blocks.get(NfcTagConfiguration.POS_BLOCK_7E);
        mConfigBlock3Tv.setText("Block " + block7e.getAddress() + ": " + block7e.getContent());

        MemoryBlock block7f = blocks.get(NfcTagConfiguration.POS_BLOCK_7F);
        mConfigBlock4Tv.setText("Block " + block7f.getAddress() + ": " + block7f.getContent());
    }

    @Override
    public void setPresenter(@NonNull CommContract.Presenter presenter) {
        mPresenter = (NfcTagConfigurationContract.Presenter) presenter;
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public void onResume() {
        super.onResume();

        if (mPresenter != null) {
            getActivity().registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(NfcTagConfigurationIntentService.ACTION_READ_TAG_CONFIGURATION));
            getActivity().registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(NfcTagConfigurationIntentService.ACTION_WRITE_TAG_CONFIGURATION));
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
        inflater.inflate(R.menu.menu_nfc_tag_configuration, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.action_read_configuration:
                mPresenter.readTagConfiguration();
                return true;
            case R.id.action_write_configuration:

                // ---- Read the configuration from the GUI ----

                ConfigSpinnerRow row = (ConfigSpinnerRow) mPowerModeSp.getSelectedItem();
                byte powerMode = row.mByteValue;

                row = (ConfigSpinnerRow) mVoltageSp.getSelectedItem();
                byte voltage = row.mByteValue;

                row = (ConfigSpinnerRow) mOutputResistanceSp.getSelectedItem();
                byte outputResistance = row.mByteValue;

                byte extendedMode;
                if (mExtendedModeEnabledRadio.isChecked()) {
                    extendedMode = (byte) 0x01;
                } else {
                    extendedMode = (byte) 0x00;
                }

                // Write the configuration
                mPresenter.writeTagConfiguration(powerMode, voltage, outputResistance, extendedMode);

                return true;
            case R.id.action_calibration:
                intent = new Intent(getActivity(), CalibrationActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(getActivity(), SettingsActivity.class);
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

    private class ConfigSpinnerRow {

        private int mPosition;
        private String mDisplayValue;
        private byte mByteValue;

        public ConfigSpinnerRow(int position, String displayValue, byte byteValue) {
            mPosition = position;
            mDisplayValue = displayValue;
            mByteValue = byteValue;
        }

        public String toString() {
            return mDisplayValue;
        }

        public byte getByteValue() {
            return mByteValue;
        }

        public int getPosition() {
            return mPosition;
        }
    }

    private TreeMap<Byte, ConfigSpinnerRow> initializePowerModeMap() {
        TreeMap<Byte, ConfigSpinnerRow> map = new TreeMap<>();

        map.put((byte) 0x00, new ConfigSpinnerRow(0, "0", (byte) 0x00));
        map.put((byte) 0x01, new ConfigSpinnerRow(1, "1", (byte) 0x01));
        map.put((byte) 0x02, new ConfigSpinnerRow(2, "2", (byte) 0x02));
        map.put((byte) 0x03, new ConfigSpinnerRow(3, "3", (byte) 0x03));

        return map;
    }

    private TreeMap<Byte, ConfigSpinnerRow> initializeVoltageMap() {
        TreeMap<Byte, ConfigSpinnerRow> map = new TreeMap<>();

        for (int i = 0; i <= 27; i++) {
            double voltage = 1.8 + i*0.1;
            String voltageAsStr = String.format("%.3f", voltage)+ " V";
            map.put((byte) i, new ConfigSpinnerRow(i, voltageAsStr, (byte) i));
        }

        return map;
    }

    private TreeMap<Byte, ConfigSpinnerRow> initializeOutputResistanceMap() {
        TreeMap<Byte, ConfigSpinnerRow> map = new TreeMap<>();

        map.put((byte) 0x00, new ConfigSpinnerRow(0, "Disabled", (byte) 0x00));
        map.put((byte) 0x01, new ConfigSpinnerRow(1, "100 Ohms", (byte) 0x01));
        map.put((byte) 0x02, new ConfigSpinnerRow(2, "50 Ohms", (byte) 0x02));
        map.put((byte) 0x03, new ConfigSpinnerRow(3, "25 Ohms", (byte) 0x03));

        return map;
    }
}