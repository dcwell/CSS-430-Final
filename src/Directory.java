import java.util.*;

public class Directory {
    private static int maxChars = 30;

    private int fsize[];    // each element stores a different file size.
    private char fnames[][];    // each element stores a different file name.

    public Directory(int maxInumber) {
        fsizes = new int[maxInumber];
        for(int i = 0; i < maxInumber; i++) {
            fsize[i] = 0;
        }
        fnames = new char[maxInumber][maxChars];
        String root = "/";
        fsize[0] = root.length();
        root.getChars(0, fsizes[0], fnames[0], 0);
    }

    public void bytes2directory(byte data[]) {
        // assumes data[] received directory information from disk
        // initializes the Directory instance with this data[]
    }

    public byte[] directory2bytes() {
        // converts and return Directory information into a plain byte array
        // this byte array will be written back to disk
        // note: only meaningfull directory information should be converted
        // into bytes.
    }

    public short ialloc(String filename) {
        // filename is the one of a file to be created.
        // allocates a new inode number for this filename
    }

    public boolean ifree(short iNum) {
        // deallocates this inumber (inode number)
        // the corresponding file will be deleted.
    }

    public short iname(String filename) {
        // returns the inumber corresponding to this filename
    }
}