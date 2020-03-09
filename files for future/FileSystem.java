import java.util.*;

public class FileSystem {
    private SuperBlock superBlock;
    private Directory directory;
    private FileTable fileTable;

    public FileSystem(int diskBlocks) {
        superBlock = new SuperBlock(diskBlocks);
        directory = new Directory(superBlock.inodeBlocks);
        fileTable = new FileTable(directory);

        FileTableEntry dirEnt = open("/", "r");
        int dirSize = fsize(dirEnt);
        if(dirSize > 0) {
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytes2directory(dirData);
        }
        close(dirEnt);
    }

    public void sync() {

    }

    public boolean format(int files) {
        return false;
    }

    public FileTableEntry open(String filename, String mode) {
        return new FileTableEntry(new Inode(), (short)1, "bar");
    }

    public boolean close(FileTableEntry ftEnt) {
        return false;
    }

    public int fsize(FileTableEntry ftEnt) {
        return -1;
    }

    public int read(FileTableEntry ftEnt, byte[] buffer) {
        return -1;
    }

    public int write(FileTableEntry ftEnt, byte[] buffer) {
        return -1;
    }

    private boolean deallocateAllBlocks(FileTableEntry ftEnt) {
        return false;
    }

    public boolean delete(String filename) {
        return false;
    }

    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    public int seek(FileTableEntry ftEnt, int offset, int whence) {
        return -1;
    }

}