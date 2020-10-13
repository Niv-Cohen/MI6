package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import javafx.util.Pair;

import java.util.List;

public class AgentsAvailableEvent implements Event<Pair<String,List<String>>>{

    private List<String> agentsList;

    public AgentsAvailableEvent(List<String> agentsNums){
        this.agentsList=agentsNums;
    }

    public List<String> getAgentsList() {
        return agentsList;
    }

    @Override
    public String toString() {
        return "AgentsAvailableEvent{" +
                "agentsList=" + agentsList +
                '}';
    }
}
