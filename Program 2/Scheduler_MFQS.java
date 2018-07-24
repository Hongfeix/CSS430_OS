import java.util.*;

public class Scheduler extends Thread
{
    private Vector[] queue = new Vector[3]; // 3 queues
    private int timeSlice;
    private static final int DEFAULT_TIME_SLICE = 500; // Default for queue[0]
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000;
		private int nextId = 0;
		
		
    // A new feature added to p161 
    // Allocate an ID array, each element indicating if that id has been used
    private void initTid( int maxThreads ) {
			tids = new boolean[maxThreads];
			for ( int i = 0; i < maxThreads; i++ )
	    	tids[i] = false;
    }
		
    // A new feature added to p161 
    // Search an available thread ID and provide a new thread with this ID
    private int getNewTid( ) {   
			for ( int i = 0; i < tids.length; i++ ) {
	    	int tentative = ( nextId + i ) % tids.length;
	    	if ( tids[tentative] == false ) {
					tids[tentative] = true;
					nextId = ( tentative + 1 ) % tids.length;
					return tentative;
	    	}
			}
			return -1;
    }

    // A new feature added to p161 
    // Return the thread ID and set the corresponding tids element to be unused
    private boolean returnTid( int tid ) {
			if ( tid >= 0 && tid < tids.length && tids[tid] == true ) {
	    	tids[tid] = false;
	   	 return true; 
			}
			return false;  
    }

    // A new feature added to p161 
    // Retrieve the current thread's TCB from the queue
    public TCB getMyTcb( ) {
			Thread myThread = Thread.currentThread( ); // Get my thread object
			synchronized( queue ) {
				for (int j = 0; j < 3; j++){ // go though 3 queues
	    		for ( int i = 0; i < queue[j].size( ); i++ ) { // check TCB in each queue
						TCB tcb = (TCB)queue[j].elementAt( i );
						Thread thread = tcb.getThread( );
						if ( thread == myThread ) // if this is my TCB, return it
		   		 		return tcb;
		   		}
	    	}
			}
			return null;
    }

    // A new feature added to p161 
    // Return the maximal number of threads to be spawned in the system
		public int getMaxThreads( ) {
			return tids.length;
    }

    public Scheduler( ) {  
			timeSlice = DEFAULT_TIME_SLICE;
			for (int i = 0; i < 3; i++){
				queue[i] = new Vector();
			}
			initTid( DEFAULT_MAX_THREADS );
    }

    public Scheduler( int quantum ) {
			timeSlice = quantum;
			for (int i = 0; i < 3; i++){
				queue[i] = new Vector();
			}
			initTid( DEFAULT_MAX_THREADS );
    }

    // A new feature added to p161 
    // A constructor to receive the max number of threads to be spawned
    public Scheduler( int quantum, int maxThreads ) {
			timeSlice = quantum;
			for (int i = 0; i < 3; i++){
				queue[i] = new Vector();
			}
			initTid( maxThreads );
    }

    private void schedulerSleep( int timeSlice ) {
			try {
	    	Thread.sleep(timeSlice);
			} 
			catch (InterruptedException e) {
			}
    }

    // A modified addThread of p161 example
    public TCB addThread( Thread t ) {
			TCB parentTcb = getMyTcb( ); // get my TCB and find my TID
			int pid = ( parentTcb != null ) ? parentTcb.getTid( ) : -1;
			int tid = getNewTid( ); // get a new TID
			if ( tid == -1)
	    	return null;
			TCB tcb = new TCB( t, tid, pid ); // create a new TCB
			queue[0].add( tcb ); 		// new thread's TCB always in Queue0
			return tcb;
    }

    // A new feature added to p161
    // Removing the TCB of a terminating thread
    public boolean deleteThread( ) {
			TCB tcb = getMyTcb( ); 
			if ( tcb!= null )
	   		return tcb.setTerminated( );
			else
	    	return false;
    }

    public void sleepThread( int milliseconds ) {
			try {
	    	sleep( milliseconds );
			} 
			catch ( InterruptedException e ) { }
    }
       
       
    /*
    public void q0(){
    
    	// get TCB
    	TCB currentTCB = (TCB)queue[0].firstElement();
    	
    	if (currentTCB.getTerminated() == true){ // done, remove from queue
    		queue[0].remove(currentTCB);
    		returnTid(currentTCB.getTid());
    		return;
    	}
    	
    	Thread current = currentTCB.getThread();
    	if (current != null){
    		if (current.isAlive()){
 	   			current.resume();
 	   		}
 	   		else {
 	   			current.start();
 	   		}
 	   	}	
 	   	
    	schedulerSleep(DEFAULT_TIME_SLICE/2);  // sleep 500ms
    	
    	synchronized(queue[0]){
    		if (current.isAlive() && current != null){
    			current.suspend();
    			queue[0].remove(currentTCB);
    			queue[1].add(currentTCB);
    		}
    	}
    }	
    
    public void q1(){
    	
    	// get TCB
    	TCB currentTCB = (TCB)queue[1].firstElement();
    	
    	if (currentTCB.getTerminated() == true){ // done, remove from queue
    		queue[1].remove(currentTCB);
    		returnTid(currentTCB.getTid());
    		return;
    	}
    	
    	Thread current = currentTCB.getThread();
    	if (current != null){
    		if (current.isAlive()){
 	   			current.resume();
 	   		}
 	   		else {
 	   			current.start();
 	   		}
 	   	}	
 	   	
    	schedulerSleep(DEFAULT_TIME_SLICE);  // sleep 1000ms
    	
    	synchronized(queue[1]){
    		if (current.isAlive() && current != null){
    			current.suspend();
    			queue[1].remove(currentTCB);
    			queue[2].add(currentTCB);
    		}
    	}
    }
    
    public void q2(){
    	
    	// get TCB
    	TCB currentTCB = (TCB)queue[2].firstElement();
    	
    	if (currentTCB.getTerminated() == true){ // done, remove from queue
    		queue[2].remove(currentTCB);
    		returnTid(currentTCB.getTid());
    		return;
    	}
    	
    	Thread current = currentTCB.getThread();
    	if (current != null){
    		if (current.isAlive()){
 	   			current.resume();
 	   		}
 	   		else {
 	   			current.start();
 	   		}
 	   	}	
 	   	
    	schedulerSleep(DEFAULT_TIME_SLICE*2);  // sleep 2000ms
    	
    	synchronized(queue[2]){
    		if (current.isAlive() && current != null){
    			current.suspend();
    			queue[2].remove(currentTCB);
    			queue[2].add(currentTCB);
    		}
    	}
    }
   
    	// A modified run of p161
    	public void run() {
				while ( true ) {
	   			try {
						if (queue[0].size() > 0){
							q0();
						}
					}
					catch (NullPointerException e3) {};
		 		} 
			}
		*/
	
	
		public void Q0(){
			// In run() method, we check if Q0's length, so there will be 
			// at least one process in the Q0
			TCB currentTCB = (TCB) queue[0].firstElement(); // choose front TCB
			Thread current = currentTCB.getThread();	// get first process

			if (current != null){ // start process
				current.start();
			}
			
			schedulerSleep(DEFAULT_TIME_SLICE);	// process running

			if (currentTCB.getTerminated() == true) { // if process done within 500ms
				queue[0].remove(currentTCB);	// remove from Q0
				returnTid(currentTCB.getTid());
			}

			synchronized (queue[0]) { // when not done in 500ms, move to Q1
				if (current != null && current.isAlive()){
					current.suspend();
				}
				queue[0].remove(currentTCB); // move from Q0
				queue[1].add(currentTCB);	// add to Q1
			}	
		}
		
		
		public void Q1(){
			TCB currentTCB = (TCB) queue[1].firstElement();
			Thread current = currentTCB.getThread();

			if (current != null) { // resume the process from Q1
				if (current.isAlive()){
					current.resume();
				}
			}

			schedulerSleep(timeSlice*2); // Q1 quantum 2*500 = 1000ms

			if (currentTCB.getTerminated() == true) { // process done, remove from Q1
				queue[1].remove(currentTCB);
				returnTid(currentTCB.getTid());
			}

			// when not done in 1000ms, move to Q2
			if (current != null && current.isAlive()){
				current.suspend();
			}
			queue[1].remove(currentTCB); // move from Q1
			queue[2].add(currentTCB);	// add to Q2
			
		}
		
		
		public int Q1(int quantum){
			TCB currentTCB = (TCB) queue[1].firstElement();
			Thread current = currentTCB.getThread();

			if (current != null) { // resume the process from Q1
				if (current.isAlive()){
					current.resume();
				}
			}
			
			schedulerSleep(timeSlice); 		
			
			quantum += timeSlice; // increase quantum time
			
			if (currentTCB.getTerminated() == true) { // process done, remove from Q1
				quantum = 0;
				queue[1].remove(currentTCB);
				returnTid(currentTCB.getTid());
			}
			
			else if (quantum == 1000){ 
				
				if (current != null && current.isAlive()){ // process not done yet
					current.suspend();
				}
				queue[1].remove(currentTCB); // remove from Q1
				queue[2].add(currentTCB); // add to Q2
				quantum = 0;
			}
			return quantum;
		}				
			
		public void Q2( ){
			TCB currentTCB =(TCB) queue[2].firstElement();
			Thread current = currentTCB.getThread();

			if (current != null) { // resume process from Q2
				if (current.isAlive()) {
				 current.resume();
				}
			}

			schedulerSleep(timeSlice*4); // Q2 quantum 4*500 = 2000ms

			if (currentTCB.getTerminated() == true) { // process done, remove from Q2
				queue[2].remove(currentTCB);
				returnTid(currentTCB.getTid());
			}

			// when not done in 2000ms, move to back of Q2
			if (current != null && current.isAlive()){
				current.suspend();
			}
			queue[2].remove(currentTCB); // remove from top Q2
			queue[2].add(currentTCB); // add to back of Q2
			
		}
		
		public int Q2(int quantum){
			TCB currentTCB =(TCB) queue[2].firstElement();
			Thread current = currentTCB.getThread();

			if (current != null) { // resume process from Q2
				if (current.isAlive()) {
				 current.resume();
				}
			}
			
			schedulerSleep(timeSlice);
			quantum += timeSlice;
			
			if (currentTCB.getTerminated() == true) { // process done remove from Q2
				quantum = 0;
				queue[2].remove(currentTCB);
				returnTid(currentTCB.getTid());
			}
			else if (quantum == 2000) { 
				if (current != null && current.isAlive()){
					current.suspend();
				}
				queue[2].remove(currentTCB); // remove from top Q2
				queue[2].add(currentTCB); // add to back of Q2
				quantum = 0;
			}
			
			if(queue[0].size() > 0 ){  // reset quantum when add new process
				quantum = 0;
			}
			
			return quantum;
		}
			
    public void run( ) {
			int quantum = 0;
			while ( true ) {
				try {
					if ( queue[0].size( ) > 0 ) { // if Q0 not empty 
						Q0();
					}
					if ( queue[0].size( ) == 0 && queue[1].size( ) > 0  ) { // Q0 empty, but Q1 not
						//Q1(); 
						quantum = Q1(quantum);
					}
					// Q0 and Q1 all empty, Q2 not
					if ( queue[0].size( ) == 0 && queue[1].size( ) == 0 && queue[2].size()>0  ) {
						//Q2();
						quantum = Q2(quantum);
					}
				
				} 
				catch ( NullPointerException e3 ) { };
			}
    }	
}
