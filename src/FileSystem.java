/**
 * Code by: Denali Cornwell & Jayden Stipek
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
            ; // wait
        }

        directory = new Directory(superblock.inodeBlocks);
        filetable = new FileTable(directory);
        superblock.format(files);
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
            if (deallocAllBlocks(ftEnt) == false)
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
            if (ftEnt.count > 0) { // if someone is using it
                return false;
            }
            return filetable.ffree(ftEnt);

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
        if (buf == null)
            return -1;
        if (ftEnt.mode != "w" && ftEnt.mode != "a") {
            int trackDataRead = 0;
            int size = buf.length;
            synchronized (ftEnt) {
                //one stop when the seek pointer is still within rang
                // and buff isnt full
                while (buf.length > 0 && ftEnt.seekPtr < fsize(ftEnt)) {
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
        return -1;
    }

    /**
     * @param ftEnt
     * @param buf
     * @return
     */
    public int write(FileTableEntry ftEnt, byte[] buf) {
        //if nothing is to write
        if (buf.length == 0 || ftEnt == null)
            return -1;

        if (ftEnt.mode.equals("r"))
            return -1;

        synchronized (ftEnt) {
            int offset = 0;
            int size = buf.length;
            while (size > 0) {
                //get Block num
                int blockNum = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                if (blockNum == -1) {
                    short freeBlock = (short) superblock.getFreeBlock();
                    short result = (short) ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, freeBlock);
                    if (result == -3) {
                        short nextFreeBlock = (short) superblock.getFreeBlock();

                        //if we are update the block, then that is an error
                        if (ftEnt.inode.registerIndexBlock(nextFreeBlock))
                            return -1;
                        if (ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, freeBlock) != 0)
                            return -1;
                        //if we do not succeed on updating the seek ptr
                    } else if (result == 0)
                        blockNum = freeBlock;
                        //the direct ptr is bad
                    else if (result == -1 || result == -2)
                        return -1;
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
            if (block >= 11) {
                ftEnt.inode.toDisk(ftEnt.iNumber);
                return true;
            }

            if (ftEnt.inode.direct[block] != -1) {
                superblock.returnBlock(block);
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
            if(ftEnt == null) return -1;

            if (whence == SEEK_SET) {
                if(offset <= fsize(ftEnt) && offset >=0)
                    ftEnt.seekPtr = offset;
            } else if (whence == SEEK_CUR) {
                if(ftEnt.seekPtr + offset <= fsize(ftEnt) && ((ftEnt.seekPtr + offset) >= 0))
                    ftEnt.seekPtr += offset;
            } else if (whence == SEEK_END) {
                if(fsize(ftEnt) + offset >= 0 && fsize(ftEnt) + offset <= fsize(ftEnt))
                    ftEnt.seekPtr = ftEnt.inode.length + offset;
                else
                    return -1;
            }
            return ftEnt.seekPtr;
        }
    }
}