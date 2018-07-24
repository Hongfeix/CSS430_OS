import java.util.Arrays;

public class Cache{

	public class Entry{
		public int frameIndex;    // disk block nuber of cached data
		public boolean reference; // check if block been access
		public boolean dirtyBit;  // if need to write back to disk 
		public byte[] buffer; 	 
		
		public Entry(int blockSize) {
			frameIndex = -1;
			reference = false;
			dirtyBit = false;
			buffer = new byte[blockSize];
		}
	}

	private int victim; // index of next victim
	private Entry[] pageTable;
	

	// initial number of cache blocks and each block size
	public Cache(int blockSize, int cacheBlock){
		pageTable = new Entry[cacheBlock];
		victim = 0;
		for (int i = 0; i < pageTable.length; i++){
			pageTable[i] = new Entry(blockSize); // initial each cache blook
		}
	}
	
	// set the index of next victim 
	public void nextVictim(){
		for (int i = victim; i < pageTable.length; i++){
			if (pageTable[i].frameIndex == -1){
				pageTable[i].reference = false;
				victim = i;
				break;
			}
		}
		
		// set next victim
		while(pageTable[victim].reference == true){
			pageTable[victim].reference = false;
			victim = (victim + 1) % pageTable.length;
		}
		
		// if this victim is dirty, write back contents to disk
		if (pageTable[victim].dirtyBit == true){
			SysLib.rawwrite(pageTable[victim].frameIndex, pageTable[victim].buffer);
		}			
	}
	
	// read into buffer specified by blockID, if not in cache, read disk block
	public synchronized boolean read (int blockID, byte buffer[]){
		// if block in cache 
		for (int i = 0; i < pageTable.length; i++){
			if (pageTable[i].frameIndex == blockID){
				pageTable[i].reference = true;
        System.arraycopy(pageTable[i].buffer, 0, buffer, 0, buffer.length);
        return true;
			}
		}
		// if block not in cache
		try{
			nextVictim();
			SysLib.rawread(blockID, buffer);
			System.arraycopy(buffer, 0, pageTable[victim].buffer, 0, buffer.length);
			pageTable[victim].frameIndex = blockID;
			pageTable[victim].reference = true;
			pageTable[victim].dirtyBit = false;
			victim = (victim + 1) % pageTable.length;
		}
		catch (Exception e){
			return false;
		}
		return true;
	}
	
	// write buffer in to cache with blockID, if not in cache, find free buffer block
	public synchronized boolean write(int blockID, byte buffer[]){
		// if block in cache 
		for (int i = 0; i < pageTable.length; i++){
			if (pageTable[i].frameIndex == blockID){
				pageTable[i].reference = true;
				pageTable[i].dirtyBit = true;
        System.arraycopy(buffer, 0, pageTable[i].buffer, 0, buffer.length);
        return true;
			}
		}
		// if block not in cache
		try{
			nextVictim();
			System.arraycopy(buffer, 0, pageTable[victim].buffer, 0, buffer.length);
			pageTable[victim].frameIndex = blockID;
			pageTable[victim].reference = true;
			pageTable[victim].dirtyBit = true;
			victim = (victim + 1) % pageTable.length;
		}
		catch (Exception e){
			return false;
		}
		return true;
	}
	
	// write back all dirty cache block to DISK
	public void sync(){
		for ( int i = 0; i < pageTable.length; i++){
			if ( pageTable[i].dirtyBit == true){
					SysLib.rawwrite(pageTable[i].frameIndex, pageTable[i].buffer);
					pageTable[i].dirtyBit = false;
			}
		}
	}
	
	// write back all dirty cache block to DISK and initial cache
	public void flush(){
		for ( int i = 0; i < pageTable.length; i++){
			if ( pageTable[i].dirtyBit == true){
					SysLib.rawwrite(pageTable[i].frameIndex, pageTable[i].buffer);
					pageTable[i].dirtyBit = false;
			}
			// reset each entry
			pageTable[i].frameIndex = -1;
			pageTable[i].reference = false;
		}	
		victim = 0;	
	}	
}

