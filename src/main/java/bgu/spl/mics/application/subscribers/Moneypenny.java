package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;
import bgu.spl.mics.application.messages.SendAgentsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.passiveObjects.Squad;
import com.google.gson.internal.bind.util.ISO8601Utils;
import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * Only this type of Subscriber can access the squad.
 * Three are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {
	private Squad squad = Squad.getInstance();

	public Moneypenny(String name) {
		super("Penny"+name);
		// TODO Implement this
	}

	@Override
	protected void initialize() {
//		// TODO Implement this

		Callback<TickBroadcast> tickBroadcastCallback = new Callback<TickBroadcast>() {
			@Override
			public void call(TickBroadcast c) throws InterruptedException {
				if(c.isLastTick()){
					System.out.println("*"+getName()+" last tick-going to terminate");
					terminate();
				}
			}
		};
		Callback<AgentsAvailableEvent> callbackAgentsAvailable = new Callback<AgentsAvailableEvent>() {
			@Override
			public void call(AgentsAvailableEvent c) {
				System.out.println("-----------------------------" + getName() + " was asked for agents: " + c.getAgentsList());
				List<String> names=new LinkedList<>();
				if (!squad.getAgents(c.getAgentsList())) {//an agent doesnt exists
					complete(c, new Pair(getName(),new LinkedList<String>()));//returning empty list of strings cause no such agents exists
					System.out.println("agent doesn't exists");
				}
				else {
					List<String> s = squad.getAgentsNames(c.getAgentsList());
					//insert Money Penny's name and it with the Agent name list.
					String name=getName();
					Pair p = new Pair(name,s);
					System.out.println(getName()+"found agents "+s.toString() + " are now acquired ");
					complete(c,p);
				}

				System.out.println(getName() + " says END OF EVENT: " + c.toString());
			}
		};

		Callback<SendAgentsEvent> sendAgentsEventCallback = new Callback<SendAgentsEvent>() {
			@Override
			public void call(SendAgentsEvent c) {
				if(c.isMissionApproved()) {
					squad.sendAgents(c.getAgentsList(), c.getDuration());

				}
				else {
					System.out.println(getName() + " going to release:" + c.getAgentsList() );
					squad.releaseAgents(c.getAgentsList());
				}
				System.out.println(getName() + " finished with the agents:" + c.getAgentsList() + " are now released, END OF MISSION by" + getName());
			}
		};
		if(Integer.parseInt(getName().substring(5))==1) {
			subscribeEvent(SendAgentsEvent.class, sendAgentsEventCallback);
		}
		else{
			subscribeEvent(AgentsAvailableEvent.class, callbackAgentsAvailable);
		}
		this.subscribeBroadcast(TickBroadcast.class, tickBroadcastCallback);
		System.out.println("TickBroadcast: " + getName() + " initialized");
	}
}
