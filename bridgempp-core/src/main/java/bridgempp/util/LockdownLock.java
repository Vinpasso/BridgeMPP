package bridgempp.util;

public class LockdownLock {

	boolean isLocked = false;
	Thread lockedBy = null;
	int lockedCount = 0;
	
	public synchronized void syncLock() throws InterruptedException
	{
		Thread callingThread = Thread.currentThread();
		while (isLocked && lockedBy != callingThread) {
			wait();
		}
		notify();
	}

	public synchronized void lock() {
		Thread callingThread = Thread.currentThread();
		isLocked = true;
		lockedCount++;
		lockedBy = callingThread;
	}

	public synchronized void unlock() {
		if (Thread.currentThread() == this.lockedBy) {
			lockedCount--;

			if (lockedCount == 0) {
				isLocked = false;
				notify();
			}
		}
	}

}
