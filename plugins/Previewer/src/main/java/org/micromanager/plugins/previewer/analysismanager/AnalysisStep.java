package org.micromanager.plugins.previewer.analysismanager;

import java.util.ArrayList;
import java.util.Objects;

public class AnalysisStep {
   private final String[] connectivityOptions = new String[] {"4", "8"};
   public String name;
   public ImageAnalysis.Method method;
   public ArrayList<AnalysisParameter> parameters;
   public boolean isBinary = false;
   public transient int[] img = null;

   public AnalysisStep() {
   }

   public AnalysisStep(String name, ImageAnalysis.Method method) {
      this.name = name;
      this.method = method;
      this.parameters = new ArrayList<>();
   }

   public AnalysisStep(ImageAnalysis.Method method) {
      parameters = new ArrayList<>();
      this.name = method.toString();
      this.method = method;
      switch (method) {
         case THRESHOLD:
            parameters.add(new AnalysisParameter("ThresholdValue",
                  AnalysisParameter.ParameterType.INTEGER, 0)
            );
            break;

         case WATERSHED:
            parameters.add(new AnalysisParameter("Minimum Droplet size",
                  AnalysisParameter.ParameterType.INTEGER, 1));
            parameters.add(new AnalysisParameter("Connectivity",
                  AnalysisParameter.ParameterType.ENUM, "8",
                  connectivityOptions));
            break;

         case ERODE:
         case DILATE:
         case OPENING:
         case CLOSING:
            parameters.add(
                  new AnalysisParameter("Size", AnalysisParameter.ParameterType.INTEGER, 1)
            );
         case CONNECTED_COMPONENT:
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
         case INVERT:
         case FILL_GAPS:
         case DISTANCE_TRANSFORM:
         case NORMALIZE:
         default:
            break;
      }
   }

   public int[] executeStep(int[] src, int width, int height, boolean inPlace)
         throws Exception {
      switch (method) {
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

   public boolean hasParameters() {
      return !parameters.isEmpty();
   }

   public AnalysisStep copy() {
      AnalysisStep copyStep = new AnalysisStep(name, method);
      for (AnalysisParameter parameter : parameters) {
         copyStep.parameters.add(parameter.copy());
      }
      return copyStep;
   }
}
