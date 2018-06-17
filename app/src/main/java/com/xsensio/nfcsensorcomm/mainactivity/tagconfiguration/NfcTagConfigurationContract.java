package com.xsensio.nfcsensorcomm.mainactivity.tagconfiguration;

import com.xsensio.nfcsensorcomm.mainactivity.CommContract;
import com.xsensio.nfcsensorcomm.model.MemoryBlock;

import java.util.List;

/**
 * Created by Michael Heiniger on 25.07.17.
 */

public interface NfcTagConfigurationContract {

    interface View extends CommContract.View {

        void setTest(String bla);

        void setExtendedMode(boolean isEnabled);

        void setPowerMode(byte powerMode);

        void setVoltage(byte voltage);

        void setOutputResistance(byte outputResistance);

        void setMemoryBlockConfiguration(List<MemoryBlock> blocks);
    }

    interface Presenter extends CommContract.Presenter {
        void readTagConfiguration();

        void writeTagConfiguration(byte powerMode, byte voltage, byte outputResistance, byte extendedMode);
    }
}