package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class GadgetAvailableEvent<Integer> implements Event<Integer>{
    private String gadget;
    public GadgetAvailableEvent(String Gadget){
            gadget=Gadget;
    }

    public String getGadget() {
        return gadget;
    }

    @Override
    public String toString() {
        return "GadgetAvailableEvent{" +
                "gadget='" + gadget ;
    }
}
