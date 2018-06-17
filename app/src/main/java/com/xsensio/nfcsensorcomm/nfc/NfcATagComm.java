package com.xsensio.nfcsensorcomm.nfc;

import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.NfcA;
import android.util.Log;

import com.xsensio.nfcsensorcomm.Utils;
import com.xsensio.nfcsensorcomm.model.MemoryBlock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Enabled to read and write data from and to a NFC Tag using NFC-A standard.
 */
public final class NfcATagComm {

    private static final String TAG = "NfcATagComm";

    private NfcATagComm() {}

    /**
     * Read 4 blocks of data (4 bytes per block) from the address specified by {@code blockToRead}
     * @param tag
     * @param blockToRead - memory block containing the address of the block to read from (including
     *                    the following 3 block)
     * @return a list of 4 MemoryBlocks containing the returned data
     * @throws IOException
     */
    public static List<MemoryBlock> read(Tag tag, MemoryBlock blockToRead) throws IOException {
        List<MemoryBlock> memoryBlocks = new ArrayList<MemoryBlock>();
        memoryBlocks.add(blockToRead);
        return readMultipleBlocks(tag, memoryBlocks);
    }

    /**
     * Read blocks from multiple addresses specified by the addresses of blocksToRead.
     * Note: There is an optimization: since the NFC standard defines that a minimum of 4 blocks
     * are read per operation, if the user specify the addresses of adjacent blocks, this function
     * will NOT read several times the same blocks but instead will only make read operations
     * at the needed addresses to recover every block.
     * @param tag
     * @param blocksToRead
     * @return a list of MemoryBlock read from the Tag
     * @throws IOException
     * @throws TagLostException
     */
    public static ArrayList<MemoryBlock> readMultipleBlocks(Tag tag, List<MemoryBlock> blocksToRead) throws IOException, TagLostException {

        if (tag == null) {
            throw new TagLostException();
        }

        NfcA nfca = NfcA.get(tag);

        ArrayList<Byte> addressesToRead = new ArrayList<Byte>();
        for (Integer address : getAddressesToRead(blocksToRead)) {
            addressesToRead.add(address.byteValue());
        }

        // Store the bytes read from the Tag
        ArrayList<MemoryBlock> memoryBlocks = new ArrayList<MemoryBlock>();

        nfca.connect();

        for (Byte address : addressesToRead) {

            Log.d(TAG, "Read 4 blocks from address " + Utils.byteToHexString(address));
            byte[] bytesRead = nfca.transceive(new byte[]{
                    (byte) 0x30,  // READ
                    address
            });

            int numBlocksRead = bytesRead.length / MemoryBlock.NUM_BYTES_PER_BLOCK;
            Log.d(TAG, "Number of blocks read: " + numBlocksRead + ", number of bits read: " + bytesRead.length);

            if (numBlocksRead >= 1 && bytesRead.length >= 4) { // First block
                memoryBlocks.add(new MemoryBlock(address, new byte[]{
                        bytesRead[0], bytesRead[1], bytesRead[2], bytesRead[3]
                }));
            }
            if (numBlocksRead >= 2 && bytesRead.length >= 8) { // Second block
                byte addressPlusOne = (byte) (((int)address) + 1);
                memoryBlocks.add(new MemoryBlock(addressPlusOne, new byte[]{
                        bytesRead[4], bytesRead[5], bytesRead[6], bytesRead[7]
                }));
            }
            if (numBlocksRead >= 3 && bytesRead.length >= 12) { // Third block
                byte addressPlusTwo = (byte) (((int)address) + 2);
                memoryBlocks.add(new MemoryBlock(addressPlusTwo, new byte[]{
                        bytesRead[8], bytesRead[9], bytesRead[10], bytesRead[11]
                }));
            }
            if (numBlocksRead >= 4 && bytesRead.length >= 16) { // Fourth block
                byte addressPlusThree = (byte) (((int)address) + 3);
                memoryBlocks.add(new MemoryBlock(addressPlusThree, new byte[]{
                        bytesRead[12], bytesRead[13], bytesRead[14], bytesRead[15]
                }));
            }

            Log.i(TAG, "Read up to 4 block from address: " + address + ", content: " + Utils.bytesToHexString(bytesRead));
        }

        return memoryBlocks;
    }

    /**
     * Remove duplicate block addresses to optimize the reading process due to the minimum of 4
     * blocks read per operation.
     * @param blocksToRead
     * @return
     */
    private static List<Integer> getAddressesToRead(List<MemoryBlock> blocksToRead) {

        // Extract all addresses
        List<Integer> addressesToRead = new ArrayList<Integer>();

        for (MemoryBlock block : blocksToRead) {
            Integer address = (int) block.getAddressAsByte();
            addressesToRead.add(address);
        }

        // Sort addresses in ascending order
        Collections.sort(addressesToRead);

        /** Add only necessary addresses since one read operation at address i
         * returns blocks i, i+1, i+2,i+3)
         */
        Set<Integer> addressesToActuallyRead = new HashSet<Integer>();
        for (Integer address : addressesToRead) {
            if (!addressesToActuallyRead.contains(address-1)) {
                if (!addressesToActuallyRead.contains(address-2)){
                    if (!addressesToActuallyRead.contains(address-3)){
                        addressesToActuallyRead.add(address);
                    }
                }
            }
        }

        List<Integer> listToReturn = Arrays.asList(addressesToActuallyRead.toArray(new Integer[addressesToActuallyRead.size()]));
        Collections.sort(listToReturn);
        return listToReturn;
    }

    /**
     * Write one 4-byte block at the address specified in {@code memoryBlock} with the content
     * specified in {@code memoryBlock}
     * @param tag
     * @param memoryBlock
     * @throws IOException
     */
    public static void writeOneBlock(Tag tag, MemoryBlock memoryBlock) throws IOException {
        List<MemoryBlock> memoryBlocks = new ArrayList<MemoryBlock>();
        memoryBlocks.add(memoryBlock);
        writeMultipleBlocks(tag, memoryBlocks);
    }


    /**
     * Write multiple 4-byte blocks at the addresses specified in {@code blocksToWrite} with the
     * content specified in {@code blocksToWrite}
     * @param tag
     * @param blocksToWrite
     * @throws IOException
     */
    public static void writeMultipleBlocks(Tag tag, List<MemoryBlock> blocksToWrite) throws IOException {

        if (tag == null) {
            throw new TagLostException();
        }

        NfcA nfca = NfcA.get(tag);

        nfca.connect();

        for (MemoryBlock block : blocksToWrite) {

            byte[] payload = block.getContentAsBytes();

            // Security measure to avoid disabling configuration over RF
            if (block.getAddress() == "7F") {
                /**
                 * Replace bits b7 and b3 of byte IC_CFG2 of block 7F by 1. This ensures that
                 * the phone can always write in the configuration of the Tag. See AMS AS3955
                 * for more details.
                 */
                payload[1] = (byte) (payload[1] | 0x88);
            }

            Log.i(TAG, "Write block: " + block.getContent() + " at address " + block.getAddress());
            byte[] resultOfWrite = nfca.transceive(new byte[]{
                    (byte) 0xA2,  // NFC-A WRITE
                    block.getAddressAsByte(),
                    payload[0], payload[1], payload[2], payload[3]
            });
        }
    }
}
