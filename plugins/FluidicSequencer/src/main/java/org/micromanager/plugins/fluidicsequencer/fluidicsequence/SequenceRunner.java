package org.micromanager.plugins.fluidicsequencer.fluidicsequence;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.util.Pair;

public class SequenceRunner implements Runnable, PropertyChangeListener {
   private HashMap<String, Integer> counters;
   private AtomicBoolean isRunning;

   private int currentStep;
   private Sequence sequence;

   public SequenceRunner(Sequence sequence) {
      this.sequence = sequence;
      counters = new HashMap<>();
      isRunning = new AtomicBoolean(false);
   }

   public void start() {

   }

   public void stop() {
      isRunning.compareAndSet(true, false);
   }

   @Override
   public void run() {

   }

   public int getCurrentStep() {
      return currentStep;
   }

   public void addActionToQueue() {

   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      switch (evt.getPropertyName()) {
         case "StepFinished": {
            if (evt.getSource() != sequence.getStep(currentStep)) {
               return;
            }
            currentStep = (Integer) evt.getNewValue();
            sequence.getStep(currentStep).start();
            break;
         }
         case "SET_COUNTER": {
            Pair<String, Integer> counterData = (Pair<String, Integer>) evt.getNewValue();
            counters.put(counterData.getKey(), counterData.getValue());
            break;
         }
         case "INCREMENT_COUNTER": {
            Pair<String, Integer> counterData = (Pair<String, Integer>) evt.getNewValue();
            counters.compute(counterData.getKey(), (k, value) -> counterData.getValue() + value);
            break;
         }
         case "DECREMENT_COUNTER": {
            Pair<String, Integer> counterData = (Pair<String, Integer>) evt.getNewValue();
            counters.compute(counterData.getKey(), (k, value) -> counterData.getValue() - value);
            break;
         }
      }
   }
}
