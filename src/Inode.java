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

    public Inode() {
        length = 0;
        count = 0;
        flag = 1;
        for (int i = 0; i < directSize; i++) {
            direct[i] = -1;
        }
        indirect = -1;
    }

    public Inode(short iNumber) {
        int blockNumber = 1 + iNumber / 16;
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(blockNumber, data);
        int offset = (iNumber % 16) * 32;
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
     * @param i
     */
    public void toDisk(short iNumber) {
        if(iNumber < 0)
            return;
        //only needs 32 bytes for a single inode
        byte[] data = new byte[32];
        byte offset = 0;
        //write the length
        SysLib.int2bytes(length, data, offset);
        int offsetInt = offset + 4;
        //write the count
        SysLib.short2bytes(count, data, offsetInt);
        offsetInt += 2;
        //write the flag
        SysLib.short2bytes(flag, data, offsetInt);
        offsetInt +=2;

        //write block number that direct pointer points to
        int pointerIndex;
        for(pointerIndex = 0; pointerIndex < directSize; pointerIndex++)
        {
            SysLib.short2bytes(direct[pointerIndex], data, offsetInt);
            offsetInt += 2;
        }
        SysLib.short2bytes(indirect, data, offsetInt);

        //find the Inode we are on
        pointerIndex = 1 + iNumber/16;
        byte[] nodeData = new byte[Disk.blockSize];
        SysLib.rawread(pointerIndex,nodeData);
        offsetInt = (iNumber % 16) * 32;

        //write it back to disk
        System.arraycopy(data,0,nodeData,offsetInt,32);
        SysLib.rawwrite(pointerIndex, nodeData);
    }


    /**
     * returns the indirect block number because it is the same
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
     * @param iNumber
     * @return
     */
    public int findTargetBlock(int iNumber) {
        int indexNode = iNumber / Disk.blockSize;
        if(indirect < 0) // if the indirect is not used
            return -1;
        else if (indexNode < directSize)
            return direct[indexNode];
        else {
            byte[] data = new byte[Disk.blockSize];
            SysLib.rawread(indirect, data);
            int node = indexNode - directSize;
            return SysLib.bytes2short(data,node *2);
        }
    }

    /**
     * @param iNumber
     * @param i1
     * @return
     */
    public int registerTargetBlock(int iNumber, short i1) {
        if (iNumber == -1)  //check if indirect block can be used
            return iNumber; //if so fail

        int block = iNumber / Disk.blockSize;
        if (direct[block] >= 0)
            return -1;
        else if (block > 0 && direct[block - 1] == -1) //if it is not the superblock and is free
            return -1;
        else {
            //set up a byte array to be written to index block
            byte[] indexSetUp = new byte[Disk.blockSize];
            SysLib.rawread(indirect, indexSetUp);
            int bInt = block - 11;
            if (SysLib.bytes2short(indexSetUp, bInt * 2) > 0)
                return -1;
            else {
                SysLib.short2bytes(i1, indexSetUp, bInt * 2);
                SysLib.rawwrite(indirect, indexSetUp);
                return 0;
            }
        }
    }

    /**
     * @return
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