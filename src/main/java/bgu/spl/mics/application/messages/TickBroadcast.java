package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Event;

public class TickBroadcast  implements Broadcast {
    private int currentTick;
    private boolean isLastTick;
    private int lastTimeTick;
    public TickBroadcast(int i){
        currentTick = i;
        isLastTick=false;
        lastTimeTick=1000000000;
    }
    public TickBroadcast(int i,int lastTimeTick){
        currentTick = i;
        isLastTick=false;
        this.lastTimeTick=lastTimeTick;
    }
    public int getCurrentTick(){
        return currentTick;
    }

    public void setLastTick(boolean lastTick) {
        this.isLastTick = lastTick;
    }

    public boolean isLastTick() {
        return isLastTick;
    }

    public int getLastTimeTick() {
        return lastTimeTick;
    }

    public void setLastTimeTick(int lastTimeTick) {
        this.lastTimeTick = lastTimeTick;
    }

    @Override
    public String toString() {
        return "TickBroadcast{" +
                "currentTick=" + currentTick +
                ", lastTick=" + isLastTick +
                '}';
    }
}
