package com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
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
import android.widget.ListView;
import android.widget.Toast;

import com.xsensio.nfcsensorcomm.Utils;
import com.xsensio.nfcsensorcomm.files.FileManagerActivity;
import com.xsensio.nfcsensorcomm.mainactivity.CommContract;
import com.xsensio.nfcsensorcomm.calibration.CalibrationActivity;
import com.xsensio.nfcsensorcomm.model.MemoryBlock;
import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Heiniger on 07.07.17.
 */

public class PhoneTagCommFragment extends Fragment implements PhoneTagCommContract.View {

    private static final String TAG = "PhoneTagCommFragment";

    private NfcTagContentAdapter mAdapter;

    private List<MemoryBlock> mNfcTagContentRows;

    private PhoneTagCommContract.Presenter mPresenter;

    private EditText mNdeftextEdt;

    private Tag mTag = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_phone_tag_comm, container, false);

        // 127 is 7F in hexademical which is the address of last memory block
        final int numElements = 127;
        mNfcTagContentRows = new ArrayList<MemoryBlock>();
        for (int i = 0; i <= numElements; i++) {
            String blockAddressAsString = Utils.intToHexString(i);
            mNfcTagContentRows.add(new MemoryBlock(blockAddressAsString, ""));
        }

        mAdapter = new NfcTagContentAdapter(getActivity(), mNfcTagContentRows, mPresenter);

        final ListView tagMemoryContentListview = (ListView) view.findViewById(R.id.listview);
        tagMemoryContentListview.setAdapter(mAdapter);

        mNdeftextEdt = (EditText) view.findViewById(R.id.ndef_text_edt);

        Button readNdefButton = (Button) view.findViewById(R.id.read_ndef_btn);
        readNdefButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                mPresenter.readNdefTag();
            }
        });

        Button writeNdefButton = (Button) view.findViewById(R.id.write_ndef_btn);
        writeNdefButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                mPresenter.writeNdefMessage(mNdeftextEdt.getText().toString());
            }
        });

        Button readAllButton = (Button) view.findViewById(R.id.read_all_btn);
        readAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.readAllMemoryBlocks();
            }
        });

        Button resetTagButton = (Button) view.findViewById(R.id.reset_tag_btn);
        resetTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.dialog_reset_tag));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    mPresenter.resetNfcTag();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Nothing to do
                    }
                });

                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        Button tagMemoryHelpButton = (Button) view.findViewById(R.id.tag_memory_help_btn);
        tagMemoryHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage(
                                "Each line is a block. The two hexadecimal digits on the left represent the memory address.\n" +
                                "Block content is represented by 4 space-separated hexadecimal string-formatted bytes.\n" +
                                "\"Read\" and \"Write\" buttons can be used to read / write each block independetly.\n" +
                                "\"Read all\" read the whole tag memory in one shot.\n" +
                                "\"Reset Tag\" writes a known and stable tag memory content. " +
                                "WARNING: Tag reset might fail depending on the tag current configuration. " +
                                "In any case, it should not make the configuration less stable than before.")
                        .setTitle("Help - Read / Write tag memory content")
                        .setPositiveButton("Ok",null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        getActivity().invalidateOptionsMenu();

        return view;
    }

    @Override
    public void setPresenter(CommContract.Presenter presenter) {
        mPresenter = (PhoneTagCommContract.Presenter) presenter;
    }

    @Override
    public void updateMemoryBlock(MemoryBlock memoryBlock) {
        // Update memory block display
        mNfcTagContentRows.set((int) memoryBlock.getAddressAsByte(), memoryBlock);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateNdefMessage(String message) {
        mNdeftextEdt.setText(message);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public void onResume() {
        super.onResume();

        if (mPresenter != null) {
            getActivity().registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(PhoneTagCommIntentService.ACTION_READ_MEMORY_BLOCK));
            getActivity().registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(PhoneTagCommIntentService.ACTION_READ_ALL_MEMORY_BLOCKS));
            getActivity().registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(PhoneTagCommIntentService.ACTION_WRITE_MEMORY_BLOCK));
            getActivity().registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(PhoneTagCommIntentService.ACTION_RESET_TAG));
            getActivity().registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(PhoneTagCommIntentService.ACTION_READ_NDEF_TAG));
            getActivity().registerReceiver((BroadcastReceiver) mPresenter, new IntentFilter(PhoneTagCommIntentService.ACTION_WRITE_NDEF_TAG));
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
        inflater.inflate(R.menu.menu_phonetagcomm, menu);
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