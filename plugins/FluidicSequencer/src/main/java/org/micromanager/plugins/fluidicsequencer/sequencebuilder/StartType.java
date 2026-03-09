package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import com.google.gson.annotations.SerializedName;

public enum StartType {
   @SerializedName("WithPrevious")
   WITH_PREVIOUS("With previous"),
   @SerializedName("AfterPrevious")
   AFTER_PREVIOUS("After previous"),
   @SerializedName("OnClick")
   ON_CLICK("On click");
   @SerializedName("On condition")

   public final String label;

   private StartType(String label) {
      this.label = label;
   }
}
