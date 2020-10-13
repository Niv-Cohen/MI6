package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.Agent;


public class GsonObject {
    public String[] inventory;
    public Services services;
    public Agent[] squad;

    public class Services{
        public int M;
        public int Moneypenny;
        public Intelligence [] intelligence;
        public int time;

        public class Intelligence{
            public Mission[] getMissions() {
                return missions;
            }

            public Mission[] missions;
            public class Mission{
                public String[] serialAgentsNumbers;
                public int duration;
                public String gadget;
                public String name;
                public int timeExpired;
                public int timeIssued;
            }
        }
    }
    public class Squad{
        public String name;
        public String serialNumber;
    }
}
