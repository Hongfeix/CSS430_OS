public class SyncQueue {
		
		// array of QueueNode obj to represents a different conditoin
		// and enqueues all threads that wait for this condition
    private QueueNode [] queue;
    
    // initialize queue which allow 10 threads to wait on this conditoin
    public SyncQueue(){
        queue = new QueueNode[10];
        for (int i = 0 ; i < 10 ; i++){
            queue[i] = new QueueNode();
        }
    }
		
		// initialize queue which allow condMax threads to wait on this conditoin
    public SyncQueue(int condMax){
        queue = new QueueNode[condMax];
        for (int i = 0 ; i < condMax ; i++){
            queue[i] = new QueueNode();
        }
    }
		
		// enqueue the calling thread to sleep until a given condition satisfied
    public int enqueueAndSleep(int condition) {
        return (condition > -1 && condition < queue.length) ? 
        queue[condition].sleep():-1;

    }
		
		// dequeue and wake up a thread waiting for given condition
		public void dequeueAndWakeup(int condition){
			if (condition > -1 && condition < queue.length){
            queue[condition].wakeup(0);
      }
    }
		
		// dequeue and wake up a thread waiting for given condition
    public void dequeueAndWakeup(int condition, int tid){
        if (condition > -1 && condition < queue.length){
            queue[condition].wakeup(tid);
        }

    }
}
