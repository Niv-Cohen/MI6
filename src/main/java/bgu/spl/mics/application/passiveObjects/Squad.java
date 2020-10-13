package bgu.spl.mics.application.passiveObjects;
import bgu.spl.mics.MessageBrokerImpl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Passive data-object representing a information about an agent in MI6.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class Squad {

	private Map<String, Agent> agents=new HashMap<>();

	/**
	 * Retrieves the single instance of this class.
	 */
	private static class SingletonHolder {
		private static Squad instance = new Squad();
	}
	public static Squad getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Initializes the squad. This method adds all the agents to the squad.
	 * <p>
	 * @param agents 	Data structure containing all data necessary for initialization
	 * 						of the squad.
	 */
	public void load (Agent[] agents) {
		for (Agent a :agents ) {
			a.release();
			this.agents.put(a.getSerialNumber(),a);
		}
	}

	/**
	 * Releases agents.
	 */
	public synchronized void releaseAgents(List<String> serials){
 		for(String a:serials){
			agents.get(a).release();

		}
		notifyAll();
	}

	/**
	 * simulates executing a mission by calling sleep.
	 * @param time   time ticks to sleep
	 */
	public synchronized void sendAgents(List<String> serials, int time){
		try{
			System.out.println("agents : "+ serials + " going to sleep");
			Thread.sleep(time*100);

		}catch (InterruptedException e){
			System.out.println("interrupted");
		}
		releaseAgents(serials);
		notifyAll();
		System.out.println("agents: " + serials + "  are released");

	}

	/**
	 * acquires an agent, i.e. holds the agent until the caller is done with it
	 * @param serials   the serial numbers of the agents
	 * @return ‘false’ if an agent of serialNumber ‘serial’ is missing, and ‘true’ otherwise
	 */
	public synchronized boolean getAgents(List<String> serials){
		for (String s:serials) {
			if(agents.get(s)==null)
				return false;
		}
		for (String s:serials) {
			while(!agents.get(s).isAvailable()){
				try {
					System.out.println("squad:" + agents.get(s).getName() +" is unavailable, waiting... ");
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("squad:" + agents.get(s).getName() +" IS ACQUIRED ");
			agents.get(s).acquire();
		}
		return true;

	}

    /**
     * gets the agents names
     * @param serials the serial numbers of the agents
     * @return a list of the names of the agents with the specified serials.
     */
    public List<String> getAgentsNames(List<String> serials){
        // TODO Implement this
	    List<String> names=new LinkedList<>();
	    for (String s:serials){
			names.add(agents.get(s).getName());
		}
	    return names;
    }

}
