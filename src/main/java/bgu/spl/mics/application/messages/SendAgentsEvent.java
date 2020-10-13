package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

import java.util.List;

public class SendAgentsEvent implements Event<String> {

    private List<String> agentsList;
    private int duration;
    private boolean missionApproved;
    public SendAgentsEvent(List<String> agentsNums,int Duration,boolean missionApproved){
        this.agentsList=agentsNums;
        duration=Duration;
        this.missionApproved=missionApproved;
    }

    public boolean isMissionApproved() {
        return missionApproved;
    }

    public List<String> getAgentsList() {
        return agentsList;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "SendAgentsEvent{" +
                "agentsList=" + agentsList +
                ", duration=" + duration +
                ", approved: " + missionApproved +
                '}';
    }
}
