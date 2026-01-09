package org.micromanager.plugins.previewer.analysismanager;

import java.util.ArrayList;
import java.util.HashMap;

public class AnalysisSequence {
   public String sequenceName;
   public ArrayList<AnalysisStep> steps;
   public HashMap<Integer, ArrayList<Integer>> references = new HashMap<Integer,
         ArrayList<Integer>>();

   public AnalysisSequence() {
   } // Used for Gson

   public AnalysisSequence(String name) {
      this.sequenceName = name;
      this.steps = new ArrayList<>();
   }

   public AnalysisSequence(String name, ArrayList<ImageAnalysis.Method> methods) {
      this.sequenceName = name;
      this.steps = new ArrayList<>();

      for (ImageAnalysis.Method method : methods) {
         steps.add(new AnalysisStep(method));
      }
      updateReferences();
   }

   public int[] execute(int[] src, int width, int height) {
      for (int i = 0; i < steps.size(); i++) {
         if (references.containsKey(i)) {
            for (int forward : references.get(i)) {
               steps.get(forward).img = src.clone();
            }
         }
         steps.get(i).executeStep(src, width, height, true);
      }
      return src;
   }

   public void addMethod(ImageAnalysis.Method method) {
      steps.add(new AnalysisStep(method));
   }

   public void addMethod(int idx, ImageAnalysis.Method method) {
      steps.add(idx, new AnalysisStep(method));
   }

   public void addMethod(AnalysisStep step) {
      steps.add(step);
   }

   public void removeMethod(int i) {
      steps.remove(i);
   }

   public AnalysisSequence copy() {
      AnalysisSequence copySequence = new AnalysisSequence(sequenceName);
      for (AnalysisStep step : steps) {
         copySequence.addMethod(step.copy());
      }
      return copySequence;
   }

   public void updateReferences() {
      references = new HashMap<>();
      for (int i = 0; i < steps.size(); i++) {
         if (steps.get(i).isBinary) {
            int reference = (int) steps.get(i).parameters.get(0).getValue();
            if (!references.containsKey(reference)) {
               references.put(reference, new ArrayList<>());
            }
            references.get(reference).add(i);
         }
      }
   }
}
