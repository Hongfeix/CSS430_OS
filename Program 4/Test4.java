import java.util.Date;
import java.util.Random;

class Test4 extends Thread{
	private boolean enabled;
	private int tcase; // test case 
	private byte[] writeBytes;
	private byte[] readBytes;
	private Random rand;
	private long start; // start time 
	private long end;  // end time 
	
	public Test4(String[] args){
		if (args[0].equals("enabled")){
			enabled = true;
		}
		else{
			enabled = false;
		}
		tcase = Integer.parseInt(args[1]);
		writeBytes = new byte [512]; // each disk block contain 512 bytes
		readBytes = new byte [512];
		rand = new Random();
	}
	
	// determine if use cache read 
	private void read(int block, byte[] buffer){
		// cache read 
		if (enabled == true){
			SysLib.cread(block,buffer);
		}
		// raw read
		else{
			SysLib.rawread(block,buffer);
		}
	}
	
	// determine if use cache write
	private void write(int block, byte[] buffer){
		// cache write
		if (enabled){
			SysLib.cwrite(block, buffer);
		}
		// raw write
		else{
			SysLib.rawwrite(block,buffer);
		}
	}
	
	private void randomA(){
	
		int[] arr = new int[100];

		for (int i = 0 ; i < 100; i++){
			arr[i] = Math.abs(rand.nextInt() % 512);
			write(arr[i], writeBytes);
		}
		
		for (int i = 0; i < 100; i++){
			read(arr[i],readBytes);
			if (readBytes[i] != writeBytes[i]){
				SysLib.cout("cahce not matching");
			}
		}
	}
	
	private void localA(){
	
		for (int i = 0; i < 10; i++){
			int j;
			for (j = 0; j < 512; j++){
				writeBytes[j] = (byte)(i+j);
			}
			
			for (j = 0; j < 1000; j += 100){
				write(j, writeBytes);
			}
			for (j = 0; j < 1000; j += 100){
				read(j, readBytes);
			}
		}
	}
	
	private void mixA(){
	
		int arr[] = new int[100];
		
		for ( int i = 0; i < 100; i++){
			// for 10% of operation
			if (Math.abs(this.rand.nextInt() % 10) > 8) {
				// random access
				arr[i] = Math.abs(rand.nextInt() % 512);
				write(arr[i],writeBytes);
			}
			// for 90% of operation
			else{
				// local access 
				arr[i] = Math.abs(rand.nextInt() % 10);
				write(arr[i],writeBytes);
			}
		}
		
		for (int i = 0; i < 100; i++){
			read(arr[i], readBytes);
		}			
	}	
	
	private void advA(){
		int i, j;
		for ( i = 0; i < 10; i++){
			for ( j = 0; j < 512; j++){
				writeBytes[j] = (byte)j;
			}
			for ( j = 0; j < 10; j++){
				write(j, writeBytes);
			}
		}
		
		for ( i = 0; i < 10; i++){
			for (j = 0; j < 10; j++){
				read(j , readBytes);
			}
		}
	}
	
	private void output(String acc){
		if (enabled == true){
			SysLib.cout(acc + "(cache enabled) time cost: " + (end - start) + "\n");
		}
		else{
			SysLib.cout(acc + "(cache disabled) time cost: " + (end - start) + "\n");
		}
	}
	
	public void run(){
		SysLib.flush(); //clear cache
		start = (new Date()).getTime();
		switch(tcase){
			case 1:
				randomA();
				end = (new Date()).getTime();
				output("Random accesses");
				break;
			case 2:
				localA();
				end = (new Date()).getTime();
				output("Localized accesses");
				break; 
			case 3:
				mixA();
				end = (new Date()).getTime();
				output("Mixed accesses");
				break;				
			case 4:
				advA();
				end = (new Date()).getTime();
				output("Adversary accesses");
				break;	
		}
		SysLib.exit();
	}
}
