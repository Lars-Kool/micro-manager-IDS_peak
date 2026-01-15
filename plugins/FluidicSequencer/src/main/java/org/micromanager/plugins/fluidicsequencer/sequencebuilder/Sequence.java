package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import java.util.ArrayList;

public class Sequence {
   public String sequenceName;
   public ArrayList<SequenceAction> actions;

   public Sequence() {
   } // Needed for Gson

   public Sequence(String name) {
      sequenceName = name;
      actions = new ArrayList<>();
      actions.add(new SequenceAction("Wait", ActionType.WAIT));
   }

   public Sequence copy() {
      Sequence temp = new Sequence();
      temp.sequenceName = sequenceName;
      temp.actions = new ArrayList<>();
      for (SequenceAction action : actions) {
         temp.actions.add(action.copy());
      }
      return temp;
   }
}
