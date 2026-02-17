package org.micromanager.plugins.previewer.analysismanager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Objects;

public class AnalysisStep implements PropertyChangeListener {
   private final transient ImageAnalysisEventHandler eventHandler;
   public transient int[] img = null;

   public String name;
   public ImageAnalysis.Method method;
   public ArrayList<AnalysisParameter> parameters;
   public boolean isBinary = false;

   /**
    * Empty constructor, for Gson use only!
    */
   public AnalysisStep() {
      eventHandler = ImageAnalysisEventHandler.getInstance();
      this.eventHandler.addListener(this);
   }

   /**
    * AnalysisStep constructor
    *
    * @param name   Name of step
    * @param method ImageAnalysis.Step of this method
    */
   public AnalysisStep(String name, ImageAnalysis.Method method) {
      this(method);
      this.name = name;
   }

   /**
    * AnalysisStep constructor. Name is derived from the method. This is the preferred constructor.
    * The constructor initializes the right parameters, given the Method
    *
    * @param method ImageAnalysis.Step of this method
    */
   public AnalysisStep(ImageAnalysis.Method method) {
      this.name = method.toString();
      this.method = method;
      this.parameters = new ArrayList<>();
      this.eventHandler = ImageAnalysisEventHandler.getInstance();
      this.eventHandler.addListener(this);

      String[] connectivityOptions = new String[] {"4", "8"};
      switch (method) {
         case DO_NOTHING:
            break;
         case THRESHOLD:
            parameters.add(new AnalysisParameter("ThresholdValue",
                  AnalysisParameter.ParameterType.INTEGER, 0)
            );
            break;
         case INVERT:
            break;
         case FILL_GAPS:
            break;
         case DISTANCE_TRANSFORM:
            break;
         case WATERSHED:
            parameters.add(new AnalysisParameter("Minimum Droplet size",
                  AnalysisParameter.ParameterType.INTEGER, 1));
            parameters.add(new AnalysisParameter("Connectivity",
                  AnalysisParameter.ParameterType.ENUM, "8",
                  connectivityOptions));
            break;
         case CONNECTED_COMPONENT:
            parameters.add(
                  new AnalysisParameter("Connectivity", AnalysisParameter.ParameterType.ENUM, "8",
                        connectivityOptions)
            );
            break;
         case ERODE:
            parameters.add(
                  new AnalysisParameter("Size", AnalysisParameter.ParameterType.INTEGER, 1)
            );
            parameters.add(
                  new AnalysisParameter("Connectivity", AnalysisParameter.ParameterType.ENUM, "8",
                        connectivityOptions)
            );
            break;
         case DILATE:
            parameters.add(
                  new AnalysisParameter("Size", AnalysisParameter.ParameterType.INTEGER, 1)
            );
            parameters.add(
                  new AnalysisParameter("Connectivity", AnalysisParameter.ParameterType.ENUM, "8",
                        connectivityOptions)
            );
            break;
         case OPENING:
            parameters.add(
                  new AnalysisParameter("Size", AnalysisParameter.ParameterType.INTEGER, 1)
            );
            parameters.add(
                  new AnalysisParameter("Connectivity", AnalysisParameter.ParameterType.ENUM, "8",
                        connectivityOptions)
            );
            break;
         case CLOSING:
            parameters.add(
                  new AnalysisParameter("Size", AnalysisParameter.ParameterType.INTEGER, 1)
            );
            parameters.add(
                  new AnalysisParameter("Connectivity", AnalysisParameter.ParameterType.ENUM, "8",
                        connectivityOptions)
            );
            break;

         case GAUSSIAN_FILTER:
            parameters.add(
                  new AnalysisParameter("Sigma", AnalysisParameter.ParameterType.FLOAT, 1)
            );
            parameters.add(
                  new AnalysisParameter("Cutoff", AnalysisParameter.ParameterType.INTEGER, 1)
            );
            break;

         case MEAN_FILTER:
         case BINOMIAL_FILTER:
         case MEDIAN_FILTER:
            parameters.add(
                  new AnalysisParameter("Sigma", AnalysisParameter.ParameterType.INTEGER, 1)
            );
            break;
         case SUBTRACT:
            parameters.add(
                  new AnalysisParameter("Image", AnalysisParameter.ParameterType.INTEGER, 0)
            );
            parameters.add(
                  new AnalysisParameter("Reverse", AnalysisParameter.ParameterType.ENUM, "False",
                        new String[] {"False", "True"})
            );
            break;
         case ADD:
         case AND:
         case OR:
         case XOR:
            parameters.add(
                  new AnalysisParameter("Image", AnalysisParameter.ParameterType.INTEGER, 0)
            );
            break;

         // Methods without additional parameters
         case NORMALIZE:
         default:
            break;
      }
   }

   /**
    * Execute this step on the provided image
    *
    * @param src     Input image
    * @param width   Image width
    * @param height  Image height
    * @param inPlace Boolean flag whether this step should be executed in place, or on a copy.
    *
    * @return Image after execution of this step (either reference to input image, or a reference
    *       to a copy, depending on the inPlace flag).
    */
   public int[] executeStep(int[] src, int width, int height, boolean inPlace) {
      switch (method) {
         case DO_NOTHING:
            return ImageAnalysis.doNothing(src, inPlace);
         case THRESHOLD:
            return ImageAnalysis.threshold(src, (int) parameters.get(0).getValue(), inPlace);
         case INVERT:
            return ImageAnalysis.invert(src, inPlace);
         case FILL_GAPS:
            return ImageAnalysis.fillGaps(src, width, height, inPlace);
         case DISTANCE_TRANSFORM:
            return ImageAnalysis.distanceTransform(src, width, height, inPlace);
         case WATERSHED:
            return ImageAnalysis.watershed(src, width, height, (int) parameters.get(0).getValue(),
                  (String) parameters.get(1).getValue(), inPlace);
         case CONNECTED_COMPONENT:
            return ImageAnalysis.connectedComponent(src, width, height,
                  (String) parameters.get(0).getValue(), inPlace);
         case ERODE:
            return ImageAnalysis.erode(src, width, height, (int) parameters.get(0).getValue(),
                  (String) parameters.get(1).getValue(), inPlace);
         case DILATE:
            return ImageAnalysis.dilate(src, width, height, (int) parameters.get(0).getValue(),
                  (String) parameters.get(1).getValue(), inPlace);
         case OPENING:
            return ImageAnalysis.opening(src, width, height, (int) parameters.get(0).getValue(),
                  (String) parameters.get(1).getValue(), inPlace);
         case CLOSING:
            return ImageAnalysis.closing(src, width, height, (int) parameters.get(0).getValue(),
                  (String) parameters.get(1).getValue(), inPlace);
         case MEAN_FILTER:
            return ImageAnalysis.meanFilter(src, width, height, (int) parameters.get(0).getValue(),
                  inPlace);
         case GAUSSIAN_FILTER:
            return ImageAnalysis.gaussianFilter(src, width, height,
                  (double) parameters.get(0).getValue(),
                  (int) parameters.get(1).getValue(), inPlace);
         case BINOMIAL_FILTER:
            return ImageAnalysis.binomialFilter(src, width, height,
                  (int) parameters.get(0).getValue(),
                  inPlace);
         case MEDIAN_FILTER:
            return ImageAnalysis.medianFilter(src, width, height,
                  (int) parameters.get(0).getValue(),
                  inPlace);
         case NORMALIZE:
            return ImageAnalysis.normalize(src, inPlace);
         case ADD:
            return ImageAnalysis.add(src, img, inPlace);
         case SUBTRACT:
            if (Objects.equals(parameters.get(0).getValue(), "True")) {
               return ImageAnalysis.subtract(img, src, inPlace);
            }
            return ImageAnalysis.subtract(src, img, inPlace);
         case AND:
            return ImageAnalysis.and(src, img, inPlace);
         case OR:
            return ImageAnalysis.or(src, img, inPlace);
         case XOR:
            return ImageAnalysis.xor(src, img, inPlace);
         default:
            return (inPlace) ? src : src.clone();
      }
   }

   /**
    * Check if this step has any parameters
    *
    * @return Boolean flag whether this step has any parameters.
    */
   public boolean hasParameters() {
      return !parameters.isEmpty();
   }

   /**
    * Create a deep-copy of this step. This copy has no reference/aliasing to this object. It is
    * recreated completely.
    *
    * @return A recreated copy of this step.
    */
   public AnalysisStep copy() {
      AnalysisStep copyStep = new AnalysisStep(name, method);
      for (AnalysisParameter parameter : parameters) {
         copyStep.parameters.add(parameter.copy());
      }
      return copyStep;
   }

   /**
    * This method is called by ImageAnalysisEventHandler upon the firing of an event.
    *
    * @param evt A PropertyChangeEvent object describing the event source
    *            and the property that has changed.
    */
   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      if (Objects.equals(evt.getPropertyName(), "Parameter changed")
            && parameters.contains((AnalysisParameter) evt.getSource())) {
         eventHandler.firePropertyChange("Step changed", this, null, null);
      }
   }
}
