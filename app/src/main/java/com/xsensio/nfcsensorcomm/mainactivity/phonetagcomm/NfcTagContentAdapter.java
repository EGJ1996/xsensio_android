package com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xsensio.nfcsensorcomm.R;
import com.xsensio.nfcsensorcomm.model.MemoryBlock;

import java.util.List;

/**
 * Adapter used to display MemoryBlock instance in a ListView
 *
 * Created by Michael Heiniger on 12.07.17.
 */

public class NfcTagContentAdapter extends ArrayAdapter<MemoryBlock> {

    private final Context mContext;

    /**
     * MemoryBlock instances to be displayed
     */
    private final List<MemoryBlock> mValues;


    private final PhoneTagCommContract.Presenter mPresenter;

    public NfcTagContentAdapter(Context context, List<MemoryBlock> values, PhoneTagCommContract.Presenter presenter) {
        super(context, -1, values.toArray(new MemoryBlock[values.size()]));

        mContext = context;
        mValues = values;
        mPresenter = presenter;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.nfc_tag_content_row, parent, false);

        // Update UI with MemoryBlock content and setup Callbacks for user-interaction
        MemoryBlock memoryBlock = mValues.get(position);

        TextView blockAddressTv = (TextView) rowView.findViewById(R.id.block_address_tv);
        blockAddressTv.setText(memoryBlock.getAddress());

        final EditText contentEdt = (EditText) rowView.findViewById(R.id.block_content_edt);
        contentEdt.setHint("00 01 02 03");
        contentEdt.setText(memoryBlock.getContent());

        Button readBtn = (Button) rowView.findViewById(R.id.read_btn);
        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.readMemoryBlock(mValues.get(position).getAddress());
            }
        });

        Button writeBtn = (Button) rowView.findViewById(R.id.write_btn);
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.writeMemoryBlock(mValues.get(position).getAddress(), contentEdt.getText().toString());
            }
        });

        // Disable the WRITE button for read-only blocks:
        if (MemoryBlock.READONLY_BLOCKS.contains(memoryBlock.getAddress())) {
            writeBtn.setEnabled(false);
            writeBtn.setVisibility(View.GONE);
        }

        return rowView;
    }
}
