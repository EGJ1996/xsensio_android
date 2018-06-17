package com.xsensio.nfcsensorcomm.calibration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.xsensio.nfcsensorcomm.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * DialogFragment displayed on creation of a new calibration profile
 */
public class NewCalibrationProfileDialogFragment extends DialogFragment {

    private static final String EXTRA_EXISTING_PROFILES_NAMES = "com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm.extra.EXISTING_PROFILES_NAMES";

    private List<String> mExistingProfilesNames;

    public NewCalibrationProfileDialogFragment() {
        // Empty constructor is required for DialogFragment
    }

    public static NewCalibrationProfileDialogFragment newInstance(String title, List<String> existingProfilesNames) {

        NewCalibrationProfileDialogFragment frag = new NewCalibrationProfileDialogFragment();


        ArrayList<String> profileNames = new ArrayList<>(existingProfilesNames);

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putStringArrayList(EXTRA_EXISTING_PROFILES_NAMES, profileNames);
        frag.setArguments(args);
        return frag;
    }

    public interface NewCalibrationProfileDialogListener {
        void onDialogPositiveClick(String profileName);
    }

    // Use this instance of the interface to deliver action events
    private NewCalibrationProfileDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Verify that the host activity implements the callback interface
        try {
            mListener = (NewCalibrationProfileDialogListener) getActivity();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExistingProfilesNames = getArguments().getStringArrayList(EXTRA_EXISTING_PROFILES_NAMES);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.fragment_new_calibration_profile_dialog, null);

        final EditText profileNameEdt = (EditText) view.findViewById(R.id.calibration_profile_name_edt);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
            // Add action buttons
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Nothing here but still needed to instantiate the button
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Nothing to do
                }
            });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog) getDialog();

        if(d != null) {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final EditText profileNameEdt = (EditText) d.findViewById(R.id.calibration_profile_name_edt);
                    String profileName = profileNameEdt.getText().toString().trim();

                    // The profile name cannot be empty
                    if(!"".equals(profileName)) {

                        // The profile name cannot exist already (since it is used as filename)
                        if (!mExistingProfilesNames.contains(profileName)) {
                            mListener.onDialogPositiveClick(profileName);
                            dismiss();
                        } else {
                            Toast.makeText(getActivity(), "The profile name exists already ;-)", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "The profile name cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}