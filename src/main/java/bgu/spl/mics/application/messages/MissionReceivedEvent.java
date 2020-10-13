package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.GsonObject;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

public class MissionReceivedEvent implements Event <Boolean>{
    private MissionInfo missionInfo;
    public MissionReceivedEvent(MissionInfo m){
      missionInfo=m;
    }

   // public String getResult() {
//        return missionInfo.g;
//    }
//
//    public void setResult(String result){
//        this.result=result;
//    }
    public long getExpiredTime(){
        return missionInfo.getTimeExpired();
    }

    public MissionInfo getMissionInfo() {
        return missionInfo;
    }
    public void Report(MissionInfo m){

    }

    @Override
    public String toString() {
        return "MissionReceivedEvent{" +
                "missionInfo=" + missionInfo +
                '}';
    }
}
