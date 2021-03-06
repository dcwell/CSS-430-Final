/**
 * @authors Denali Cornwell & Jayden Stipek
 * Revised: 3/13/2020
 * This class is the Inode class that will represent an Inode in a Unix-Like file system.
 * It will be able to store a finite amount of files, with finite sized names.
 * Inodes go inside blocks and hold information in virtual memory that can be changed and manipulated from disk
 */

public class Inode {
    public static final int iNodeSize = 32;
    public static final int directSize = 11;
    public static final int NoError = 0;
    public static final int ErrorBlockRegistered = -1;
    public static final int ErrorPrecBlockUnused = -2;
    public static final int ErrorIndirectNull = -3;
    public int length;                              //file size in bytes
    public short count;                             //# of file-table entries pointing to this
    public short flag;                              //an indirect pointer
    public short[] direct = new short[directSize];  //direct pointers
    public short indirect;                          //indirect pointer

    /**
     * Default constructor for an Inode.
     */
    public Inode() {
        length = 0;
        count = 0;
        flag = 1;
        for (int i = 0; i < directSize; i++) {
            direct[i] = -1;
        }
        indirect = -1;
    }

    /**
     * Secondary contructor for Inode, will construct an Inode with an iNumber.
     *
     * @param iNumber Index number to place the Inode at.
     */
    public Inode(short iNumber) {
        int blockNumber = 1 + iNumber / 16;
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(blockNumber, data);
        int offset = (iNumber % 16) * iNodeSize;
        length = SysLib.bytes2int(data, offset);
        offset += 4;
        count = SysLib.bytes2short(data, offset);
        offset += 2;
        flag = SysLib.bytes2short(data, offset);
        offset += 2;
        for (int i = 0; i < directSize; i++) {
            direct[i] = SysLib.bytes2short(data, offset);
            offset += 2;
        }
        indirect = SysLib.bytes2short(data, offset);
    }


    /**
     * Save to disk as the i-th inodes
     * Move memory into disk using the inumber Value
     *
     * @param i the index number to place the node at.
     */
    public void toDisk(short iNumber) {
        if (iNumber < 0)
            return;
        //only needs 32 bytes for a single inode
        byte[] data = new byte[iNodeSize];
        byte offset = 0;
        //write the length
        SysLib.int2bytes(length, data, offset);
        int offsetInt = offset + 4;
        //write the count
        SysLib.short2bytes(count, data, offsetInt);
        offsetInt += 2;
        //write the flag
        SysLib.short2bytes(flag, data, offsetInt);
        offsetInt += 2;

        //write block number that direct pointer points to
        int pointerIndex;
        for (pointerIndex = 0; pointerIndex < directSize; pointerIndex++) {
            SysLib.short2bytes(direct[pointerIndex], data, offsetInt);
            offsetInt += 2;
        }
        SysLib.short2bytes(indirect, data, offsetInt);

        //find the Inode we are on
        pointerIndex = 1 + iNumber / 16;
        byte[] nodeData = new byte[Disk.blockSize];
        SysLib.rawread(pointerIndex, nodeData);
        offsetInt = (iNumber % 16) * iNodeSize;

        //write it back to disk
        System.arraycopy(data, 0, nodeData, offsetInt, 32);
        SysLib.rawwrite(pointerIndex, nodeData);
    }


    /**
     * Returns the indirect block number because it is the same.
     *
     * @return Index Block
     */
    public int findIndexBlock() {
        return indirect;
    }

    /**
     * Sets up the indrect block so it can be used by the file system. Only called once needed by the system.
     *
     * @param indexBlock The block number of the index block.
     * @return true uf success, false if there are direct block that can still be used.
     */
    public boolean registerIndexBlock(short indexBlock) {
        for (int i = 0; i < directSize; i++) { //check if any direct blocks can be used
            if (direct[i] == -1)
                return false; //if so fail.
        }

        if (indirect != -1) //check if indirect block can be used
            return false; //if so fail
        else {
            indirect = indexBlock;
            //set up a byte array to be written to index block
            byte[] indexSetUp = new byte[Disk.blockSize];

            for (int i = 0; i < (Disk.blockSize / 2); i++) {
                SysLib.short2bytes((short) -1, indexSetUp, i * 2);
            }

            SysLib.rawwrite(indexBlock, indexSetUp);
            return true;
        }
    }


    /**
     * Returns the index of the target block that should be searched for.
     *
     * @param offset the offset of the target.
     * @return The short telling us where the target block is.
     */
    public int findTargetBlock(int offset) {
        int iNumber = offset / Disk.blockSize;
        if (iNumber < directSize) {
            if (direct[iNumber] < 0)
                return -1;
            else
                return direct[iNumber];
        } else if (indirect == -1)
            return -1;
        else {
            byte[] data = new byte[Disk.blockSize];
            SysLib.rawread(indirect, data);
            int block = iNumber - directSize;
            return SysLib.bytes2short(data, block * 2);
        }
    }

    /**
     * Allocating a target block.
     *
     * @param iNumber index number of the target block.
     * @param targetBlockNumber the block number to be put into the target
     * @return free, failed (exe of method), or occupied block
     */
    public int registerTargetBlock(int numBytes, short targetBlockNumber) {
        int offsetInt = numBytes / Disk.blockSize;

        if(offsetInt < 11) {
            if (direct[offsetInt] >= 0)
                return -1;
            else if (offsetInt > 0 && direct[offsetInt - 1] == -1)
                return -2;
            else {
                direct[offsetInt] = targetBlockNumber;
                return 0;
            }
        }else if(indirect < 0)
            return -3; //to replicate what you had
        else {
            byte[] data = new byte[Disk.blockSize];
            SysLib.rawread(indirect, data);
            int temp = offsetInt - directSize;
            if (SysLib.bytes2short(data, temp * 2) > 0)
                return -1;
            else {
                SysLib.short2bytes(targetBlockNumber, data, temp * 2);
                SysLib.rawwrite(indirect, data);
                return 0;
            }

        }
    }

    /**
     * Deallocating index block for indirect pointers.
     *
     * @return The contense of the index block.
     */
    public byte[] unregisterIndexBlock() {
        if (indirect > -1) { //if it is actually being used
            //set up a byte array to be written to index block
            byte[] indexSetUp = new byte[Disk.blockSize];
            SysLib.rawread(indirect, indexSetUp); //read up all the data inside the block
            indirect = -1; //reset it to not being used
            return indexSetUp;
        }
        return null;
    }
}