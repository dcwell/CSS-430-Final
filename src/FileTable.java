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

            if(iNumber > -1) {

                newNode = new Inode(iNumber);
                if(mode.equals("r")) {

                    if(newNode.flag == 0 || newNode.flag == 1 || newNode.flag == 2) {
                        newNode.flag = 2;
                        break;
                    } else {
                        try {
                            wait();
                        } catch(InterruptedException e) {}
                    }

                } else {

                    if(newNode.flag == 0 || newNode.flag == 1 || newNode.flag == 3) {
                        newNode.flag = 3;
                        break;
                    } else {
                        try {
                            wait();
                        } catch(InterruptedException e) {}
                    }

                }

            } else if(!mode.equals("r")) {

                iNumber = dir.ialloc(fileName);
                newNode = new Inode(iNumber);
                newNode.flag = 3;
                break;

            } else {
                return null;
            }


        }

        //this is FOR SURE GOOD
        newNode.count++; //incrememt the inode count
        newNode.toDisk(iNumber); //immediatly write this inode to the disk4
        FileTableEntry entry = new FileTableEntry(newNode, iNumber, mode); //make the FTE to return
        table.add(entry); //add the new FTE to the table
        return entry;//return a refrence to the file table entry
    }


    public synchronized boolean ffree(FileTableEntry entry) {
        if(table.removeElement(entry)) {
            entry.inode.count--;
            int flag = entry.inode.flag;
            //The node is being read so reset to default
            if(flag == 1 || flag == 2)
                entry.inode.flag = 0;
            //Reset the node to bein in a special condition
            if(flag == 4 || flag == 5)
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