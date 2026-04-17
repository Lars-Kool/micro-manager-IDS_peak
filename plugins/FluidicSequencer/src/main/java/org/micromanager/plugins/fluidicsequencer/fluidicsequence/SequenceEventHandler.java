package org.micromanager.plugins.fluidicsequencer.fluidicsequence;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.micromanager.Studio;

public class SequenceEventHandler {
   private SequenceEventHandler instance;
   private final Studio studio;
   private final BlockingQueue<SequenceEvent> queue;
   private final Thread workerThread;
   private final AtomicBoolean running = new AtomicBoolean(false);

   private HashMap<String, Integer> counters;
   private Sequence sequence;
   private int index;

   private SequenceEventHandler(Studio studio) {
      this.studio = studio;
      this.queue = new LinkedBlockingQueue<>();
      this.workerThread = new Thread(this::dispatchLoop, "EventDispatcherThread");
      this.counters = new HashMap<>();
   }

   public SequenceEventHandler initialize(Studio studio) {
      if (instance == null) {
         instance = new SequenceEventHandler(studio);
      }
      return instance;
   }

   public SequenceEventHandler getInstance() throws Exception {
      if (instance == null) {
         throw new Exception("SequenceEventHandler not yet initialized!");
      }
      return instance;
   }

   public void start() {
      if (running.compareAndSet(false, true)) {
         workerThread.start();
      }
   }

   public void stop() {
      running.set(false);
      workerThread.interrupt();
   }

   public void submit(SequenceEvent sequenceEvent) {
      if (!running.get()) {
         studio.logs().logError("Dispatcher not running!");
         return;
      }
      queue.add(sequenceEvent);
   }

   public void dispatchLoop() {
      while (running.get()) {
         try {
            handleEvent(queue.take());
         } catch (InterruptedException e) {
            if (!running.get()) {
               Thread.currentThread().interrupt();
               break;
            }
         } catch (Exception e) {
            studio.logs().logError(e);
         }
      }
   }

   private void handleEvent(SequenceEvent event) {
      switch (event.getEventType()) {
         case SET_COUNTER: {
            StepCounter tmp = (StepCounter) event.getSource();
            counters.put(tmp.getCounterName(), tmp.getValue());
            break;
         }
         case MODIFY_COUNTER: {
            StepCounter tmp = (StepCounter) event.getSource();
            if (!counters.containsKey(tmp.getCounterName())) {
               counters.put(tmp.getCounterName(), 0);
            }
            counters.compute(tmp.getCounterName(), (key, value) -> value + tmp.getValue());
            break;
         }
         case STEP_FINISHED: {

         }
      }
   }
}
