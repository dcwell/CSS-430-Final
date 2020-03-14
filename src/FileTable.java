import java.util.*;

public class FileTable {
    private Vector<FileTableEntry> table;
    private Directory dir;

    public FileTable(Directory directory) {
        table = new Vector();
        dir = directory;
    }

    public synchronized FileTableEntry falloc(String fileName, String mode) {
        //need to set these up to eventually make a FileTableEntry with.
        Inode newNode;
        short iNumber;

        while(true) {

            if(fileName.equals("/")) {
                iNumber = 0;
            } else {
                iNumber = dir.namei(fileName);
            }

            newNode = new Inode(iNumber);

            if(iNumber > -1) {
                if(mode.equals("r")) { //present and requesting read

                    if(newNode.flag == 3) {
                        try {
                            wait();
                        } catch(InterruptedException e) {}
                    } else {
                        newNode.flag = 2;
                        break;
                    }

                } else { //if present and requesting write
                    if(newNode.flag == 0 || newNode.flag == 1) {
                        newNode.flag = 3;
                        break;
                    } else {
                        try {
                            wait();
                        } catch(InterruptedException e) {}
                    }
                }

                //NEEDS ONE MORE CASE FOR NON-PRESENT INUMBER
                //SHOULD USE DIR.IALLOC(FILENAME) TO ALLOCATE A SPACE
                //FOR FILE THAT DOES NOT ALREADY EXIST.

            } else {
                return null;
            }

        }

        //this is FOR SURE GOOD
        newNode.count++; //incrememt the inode count
        newNode.toDisk(iNumber); //immediatly write this inode to the disk
        FileTableEntry entry = new FileTableEntry(newNode, iNumber, mode); //make the FTE to return
        table.add(entry); //add the new FTE to the table
        return entry;//return a refrence to the file table entry
    }

    public synchronized boolean ffree(FileTableEntry entry) {
        return false;
    }

    public synchronized boolean fempty() {
        return table.isEmpty();
    }
}