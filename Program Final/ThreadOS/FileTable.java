import java.util.Vector;
public class FileTable {
    private static final int DELETE = 2;
    private final static int READ = 3;
    private static final int WRITE = 4;

    private Vector<FileTableEntry> table;         // the actual entity of this file table
    private Directory dir;        // the root directory

    public FileTable( Directory directory ) { // constructor
        table = new Vector<FileTableEntry>( );     // instantiate a file (structure) table
        dir = directory;           // receive a reference to the Director
    }                             // from the file system

    // major public methods
    public synchronized FileTableEntry falloc( String filename, String mode ) {
        // allocate a new file (structure) table entry for this file name
        // allocate/retrieve and register the corresponding inode using dir
        // increment this inode's count
        // immediately write back this inode to the disk
        // return a reference to this file (structure) table entry

        short iNum = -1;
        Inode inode = null;

        while (true)
        {
            // since directory considered as a file, if it is a root, then set to inode 0
            if (filename.equals("/"))
                iNum = 0;
            else
                iNum = this.dir.namei(filename);

            if (iNum < 0)
            {
                if(!mode.equals("r"))
                {
                    inode = new Inode();
                    inode.flag = WRITE;
                    iNum = dir.ialloc(filename);
                }
                break;
            }
            else if(iNum >= 0) 
            {
                inode = new Inode(iNum);

                if(mode.equals("r"))
                {
                    if(inode.flag == READ)
                        break;
                    else if(inode.flag == WRITE)
                    {
                        try { wait(); }
                        catch (InterruptedException e) {}
                    }
                    else
                    {
                        inode.flag = READ;
                        break;
                    }
                }
                else if(mode.equals("w") || mode.equals("w+") || mode.equals("a"))
                {
                    if(inode.flag == READ || inode.flag == WRITE)
                    {
                        try { wait(); }
                        catch (InterruptedException e) {}
                    }
                    else
                        inode.flag = WRITE;
                        break;
                }
            }
        }

        if(inode == null)
            return null;
        FileTableEntry newEntry = new FileTableEntry(inode,iNum,mode); // allocate a new file table entry for this file name
        table.add(newEntry);    // add new entry to filetable
        inode.count++;          // increment this inode's count
        inode.toDisk(iNum);     // write back inode to the disk 
        return newEntry;
    }

    public synchronized boolean ffree( FileTableEntry e ) {
        // receive a file table entry reference
        // save the corresponding inode to the disk
        // free this file table entry.
        // return true if this file table entry found in my table
        int entry = -1;
        // find entry index
        for(int i = 0; i <table.size(); i++){
            if(table.elementAt(i).equals(e))
                entry = i;
        }

        // if cant find entry, return false
        if (entry == -1)
            return false;

        // decrease threads use entry
        e.inode.count--;
        if (e.inode.count == 0)
            e.inode.flag = 0;
        e.inode.toDisk(e.iNumber);		                //save updated to disk
        return table.remove(table.elementAt(entry));   //remove e from table
    }


    public synchronized boolean fempty( ) {
        return table.isEmpty( );  // return if table is empty
    }                            // should be called before starting a format
}
