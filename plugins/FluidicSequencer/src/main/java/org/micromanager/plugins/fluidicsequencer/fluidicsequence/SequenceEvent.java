package org.micromanager.plugins.fluidicsequencer.fluidicsequence;

import java.util.HashMap;

public class SequenceEvent {
   private final EventType eventType;
   private final Object source;
   private final HashMap<String, Integer> parameters;

   public SequenceEvent(EventType eventType, Object source, HashMap<String, Integer> parameters) {
      this.eventType = eventType;
      this.source = source;
      this.parameters = parameters;
   }

   public EventType getEventType() {
      return eventType;
   }

   public Object getSource() {
      return source;
   }

   public HashMap<String, Integer> getParameters() {
      return parameters;
   }

   public enum EventType {
      STEP_FINISHED,
      SET_COUNTER,
      MODIFY_COUNTER
   }
}
