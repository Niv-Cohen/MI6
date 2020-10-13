package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.MissionInfo;
import bgu.spl.mics.application.passiveObjects.Report;

import javafx.util.Pair;


import java.util.List;


/**
 * M handles ReadyEvent - fills a report and sends agents to mission.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {
  private int currTick;
  private int lastTick;
	public M(String serialNumber) {
		super("M"+serialNumber);
	}
    private void setToReport(Future<Pair<String, List<String>>> futureAgentEvent, MissionReceivedEvent missionReceivedEvent, Future<Integer> futureGadgetEvent){
		Report report=new Report();
		try {
			report.setAgentsNames(futureAgentEvent.get().getValue());
			System.out.println("set futureAgentEvent");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		report.setGadgetName(missionReceivedEvent.getMissionInfo().getGadget());
		report.setMissionName(missionReceivedEvent.getMissionInfo().getMissionName());
		report.setM(Integer.parseInt(getName().substring(1)));
		report.setAgentsSerialNumbers(missionReceivedEvent.getMissionInfo().getSerialAgentsNumbers());
		try {
			System.out.println(getName() + "is writing report for:" + missionReceivedEvent.getMissionInfo().getMissionName());
			report.setMoneypenny(Integer.parseInt(futureAgentEvent.get().getKey().substring(5)));
			System.out.println(getName() + "writing: " + futureAgentEvent.get());

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			report.setQTime(futureGadgetEvent.get());
			System.out.println(getName() + "writing Qtime: " + futureGadgetEvent.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		report.setTimeCreated(currTick);
		report.setTimeIssued(missionReceivedEvent.getMissionInfo().getTimeIssued());
		Diary.getInstance().addReport(report);
	}

	private <T> boolean checkIfMissionPossible (MissionReceivedEvent missionReceivedEvent,Future<T> future){
		if (future==null){
			System.out.println(getName() + " says NO SUBSRIBER to operate on event " + missionReceivedEvent.getClass() + ", ABORTING mission, releasing agents");
//			getSimplePublisher().sendEvent(new SendAgentsEvent(missionReceivedEvent.getMissionInfo().getSerialAgentsNumbers(),missionReceivedEvent.getMissionInfo().getDuration(),false));
//			complete(missionReceivedEvent, false);
			abortMission(missionReceivedEvent);
			return false;
		}
		return true;
	}
	private <T> void abortMission (MissionReceivedEvent missionReceivedEvent) {
		getSimplePublisher().sendEvent(new SendAgentsEvent(missionReceivedEvent.getMissionInfo().getSerialAgentsNumbers(),missionReceivedEvent.getMissionInfo().getDuration(),false));
		complete(missionReceivedEvent, false);
		System.out.println(getName() + " ABORTING MISSION " + missionReceivedEvent.getMissionInfo().getMissionName());
	}

	@Override
	protected void initialize() {
		Callback<TickBroadcast> tickBroadcastCallback = new Callback<TickBroadcast>() {
			@Override
			public void call(TickBroadcast c) throws InterruptedException {
				lastTick =c.getLastTimeTick();
				if(c.isLastTick()){
					System.out.println("*"+getName()+" last tick-going to terminate");
					terminate();
				}
				currTick = c.getCurrentTick();
			}
		};
		Callback<MissionReceivedEvent>  callback = new Callback<MissionReceivedEvent>() {
			@Override
			public void call(MissionReceivedEvent c) throws InterruptedException {
				System.out.println(getName() + " begins working on event :" + c.getMissionInfo().getMissionName());
		 		Diary.getInstance().incrementTotal();
				System.out.println(getName() + " asking for :" + c.getMissionInfo().getSerialAgentsNumbers());
				Future<Pair<String, List<String>>> futureAgentEvent = getSimplePublisher().sendEvent(new AgentsAvailableEvent(c.getMissionInfo().getSerialAgentsNumbers()));

				if (checkIfMissionPossible(c, futureAgentEvent)) {
					Pair<String, List<String>> futureAgentEventResult = futureAgentEvent.get();
					if(futureAgentEventResult.getValue().isEmpty()){
						System.out.println();
						complete(c, false);
						System.out.println(getName() + " ABORTING MISSION: + " + c.getMissionInfo().getMissionName() + " becuase agents: " + c.getMissionInfo().getSerialAgentsNumbers() +  " doesn't exists" );
					}
					else {
						System.out.println(getName() + " asked: " + futureAgentEventResult.getKey().toString() + "to acquire :" + futureAgentEventResult.getValue().toString());
						Future<Integer> futureGadgetEvent = getSimplePublisher().sendEvent(new GadgetAvailableEvent(c.getMissionInfo().getGadget()));
						System.out.println(getName() + " is looking for gadget: " + c.getMissionInfo().getGadget());
						if (checkIfMissionPossible(c, futureGadgetEvent)) {
							Integer futureGadgetResult = futureGadgetEvent.get();
							System.out.println(getName() + " got an answer  about: " + c.getMissionInfo().getGadget() + " as follows: " + futureGadgetResult);
							if (futureGadgetResult == -1) {
								abortMission(c);
							} else if (futureGadgetResult + currTick > c.getMissionInfo().getTimeExpired()) {
								System.out.println(getName() + " got an answer from Q TOO LATE");
								abortMission(c);
							} else {
								//check if the mission is still relevant and check whether the agents are still available
								if (c.getExpiredTime() >= currTick) {
									System.out.println(getName() + "is starting mission: " + c.getMissionInfo().getMissionName());
									getSimplePublisher().sendEvent(new SendAgentsEvent(c.getMissionInfo().getSerialAgentsNumbers(), c.getMissionInfo().getDuration(), true));
									complete(c, true);
									setToReport(futureAgentEvent, c, futureGadgetEvent);
									System.out.println(getName() + " announcing mission" + c.getMissionInfo().getMissionName() + " IS COMPLETE");
								} else {
									System.out.println(getName() + " aborting mission name: " + c.getMissionInfo().getMissionName() + "with expired time: " + c.getMissionInfo().getTimeExpired() + " cause of currtick:  " + currTick);
									abortMission(c);
								}
							}
						}
					}
				}
			}
		};
		this.subscribeBroadcast(TickBroadcast.class, tickBroadcastCallback);
		System.out.println("TickBroadcast: " + getName() + " initialized");
//
		this.subscribeEvent(MissionReceivedEvent.class, callback);
		System.out.println("MissionReceivedEvent: " + getName() + " initialized");
	}

}
