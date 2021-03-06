/**
 * @authors Denali Cornwell & Jayden Stipek
 * Revised: March,15 2020
 * This class is the FileSystem class that will represent the FileSystem in a Unix-Like file system.
 */
public class FileSystem {
    //Super block to control all blocks and freelist
    private SuperBlock superblock;
    //Directory files will be in
    private Directory directory;
    //Tracks files and their statuses/modes
    private FileTable filetable;

    //Help for the seek method.
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;


    /**
     * Constructor for the file system class. Given in slides.
     *
     * @param diskBlocks disk blocks to set up the file system with, should be 1000 in our case.
     */
    public FileSystem(int diskBlocks) {
        superblock = new SuperBlock(diskBlocks);
        directory = new Directory(superblock.inodeBlocks);
        filetable = new FileTable(directory);

        //read the "/" file from disk
        FileTableEntry dirEnt = open("/", "r");
        int dirSize = fsize(dirEnt);
        if (dirSize > 0) {
            //The directory has some data
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytes2directory(dirData);
        }
        close(dirEnt);
    }

    /**
     * Syncs the data from the directory back to the disk.
     */
    public void sync() {
        FileTableEntry dirEnt = open("/", "r");
        byte[] data = directory.directory2bytes();
        write(dirEnt, data); //write back to disk
        close(dirEnt);
        superblock.sync();
    }

    /**
     * formats the files after being shut down previously
     *
     * @param files number of files to format
     * @return true if success
     */
    public boolean format(int files) {
        while (!filetable.fempty()) {
        }

        superblock.format(files);
        directory = new Directory(superblock.inodeBlocks);
        filetable = new FileTable(directory);
        return true;
    }

    /**
     * Allocates a new fd with filename and mode.
     *
     * @param filename the name of file to be opened
     * @param mode     the mode to open the file into
     * @return A File table entry that correspons to the file with the given mode
     */
    public FileTableEntry open(String filename, String mode) {
        FileTableEntry ftEnt = filetable.falloc(filename, mode);
        if (mode.equals("w")) {
            if (!deallocAllBlocks(ftEnt))
                return null;
        }
        return ftEnt;
    }

    /**
     * Closes an fd from the file table.
     *
     * @param ftEnt entry to close.
     * @return false if fail, true on success.
     */
    public boolean close(FileTableEntry ftEnt) {
        synchronized (ftEnt) {
            ftEnt.count--;
            if (ftEnt.count <= 0) { // if someone is using it
                return filetable.ffree(ftEnt);
            }
            return true;
        }
    }

    /**
     * Returns the size of a file
     *
     * @param ftEnt the target file
     * @return file size as int
     */
    public int fsize(FileTableEntry ftEnt) {
        synchronized (ftEnt) {
            return ftEnt.inode.length;
        }
    }

    /**
     * Reads to a buffer from an fd. Reads from seek ptr location.
     *
     * @param ftEnt fd to read from
     * @param buf   buffer to read into
     * @return failure status or amount of data read.
     */
    public int read(FileTableEntry ftEnt, byte[] buf) {
        if (buf == null || ftEnt == null)
            return -1;
        if (ftEnt.mode == "w" || ftEnt.mode == "a") return -1;
        int trackDataRead = 0;
        int size = buf.length;
        synchronized (ftEnt) {
            //one stop when the seek pointer is still within rang
            // and buff isnt full
            while (size > 0 && ftEnt.seekPtr < fsize(ftEnt)) {
                //get Block num
                int blockNum = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                if (blockNum != -1) {
                    byte[] tempRead = new byte[Disk.blockSize];
                    //this is the location to read from
                    SysLib.rawread(blockNum, tempRead);

                    int dataInto = ftEnt.seekPtr % Disk.blockSize;
                    int remainingBlocks = Disk.blockSize - dataInto;
                    int remainingBytes = fsize(ftEnt) - ftEnt.seekPtr;

                    int leftToRead = Math.min(Math.min(remainingBlocks, size), remainingBytes);
                    System.arraycopy(tempRead, dataInto, buf, trackDataRead, leftToRead);
                    //update the variable to read into the byte array
                    trackDataRead += leftToRead;
                    //Update The Seek Pointer
                    ftEnt.seekPtr += leftToRead;
                    //Update the size
                    size -= leftToRead;
                } else {
                    //wrong block locations
                    break;
                }
            }
            return trackDataRead;
        }
    }

    /**
     * writes the contents of the buffer to the file
     * indicated by fd -> starting at the position indicated by the seek pointer.
     *
     * @param ftEnt the FileTableEntry that we want to write the data into
     * @param buf the buffer that has the data that needs to be rewritten
     * @return the location of where it was written on disk
     */
    public int write(FileTableEntry ftEnt, byte[] buf) {
        //if nothing is to write
        if (buf.length == 0 || ftEnt == null) {
            SysLib.cout("buf length 0");
            return -1;
        }

        if (ftEnt.mode.equals("r")) {
            SysLib.cout("buf length 0");
            return -1;
        }

        synchronized (ftEnt) {
            int offset = 0;
            int size = buf.length;
            while (size > 0) {
                int blockNum = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                if (blockNum == -1) {
                    short freeBlock = (short)this.superblock.getFreeBlock();
                    switch(ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, freeBlock)) {
                        case -3:
                            short nextFreeBlock = (short)this.superblock.getFreeBlock();
                            if (!ftEnt.inode.registerIndexBlock(nextFreeBlock)) {
                                SysLib.cerr("ThreadOS: panic on write\n");
                                return -1;
                            }

                            if (ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, freeBlock) != 0) {
                                SysLib.cerr("ThreadOS: panic on write\n");
                                return -1;
                            }
                        case 0:
                        default:
                            blockNum = freeBlock;
                            break;
                        case -1:
                        case -2:
                            SysLib.cerr("ThreadOS: filesystem panic on write\n");
                            return -1;
                    }
                }
                byte[] tempRead = new byte[Disk.blockSize];
                //this is the location to read from what it is pointing to
                if (SysLib.rawread(blockNum, tempRead) == -1)
                    System.exit(2);
                int position = ftEnt.seekPtr % Disk.blockSize;
                int remaining = Disk.blockSize - position;
                int availablePlace = Math.min(remaining, size);
                System.arraycopy(buf, offset, tempRead, position, availablePlace);
                SysLib.rawwrite(blockNum, tempRead);
                ftEnt.seekPtr += availablePlace;
                offset += availablePlace;
                size -= availablePlace;

                if (ftEnt.seekPtr > ftEnt.inode.length)
                    ftEnt.inode.length = ftEnt.seekPtr;

            }
            //update the inode
            ftEnt.inode.toDisk(ftEnt.iNumber);
            return offset;
        }
    }

    /**
     * Deallocates all blocks associated with the fd file table entry.
     *
     * @param ftEnt fd to deallocate block from
     * @return true if success, false if not.
     */
    private boolean deallocAllBlocks(FileTableEntry ftEnt) {
        if (ftEnt.inode.count != 1) {
            return false;
        }
        byte[] trash = ftEnt.inode.unregisterIndexBlock();
        if (trash != null) {
            short ptr;
            while ((ptr = SysLib.bytes2short(trash, 0)) != -1) {
                superblock.returnBlock(ptr);
            }
        }

        for (int block = 0; block < ftEnt.inode.directSize; block++) {

            if (ftEnt.inode.direct[block] != -1) {
                superblock.returnBlock(ftEnt.inode.direct[block]);
                ftEnt.inode.direct[block] = -1;
            }
        }

        ftEnt.inode.toDisk(ftEnt.iNumber);
        return true;
    }

    /**
     * Deletes the file and frees the block.
     *
     * @param filename file to delete
     * @return true if success, false if not.
     */
    public boolean delete(String filename) {
        FileTableEntry ftEnt = open(filename, "w");
        short iNumber = ftEnt.iNumber;
        if (close(ftEnt) && directory.ifree(iNumber))
            return true;
        return false;
    }

    /**
     * Allows for seeking accross a files bytes.
     *
     * @param ftEnt  entry to change the seekPtr in
     * @param offset Amount of bytes specified to deiviate from original position
     * @param whence refrence of where to start the seeking.
     * @return -1 on fail or the new value of seekPtr
     */
    public int seek(FileTableEntry ftEnt, int offset, int whence) {
        synchronized (ftEnt) {
            if (ftEnt == null) return -1;

            if (whence == SEEK_SET) {
                if (offset <= fsize(ftEnt) && offset >= 0)
                    ftEnt.seekPtr = offset;
            } else if (whence == SEEK_CUR) {
                if (ftEnt.seekPtr + offset <= fsize(ftEnt) && ((ftEnt.seekPtr + offset) >= 0))
                    ftEnt.seekPtr += offset;
            } else if (whence == SEEK_END) {
                if (fsize(ftEnt) + offset >= 0 && fsize(ftEnt) + offset <= fsize(ftEnt))
                    ftEnt.seekPtr = ftEnt.inode.length + offset;
                else
                    return -1;
            }
            return ftEnt.seekPtr;
        }
    }
}