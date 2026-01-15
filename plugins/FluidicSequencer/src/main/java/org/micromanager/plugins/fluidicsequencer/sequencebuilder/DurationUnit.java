package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import com.google.gson.annotations.SerializedName;

public enum DurationUnit {
   @SerializedName("s")
   SECOND,
   @SerializedName("m")
   MINUTE,
   @SerializedName("h")
   HOUR
}
