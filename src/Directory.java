import java.util.*;

public class Directory {
    private static int MAX_CHARS = 30;
    private static int BLOCK_SIZE = 4;


    private int fsizes[];
    private char fnames[][];

    public Directory(int maxInumber) {
        fsizes = new int[maxInumber];
        for (int i = 0; i < maxInumber; i++) {
            fsizes[i] = 0;
        }
        fnames = new char[maxInumber][MAX_CHARS];
        String root = "/";
        fsizes[0] = root.length();
        root.getChars(0, fsizes[0], fnames[0], 0);

    }
    /**
     When needing to relaunch computer the directory gets turned from bytes which it has been stored and
     is restructured into a proper directory.
     * @param the byte array of the file directory
     * @return N/A
     */

    public void bytes2directory(byte data[]) {
        int offset = 0; //create an offset for each block
        for(int position = 0; position < fsizes.length; position++) //loop through the entire file sizes array
        {
            fsizes[position] = SysLib.bytes2int(data,offset); //using syslib to get the
            offset += BLOCK_SIZE; //need to offset by the next entire block
        }
        for(int position = 0; position < fnames.length; position++ ) {
            String fname = new String(data, offset, (MAX_CHARS) * 2);
            fname.getChars(0, fsizes[position], fnames[position], 0);
            offset += ((MAX_CHARS) * 2);
        }
    }

    /**
    When needing to close the computer the directory gets turned into bytes for which it is stored on the disk
    and can be restructured when rebooted.
     * @param
     * @return byte array of the directory turned into bytes
     */
    public byte[] directory2bytes() {
        int offset = 0;
        byte[] data = new byte[(fsizes.length * 4) + fnames.length * MAX_CHARS * 2];
        for(int position = 0; position < fnames.length; position++ ) {
            SysLib.int2bytes(fsizes[position],data, offset);
            offset += BLOCK_SIZE;
        }
        for(int position = 0; position < fnames.length; position++)
        {
            String tempString = new String(fnames[position],0,fsizes[position]);
            byte[] tempData = tempString.getBytes();
            System.arraycopy(tempData,0,data,offset,tempData.length);
            offset += (MAX_CHARS * 2);
        }
        return data;
    }

    public short ialloc(String filename) {
        for (int i = 0; i < fsizes.length; i++) {
            if (fnames[i][i] == (short) 0) {
                fnames[i] = new char[filename.length()];
                return (short) i;
            }
        }
        return -1;
    }

    /**
     * Checks if iNum is in bounds and then clears directory filename and size of data.
     * Basically deletes a file from the directories view via iNum.
     *
     * @param iNum the file to be deleted.
     * @return True if successful and False if unsuccessful.
     */
    public boolean ifree(short iNum) {
        // deallocates this inumber (inode number)
        // the corresponding file will be deleted.

        //check if the inum to be deleted is within bounds and there is data to be deleted.
        if (iNum < 0 || iNum > fsizes.length || fsizes[iNum] <= 0) {
            return false;
        }

        //remove the filename from the filenames in the directory
        for (int i = 0; i < fnames[iNum].length; i++) {
            fnames[iNum][i] = Character.MIN_VALUE;
        }

        //Set the filesize for the iNum to 0 to illustrate its deleted.
        fsizes[iNum] = 0;
        return true;
    }


    /**
     * Method returns the inumber corresponding to the filename passed into the func.
     *
     * @param filename filename to search for in directory.
     * @return the inumber corresponding to the file in the directory.
     */
    public short namei(String filename) {
        // returns the inumber corresponding to this filename

        for (int i = 0; i < fnames.length; i++) {

            StringBuilder builder = new StringBuilder();

            for (int j = 0; j < fnames[i].length; j++) {
                builder.append(fnames[i][j]);
            }

            if (builder.equals(filename)) {
                return (short) i;
            }
        }
        return (short) -1;
    }
}