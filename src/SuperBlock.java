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
            format(defaultInodeBlocks);
        }
    }

    public void sync() {
        byte [] superBlock = new byte[Disk.blockSize];
        SysLib.int2bytes(totalBlocks, superBlock, 0);
        SysLib.int2bytes(inodeBlocks, superBlock, 4);
        SysLib.int2bytes(freeList, superBlock, 8);
        SysLib.rawwrite(0,superBlock);
    }

    public void format() {

    }

    public void format(int totalInodes) {

    }

    public int getFreeBlock() {
        return freeList++;
    }

    public boolean returnBlock(int i) {
        return false;
    }
}