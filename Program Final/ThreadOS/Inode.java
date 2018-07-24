public class Inode {
   private final static int iNodeSize = 32;      // fix to 32 bytes
   private final static int directSize = 11;     // # direct pointers

   public int length;                      				// file size in bytes
   public short count;                     				// # file-table entries pointing to this
   public short flag;                      				// 0 = unused, 1 = used, 2 = delete, 3 = read, 4 = write
   public short direct[] = new short[directSize];		// direct pointers
   public short indirect;                   			// a indirect pointer

   Inode( ) {                           			 	// a default constructor
     length = 0;
     count = 0;
     flag = 1;
     for ( int i = 0; i < directSize; i++ )
       direct[i] = -1;
     indirect = -1;
   }
		
	 // find iNumber inode from disk and save in memeory 
   Inode( short iNumber ) {                  			// retrieving inode from disk
     // design it by yourself.
     int blockNum = 1 + iNumber / 16;							// find the disk block with inode number
     byte data[] = new byte[512];								// setting the buffer size of a block 512 byte
	   SysLib.rawread(blockNum, data);							// read from inode block into data buffer

	   int offset = (iNumber % 16) * iNodeSize;		// locate where we are

	   // locates corresponding inode information in the corresponding block
	   length = SysLib.bytes2int(data, offset);
	   offset += 4;																// increase offset 4 bytes for integer "length"
	   count = SysLib.bytes2short(data, offset);
	   offset += 2;																// increase offset 2 bytes for short
	   flag = SysLib.bytes2short(data, offset);
	   offset += 2;																// increase offset 2 bytes for short
	   for(int i =0; i < directSize; i++) {				// direct pointers
		   direct[i] = SysLib.bytes2short(data, offset);
		   offset += 2;
	   }
	   indirect = SysLib.bytes2short(data, offset);// indirect pointers
       offset+=2;
   }

	 // write the iNumber inode in to disk 
   public void toDisk( short iNumber ) {              
     // design it by yourself.
     
     byte[] data = new byte[512];
     
     int blockNum = 1 + iNumber / 16;							// find the block where the inode is
     SysLib.rawread(blockNum,data);									// read origin block
     
     int offset = (iNumber % 16) * 32;						// find offset in the block 
			SysLib.int2bytes(length, data, offset);			// write to buffer
			offset += 4;
			SysLib.short2bytes(count, data, offset);
			offset += 2;
			SysLib.short2bytes(flag, data, offset);
			offset += 2;
			for(int i = 0; i < directSize; i++) {
				SysLib.short2bytes(this.direct[i], data, offset);
				offset += 2;
			}
			SysLib.short2bytes(this.indirect, data, offset);
			offset += 2;
			
     SysLib.rawwrite(blockNum, data);							// write to disk
   }
        // return the indirect pointer of iNode

   short getIndexBlockNumber(){
   	    return indirect;
   }

   // set indirect block pointer to given index
   boolean setIndexBlockNumber(short indexBlockNumber){
   		//if any direct block unused, return false
		for(int i = 0; i < 11; i++){
			if(direct[i] == -1){
				return false;
			}
		}
		// if the indirect block been used, return false
	   if (indirect != -1){
			return false;
	   }
	   else{
			indirect = indexBlockNumber;
			byte[] data = new byte[512];

		    // write 265 indirect pointers with -1
			for (int i = 0; i < 256; i++){
				SysLib.short2bytes((short)-1, data, i*2);
			}

			SysLib.rawwrite(indexBlockNumber, data);
			return true;
	   }
   }

   // return target block by given offset
   // return -1 on error,
   // if reach indirect block and it hasn't been used return -2
	int setTargetBlock(int offset, short block){
   		int blockNum = offset / 512;
   		// if in the direct block
   		if ( blockNum < 11)
        {
   			// if the direct block been used, return error
   			if(direct[blockNum] != -1)
				return -1;
			direct[blockNum] = block;
			return blockNum;
		}
		// if indirect pointer been used
		else if (indirect > 0) {
			byte[] indblock = new byte[512];
			// read to buffer
			SysLib.rawread(indirect,indblock);
			// find indirect pointer
			int indNumber = blockNum - 11;
			SysLib.short2bytes(block, indblock, indNumber * 2);
			SysLib.rawwrite(indirect, indblock);
			return blockNum;
		}
		// if the indirect < 0
		return -2;
   }

   public short findTargetBlock(int seekPtr)
   {
        int block, iOffset;
        block = seekPtr / Disk.blockSize;

        if(block < directSize)
            return direct[block];
        if(indirect == -1)
            return -1;

        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(indirect, data);
        iOffset = block - directSize;
        return SysLib.bytes2short(data, iOffset * 2);
   }

   public byte[] freeIndirectBlock()
   {
        if(indirect == -1)
            return null;
        byte[] data = new byte[512];
        SysLib.rawread(indirect, data);
        indirect = -1;
        return data;
   }
}
