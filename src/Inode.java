public class Inode {
    public static final int iNodeSize = 32;
    public static final int directSize = 11;
    public static final int NoError = 0;
    public static final int ErrorBlockRegistered = -1;
    public static final int ErrorPrecBlockUnused = -2;
    public static final int ErrorIndirectNull = -3;
    public int length;
    public short count;
    public short flag;
    public short[] direct = new short[directSize];
    public short indirect;

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
        offset += 4;
    }

    /**
     * Save to disk as the i-th inodes
     *
     * @param i
     */
    public void toDisk(short iNumber) {
        byte[] data = new byte[Disk.blockSize];
        int offset = (iNumber % 16) * 32;
        SysLib.short2bytes(iNumber, data, offset);
        SysLib.rawwrite(iNumber, data);
    }


    /**
     * Finds the Index Block Number
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
            if (direct[i] == -1) {
                return false; //if so fail.
            }
        }

        if (indirect == -1) //check if indirect block can be used
            return false; //if so fail
        else {

            //set up a byte array to be written to index block
            byte[] indexSetUp = new byte[Disk.blockSize];
            int offset = 0;

            for (int i = 0; i < (Disk.blockSize / 2); i++) {
                SysLib.short2bytes((short) -1, indexSetUp, offset);
                offset += 2;
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
        if (iNumber < 0)
            return -1;
        else
            return (iNumber % 16) * 32;
    }

    /**
     * @param i
     * @param i1
     * @return
     */
    public int registerTargetBlock(int i, short i1) {
        if (i == -1)  //check if indirect block can be used
            return i; //if so fail

        //set up a byte array to be written to index block
        byte[] indexSetUp = new byte[Disk.blockSize];
        int offset = 0;

        for (int index = 0; index < (Disk.blockSize / 2); index++) {
            SysLib.short2bytes((short) i1, indexSetUp, offset);
            offset += 2;
        }
        SysLib.rawwrite(i, indexSetUp);
        return i;
    }

    /**
     * @return
     */
    public byte[] unregisterIndexBlock() {
        if (indirect > -1) { //if it is actually being used
            //set up a byte array to be written to index block
            byte[] indexSetUp = new byte[Disk.blockSize];
            int offset = 0;
            SysLib.rawread(indirect, indexSetUp); //read up all the data inside the block
            indirect = -1; //reset it to not being used
            return indexSetUp;
        }
        return null;
    }
}