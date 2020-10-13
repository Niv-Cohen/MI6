package bgu.spl.mics.application.publishers;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Publisher;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;


/**
 * TimeService is the global system timer There is only one instance of this Publisher.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other subscribers about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends Publisher {
	private int intevalPeriod;
	private Timer timer;
	private TimerTask task;
	private static int ticksToDie;
	private int delay;
	private int counter;
//	private CountDownLatch countDownLatch;

	public TimeService(int ticksToDie) {
		super("timeService");
		this.ticksToDie = ticksToDie;
		timer = new Timer();
		delay = 0;
		intevalPeriod = 1 * 100;// schedules the task to be run in an interval
		counter = 1;
	}

	private class SingletonHolder {
		private TimeService instance = new TimeService(ticksToDie);
	}

	@Override
	protected void initialize() {

	}

	@Override
	public void run() {

		System.out.println( getName() + " is Up and running.");
		//reduce the count by 1
		System.out.println("max ticks duration: " + ticksToDie);
		task = new TimerTask() {
			@Override
			public void run() {

				if (counter == ticksToDie) {
					System.out.println("reached ticksToTermination, sending TerminateEvent");
					TickBroadcast T = new TickBroadcast(counter, ticksToDie);
					T.setLastTick(true);
					getSimplePublisher().sendBroadcast(T);
					System.out.println("terminating...");
					timer.cancel();
					task.cancel();
					System.out.println("trying to kill Time Service");
				} else {
					System.out.println("timerService sending tick number:" + counter);
					getSimplePublisher().sendBroadcast(new TickBroadcast(counter, ticksToDie));
					counter++;
				}
			}
		};
		timer.scheduleAtFixedRate(task, 1000,100);
	}
}
