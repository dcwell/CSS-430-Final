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
     * Default constructor for an Inode
     */
    public Inode() {
        //SysLib.cout("\n**************** CALLING INODE DEFAULT CONST *******************\n");
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
        //SysLib.cout("\n**************** CALLING INODE ONE ARG CONST *******************\n");
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
     * @param i
     */
    public void toDisk(short iNumber) {
        //SysLib.cout("\n**************** CALLING INODE TODISK *******************\n");
        if(iNumber < 0)
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
        offsetInt = (iNumber % 16) * iNodeSize;

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
        SysLib.cout("\n**************** CALLING INODE FIND INDEX BLOCK *******************\n");
        return indirect;
    }

    /**
     * Sets up the indrect block so it can be used by the file system. Only called once needed by the system.
     *
     * @param indexBlock The block number of the index block.
     * @return true uf success, false if there are direct block that can still be used.
     */
    public boolean registerIndexBlock(short indexBlock) {
        SysLib.cout("\n**************** CALLING INODE REGISTER INDEX BLOCK *******************\n");
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
   public int findTargetBlock(int offset) {
       int iNumber = offset/Disk.blockSize;
        if(iNumber < directSize)
        {
            if(direct[iNumber] < 0)
                return -1;
            else
                return direct[iNumber];
        }
        else
            if(indirect == -1)
                return -1;
            else {
                byte[] data = new byte[Disk.blockSize];
                SysLib.rawread(indirect, data);
                int block = iNumber - directSize;
                return SysLib.bytes2short(data, block * 2);
            }
    }

    /**
     * @param iNumber
     * @param i1
     * @return
     */
   public int registerTargetBlock(int var1, short var2) {
        int var3 = var1 / 512;
        if (var3 < 11) {
            if (this.direct[var3] >= 0) {
                return -1;
            } else if (var3 > 0 && this.direct[var3 - 1] == -1) {
                return -2;
            } else {
                this.direct[var3] = var2;
                return 0;
            }
        } else if (this.indirect < 0) {
            return -3;
        } else {
            byte[] var4 = new byte[512];
            SysLib.rawread(this.indirect, var4);
            int var5 = var3 - 11;
            if (SysLib.bytes2short(var4, var5 * 2) > 0) {
                SysLib.cerr("indexBlock, indirectNumber = " + var5 + " contents = " + SysLib.bytes2short(var4, var5 * 2) + "\n");
                return -1;
                } else {
                    SysLib.short2bytes(var2, var4, var5 * 2);
                    SysLib.rawwrite(this.indirect, var4);
                    return 0;
                }
            }



   }

    /**
     * @return
     */
    public byte[] unregisterIndexBlock() {
        //SysLib.cout("\n**************** CALLING INODE UN-REGISTER INDEX BLOCK *******************\n");
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