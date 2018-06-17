package com.xsensio.nfcsensorcomm.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.xsensio.nfcsensorcomm.Utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

/**
 * This class represents one memory block of the NFC tag EEPROM
 *
 * Note: AMS' Extended mode communication between Phone and MCU uses EEPROM-memory-block-like
 * blocks. That is, this class can also be used to store address and content of blocks
 * received from/sent to the MCU. See official AMS AS3955 Datasheet for more details.
 *
 */
public class MemoryBlock implements Parcelable, Serializable {

    /** Number of bytes per memory block */
    public static final int NUM_BYTES_PER_BLOCK = 4;

    /** Content of the block when undefined */
    public static final String UNDEFINED_CONTENT = "";

    /**
     * Store addresses of READ-ONLY blocks of the Tag EEPROM. Blocks are either READ-ONLY or OTP.
     */
    public static final HashSet<String> READONLY_BLOCKS = new HashSet<String>(Arrays.asList("00","01","02","03","7A","7B", "7C", "7a","7b", "7c"));

    /**
     * Address of the memory block as 1 hexadecimal string (e.g. 7F)
     */
    private final String mAddress;

    /**
     * Content of the memory block as 4 space-separated hexadecimal string values
     * (e.g. 34 AF 4E 32)
     */
    private final String mContent;

    /**
     * An instance of this class represents a memory block of the NFC tag EEPROM or of the
     * "Extended mode" communication between the Phone and the MCU
     * @param address address of the block as hexadecimal String value,
     *                e.g. "00", "01", "1E", "FF", ... (without quotes)
     * @param content content of the block as space-separated hexadecimal String values.
     *                Must contain 4 space-separated hexadecimal values since a memory block
     *                contains 4 bytes.
     *                e.g. "00 01 1E FF", "AA E2 11 55", ... (without quotes)
     */
    public MemoryBlock(String address, String content) {
        address = address.trim();
        content = content.trim();

        if (addressValid(address) && (contentValid(content) || UNDEFINED_CONTENT.equals(content))) {
            mAddress = address;
            mContent = content;
        } else {
            throw new IllegalArgumentException("String format of address and/or content is invalid(must be hex-formatted bytes separated by spaces)");
        }
    }

    /**
     * An instance of this class represents a memory block of the NFC tag EEPROM or of the
     * "Extended mode" communication between the Phone and the MCU
     * @param address address of the block as byte
     * @param content content of the block as byte-array.
     *                Must contain 4 bytes since a memory block represents 4 bytes.
     */
    public MemoryBlock(byte address, byte[] content) {
        this(Utils.intToHexString((int) address), Utils.bytesToHexString(content));
    }

    /**
     * An instance of this class represents a memory block of the NFC tag EEPROM or of the
     * "Extended mode" communication between the Phone and the MCU
     * @param address address of the block as byte
     * @param content content of the block as space-separated hexadecimal String values.
     *                Must contain 4 hexademical values since a memory block contains 4 bytes.
     *                e.g. "00 01 1E FF", "AA, E2, 11, 55", ... (without quotes)
     */
    public MemoryBlock(byte address, String content) {
        this(Utils.intToHexString((int) address),content);
    }

    /**
     * An instance of this class represents a memory block of the NFC tag EEPROM or of the
     * "Extended mode" communication between the Phone and the MCU
     * @param address address of the block as hexadecimal String value,
     *                e.g. "00", "01", "1E", "FF", ... (without quotes)
     * @param content content of the block as byte-array.
     *                Must contain 4 bytes since a memory block represents 4 bytes.
     */
    public MemoryBlock(String address, byte[] content) {
        this(Utils.stringToByte(address), content);
    }

    public byte getAddressAsByte() {
        return Utils.stringToByte(mAddress);
    }

    public String getAddress() {
        return mAddress;
    }

    public byte[] getContentAsBytes() {
        return Utils.stringToBytes(mContent);
    }

    public String getContent() {
        return mContent;
    }

    /**
     * Get one of the 4 bytes of the memory block
     * @param byteNumber 0,1,2 or 3 where 0 represents the first byte (from the left, see Memory Map
     *                   in AMS AS3955 datasheet) of the block and 3 the last one.
     * @return the corresponding byte
     */
    public byte getByte(int byteNumber) {
        if (byteNumber >= 0 && byteNumber < NUM_BYTES_PER_BLOCK) {
            return (Utils.stringToBytes(mContent))[byteNumber];
        } else {
            throw new IllegalArgumentException("byteNumber must be in {0,1,2,3}");
        }
    }

    /**
     * Check the String format of the address. It must be a valid hexadecimal String value.
     * @param address address to check
     * @return true if the format of the address is valid, false otherwise
     */
    public static boolean addressValid(String address) {
        return address.matches("^[0-9a-fA-F]{2}$");
    }

    /**
     * Check the String format of the content. It must be 4 space-separeted hexadecimal
     * String values.
     * @param content content to check
     * @return true if the format of the content is valid, false otherwise
     */
    public static boolean contentValid(String content) {
        return content.matches("^[0-9a-fA-F]{2}\\s[0-9a-fA-F]{2}\\s[0-9a-fA-F]{2}\\s[0-9a-fA-F]{2}$");
    }


    ///////////// Parcelable interface implementation /////////////

    protected MemoryBlock(Parcel in) {
        mAddress = in.readString();
        mContent = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAddress);
        dest.writeString(mContent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MemoryBlock> CREATOR = new Creator<MemoryBlock>() {
        @Override
        public MemoryBlock createFromParcel(Parcel in) {
            return new MemoryBlock(in);
        }

        @Override
        public MemoryBlock[] newArray(int size) {
            return new MemoryBlock[size];
        }
    };
}