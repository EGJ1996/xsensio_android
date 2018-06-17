package com.xsensio.nfcsensorcomm;

import android.os.Environment;
import android.util.Log;

import com.xsensio.nfcsensorcomm.model.MemoryBlock;

import java.nio.ByteBuffer;
import java.util.List;


/**
 * Defines general helper functions
 */
public final class Utils {

    private static final String TAG = "Utils";

    private Utils() {}

    /**
     * Convert a byte array into its hexadecimal String representation.
     * Hexadecimal pairs of characters are separated by a white space
     * @param bytes
     * @return hexadecimal String representation of provided byte array
     */
    public static String bytesToHexString(byte[] bytes) {

        if (bytes.length == 0) {
            return "";
        }

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[3*bytes.length-1];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[3*i] = hexArray[v >>> 4];
            hexChars[3*i+1] = hexArray[v & 0x0F];

            if (i != bytes.length-1) {
                hexChars[3 * i + 2] = ' '; // Add space between bytes
            }
        }
        return new String(hexChars);
    }

    /**
     * Convert a byte array into its hexadecimal String representation.
     * Hexadecimal pairs of characters are separated by a white space
     * @param bytes
     * @return hexadecimal String representation of provided byte array
     */
    public static String bytesToHexString(Byte[] bytes) {
        return bytesToHexString(bytesWrapperToBytesPrim(bytes));
    }

    public static String intToHexString(int integer) {
        return  bytesToHexString(new byte[] {Integer.valueOf(integer).byteValue()});
    }

    public static String byteToHexString(byte byteToConvert) {
        return intToHexString(byteToConvert);
    }

    public static byte stringToByte(String str) {
        return Integer.valueOf(str, 16).byteValue();
    }

    public static byte[] stringToBytes(String str) {
        String[] substr = str.split("\\s");
        byte[] result = new byte[substr.length];
        for (int i = 0; i < substr.length; i++) {
            result[i] = stringToByte(substr[i]);
        }
        return result;
    }

    /**
     * Convert byte-wrapper array Byte[] into byte-primitive array byte[]
     * @param bytes
     * @return
     */
    public static byte[] bytesWrapperToBytesPrim(Byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        byte[] bytesPrim = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            bytesPrim[i] = bytes[i];
        }
        return bytesPrim;
    }

    public static byte[] bytesListToArray(List<Byte> bytes) {
        if (bytes == null) {
            return null;
        }

        byte[] bytesPrim = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bytesPrim[i] = bytes.get(i);
        }
        return bytesPrim;
    }

    public static byte[] stringArraytoByteArray(String[] stringArray) {
        byte[] bytes = new byte[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            bytes[i] = stringToByte(stringArray[i]);
        }
        return bytes;
    }

    /**
     * Checks if external storage is available for read and write
     * @return true if writable
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     * @return true if readable
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }


    /**
     * Transform a boolean value into an integer: True -> 1, False -> 0
     * @param value
     * @return 1 if boolean is True, 0 if boolean is False
     */
    public static int booleanToInt(boolean value) {
        if (value) {
            return 1;
        } else {
            return 0;
        }
    }

    public static byte[] createByteArrayWithzeros(int size) {
        byte[] array = new byte[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) 0x00;
        }
        return array;
    }

    public static byte[] getContentFromMemoryBlocks(List<MemoryBlock> memoryBlocks) {

        byte[] byteArray = new byte[memoryBlocks.size()*MemoryBlock.NUM_BYTES_PER_BLOCK];

        int i = 0;
        for (MemoryBlock block : memoryBlocks) {
            byte[] blockContent = block.getContentAsBytes();
            byteArray[i] = blockContent[0];
            byteArray[i] = blockContent[1];
            byteArray[i] = blockContent[2];
            byteArray[i] = blockContent[3];
            i += 4;
        }
        return byteArray;
    }

    public static byte[] intToBytes(int number) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong((long)number);
        return buffer.array();
    }

    /**
     *
     * @param bytes
     * @return
     * Source: https://stackoverflow.com/a/4485196/6641415
     */
    public static int bytesToInt(byte[] bytes) {
        // Pad byte array with zeros on the left
        byte[] paddedArray = Utils.createByteArrayWithzeros(8);
        for (int i = 0; i < bytes.length; i++) {
            paddedArray[paddedArray.length-bytes.length+i] = bytes[i];
            //paddedArray[i] = bytes[i];
        }
        //Log.d(TAG, "Bytes to Int: " + Utils.bytesToHexString(paddedArray));

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(paddedArray);
        buffer.flip();
        return (int) buffer.getLong();
    }
}
