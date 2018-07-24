// A disk thread
class TestThread3B extends Thread{
	
	byte[] buffer = new byte[512]; //one disk block
	
	public void run(){
		for(int i = 0; i < 1000; i++){
			SysLib.rawwrite(i,buffer); // write
			SysLib.rawread(i,buffer);  // read
		}
		SysLib.cout("disk finished...\n");
		SysLib.exit();	
	}
}
