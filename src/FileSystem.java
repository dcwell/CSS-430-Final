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

    /**
     * syncs the data from the directory back to the disk
     */
    public void sync() {
        FileTableEntry dirEnt = open("/","r");
        byte[] data = directory.directory2bytes();
        write(dirEnt,data); //write back to disk
        close(dirEnt);
        superblock.sync();
    }

    /**
     * formats the files after being shut down previously
     * @param files
     * @return
     */
    public boolean format(int files) {
        directory = new Directory(superblock.inodeBlocks);
        filetable = new FileTable(directory);
        superblock.format(files);
        return true;
    }

    /**
     * allocates a new file
     *
     * @param filename the name of file to be opened
     * @param mode the mode to open the file into
     * @return A File table entry that correspons to the file with the given mode 
     */
    public FileTableEntry open(String filename, String mode) {
        FileTableEntry ftEnt = filetable.falloc(filename, mode);
        if(mode.equals("w")) {
            if(deallocAllBlocks(ftEnt) == false)
                return null;
        }
        return ftEnt;
    }

    /**
     *
     * @param ftEnt
     * @return
     */
    public boolean close(FileTableEntry ftEnt) {
        synchronized (ftEnt) {
            ftEnt.count--;
            if(ftEnt.count == 1) {
                return filetable.ffree(ftEnt);
            }
            return true;
        }
    }

    /**
     *
     * @param ftEnt
     * @return
     */
    public int fsize(FileTableEntry ftEnt) {
        synchronized (ftEnt) {
            return ftEnt.inode.length;
        }
    }

    /**
     *
     * @param ftEnt
     * @param buf
     * @return
     */
    public int read(FileTableEntry ftEnt, byte[] buf) {

        return -1;
    }

    /**
     *
     * @param ftEnt
     * @param buf
     * @return
     */
    public int write(FileTableEntry ftEnt, byte[] buf) {
        return -1;
    }

    /**
     *
     * @param ftEnt
     * @return
     */
    private boolean deallocAllBlocks(FileTableEntry ftEnt) {
        short iNumber = ftEnt.iNumber;
        if(directory.ifree(iNumber))
            return true;
        return false;
    }

    /**
     *
     * @param filename
     * @return
     */
    public boolean delete(String filename) {
        FileTableEntry ftEnt = open(filename, "w");
        short iNumber = ftEnt.iNumber;
        if(close(ftEnt) && directory.ifree(iNumber))
            return true;
        return false;
    }

    /**
     *
     * @param ftEnt
     * @param offset
     * @param whence
     * @return
     */
    public int seek(FileTableEntry ftEnt, int offset, int whence) {
        return -1;
    }
}