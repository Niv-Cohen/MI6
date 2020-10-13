package bgu.spl.mics.application;

import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.publishers.TimeService;
import bgu.spl.mics.application.subscribers.Intelligence;
import bgu.spl.mics.application.subscribers.M;
import bgu.spl.mics.application.subscribers.Moneypenny;
import bgu.spl.mics.application.subscribers.Q;
import com.google.gson.Gson;
import bgu.spl.mics.Event;
import com.google.gson.stream.JsonReader;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class MI6Runner {
    public static void main(String[] args) throws FileNotFoundException {

        Gson gson = new Gson();
        JsonReader jr = new JsonReader(new FileReader(args[0]));
        GsonObject data = gson.fromJson(jr, GsonObject.class);
        Inventory inventory = Inventory.getInstance();
        inventory.load(data.inventory);
        Squad squad = Squad.getInstance();
        squad.load(data.squad);
        MessageBrokerImpl MB = (MessageBrokerImpl) MessageBrokerImpl.getInstance();
        List<Thread> listOfThreads = new LinkedList<>();
        List<Thread> listOfInitThreads = new LinkedList<>();
        int counter = 2;
        Thread thread0 = new Thread(new Moneypenny(String.valueOf(1)));
        thread0.setName("MoneyPenny" + 1);
        listOfThreads.add(thread0);
        for (int i = 1; i < data.services.Moneypenny; i++) {
            Thread thread = new Thread(new Moneypenny(String.valueOf(counter)));
            thread.setName("MoneyPenny" + counter);
            listOfThreads.add(thread);
            counter++;
        }
        for (int i = 0; i < data.services.M; i++) {
            Thread thread = new Thread(new M(String.valueOf(counter)));
            thread.setName("M" + counter);
            listOfThreads.add(thread);
            counter++;

        }

        for (int i = 0; i < data.services.intelligence.length; i++) {
            Thread thread = new Thread(new Intelligence(String.valueOf(counter), data.services.intelligence[i].getMissions()));
            thread.setName("Intel" + counter);

            listOfThreads.add(thread);
            counter++;
        }
        Thread thread = new Thread(new Q());
        thread.setName("Q");
        listOfThreads.add(thread);

        for (Thread tmpThread : listOfThreads) {
            tmpThread.start();
        }
        while (!listOfThreads.isEmpty()) {
            while (!listOfThreads.isEmpty() && (listOfThreads.get(0).getState() == Thread.State.BLOCKED || listOfThreads.get(0).getState() == Thread.State.WAITING)) {
                listOfInitThreads.add(listOfThreads.get(0));
                listOfThreads.remove(0);
            }
        }
        Thread timeThread = new Thread(new TimeService(data.services.time));
        timeThread.start();


        //wait for every thread to be Terminated before Creating output files.
        while(!listOfInitThreads.isEmpty()){
            while (!listOfInitThreads.isEmpty() &&listOfInitThreads.get(0).getState()== Thread.State.TERMINATED){
                listOfInitThreads.remove(0);
            }
        }

        Diary diary = Diary.getInstance();
        diary.printToFile(args[2]);
        inventory.printToFile(args[1]);

    }
}
