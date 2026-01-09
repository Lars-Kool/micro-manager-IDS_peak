package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import mmcorej.DeviceType;
import org.micromanager.Studio;

public class FluidicAction {
   private String deviceName;
   private ActionType type;
   private double value;
   private double duration;

   public FluidicAction() {}

   public FluidicAction(String deviceName, ActionType type) {
      this.deviceName = deviceName;
      this.type = type;
      this.value = value;
      this.duration = duration;
   }

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
}
