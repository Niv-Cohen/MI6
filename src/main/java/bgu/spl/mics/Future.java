package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {

	/**
	 * This should be the the only public constructor in this class.
	 */
	private T result;
	private static Object lock;
	public Future() {
		result=null;
		lock = new Object();
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	public  T get() throws InterruptedException {
		//TODO: implement this.
		synchronized(this) {
			try {
				while (!this.isDone()) {
					wait();
				}
				return result;
			}
			catch (InterruptedException e){
				System.out.println("interrupted - returning null");
				return null;
			}
		}
	}
	
	/**
     * Resolves the result of this Future object.
     */
	public synchronized void resolve (T result) {
		//TODO: implement this.
		this.result = result;
		notifyAll();

	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	public boolean isDone() {
		return result!=null;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException {
		long ti = timeout;
		try{
			if(!this.isDone() ){
				unit.timedWait(this, timeout);
			}
			return result;
		}
		catch (InterruptedException e){
			System.out.println("get timed out/interrupted");
			return null;
		}


	}

}
