import java.util.LinkedList;
import java.util.Queue;

public class QueueNode{

	private Queue<Integer> q;
	
	public QueueNode(){
		q = new LinkedList<>(); // used to save waiting thread
	}	
	
	public synchronized int sleep(){
		if (q.size() == 0){
			try{
				//tells the calling thread to give up the lock and go to sleep 
				//until some other thread enters the same monitor and calls notify()
				wait();		
			}
			catch (InterruptedException e){
				System.out.println("Thread unable to sleep");
			}
			return q.remove(); // return the parent thread
		}
		return -1;
	}
	
	public synchronized void wakeup(int tid){
	
		q.add(tid);	 // add to waiting list
		
		notify();	// wake up the parent
	}
}
		
	
