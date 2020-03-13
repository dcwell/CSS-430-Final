/**
 * @authors Denali Cornwell & Jayden Stipek.
 */
public class SuperBlock {
    private final int defaultInodeBlocks = 64;
    public int totalBlocks;
    public int inodeBlocks;
    public int freeList;

    /**
     * 
     * @param diskSize
     */

    public SuperBlock(int diskSize) {
        //read the superblock from disk
        byte [] superBlock = new byte[Disk.blockSize];
        SysLib.rawread(0,superBlock);
        totalBlocks = SysLib.bytes2int(superBlock, 0);
        inodeBlocks = SysLib.bytes2int(superBlock,4);
        freeList = SysLib.bytes2int(superBlock, 8 );

        if(totalBlocks == diskSize && inodeBlocks > 0 && freeList >= 2)
            //disk contents are valid
            return;
        else {
            //need to format disk
            totalBlocks = diskSize;
            format();
        }
    }

    /**
     *
     */

    public void sync() {
        byte[] superBlock = new byte[Disk.blockSize];
        SysLib.int2bytes(totalBlocks, superBlock, 0);
        SysLib.int2bytes(inodeBlocks, superBlock, 4);
        SysLib.int2bytes(freeList, superBlock, 8);
        SysLib.rawwrite(0,superBlock);
        SysLib.cerr("Superblock synchronized\n"); //to format it like it was
    }

    /**
     *uses format(int nodes) for simplicity
     */

    public void format() {
        format(defaultInodeBlocks); //already written why write it again
    }

    /**
     * 
     * @param nodes
     */
    public void format(int nodes) {
        SysLib.cout("\n*************** EXECUTING FORMAT ****************\n");
        for(short i = 0; i < nodes; i++) {
            Inode blankNode = new Inode();
            blankNode.toDisk(i);
        }

        for(int i = freeList; i < nodes; i++) {
            byte[] blankData = new byte[Disk.blockSize];
            SysLib.int2bytes(i + 1, blankData, 0);
            SysLib.rawwrite(i, blankData);
        }
        sync();
    }

    /**
     *Returns the first free block
     * @return block ptr
     */

    public int getFreeBlock() {
        int temp = freeList; //create a temp block pointer
        if(temp > 0) //if there is a block available
        {
            byte[] tempData = new byte[Disk.blockSize]; //back up data with Phyiscal memory
            SysLib.rawread(freeList,tempData); //read from Disk
            freeList = SysLib.bytes2int(tempData,0); // Sets the next free block
        }
        return temp; //return block ptr
    }

    /**
     * Enqueues a given block to the beginning of the free list
     * @param blockNumber index of block
     * @return
     */

    public boolean returnBlock(int blockNumber) {
        if(blockNumber >= 0) {
            byte[] blankBlock = new byte[Disk.blockSize];
            SysLib.rawwrite(blockNumber, blankBlock);
            SysLib.int2bytes(freeList, blankBlock, 0);
            SysLib.rawwrite(freeList, blankBlock);
            freeList = blockNumber;
            return true;
        }
        return false;
    }
}