import java.util.*;

public class FileTable {
    private Vector table;
    private Directory dir;

    public FileTable(Directory directory) {
        table = new Vector();
        dir = directory;
    }

    public synchronized FileTableEntry falloc(String filename, String mode) {
        //BUNCH OF COMMENTS IN SLIDES
        return new FileTableEntry(new Inode(), (short)1, "foo");
    }

    public synchronized boolean ffree(FileTableEntry e) {
        //BUNCH OF COMMENTS IN SILDES
        return false;
    }

    public synchronized boolean fempty() {
        return table.isEmpty();
    }


}