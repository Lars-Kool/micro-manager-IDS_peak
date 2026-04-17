package org.micromanager.plugins.fluidicsequencer.fluidicsequence;

import java.util.ArrayList;

public class SequenceBuilder {
   private String name;
   private ArrayList<SequenceStep> steps;

   public SequenceBuilder() {
      name = "";
      steps = new ArrayList<>();
   }

   public SequenceBuilder(Sequence sequence) {
      name = sequence.getName();
      steps = sequence.getSteps();
   }

   public String getName() {
      return name;
   }

   public SequenceBuilder setName(String name) {
      this.name = name;
      return this;
   }

   public ArrayList<SequenceStep> getSteps() {
      return new ArrayList<>(steps);
   }

   public SequenceBuilder setSteps(ArrayList<SequenceStep> steps) {
      this.steps = steps;
      return this;
   }

   public SequenceBuilder appendStep(SequenceStep step) {
      steps.add(step);
      return this;
   }

   public SequenceBuilder addStep(int idx, SequenceStep step) {
      steps.add(idx, step);
      return this;
   }

   public SequenceBuilder removeStep(int idx) {
      steps.remove(idx);
      return this;
   }
}
