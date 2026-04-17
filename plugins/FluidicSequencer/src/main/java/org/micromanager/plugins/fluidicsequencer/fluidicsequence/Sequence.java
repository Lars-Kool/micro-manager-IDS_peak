package org.micromanager.plugins.fluidicsequencer.fluidicsequence;

import java.util.ArrayList;

public class Sequence {
   private final String name;
   private final ArrayList<SequenceStep> steps;

   public Sequence() {
      name = null;
      steps = null;
   } // Needed for Gson

   public Sequence(SequenceBuilder builder) {
      name = builder.getName();
      steps = builder.getSteps();
   }

   public String getName() {
      return name;
   }

   public ArrayList<SequenceStep> getSteps() {
      return new ArrayList<>(steps);
   }

   public SequenceStep getStep(int idx) {
      return steps.get(idx);
   }
}
