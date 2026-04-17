package org.micromanager.plugins.fluidicsequencer.fluidicsequence;

import java.util.HashMap;
import javafx.util.Pair;

public class StepCounter implements SequenceStep {
   private final transient SequenceEventHandler eventHandler;
   private final CounterMode mode;
   private final String counterName;
   private final int value;

   public StepCounter() {
      eventHandler = SequencerEventHandler.getInstance();
      mode = CounterMode.SET_COUNTER;
      counterName = "";
      value = 0;
   }

   @Override
   public void start(SequenceEventHandler eventHandler) {
      eventHandler.submit(
            new SequenceEvent() {}
      );
      eventHandler.firePropertyChange(mode.toString(), this, new Pair<>(counterName, value));
      stop();
   }

   @Override
   public void stop() {
      eventHandler.firePropertyChange("StepFinished", this, null);
   }

   public String getCounterName() {
      return  counterName;
   }

   public int getValue() {
      return value;
   }

   public enum CounterMode {
      SET_COUNTER,
      INCREMENT_COUNTER,
      DECREMENT_COUNTER
   }
}
