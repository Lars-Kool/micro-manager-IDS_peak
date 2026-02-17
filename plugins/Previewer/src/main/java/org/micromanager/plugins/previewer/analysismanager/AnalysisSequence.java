package org.micromanager.plugins.previewer.analysismanager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AnalysisSequence implements PropertyChangeListener {
   private final transient ImageAnalysisEventHandler eventHandler;
   public String sequenceName;
   public ArrayList<AnalysisStep> steps;
   public HashMap<Integer, ArrayList<Integer>> references = new HashMap<Integer,
         ArrayList<Integer>>();

   /**
    * Empty constructor, only to be used by gson for serialization purposes.
    */
   public AnalysisSequence() {
      this.eventHandler = ImageAnalysisEventHandler.getInstance();
      this.eventHandler.addListener(this);
   }

   /**
    * Constructor of empty sequence given a name.
    *
    * @param name Name of sequence
    */
   public AnalysisSequence(String name) {
      this.sequenceName = name;
      this.steps = new ArrayList<>();
      this.steps.add(new AnalysisStep(ImageAnalysis.Method.DO_NOTHING));
      this.eventHandler = ImageAnalysisEventHandler.getInstance();
      this.eventHandler.addListener(this);
   }

   /**
    * Constructor of sequence given a name and list of methods
    *
    * @param name    Name of sequence
    * @param methods List of methods
    */
   public AnalysisSequence(String name, ArrayList<ImageAnalysis.Method> methods) {
      this.sequenceName = name;
      this.steps = new ArrayList<>();

      for (ImageAnalysis.Method method : methods) {
         steps.add(new AnalysisStep(method));
      }
      updateReferences();
      this.eventHandler = ImageAnalysisEventHandler.getInstance();
      this.eventHandler.addListener(this);
   }

   /**
    * Execute the image analysis sequence on the provided image.
    *
    * @param src    Input image
    * @param width  Width of image
    * @param height Height of image
    *
    * @return Image after analysis sequence
    */
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

   /**
    * Execute the image analysis sequence on the provided image.
    *
    * @param src    Input image
    * @param width  Width of image
    * @param height Height of image
    *
    * @return ArrayList of images, with the raw image and an image after each step.
    */
   public ArrayList<int[]> executeStack(int[] src, int width, int height) {
      ArrayList<int[]> imgs = new ArrayList<>();
      int[] img = src.clone();
      imgs.add(img.clone());
      for (int i = 0; i < steps.size(); i++) {
         if (references.containsKey(i)) {
            for (int forward : references.get(i)) {
               steps.get(forward).img = img.clone();
            }
         }
         imgs.add(steps.get(i).executeStep(img, width, height, true).clone());
      }
      return imgs;
   }

   /**
    * Appends step of type method to the sequence
    * Fires "Sequence changed" event to indicate which sequence changed.
    *
    * @param method Type of step to be appended.
    */
   public void addMethod(ImageAnalysis.Method method) {
      steps.add(new AnalysisStep(method));
      eventHandler.firePropertyChange("Sequence changed", this, null, null);
   }

   /**
    * Adds step of type method at index idx to the sequence.
    * Note: idx should be a valid index, otherwise a ArrayIndexOutOfBoundsException (or similar)
    * will be thrown.
    * Fires "Sequence changed" event to indicate which sequence changed.
    *
    * @param idx    Integer index where the step should be inserted.
    * @param method Type of step that should be inserted.
    */
   public void addMethod(int idx, ImageAnalysis.Method method) {
      steps.add(idx, new AnalysisStep(method));
      eventHandler.firePropertyChange("Sequence changed", this, null, null);
   }

   /**
    * Appends step to the sequence of steps.
    * Fires "Sequence changed" event to indicate which sequence changed.
    *
    * @param step AnalysisStep to be appended
    */
   public void addMethod(AnalysisStep step) {
      steps.add(step);
      eventHandler.firePropertyChange("Sequence changed", this, null, null);
   }

   /**
    * Removes method at index i from the sequence.
    *
    * @param i Index of step to be removed.
    */
   public void removeMethod(int i) {
      steps.remove(i);
   }

   /**
    * Update references related to binary methods.
    */
   private void updateReferences() {
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

   /**
    * Create a deep-copy of this sequence.
    *
    * @return copy of the AnalysisSequence instance.
    */
   public AnalysisSequence copy() {
      AnalysisSequence copySequence = new AnalysisSequence(sequenceName);
      for (AnalysisStep step : steps) {
         copySequence.addMethod(step.copy());
      }
      return copySequence;
   }

   /**
    * ImageAnalysisEventHandler calls this function when an event is fired.
    *
    * @param evt A PropertyChangeEvent object describing the event source
    *            and the property that has changed.
    */
   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      if (!Objects.equals(evt.getPropertyName(), "Step changed")) {
         return;
      }
      updateReferences();
      eventHandler.firePropertyChange("Sequence changed", this, null, null);
   }
}
