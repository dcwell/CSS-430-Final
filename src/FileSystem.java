public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;


    public FileSystem(int diskBlocks) {
        superblock = new SuperBlock(diskBlocks);
        directory = new Directory(superblock.inodeBlocks);
        filetable = new FileTable(directory);

        //read the "/" file from disk
        FileTableEntry dirEnt = open("/", "r");
        int dirSize = fsize(dirEnt);
        if(dirSize > 0)
        {
            //The directory has some data
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytes2directory(dirData);
        }
        close(dirEnt);
    }

    public void sync() {
        FileTableEntry dirEnt = open("/","r");
        byte[] data = directory.directory2bytes();
        write(dirEnt,data);
        close(dirEnt);
        superblock.sync();
    }

    public boolean format(int files) {
        FileTableEntry dirEnt = open("/","r");
        directory = new Directory(superblock.inodeBlocks);
        filetable = new FileTable(directory);
        superblock.format(files);
        return true;
    }

    public FileTableEntry open(String filename, String mode) {
        FileTableEntry ftEnt = filetable.falloc(filename, mode);
        if(mode.equals("w")) {
            if(deallocAllBlocks(ftEnt) == false)
                return null;
        }
        return ftEnt;
    }

    public boolean close(FileTableEntry ftEnt) {
        synchronized (ftEnt) {
            ftEnt.count--;
            if(ftEnt.count == 1) {
                return filetable.ffree(ftEnt);
            }
            return true;
        }
    }

    public int fsize(FileTableEntry ftEnt) {
        synchronized (ftEnt) {
            return ftEnt.inode.length;
        }
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