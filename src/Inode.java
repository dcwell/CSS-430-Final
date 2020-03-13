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
        if(blockNumber < 11) { //direct blocks
            for (int i = 0; i < directSize; i++) {
                direct[i] = SysLib.bytes2short(data, offset);
                offset += 2;
            }
            indirect = SysLib.bytes2short(data, offset);
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
     * Save to disk as the i-th inode
     * @param i
     */
    public void toDisk(short iNumber) {
        byte[] data = new byte[Disk.blockSize];
        int offset = (iNumber % 16) * 32;
        SysLib.short2bytes(iNumber, data, offset);
        SysLib.rawwrite(iNumber,data);
    }


    /**
     * Finds the Index Block Number
     * @return Index Block
     */
    public int findIndexBlock() {
        return indirect;
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