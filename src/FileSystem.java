public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    public FileSystem(int i) { }

    public void sync() { }

    public boolean format(int files) {
        return false;
    }

    public FileTableEntry open(String filename, String mode) {
        return new FileTableEntry(new Inode(), (short) 0, "r");
    }

    public boolean close(FileTableEntry ftEnt) {
        return false;
    }

    public int fsize(FileTableEntry ftEnt) {
        return -1;
    }

    public int read(FileTableEntry ftEnt, byte[] buf) {
        return -1;
    }

    public int write(FileTableEntry ftEnt, byte[] buf) {
        return -1;
    }

    private boolean deallocAllBlocks(FileTableEntry ftEnt) {
        return false;
    }

    public boolean delete(String filename) {
        return false;
    }

    public int seek(FileTableEntry ftEnt, int offset, int whence) {
        return -1;
    }

}