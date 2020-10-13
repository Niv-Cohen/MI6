package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {
	private Inventory inventory;
	private int currTick;
	private static class SingletonHolder {
		private static Q instance  = new Q();
	}
	public Q() {
		super("Q");
	    inventory=Inventory.getInstance();
	}

	public static Q getInstance(){return SingletonHolder.instance;}
	@Override
	protected void initialize() {
		Callback<GadgetAvailableEvent> Gadgetcallback=new Callback<GadgetAvailableEvent>() {
			@Override
			public void call(GadgetAvailableEvent c) {
					if(inventory.getItem(c.getGadget())){
						System.out.println("Q found gadget: " + c.getGadget());
						complete(c, currTick);
					}
					else{
						System.out.println("Q did NOT found gadget: " + c.getGadget());
						complete(c,-1);
					}
			}
		};
		Callback<TickBroadcast> tickBroadcastCallback = new Callback<TickBroadcast>() {
			@Override
			public void call(TickBroadcast c) throws InterruptedException {
				if(c.isLastTick()){
					System.out.println("*"+getName()+" last tick-going to terminate");
					terminate();
				}
				currTick = c.getCurrentTick();
			}
		};
		this.subscribeEvent(GadgetAvailableEvent.class,Gadgetcallback);
		this.subscribeBroadcast(TickBroadcast.class, tickBroadcastCallback);
	}
}
