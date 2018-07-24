public class Directory
{
    private static final int CHAR_BYTES = 2;
    private static final int INT_BYTES = 4;
    private static final int ERROR = -1;
    private static int maxChars = 30;
    private int fsizes[];
    private char fnames[][];

    public Directory(int maxInumber)
    {
        fnames = new char[maxInumber][maxChars];
        fsizes = new int[maxInumber];
        for(int i = 0; i < maxInumber; i++)
            fsizes[i] = 0;

        String root = "/";
        fsizes[0] = root.length();
        root.getChars(0, fsizes[0], fnames[0], 0);
    }

    // filename is the one of a file to be created.
    // allocates a new inode number for this filename
    public short ialloc(String fName)
    {
        for(short i = 1; i < fsizes.length; i++)
        {
            if(fsizes[i] != 0)
                continue;
            int size = fName.length(); 
            if(size > maxChars)
                size = maxChars;

            fsizes[i] = size;
            fName.getChars(0, fsizes[i], fnames[i], 0);
            return i;
        }
        return ERROR;
    }

    // deallocates this inumber (inode number)
    // the corresponding file will be deleted.
    public boolean ifree(short iNumber)
    {
        if(fsizes[iNumber] == 0)
            return false;
        fsizes[iNumber] = 0;
        return true;
    }

    // returns the inumber corresponding to this filename
    public short namei(String fName)
    {
        for(short i = 0; i < fsizes.length; i++)
        {
            if(fsizes[i] != fName.length())
                continue;
            String cur = new String(fnames[i], 0, fsizes[i]);
            if(cur.equals(fName))
                return i;
        }
        return ERROR;
    }

    // assumes data[] received directory information from disk
    // initializes the Directory instance with this data[]
    public void bytes2directory(byte data[])
    {
        int offset = 0;
        for(int i = 0; i < fsizes.length; i++, offset+= 4)
            fsizes[i] = SysLib.bytes2int(data, offset);
        for(int i = 0; i < fnames.length; i++, offset += maxChars*2)
        {
            String fname = new String(data, offset, maxChars*2);
            fname.getChars(0, fsizes[i], fnames[i], 0);
        }
    }

    // converts and return Directory information into a plain byte array
    // this byte array will be written back to disk
    // note: only meaningfull directory information should be converted
    // into bytes.
    public byte[] directory2bytes()
    {
        byte[] dirBuf = new byte[(fsizes.length * INT_BYTES) + (fnames.length * maxChars * CHAR_BYTES)];
        int offSet = 0;

        for(int i = 0; i < fsizes.length; i++, offSet += INT_BYTES)
            SysLib.int2bytes(fsizes[i], dirBuf, offSet);
        for(int i = 0; i < fnames.length; i++, offSet += maxChars * CHAR_BYTES)
        {
            String fname = new String(fnames[i], 0, fsizes[i]);
            byte dirBytes[] = fname.getBytes();

            for(int j = 0; i < dirBytes.length; j++)
                dirBuf[j + offSet] = dirBytes[j];
        }
        return dirBuf;
    }
}