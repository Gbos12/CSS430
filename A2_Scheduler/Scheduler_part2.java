import java.util.Vector;

public class Scheduler extends Thread
{
    private Vector queue0, queue1, queue2;
    private int timeSlice0; //Half of DEFAULT_TIME_SLICE
    private static final int DEFAULT_TIME_SLICE = 500;

    // New data added to p161 
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000;

    // A new feature added to p161 
    // Allocate an ID array, each element indicating if that id has been used
    private int nextId = 0;
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
	synchronized( queue0 ) { //Check queue0
	    for ( int i = 0; i < queue0.size( ); i++ ) {
		TCB tcb = ( TCB )queue0.elementAt( i );
		Thread thread = tcb.getThread( );
		if ( thread == myThread ) // if this is my TCB, return it
		    return tcb;
	    }
	}
		//Check queue1
		synchronized( queue1 ) {
			for ( int i = 0; i < queue1.size( ); i++ ) {
				TCB tcb = ( TCB )queue1.elementAt( i );
				Thread thread = tcb.getThread( );
				if ( thread == myThread ) // if this is my TCB, return it
					return tcb;
			}
		}
		//Check queue2
		synchronized( queue2 ) {
			for ( int i = 0; i < queue2.size( ); i++ ) {
				TCB tcb = ( TCB )queue2.elementAt( i );
				Thread thread = tcb.getThread( );
				if ( thread == myThread ) // if this is my TCB, return it
					return tcb;
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
	timeSlice0 = DEFAULT_TIME_SLICE;
        //Initialize queue's
	queue0 = new Vector( );
		queue1 = new Vector( );
		queue2 = new Vector( );
	initTid( DEFAULT_MAX_THREADS );
    }

    public Scheduler(int quantum ) {
	timeSlice0 = quantum;
        //Initialize queue's
	queue0 = new Vector( );
        queue1 = new Vector( );
        queue2 = new Vector( );
	initTid( DEFAULT_MAX_THREADS );
    }

    // A new feature added to p161 
    // A constructor to receive the max number of threads to be spawned
    public Scheduler(int quantum, int maxThreads ) {
    	//Initialize queue's
        timeSlice0 = quantum;
		queue0 = new Vector( );
		queue1 = new Vector( );
		queue2 = new Vector( );
	initTid( maxThreads );
    }

    private void schedulerSleep( ) {
	try {
	    Thread.sleep( timeSlice0 );
	} catch ( InterruptedException e ) {
	}
    }

    // A modified addThread of p161 example
    public TCB addThread( Thread t ) {
	//t.setPriority( 2 );
	TCB parentTcb = getMyTcb( ); // get my TCB and find my TID
	int pid = ( parentTcb != null ) ? parentTcb.getTid( ) : -1;
	int tid = getNewTid( ); // get a new TID
	if ( tid == -1)
	    return null;
	TCB tcb = new TCB( t, tid, pid ); // create a new TCB
	queue0.add( tcb );
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
	} catch ( InterruptedException e ) { }
    }
    public void run( ) {
        Thread current = null;
       int count1 = 0, count2 = 0;
        while ( true ) {
            try {
                // get the next TCB and its thread
                if ( queue0.size() != 0 )
                { //Check if queue has received any arguments

                    TCB currentTCB = (TCB) queue0.firstElement(); 
                    current = currentTCB.getThread();   
                    if (current != null)  
                    {
                        current.start(); 
                    }
                        schedulerSleep(); 
           
                    if (currentTCB.getTerminated()) 
                    {
                        queue0.remove(currentTCB); 
                        returnTid(currentTCB.getTid()); 
                    }
                    else
                    {
                        current.suspend();
                        queue0.remove(currentTCB); 
                        queue1.add(currentTCB);
                    }
                        continue;
                }
                else if (queue1.size() != 0)
                {
                    TCB currentTCB = (TCB) queue1.firstElement(); 
                    current = currentTCB.getThread();
                    if (current != null && current.isAlive())
                       {
                           current.resume();
                       }
                    //Sleep for 500ms
                    schedulerSleep(); 
                    count1++; 
                    if (currentTCB.getTerminated())
                    {
                        count1 = 0;  
                        queue1.remove(currentTCB); 
                        returnTid(currentTCB.getTid()); 
                    }
                   else if(count1 == 2) 
                    {
                        if (current != null)
                        {
                            if (current.isAlive()) 
                                current.suspend();
                            queue1.remove(currentTCB); 
                            queue2.add(currentTCB); 
                            count1 = 0; 
                        }
                    }

                    continue;
                }
                else if (queue2.size() != 0)
                {
                    TCB currentTCB = (TCB) queue2.firstElement(); 
                    current = currentTCB.getThread();
                    if (current != null && current.isAlive())
                    {
                            current.resume();
                    }

                    schedulerSleep();
                    count2++;
                    if(currentTCB.getTerminated()) 
                    {
                        queue2.remove(currentTCB);
                        count2 = 0; 
                        returnTid(currentTCB.getTid()); 
                        continue; 
                    }
                    if(count2 == 4) 
                    {
                        synchronized (queue2)
                        {
                            if (current != null && current.isAlive())
                                current.suspend();
                            queue2.remove(currentTCB); 
                            queue2.add(currentTCB); 
                            count2 = 0; 
                            continue; 
                        }
                    }
                }
            } catch ( NullPointerException e3 ) { };
        }
    }
}
