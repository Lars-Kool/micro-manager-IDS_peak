package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

public class SequenceAction {
   public String deviceName;
   public ActionType actionType;
   public double value;
   public ValueUnit valueUnit;
   public double duration;
   public DurationUnit durationUnit;
   public StartType startType;

   public SequenceAction() {
   } // Needed for Gson

   public SequenceAction(String deviceName, ActionType actionType) {
      this(deviceName, actionType, 0, 0, StartType.AFTER_PREVIOUS);
   }

   public SequenceAction(String deviceName, ActionType actionType, double value, double duration,
       StartType startType) {
      this.deviceName = deviceName;
      this.actionType = actionType;
      this.value = value;
      this.valueUnit = (actionType == ActionType.SET_PRESSURE) ? ValueUnit.kPa : ValueUnit.uL_s;
      this.duration = duration;
      this.durationUnit = DurationUnit.SECOND;
      this.startType = startType;
   }

   public SequenceAction copy() {
      return new SequenceAction(deviceName, actionType, value, duration, startType);
   }
}
