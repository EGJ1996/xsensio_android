package com.xsensio.nfcsensorcomm.mainactivity.phonetagcomm;

import com.xsensio.nfcsensorcomm.mainactivity.CommContract;
import com.xsensio.nfcsensorcomm.model.MemoryBlock;

/**
 * Created by Michael Heiniger on 12.07.17.
 */

public interface PhoneTagCommContract {

    interface View extends CommContract.View {

        void updateMemoryBlock(MemoryBlock memoryBlock);

        void updateNdefMessage(String message);
    }

    interface Presenter extends CommContract.Presenter {

        void readMemoryBlock(String blockAddress);

        void readAllMemoryBlocks();

        void readNdefTag();

        void writeMemoryBlock(String blockAddressAsString, String blockContent);

        void resetNfcTag();

        void writeNdefMessage(String message);
    }
}
