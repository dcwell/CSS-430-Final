import java.util.*;

public class FileTable {
    private Vector<FileTableEntry> table;
    private Directory dir;

    public FileTable(Directory directory) {
        table = new Vector();
        dir = directory;
    }

    /**
     * @param fileName
     * @param mode
     * @return
     */

    public synchronized FileTableEntry falloc(String fileName, String mode) {
        //need to set these up to eventually make a FileTableEntry with.
        Inode theINode = null;
        short iNumber = -1;

        while (true) {
            if (fileName.equals("/"))
                iNumber = 0;
            else
                iNumber = dir.namei(fileName);


            if (iNumber < 0) {
                if (mode.equals("r"))
                    return null;
            } else { //get the new Inode given the Inumber
                theINode = new Inode(iNumber);
                if (theINode.equals("r")) {
                    if (theINode.flag != 0 && theINode.flag != 1) {
                        try {
                            wait();
                        } catch (InterruptedException e) {

                        }
                        continue;
                    }
                    theINode.flag = 1;
                    break;
                }
                //if they are in different modes
                if (theINode.flag != 0 && theINode.flag != 3) {
                    if (theINode.flag == 1 || theINode.flag == 2) {
                        theINode.flag = (short) 4;
                        theINode.toDisk(iNumber);
                    }
                    try {
                        wait();
                    } catch (InterruptedException exception) {
                    }
                    continue;
                }
                theINode.flag = 2;
                break;
            }
            iNumber = dir.ialloc(fileName);
            theINode = new Inode();
            theINode.flag = 2;
            break;
        }


        //this is FOR SURE GOOD
        theINode.count++; //incrememt the inode count
        theINode.toDisk(iNumber); //immediatly write this inode to the disk4
        FileTableEntry entry = new FileTableEntry(theINode, iNumber, mode); //make the FTE to return
        table.addElement(entry); //add the new FTE to the table
        return entry;//return a refrence to the file table entry
    }

    /**
     * Recieve a file table entry reference
     * Save the corresponding inode to the disk
     * free this file table entry
     * return true if this file table entry found in my table
     *
     * @param entry FileTable entry we are trying to free
     * @return true if freed, false if not
     */
    public synchronized boolean ffree(FileTableEntry entry) {
        if (table.removeElement(entry)) {
            entry.inode.count--;
            int flag = entry.inode.flag;
            //The node is being read so reset to default
            if (flag == 1 || flag == 2)
                entry.inode.flag = 0;
            //Reset the node to bein in a special condition
            if (flag == 4 || flag == 5)
                entry.inode.flag = 3;
            entry.inode.toDisk(entry.iNumber);
            notify();
            return true;
        }
        return false;
    }

    public synchronized boolean fempty() {
        return table.isEmpty();
    }
}