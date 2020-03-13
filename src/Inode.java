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
        for(int i = 0; i < directSize; i++) {
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
        if(blockNumber > 15) { //direct blocks
            for (int i = 0; i < iNumber; i++) {
                direct[i] = -1;
            }
            offset += (iNumber * 2);
            indirect = -1;
        }else
        {
            for(int i  = indirect; i < indirect + 256; i++) //for if you are using indirect block
            {
                SysLib.bytes2int(data,offset);
                SysLib.rawwrite(i,data);
            }
        }
    }

    /**
     * save to disk as the i-th inode
     * @param i
     */
    public void toDisk(short iNumber) {

    }

    public int findIndexBlock() {
        return -1;
    }

    public boolean registerIndexBlock(short i){
        return false;
    }

    public int findTargetBlock(int i) {
        return -1;
    }

    public int registerTargetBlock(int i, short i1) {
        return -1;
    }

    public byte[] unregisterIndexBlock() {
        return new byte[10];
    }

}