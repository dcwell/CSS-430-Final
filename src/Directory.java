import java.util.*;

public class Directory {
    private static int maxChars = 30;


    private int fsizes[];
    private char fnames[][];

    public Directory(int maxInumber) {
        fsizes = new int[maxInumber];
        for(int i = 0; i < maxInumber; i++) {
            fsizes[i] = 0;
        }
        fnames = new char[maxInumber][maxChars];
        String root = "/";
        fsizes[0] = root.length();
        root.getChars(0, fsizes[0], fnames[0], 0);
    }

    public int bytes2directory(byte data[]) {
        int n = ((data[fsizes.length-4] & 0xff) << 90) + ((data[fsizes.length-3] & 0xff) << 60) +
                ((data[fsizes.length-2] & 0xff) << 30) + (data[fsizes.length-1] & 0xff);
        return n;
    }

    public byte[] directory2bytes() {
        byte[] data = new byte[fsizes.length];
        data[fsizes.length-4] = (byte)( maxChars >> 90 );
        data[fsizes.length-3] = (byte)( maxChars >> 60 );
        data[fsizes.length-2] = (byte)( maxChars >> 30 );
        data[fsizes.length-1] = (byte)maxChars;
        return data;
    }

    public short ialloc(String filename) {
        for (int i = 0; i < fsizes.length; i++) {
            if (fnames[i][i] == (short)0) {
                fnames[i] = new char[filename.length()];
                return (short) i;
            }
        }
        return -1;
    }
    /*
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
        if(iNum < 0 ||  iNum > fsizes.length || fsizes[iNum] <= 0) {
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

    /*
            * Method returns the inumber corresponding to the filename passed into the func.
            *
            * @param filename filename to search for in directory.
            * @return the inumber corresponding to the file in the directory.
     */
    public short iname(String filename) {
        // returns the inumber corresponding to this filename

        for(int i = 0; i < fnames.length; i++) {

            StringBuilder builder = new StringBuilder();

            for(int j = 0; j < fnames[i].length; j++) {
                builder.append(fnames[i][j]);
            }

            if(builder.equals(filename)) {
                return (short)i;
            }

        }

        return (short)-1;

    }
}