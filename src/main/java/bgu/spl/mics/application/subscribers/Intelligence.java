package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBroker;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.GsonObject;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A Publisher\Subscriber.
 * Holds a list of Info objects and sends them
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Intelligence extends Subscriber {
	private long currTick;
	private Object tickLock;
	List<GsonObject.Services.Intelligence.Mission> missionList;

	public Intelligence(String serialNumber, GsonObject.Services.Intelligence.Mission[] MissionList) {
		super("Intelligene  "+serialNumber);
		this.missionList = new LinkedList<>(Arrays.asList(MissionList));
		tickLock=new Object();
	}

	@Override
	protected void initialize() {
		Callback<TickBroadcast> callback = new Callback<TickBroadcast>() {

			public synchronized void call(TickBroadcast c) throws InterruptedException {
				//int index = 0;
				currTick = c.getCurrentTick();
				for (int i=0;i<missionList.size();i++){
					GsonObject.Services.Intelligence.Mission mission=missionList.get(i);
					if (mission.timeIssued <= currTick) {
						MissionInfo m = new MissionInfo();
						m.setDuration(mission.duration);
						m.setSerialAgentsNumbers(Arrays.asList(mission.serialAgentsNumbers));
						m.setMissionName(mission.name);
						m.setGadget(mission.gadget);
						m.setTimeExpired(mission.timeExpired);
						m.setTimeIssued(mission.timeIssued);
						System.out.println(getName() + " sending new mission" + mission.name);
						getSimplePublisher().sendEvent(new MissionReceivedEvent(m));
						missionList.remove(mission);
//
					}
				}
				if(missionList.size()==0){
					System.out.println(getName() + " is terminating");
					terminate();
				}
			}
		};
		subscribeBroadcast(TickBroadcast.class, callback);
		System.out.println("TickBroadcast: " + getName() + " initialized");

	}
}
