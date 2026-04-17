package org.micromanager.plugins.fluidicsequencer.fluidicsequence;

import java.util.TimerTask;

public class StepWait implements SequenceStep {
   private final transient SequencerEventHandler eventHandler;
   private final WaitMode mode;
   private final int value;

   public StepWait() {
      eventHandler = SequencerEventHandler.getInstance();
      mode = WaitMode.DURATION;
      value = 0;
   }

   @Override
   public void start() {
      if (mode != WaitMode.DURATION) {
         return;
      }

      TimerTask task = new TimerTask() {}
   }

   @Override
   public void stop() {
      eventHandler.firePropertyChange("StepFinished", this, null);
   }

   public enum WaitMode {
      INDEFINITE,
      DURATION,
      MANUAL
   }
}
