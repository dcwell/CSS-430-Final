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
    public short[] direct;
    public short indirect;

    public Inode() {}
    public Inode(short i){}
    public void toDisk(short i) {}
    public int findIndexBlock() {}
    public boolean registerIndexBlock(short i){}
    public int findTargetBlock(int i) {}
    public int registerTargetBlock(int i, short i1) {}
    public byte[] unregisterIndexBlock() {}

}