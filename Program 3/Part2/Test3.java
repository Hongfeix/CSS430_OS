import java.util.Date;

class Test3 extends Thread{

	private int threads;
	
	public Test3(String[] args){
		threads = Integer.parseInt(args[0]); // get number of thread pair	
	} 
	
	public void run(){
		float t1 = new Date().getTime(); // get current time 
		
		String[] testThread3A = SysLib.stringToArgs("TestThread3A"); // call compute thread
		String[] testThread3B = SysLib.stringToArgs("TestThread3B"); // call disk thread 
		
		//run computation thread number of times 
		for(int i = 0; i < threads; i++){
			SysLib.exec(testThread3A);
		}
		
		//run disk thread number of times
		for(int i = 0; i < threads; i++){
			SysLib.exec(testThread3B);
		}
		
		// wait for computation and disk thread finish 
		for(int i = 0; i < 2*threads; i++){
			SysLib.join();
		}
		
		float t2 = new Date().getTime(); // get running time
		
		SysLib.cout("Total time elapse: " + (t2 - t1) + "ms" + "\n");
		SysLib.exit(); 
	}
}
