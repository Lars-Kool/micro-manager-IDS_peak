package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import com.google.gson.annotations.SerializedName;

public enum CounterAction {
   @SerializedName("SetValue")
   SET_VALUE("Set value"),
   @SerializedName("Increment")
   INCREMENT("Increment"),
   @SerializedName("Decrement")
   DECREMENT("Increment");

   public final String label;

   CounterAction(String label) {
      this.label = label;
   }
}
