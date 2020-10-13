package bgu.spl.mics;

import bgu.spl.mics.application.messages.AgentsAvailableEvent;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {
    private ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<Subscriber>> eventsBySubs;
    private ConcurrentHashMap<Subscriber, LinkedBlockingQueue<Message>> subByMess = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Event, Future> eventsByFuture = new ConcurrentHashMap<>();
   // Object newEventLock;

    public MessageBrokerImpl() {
        //newEventLock = new Object();
        eventsBySubs = new ConcurrentHashMap<>();
        eventsBySubs.put(AgentsAvailableEvent.class, new ConcurrentLinkedQueue<>());
        eventsBySubs.put(GadgetAvailableEvent.class, new ConcurrentLinkedQueue<>());
        eventsBySubs.put(MissionReceivedEvent.class, new ConcurrentLinkedQueue<>());
        eventsBySubs.put(TickBroadcast.class, new ConcurrentLinkedQueue<>());
    }

    private static class SingletonHolder {
        private static MessageBrokerImpl instance = new MessageBrokerImpl();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static MessageBroker getInstance() {
        return SingletonHolder.instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
        if (!eventsBySubs.containsKey(type)) {
            eventsBySubs.put(type, new ConcurrentLinkedQueue<>());
        }
        eventsBySubs.get(type).add(m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {

        if (!eventsBySubs.containsKey(type)) {
            eventsBySubs.put(type, new ConcurrentLinkedQueue<>());
        }
        eventsBySubs.get(type).add(m);
    }

    @Override
    public synchronized  <T> void complete(Event<T> e, T result) {
        // TODO Auto-generated method stub
        eventsByFuture.get(e).resolve(result);
        notifyAll();
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        // TODO Auto-generated method stub

      //  synchronized (newEventLock) {
            if (!eventsBySubs.containsKey(b.getClass())) {
                System.out.println("type doesnt exist");
            } else {
                ConcurrentLinkedQueue<Subscriber> Q = eventsBySubs.get(b.getClass());
                for (Subscriber subscriber : Q) {
                    subByMess.get(subscriber).add(b);
                }
            }
       //     newEventLock.notifyAll();
      //  }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {


        if (!eventsBySubs.containsKey(e.getClass())) {//todo maybe insert to sync
            System.out.println("msgbrkr says NO SUBSCRIBER for event type: " + e.getClass());
            return null;

        }
        synchronized (eventsBySubs.get(e.getClass())) { //TODO: possible to make the arraylist list outside of the scope and do sync to the list object
            ConcurrentLinkedQueue<Subscriber> subscribersQueue = eventsBySubs.get(e.getClass());
            Subscriber subscriber = subscribersQueue.poll();
            if(subscriber == null){
                System.out.println("msgbrkr says NO SUBSCRIBER for event type: " + e.getClass());
                return null;
            }
            Future<T> future = new Future<>();
            eventsByFuture.put(e, future);
            subscribersQueue.add(subscriber);
            subByMess.get(subscriber).add(e);
            return future;
        }
    }
    @Override
    public void register(Subscriber m) {
        subByMess.put(m, new LinkedBlockingQueue<>());
    }

    @Override
    public void unregister(Subscriber m) {
        //delete the queue of the subscriber from  subByMess
        if (subByMess.containsKey(m)) {
            System.out.println("*msgBroker is unregistering " + m.getName() + ", and deleting it from subByMess");
            subByMess.remove(m);
        }
        //delete the subscriber from the subscribers map
        for (ConcurrentLinkedQueue<Subscriber> queue : eventsBySubs.values()) {
            if (queue.contains(m)) {
                System.out.println("*msgBroker is deleting: " + m.getName() + " from map of subsByEvent");
                System.out.println();
                queue.remove(m);
                if (queue.isEmpty()) {
                    System.out.println("delete the queue of subscriber of type " + m.getClass());
                    eventsBySubs.remove(queue);//no more subscribers to that event, delete the key
                }
            }
        }
    }

    @Override
    public Message awaitMessage(Subscriber m) throws InterruptedException {

        LinkedBlockingQueue temoQ = subByMess.get(m);
        if (temoQ == null) {
            throw new InterruptedException("bla");
        }
        Message msg = (Message) temoQ.take();
        return msg;
    }
/*
        if(subByMess.get(m)!=null){
            synchronized (subByMess.get(m)){
                 while (subByMess.get(m).isEmpty()){
                     try{
                         subByMess.get(m).wait();
                     }
                     catch (InterruptedException e){
                         throw e;
                     }
                 }
            }
            System.out.println("msgbroker is sending " + m.getName() + " the event: " + subByMess.get(m).peek());
            return subByMess.get(m).poll();
        }
        else{
            throw new IllegalStateException();
        }
    }
}
*/

     /*   if(!subByMess.containsKey(m)){
            System.out.println(m.getName() + "is not in the map of the subscribers");
        }
        try{
            while(subByMess.get(m).peek() == null)
            {
                synchronized (newEventLock) {
                    System.out.println(m.getName() + " is waiting for message");
                    newEventLock.wait();
                }
            }

            Message msg = subByMess.get(m).peek();
            System.out.println(m.getName() +  " got the following message:" + msg.toString());
            subByMess.get(m).remove();
            return msg;

        }catch(IllegalStateException e ){
            System.out.println("no such subscriber exists, returning null");
            return null;
        }
        catch(InterruptedException e1){
            System.out.println("Thread "+m+ "was interrupted");
            return null;
        }*/
}
