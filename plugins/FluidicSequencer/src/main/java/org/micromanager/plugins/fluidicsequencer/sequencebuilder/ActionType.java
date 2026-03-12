package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import com.google.gson.annotations.SerializedName;

public enum ActionType {
   SET_PRESSURE("Set pressure"),
   SET_FLOWRATE("Set flowrate"),
   WAIT("Wait"),
   SET_COUNTER("Set counter"),
   UPDATE_COUNTER("Update counter"),
   JUMP("Jump"),
   JUMP_IF("Jump if"),

   public final String label;

   ActionType(String label) {
      this.label = label;
   }
}
