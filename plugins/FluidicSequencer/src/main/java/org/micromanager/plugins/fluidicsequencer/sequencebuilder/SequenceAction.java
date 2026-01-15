package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

public class SequenceAction {
   public String deviceName;
   public ActionType type;
   public double value;
   public ValueUnit valueUnit;
   public double duration;
   public DurationUnit durationUnit;

   public SequenceAction() {
   } // Needed for Gson

   public SequenceAction(String deviceName, ActionType type) {
      this(deviceName, type, 0, 0);
   }

   public SequenceAction(String deviceName, ActionType type, double value, double duration) {
      this.deviceName = deviceName;
      this.type = type;
      this.value = value;
      this.valueUnit = (type == ActionType.SET_PRESSURE) ? ValueUnit.kPa : ValueUnit.uL_s;
      this.duration = duration;
      this.durationUnit = DurationUnit.SECOND;
   }

   public SequenceAction copy() {
      return new SequenceAction(deviceName, type, value, duration);
   }
}
