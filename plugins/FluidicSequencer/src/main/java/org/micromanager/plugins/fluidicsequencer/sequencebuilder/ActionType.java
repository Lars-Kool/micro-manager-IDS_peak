package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import com.google.gson.annotations.SerializedName;

public enum ActionType {
   @SerializedName("SetPressure")
   SET_PRESSURE,
   @SerializedName("SetFlowrate")
   SET_FLOWRATE,
   @SerializedName("SetPosition")
   SET_POSITION,
   @SerializedName("Wait")
   WAIT
}
