package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import com.google.gson.annotations.SerializedName;

public enum ConditionAction {
   COUNTER("Counter"),
   BUTTON("Button"),
   IMAGE_ANALYSIS("Image analysis");

   public final String label;

   ConditionAction(String label) {
      this.label = label;
   }
}
