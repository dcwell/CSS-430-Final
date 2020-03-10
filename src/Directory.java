import java.util.*;

public class Directory {
    private static int maxChars = 30;

    private int fsize[];
    private char fnames[][];

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

    }

    public byte[] directory2bytes() {

    }

    public short ialloc(String filename) {

    }
    
    public boolean ifree(short iNum) {

    }

    public short iname(String filename) {

    }
}