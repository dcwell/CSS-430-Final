/**
 * @authors Denali Cornwell & Jayden Stipek.
 */
public class SuperBlock {
    private final int defaultInodeBlocks = 64;
    public int totalBlocks;
    public int inodeBlocks;
    public int freeList;

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

    public void sync() {
        byte[] sendToDisk = new byte[Disk.blockSize];
        SysLib.int2bytes(totalBlocks, sendToDisk, 0);
        SysLib.int2bytes(inodeBlocks, sendToDisk, 4);
        SysLib.int2bytes(freeList, sendToDisk, 8);
        SysLib.rawwrite(0, sendToDisk);
    }

    public void format() {

        for(int i = 0; i < defaultInodeBlocks; i++) {
            Inode blankNode = new Inode();
        }

        for(i = freeList; i < defaultInodeBlocks; i++) {
            byte[] blankData = new byte[Disk.blockSize];
            SysLib.int2bytes(i + 1, blankData, 0);
            SysLib.rawwrite(i, blankData);
        }

    }

    public void format(int nodes) {
        for(int i = 0; i < nodes; i++) {
            Inode blankNode = new Inode();
        }
        for(i = freeList; i < defaultInodeBlocks; i++) {
            byte[] blankData = new byte[Disk.blockSize];
            SysLib.int2bytes(i + 1, blankData, 0);
            SysLib.rawwrite(i, blankData);
        }
    }

    public int getFreeBlock() {
        return -1;
    }

    public boolean returnBlock(int i) {
        return false;
    }
}