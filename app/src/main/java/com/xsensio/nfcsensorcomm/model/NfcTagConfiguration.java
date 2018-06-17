package com.xsensio.nfcsensorcomm.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.xsensio.nfcsensorcomm.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent the configuration of a 4-Kbit NFC tag.
 */
public final class NfcTagConfiguration implements Parcelable, Serializable {

    private static final String TAG = "NfcTagConfiguration";

    /** Position in the  {@code mConfiguration} of the block 7C */
    public static final int POS_BLOCK_7C = 0; // RFP0, RFP1, RFP2, RFP3 (i.e. password)

    /** Position in the  {@code mConfiguration} of the block 7D */
    public static final int POS_BLOCK_7D = 1; // CHIP_KILL, AUTH_CNT, AUTH_LIM, AUTH_CFG

    /** Position in the  {@code mConfiguration} of the block 7E */
    public static final int POS_BLOCK_7E = 2; // SENSR1, SENSR2, SELR, IC-CFG0

    /** Position in the  {@code mConfiguration} of the block 7F */
    public static final int POS_BLOCK_7F = 3; // IC-CFG1, IC-CFG2, MIRGQ_0, MIRQ_1

    /** List of memory blocks of the tag configuration */
    private List<MemoryBlock> mConfiguration;

    public NfcTagConfiguration(List<MemoryBlock> configMemoryBlocks) {
        mConfiguration = new ArrayList<MemoryBlock>(configMemoryBlocks);
    }

    public NfcTagConfiguration(NfcTagConfiguration initialConfig, byte powerMode, byte voltage, byte outputResistance, byte extendedMode) {

        if (initialConfig == null) {
            throw new IllegalArgumentException("Initial tag configuration cannot be null.");
        }
        mConfiguration = new ArrayList<MemoryBlock>();

        // Copy blocks 7C, 7D, 7E from initial config
        MemoryBlock block7C = initialConfig.getConfigAsMemoryBlocks().get(POS_BLOCK_7C);
        MemoryBlock block7D = initialConfig.getConfigAsMemoryBlocks().get(POS_BLOCK_7D);
        MemoryBlock block7E = initialConfig.getConfigAsMemoryBlocks().get(POS_BLOCK_7E);

        // Copy bytes MIRQ_0 and MIRQ_1 from initial config
        byte mirq0 = initialConfig.getConfigAsMemoryBlocks().get(POS_BLOCK_7F).getContentAsBytes()[2];
        byte mirq1 = initialConfig.getConfigAsMemoryBlocks().get(POS_BLOCK_7F).getContentAsBytes()[3];

        byte oldIcCfg1 = initialConfig.getConfigAsMemoryBlocks().get(POS_BLOCK_7F).getContentAsBytes()[0];
        byte unchangedBitsIcCfg1 = (byte) (oldIcCfg1 & 0x80); // Get values of bits b7
        byte icCfg1 = (byte) (unchangedBitsIcCfg1 | (voltage << 2) | outputResistance);

        byte oldIcCfg2 = initialConfig.getConfigAsMemoryBlocks().get(POS_BLOCK_7F).getContentAsBytes()[1];
        byte unchangedBitsIcCfg2 = (byte) (oldIcCfg2 & 0x14); // Get values of bits b2 and b4
        // We make sure the bits b7 and b3 are set to 1
        byte icCfg2 = (byte) (0x88 | unchangedBitsIcCfg2 | (extendedMode << 5) | powerMode);

        // Last two bytes are MIRQ_0 and MIRQ_1
        MemoryBlock block7F = new MemoryBlock("7F", new byte[]{icCfg1, icCfg2, mirq0, mirq1});

        mConfiguration.add(block7C);
        mConfiguration.add(block7D);
        mConfiguration.add(block7E);
        mConfiguration.add(block7F);

    }

    /**
     * Set the bit corresponding to the Extended mode to 0 or 1 in the configuration
     * in funtion of isEnabled.
     * @param isEnabled
     * @return
     */
    public NfcTagConfiguration setExtendedMode(boolean isEnabled) {
        // Modify the block containing the bit of the ExtendedMode status
        MemoryBlock block7F = mConfiguration.get(POS_BLOCK_7F);
        byte[] block7FContent = block7F.getContentAsBytes();
        byte icCfg2 = block7FContent[1]; // IC_CFG2 dans Memory Map
        if (isEnabled) {
            icCfg2 = (byte) (icCfg2 | 0x20); // Set the 6th bits (from the right) to 1
        } else {
            icCfg2 = (byte) (icCfg2 & 0xDF); // Set the 6th bits (from the right) to 0
        }

        // Update config
        block7FContent[1] = icCfg2;
        mConfiguration.set(POS_BLOCK_7F, new MemoryBlock(block7F.getAddress(), block7FContent));

        return new NfcTagConfiguration(mConfiguration);
    }

    /**
     * Get the byte-value of the voltage.
     * Note: The byte is right-shifted such that the bits used to define the voltage are
     * b0, b1, b2, b3, b4
     * @return
     */
    public byte getVoltageValueAsByte() {
        MemoryBlock block7F = mConfiguration.get(POS_BLOCK_7F);
        byte[] block7FContent = block7F.getContentAsBytes();
        byte icCfg1 = block7FContent[0]; // IC_CFG1 dans Memory Map

        // Voltage is coded on bits b6, b5, b4, b3, b2 of IC_CFG1
        int value = ((icCfg1 & 0x7C) >> 2);
        return (byte) value;
    }

    /**
     * Return the String representation of the voltage based on the corresponding bytes
     * in the configuration of the NFC tag.
     * @return the String representation of the voltage
     */
    public String getVoltageAsString() {
        // Voltage is coded on the first 5 bits (from the right) of IC_CFG1
        int value = getVoltageValueAsByte();
        return String.valueOf(1.8 + 0.1*value);
    }

    public byte getOutputResistanceAsByte() {
        MemoryBlock block7F = mConfiguration.get(POS_BLOCK_7F);
        byte[] block7FContent = block7F.getContentAsBytes();
        byte icCfg1 = block7FContent[0]; // IC_CFG1 dans Memory Map

        // Resistance is coded on the first 2 bits (from the right) of the 12th byte
        int value = icCfg1 & 0x03;
        return (byte) value;
    }

    public String getOutputResistanceAsString() {
        // Resistance is coded on the first 2 bits (from the right) of the 12th byte
        int value = getOutputResistanceAsByte();

        if (value == 0) {
            return "X";
        } else if (value == 1) {
            return "100 ohms";
        } else if (value == 2) {
            return "50 ohms";
        } else if (value == 3) {
            return "25 ohms";
        } else {
            return "Erroneous resistance value";
        }
    }

    /**
     * Return the status of the Extended mode
     * @return
     */
    public boolean isExtendedModeEnabled() {
        MemoryBlock block7F = mConfiguration.get(POS_BLOCK_7F);
        byte[] block7FContent = block7F.getContentAsBytes();
        byte icCfg2 = block7FContent[1]; // IC_CFG2 dans Memory Map

        // Extended mode is enabled if the 6th bit (from the right) of the 14th byte is 1
        return (icCfg2 & 0x20) > 0;
    }

    /**
     * Return the status of the Tunneling mode
     * @return
     */
    public boolean isTunnelingModeEnabled() {
        MemoryBlock block7F = mConfiguration.get(POS_BLOCK_7F);
        byte[] block7FContent = block7F.getContentAsBytes();
        byte icCfg2 = block7FContent[1]; // IC_CFG2 dans Memory Map

        // Tunneling mode is enabled if the 7th bit (from the right) of the 14th byte is 1
        return (icCfg2 & 0x40) > 0;
    }

    /**
     * Get the byte-value of the voltage.
     * Note: The byte is right-shifted such that the bits used to define the voltage are
     * b0, b1, b2, b3
     * @return
     */
    public byte getPowerModeAsByte() {
        MemoryBlock block7F = mConfiguration.get(POS_BLOCK_7F);
        byte[] block7FContent = block7F.getContentAsBytes();
        byte icCfg2 = block7FContent[1]; // IC_CFG2 dans Memory Map

        // Power mode is coded on the first 2 bits (from the right) of the 14th byte
        int value = icCfg2 & 0x03;
        return (byte) value;
    }

    public String getPowerModeAsString() {
        // Power mode is coded on the first 2 bits (from the right) of the 14th byte
        int value = getPowerModeAsByte();
        if (value >= 0 && value <= 3) {
            return "Power mode " + value;
        } else {
            return "Erroneous power mode value";
        }
    }

    public String toString() {
        return "NFC Tag Configuration:\n"
                + "Voltage: " + getVoltageAsString() + "\n"
                + "Resistance: " + getOutputResistanceAsString() + "\n"
                + "Power mode: " + getPowerModeAsString() + "\n"
                + "Extended mode: " + isExtendedModeEnabled() + "\n"
                + "Tunneling mode: " + isTunnelingModeEnabled() + "\n";
    }

    public List<MemoryBlock> getConfigAsMemoryBlocks() {
       return new ArrayList<MemoryBlock>(mConfiguration);
    }

    public byte[] getConfigAsBytes() {
        return Utils.getContentFromMemoryBlocks(mConfiguration);
    }


    ///////////// Parcelable interface implementation /////////////

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(mConfiguration);
    }

    public static final Parcelable.Creator<NfcTagConfiguration> CREATOR
            = new Parcelable.Creator<NfcTagConfiguration>() {
        public NfcTagConfiguration createFromParcel(Parcel in) {
            return new NfcTagConfiguration(in);
        }

        public NfcTagConfiguration[] newArray(int size) {
            return new NfcTagConfiguration[size];
        }
    };

    private NfcTagConfiguration(Parcel in) {
        mConfiguration = new ArrayList<MemoryBlock>();
        in.readTypedList(mConfiguration, MemoryBlock.CREATOR);
    }
}