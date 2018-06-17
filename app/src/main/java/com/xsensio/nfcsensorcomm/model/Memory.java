package com.xsensio.nfcsensorcomm.model;

import com.xsensio.nfcsensorcomm.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Models the EEPROM memory of a NFC Tag
 */
public class Memory {

    /** Highest block address of a 4-Kbit EEPROM starting at 0x00 */
    public static final int EEPROM_MAX_ADDRESS = 0x7F;

    /** Highest block address of the DATA part of a 4-kbit EEPROM */
    public static final int EEPROM_MAX_DATA_ADDRESS = 0x79;

    /**
     * Returns a stable memory tag state: the NFC tag is configured as NFC-Forum Type 2 and contains
     * one NDEF message of MIME-type plain/text with content "Hello World !"
     *
     * Note: This configuration may not necessarily applied to a tag: depending on its
     * configuration, this Android app might be unauthorized to alter the Tag configuration.
     * Nevertheless, this provides a stable configuration that can always be applied using
     * the Windows Evaluation application provided by AMS for the AS3955 Demo Kit.
     *
     * @return a List of MemoryBlock
     */
    public static List<MemoryBlock> getStableMemoryConfiguration() {

        ArrayList<MemoryBlock> memoryBlocks = new ArrayList<MemoryBlock>();

        // Blocks 00, 01, 02, 03 are either RO of OTP so we don't write them

        /**
         * Blocks 04 to 09 are DATA blocks and are set such that the Tag is configured
         * as NFC-Forum Type 2 and contains one NDEF message of MIME-type plain/text
         * with content "Hello World !"
         */
        memoryBlocks.add(new MemoryBlock("04", "03 14 D1 01"));
        memoryBlocks.add(new MemoryBlock("05", "10 54 02 66"));
        memoryBlocks.add(new MemoryBlock("06", "72 48 65 6c"));
        memoryBlocks.add(new MemoryBlock("07", "6C 6F 20 57"));
        memoryBlocks.add(new MemoryBlock("08", "6F 72 6C 64"));
        memoryBlocks.add(new MemoryBlock("09", "20 21 FE 00"));

        for (int i = 10; i <= EEPROM_MAX_DATA_ADDRESS; i++) {
            memoryBlocks.add(new MemoryBlock(Utils.intToHexString(i), "00 00 00 00"));
        }

        // Blocks 7A and 7B are OTP so we don't write them

        /**
         * Block 7C contains the password for RF authentication. This block can only be written
         * in AUTHENTICATED mode (see page 33 of official AMS AS3955 datasheet)
         */
        //memoryBlocks.add(new MemoryBlock("7C", "00 00 00 00"));
        memoryBlocks.add(new MemoryBlock("7E", "44 00 00 00"));

        /**
         * By default from factory, the block 7F is 00 80 00 00
         * The advantage to put 00 A0 00 00 instead is that the "auth_set" bit is set to 1
         * which means that the Authentication Settings (i.e. block 7D) is WRITABLE through NFC
         * (see page 41 of the official AMS AS3955 datasheet)
         * That is why this MemoryBlock should be written BEFORE the MemoryBlock 7F
         */
        memoryBlocks.add(new MemoryBlock("7F", "3F A9 F9 FF"));

        /**
         * This block is written at last so that we are sure that it is WRITABLE
         * (see comment on block 7F)
         */
        memoryBlocks.add(new MemoryBlock("7D", "00 77 FF 00"));

        return memoryBlocks;
    }

    /**
     * Returns all memory addresses of a 4-Kbit EEPROM as MemoryBlocks (without any content)
     * @return
     */
    public static List<MemoryBlock> getAllMemoryAddresses() {

        List<MemoryBlock> addresses = new ArrayList<MemoryBlock>();

        for (int i = 0; i <= EEPROM_MAX_ADDRESS; i++) {
            addresses.add(new MemoryBlock(Utils.intToHexString(i), ""));
        }
        return addresses;
    }

    /**
     * Returns all memory addresses of a 4-Kbit EEPROM as a byte-array: every byte contains a
     * single block address.
     * @return
     */
    public static byte[] getAllMemoryAddressesAsBytes() {

        byte[] addresses = new byte[EEPROM_MAX_ADDRESS +1];

        for (int i = 0; i <= EEPROM_MAX_ADDRESS; i++) {
            addresses[i] = (byte) i;
        }
        return addresses;
    }
}