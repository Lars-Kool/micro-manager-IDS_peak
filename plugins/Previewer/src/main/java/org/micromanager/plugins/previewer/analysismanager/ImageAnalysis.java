package org.micromanager.plugins.previewer.analysismanager;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

public class ImageAnalysis {
   // List of methods that operate on two images
   public static ArrayList<Method> binaryMethods = new ArrayList<>(Arrays.asList(
         Method.ADD,
         Method.SUBTRACT,
         Method.AND,
         Method.OR,
         Method.XOR
   ));

   /**
    * Retrieves list of neighbors (dx, dy) based on connectivity (4 / 8 way connectivity)
    *
    * @param connectivity - either "4" or "8"
    *
    * @return list of [dx, dx] for each neighbor
    */
   private static int[][] getNeighbors(String connectivity) {
      if (Objects.equals(connectivity, "8")) {
         return new int[][] {
               {1, 1}, {1, 0}, {1, -1},
               {0, 1}, {0, -1},
               {-1, 1}, {-1, 0}, {-1, -1}
         };
      } else {
         return new int[][] {
               {1, 0},
               {0, -1}, {0, 1},
               {-1, 0}
         };
      }
   }

   /**
    * Simple NOOP function
    *
    * @param src     - Input image
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] doNothing(int[] src, boolean inPlace) {
      return (inPlace) ? src : src.clone();
   }

   /**
    * Threshold function to binarize image. All values above the 'thresholdValue' will be set to
    * 1, and other pixels will be set to 0.
    *
    * @param src            - Input image
    * @param thresholdValue - Threshold value
    * @param inPlace        - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] threshold(int[] src, int thresholdValue, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] = (out[i] >= thresholdValue) ? 1 : 0;
      }
      return out;
   }

   /**
    * Invert image, assumes all images are 8-bit.
    *
    * @param src     - Input image
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] invert(int[] src, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] = 255 - out[i];
      }
      return out;
   }

   /**
    * Fills gaps, i.e. converts donut into a circle
    *
    * @param src     - Input binary image (1 foreground, 0 background)
    * @param width   - Image width
    * @param height  - Image height
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] fillGaps(int[] src, int width, int height, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      // Set all background pixels to temporary value (2)
      for (int i = 0; i < out.length; i++) {
         if (out[i] == 0) {
            out[i] = 2;
         }
      }

      // Add all edge background pixels to a queue. These are known to be outside
      // a particle.
      Queue<Integer> queue = new LinkedList<>();
      // Vertical edges
      for (int y = 0; y < height; y++) {
         int i = y * width;
         if (out[i] == 2) {
            out[i] = 0;
            queue.add(i);
         }
         i = y * width + width - 1;
         if (out[i] == 2) {
            out[i] = 0;
            queue.add(i);
         }
      }

      // Horizontal edges
      for (int x = 0; x < width; x++) {
         int i = x;
         if (out[i] == 2) {
            out[i] = 0;
            queue.add(i);
         }
         i = (height - 1) * width + x;
         if (out[i] == 2) {
            out[i] = 0;
            queue.add(i);
         }
      }

      // Walk the queue. Every pixel neighboring a known outside pixel, is also outside
      while (!queue.isEmpty()) {
         int i = queue.remove();
         // Left
         if (i % width != 0 && out[i - 1] == 2) {
            out[i - 1] = 0;
            queue.add(i - 1);
         }
         // Above
         if (i / width != 0 && out[i - width] == 2) {
            out[i - width] = 0;
            queue.add(i - width);
         }
         // Right
         if (i % width != width - 1 && out[i + 1] == 2) {
            out[i + 1] = 0;
            queue.add(i + 1);
         }
         // Below
         if (i / width != height - 1 && out[i + width] == 2) {
            out[i + width] = 0;
            queue.add(i + width);
         }
      }

      // All pixels not reset to 0 must be inside. Set them to 1
      for (int i = 0; i < out.length; i++) {
         if (out[i] == 2) {
            out[i] = 1;
         }
         // if (out[i] == 1) {
         //    out[i] = 0;
         // }
      }
      return out;
   }

   /**
    * Distance transform, sets every pixel to its shortest distance to a background pixel.
    *
    * @param src     - Input binary image (1 foreground, 0 background)
    * @param width   - Image width
    * @param height  - Image height
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] distanceTransform(int[] src, int width, int height, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      ////////////////////// forward scan ////////////////////////
      // For top row, check only left, limit distance to 255
      out[0] = (out[0] == 0) ? 0 : 1;
      for (int x = 1; x < width; x++) {
         out[x] = (out[x] == 0) ? 0 : Math.min(out[x - 1] + 1, 255);
      }


      for (int y = 1; y < height; y++) {
         // For left pixel, check only up
         out[y * width] = (out[y * width] == 0) ? 0 : Math.min(out[(y - 1) * width] + 1, 255);
         for (int x = 1; x < width; x++) {
            if (out[y * width + x] == 0) {
               continue;
            }
            // Set value to highest between left and top, limited to 255
            out[y * width + x] = Math.min(
                  out[y * width + x - 1] + 1,
                  Math.min(out[(y - 1) * width + x] + 1, 255)
            );
         }
      }

      //////////////////////// backward scan ///////////////////////
      // For the bottom row, check only right
      for (int x = width - 2; x >= 0; x--) {
         out[(height - 1) * width + x] = Math.min(
               out[(height - 1) * width + x], out[(height - 1) * width + x + 1]
         );
      }

      for (int y = height - 2; y >= 0; y--) {
         // For right pixel, check only down
         out[(y + 1) * width - 1] = Math.min(out[(y + 1) * width - 1], out[(y + 2) * width - 1]);
         for (int x = width - 2; x >= 0; x--) {
            if (out[y * width + x] == 0) {
               continue;
            }
            // Set value to highest between right, bottom and current.
            // Current is already limited by 255, so everything is limited by 255
            out[y * width + x] = Math.min(
                  out[y * width + x + 1] + 1,
                  Math.min(out[(y + 1) * width + +x] + 1, out[y * width + x])
            );
         }
      }
      return out;
   }

   public static PriorityQueue<PriorityObject> findPeaks(int[] src, int width, int height,
                                                         int minDropletSize) {
      PriorityQueue<PriorityObject> peaks = new PriorityQueue<>(new SortDecreasing());
      for (int i = 2; i < height - 2; i++) {
         for (int j = 2; j < width - 2; j++) {
            if (
                  src[i * width + j] > 0 // Make sure pixel is foreground
                        && src[i * width + j] >= src[(i - 2) * width + j] // Two above
                        && src[i * width + j] >= src[(i - 1) * width + (j - 1)]
                        // One above, one left
                        && src[i * width + j] >= src[(i - 1) * width + j] // One above
                        && src[i * width + j] >= src[(i - 1) * width + (j + 1)] // One above, right
                        && src[i * width + j] >= src[i * width + (j - 2)] // Two left
                        && src[i * width + j] >= src[i * width + (j - 1)] // One left
                        && src[i * width + j] >= src[i * width + (j + 1)] // One right
                        && src[i * width + j] >= src[i * width + (j + 2)] // Two right
                        && src[i * width + j] >= src[(i + 1) * width + (j - 1)]
                        // One below, one left
                        && src[i * width + j] >= src[(i + 1) * width + j] // One below
                        && src[i * width + j] >= src[(i + 1) * width + (j + 1)]
                        // One below, one right
                        && src[i * width + j] >= src[(i + 2) * width + j] // Two below
                        && src[i * width + j] >= minDropletSize // Size is more than minimum value
            ) {
               // If pixel is local maximum (higher than the four surrounding
               // pixels) and higher than minPeakHeight, add location to
               // vector with local maxima.
               peaks.add(new PriorityObject(j, i, peaks.size() + 2, src[i * width + j]));
            }
         }
      }

      if (peaks.isEmpty()) {
         return peaks;
      }

      // Local maxima are in priority queue, sorted in decreasing height
      // For each local maxima check if there is a maximum of equal or higher
      // value in its surrounding. If not, include it in output.
      PriorityQueue<PriorityObject> out = new PriorityQueue<>(new SortDecreasing());
      out.add(peaks.remove());
      while (!peaks.isEmpty()) {
         PriorityObject curr = peaks.remove();
         boolean overlap = false;
         for (PriorityObject peak : out) {
            if (Math.abs(peak.x - curr.x) + Math.abs(peak.y - curr.y) < 1.5 * peak.priority) {
               overlap = true;
               break;
            }
         }
         if (!overlap) {
            out.add(curr);
         }
      }

      int i = 0;
      for (PriorityObject peak : out) {
         peak.id = i;
         i++;
      }
      return out;
   }

   /**
    * Watershed labelling.
    *
    * @param src            - Distance transformed image
    * @param width          - Image width
    * @param height         - Image height
    * @param minDropletSize - Minimum droplet size in pixels
    * @param connectivity   - Connectivity (4 or 8 way)
    * @param inPlace        - Boolean flag whether analysis should be performed in place
    *
    * @return - Labelled image where each feature has a unique index, where 0 is reserved for
    *       background pixels, and 1 is skipped (the first particle is labelled 2)
    */
   public static int[] watershed(int[] src, int width, int height, int minDropletSize,
                                 String connectivity, boolean inPlace) throws RuntimeException {
      int[] out = new int[src.length];
      PriorityQueue<PriorityObject> queue = findPeaks(src, width, height, minDropletSize);

      double[] sizes = new double[queue.size()];
      boolean[] isBulkDroplet = new boolean[queue.size()];
      Arrays.fill(isBulkDroplet, true);

      int[][] neighbors = getNeighbors(connectivity);
      while (!queue.isEmpty()) {
         // Pop highest priority
         PriorityObject curr = queue.remove();

         for (int[] neighbor : neighbors) {
            if (curr.x == 0 || curr.y == 0 || curr.x == width - 1 || curr.y == height - 1) {
               isBulkDroplet[curr.id] = false;
               continue;
            }

            // To be assigned to a segment
            if (
                  src[(curr.y + neighbor[0]) * width + (curr.x + neighbor[1])] > 0
            ) {
               queue.add(new PriorityObject(
                     curr.x + neighbor[1],
                     curr.y + neighbor[0],
                     curr.id,
                     src[(curr.y + neighbor[0]) * width + (curr.x + neighbor[1])]));
               src[(curr.y + neighbor[0]) * width + (curr.x + neighbor[1])] = 0;
               sizes[curr.id]++;
               out[(curr.y + neighbor[0]) * width + (curr.x + neighbor[1])] = curr.id;
            }
         }
      }

      // remove edge droplets

      if (inPlace) {
         System.arraycopy(out, 0, src, 0, src.length);
      }
      return out;
   }

   /**
    * Connected component labelling.
    *
    * @param src          - Input binarized image
    * @param width        - Image width
    * @param height       - Image height
    * @param connectivity - Connectivity (4 or 8 way)
    * @param inPlace      - Boolean flag whether analysis should be performed in place
    *
    * @return - Labelled image where each feature has a unique index, where 0 is reserved for
    *       background pixels, and 1 is skipped (the first particle is labelled 2)
    */
   public static int[] connectedComponent(int[] src, int width, int height, String connectivity,
                                          boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      int[][] neighbors = getNeighbors(connectivity);

      int id = 2;
      Queue<Integer> queue = new ArrayDeque<>();
      for (int i = 0; i < out.length; i++) {
         if (out[i] != 1) {
            continue;
         }

         queue.add(i);
         out[i] = id;
         while (!queue.isEmpty()) {
            int idx = queue.remove();
            int y = idx / width;
            int x = idx % width;
            for (int[] neighbor : neighbors) {
               if (
                     x + neighbor[1] < 0 || x + neighbor[1] == width
                           || y + neighbor[0] < 0 || y + neighbor[0] == height
               ) {
                  continue;
               }

               queue.add((y + neighbor[0]) * width + neighbor[1] + x);
               out[(y + neighbor[0]) * width + neighbor[1] + x] = id;
            }
         }
         id += 1;
      }
      return out;
   }

   /**
    * Erode operation. Removes edge pixels to shrink features.
    *
    * @param src          Input binary image
    * @param width        Image width
    * @param height       Image height
    * @param sigma        Distance to shrink feature (in pixels)
    * @param connectivity - Connectivity (4 or 8 way)
    * @param inPlace      - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] erode(int[] src, int width, int height, int sigma,
                             String connectivity, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      int[][] neighbors = getNeighbors(connectivity);

      // Get queue of foreground pixels neighboring a background pixel
      Queue<Integer> queue = new LinkedList<>();
      for (int i = 0; i < out.length; i++) {
         if (out[i] != 0) {
            continue;
         }
         for (int[] neighbor : neighbors) {
            int x = i % width + neighbor[1];
            int y = i / width + neighbor[0];
            if (x < 0 || x >= width || y < 0 || y >= height) {
               continue;
            }
            if (out[y * width + x] != 1) {
               continue;
            }
            out[y * width + x] = 2;
            queue.add(y * width + x);
         }
      }

      while (!queue.isEmpty()) {
         int i = queue.poll();
         for (int[] neighbor : neighbors) {
            int x = i % width + neighbor[1];
            int y = i / width + neighbor[0];
            // Check bounds
            if (x < 0 || x >= width || y < 0 || y >= height) {
               continue;
            }
            if (out[y * width + x] != 1) {
               continue;
            }
            out[y * width + x] = out[i] + 1;
            queue.add(y * width + x);
         }
      }

      for (int i = 0; i < out.length; i++) {
         out[i] = (out[i] > sigma + 1) ? 1 : 0;
      }
      return out;
   }

   /**
    * Dilate operation. Adds edge pixels to dilate features.
    *
    * @param src          Input binary image
    * @param width        Image width
    * @param height       Image height
    * @param sigma        Distance to shrink feature (in pixels)
    * @param connectivity - Connectivity (4 or 8 way)
    * @param inPlace      - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] dilate(int[] src, int width, int height, int sigma, String connectivity,
                              boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      if (sigma <= 0) {
         return out;
      }

      int[][] neighbors = getNeighbors(connectivity);
      Queue<Integer> queue = new LinkedList<>();
      for (int i = 0; i < out.length; i++) {
         if (out[i] != 1) {
            continue;
         }
         for (int[] neighbor : neighbors) {
            int x = i % width + neighbor[1];
            int y = i / width + neighbor[0];
            if (x < 0 || x >= width || y < 0 || y >= height) {
               continue;
            }
            if (out[y * width + x] > 0) {
               continue;
            }
            out[y * width + x] = 2;
            queue.add(y * width + x);
         }
      }

      while (!queue.isEmpty()) {
         int i = queue.poll();
         if (out[i] >= sigma + 1) {
            continue;
         }
         for (int[] neighbor : neighbors) {
            int x = i % width + neighbor[1];
            int y = i / width + neighbor[0];
            // Check bounds
            if (x < 0 || x >= width || y < 0 || y >= height) {
               continue;
            }
            if (out[y * width + x] != 0) {
               continue;
            }
            out[y * width + x] = out[i] + 1;
            queue.add(y * width + x);
         }
      }

      for (int i = 0; i < out.length; i++) {
         out[i] = (out[i] > 0) ? 1 : 0;
      }
      return out;
   }

   /**
    * Opening operation (erosion followed by dilation), rounds convex corners.
    *
    * @param src          Input binary image
    * @param width        Image width
    * @param height       Image height
    * @param sigma        Distance to shrink feature (in pixels)
    * @param connectivity - Connectivity (4 or 8 way)
    * @param inPlace      - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] opening(int[] src, int width, int height, int sigma, String connectivity,
                               boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      out = erode(out, width, height, sigma, connectivity, inPlace);
      out = dilate(out, width, height, sigma, connectivity, inPlace);
      return out;
   }

   /**
    * Closing operation (dilation followed by erosion), rounds concave corners.
    *
    * @param src          Input binary image
    * @param width        Image width
    * @param height       Image height
    * @param sigma        Distance to shrink feature (in pixels)
    * @param connectivity - Connectivity (4 or 8 way)
    * @param inPlace      - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] closing(int[] src, int width, int height, int sigma, String connectivity,
                               boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      out = dilate(out, width, height, sigma, connectivity, inPlace);
      out = erode(out, width, height, sigma, connectivity, inPlace);
      return out;
   }

   /**
    * Single pass of a mean filter using a sliding window algorithm. Watch out, this algorithm
    * also transposes the image! This way two passes results in a 2D mean filter.
    *
    * @param src     Input image
    * @param width   Image width
    * @param height  Image height
    * @param sigma   Filter size
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   private static int[] meanFilterPass(int[] src, int width, int height, int sigma,
                                       boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      int[] temp = out.clone();
      int partialSum = 0;
      int size = 2 * sigma + 1;
      for (int y = 0; y < height; y++) {
         // Initialize window
         partialSum = 0;
         for (int x = 0; x <= sigma; x++) {
            partialSum += temp[y * width + x];
         }
         // Start of window
         for (int x = 0; x < sigma; x++) {
            out[x * height + y] = partialSum / (sigma + x + 1);
            partialSum += temp[y * width + x + sigma + 1];
         }
         // Bulk
         for (int x = sigma; x < width - sigma - 1; x++) {
            out[x * height + y] = partialSum / size;
            partialSum += temp[y * width + x + sigma + 1] - temp[y * width + x - sigma];
         }
         // End of window
         for (int x = width - sigma - 1; x < width; x++) {
            out[x * height + y] = partialSum / (sigma + width - x);
            partialSum -= temp[y * width + x - sigma];
         }
      }
      return out;
   }

   /**
    * Mean filter using a sliding window algorithm.
    *
    * @param src     Input image
    * @param width   Image width
    * @param height  Image height
    * @param sigma   Filter size
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] meanFilter(int[] src, int width, int height, int sigma, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      out = meanFilterPass(out, width, height, sigma, inPlace);
      // Inverse width and height, as image is transposed!
      out = meanFilterPass(out, height, width, sigma, inPlace);
      return out;
   }

   /**
    * Helper function that applies a provided 1D weighted kernel to an image
    *
    * @param src     Input image
    * @param width   Image width
    * @param height  Image height
    * @param kernel  Kernel to be applied (relative weights)
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   private static int[] applyKernel(int[] src, int width, int height, double[] kernel,
                                    boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      double[] values = new double[out.length];
      double[] counts = new double[out.length];
      int offset = kernel.length / 2;
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int start = Math.max(0, (kernel.length / 2) - x);
            int end = Math.min(kernel.length, width - x + kernel.length / 2);
            for (int i = start; i < end; i++) {
               values[x * height + y] += src[y * width + x - offset + i] * kernel[i];
               counts[x * height + y] += kernel[i];
            }
         }
      }

      for (int i = 0; i < out.length; i++) {
         if (counts[i] == 0) {
            values[i] = 0;
         } else {
            out[i] = (int) Math.max(values[i] / counts[i], 0);
         }
      }
      return out;
   }

   /**
    * Generate gaussian kernel, given a standard deviation (sigma)
    *
    * @param sigma  Standard deviation
    * @param cutoff Size of gaussian kernel (size = 2 * cutoff + 1)
    *
    * @return 1D symmetric Gaussian kernel, where i = cutoff + 1 is the center.
    */
   private static double[] getGaussianKernel(double sigma, int cutoff) {
      double[] output = new double[2 * cutoff + 1];
      for (int i = -cutoff; i < cutoff + 1; i++) {
         output[i + cutoff] = Math.pow(Math.E, (double) -(i * i) / (2 * sigma * sigma));
      }
      return output;
   }

   /**
    * Apply Gaussian filter to image.
    *
    * @param src     Input image
    * @param width   Image width
    * @param height  Image height
    * @param sigma   Standard deviation
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] gaussianFilter(int[] src, int width, int height, double sigma,
                                      int cutoff, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      double[] kernel = getGaussianKernel(sigma, cutoff);
      out = applyKernel(out, width, height, kernel, true);
      // Inverse width and height, as image is transposed!
      out = applyKernel(out, height, width, kernel, true);
      return out;
   }

   /**
    * Generate binomial kernel of size 2 * sigma + 1.
    * This is a fast approximation of a gaussian kernel that reaches zero at the nyquist limit.
    * To approximate a gaussian with sigma = sigma_gauss, you need a sigma_binom of 4 *
    * (sigma_gauss ** 2), e.g. sigma_gauss = 3 -> sigma_binom = 36
    *
    * @param sigma Standard deviation
    *
    * @return 1D symmetric Gaussian kernel, where i = cutoff + 1 is the center.
    */
   public static double[] getBinomialKernel(int sigma) {
      double[] kernel = new double[2 * sigma + 1];
      kernel[0] = 1;
      for (int i = 0; i < 2 * sigma + 1; i++) {
         for (int j = i; j > 0; j--) {
            kernel[j] = kernel[j] + kernel[j - 1];
         }
      }
      return kernel;
   }

   /**
    * Apply binomial filter to image.
    *
    * @param src     Input image
    * @param width   Image width
    * @param height  Image height
    * @param sigma   Standard deviation
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] binomialFilter(int[] src, int width, int height, int sigma,
                                      boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      double[] kernel = getBinomialKernel(sigma);
      out = applyKernel(out, width, height, kernel, true);
      // Inverse width and height, as image is transposed!
      out = applyKernel(out, height, width, kernel, true);
      return out;
   }

   /**
    * Apply median filter to image.
    *
    * @param src     Input image
    * @param width   Image width
    * @param height  Image height
    * @param sigma   Region to look for median
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] medianFilter(int[] src, int width, int height, int sigma, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      int[] temp = src.clone();

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            ArrayList<Integer> vals = new ArrayList<Integer>();
            for (int dy = Math.max(0, y - sigma); dy < Math.min(height, y + sigma); dy++) {
               for (int dx = Math.max(0, x - sigma); dx < Math.min(width, x + sigma); dx++) {
                  vals.add(temp[dy * width + dx]);
               }
            }
            out[y * width + x] = vals.get(vals.size() / 2);
         }
      }
      return out;
   }

   /**
    * Normalize image between 0 and 255.
    *
    * @param src     Input image
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] normalize(int[] src, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      int min = Integer.MAX_VALUE;
      int max = Integer.MIN_VALUE;
      for (int val : out) {
         if (val >= max) {
            max = val;
         }
         if (val <= min) {
            min = val;
         }
      }

      int diff = max - min;
      for (int i = 0; i < out.length; i++) {
         out[i] = (out[i] - min) * 255 / diff;
      }
      return out;
   }

   /**
    * Transpose image
    *
    * @param src     Input image
    * @param width   Image width
    * @param height  Image height
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] transpose(int[] src, int width, int height, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      int[] temp = src.clone();
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            out[x * height + y] = temp[y * width + x];
         }
      }
      return out;
   }

   /**
    * Adds two images pixel by pixel
    *
    * @param src     Input image
    * @param binImg  Second input image
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] add(int[] src, int[] binImg, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] += binImg[i];
      }
      return out;
   }

   // Binary methods
   // TODO: Add mask logical operators

   /**
    * Subtract binImg from src, pixel by pixel
    *
    * @param src     Input image
    * @param binImg  Second input image
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] subtract(int[] src, int[] binImg, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] -= binImg[i];
      }
      return out;
   }

   /**
    * Apply bitwise logical AND operator pixel by pixel between two images. If both the src image
    * and the mask are binary, this becomes binary operation. Otherwise, it performs the and bitwise
    *
    * @param src     Input image
    * @param mask    Image mask
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] and(int[] src, int[] mask, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] &= mask[i];
      }
      return out;
   }

   /**
    * Apply bitwise logical OR operator pixel by pixel between two images. If both the src image
    * and the mask are binary, this becomes binary operation. Otherwise, it performs the and bitwise
    *
    * @param src     Input image
    * @param mask    Image mask
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] or(int[] src, int[] mask, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] |= mask[i];
      }
      return out;
   }

   /**
    * Apply bitwise logical XOR operator pixel by pixel between two images. If both the src image
    * and the mask are binary, this becomes binary operation. Otherwise, it performs the and bitwise
    *
    * @param src     Input image
    * @param mask    Image mask
    * @param inPlace - Boolean flag whether analysis should be performed in place
    *
    * @return - Image after analysis (either reference to input image, or deepcopy, based on
    *       inPlace flag)
    */
   public static int[] xor(int[] src, int[] mask, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] ^= mask[i];
      }
      return out;
   }

   /**
    * Returns the average size of a foreground region, given a segmented image. Foreground pixels
    * should be labelled 2 or higher, whereas 0 and 1 are reserved for background. There should
    * be at least 3 regions.
    *
    * @param src     Input binary image
    * @param minSize Minimum diameter of object (
    *
    * @return Average diameter of a foreground region, assuming it is circular
    */
   public static double getAverageSize(int[] src, double minSize) {
      HashMap<Integer, Integer> counts = new HashMap<>();
      for (int val : src) {
         counts.put(val, counts.getOrDefault(val, 0) + 1);
      }

      double areaSum = 0;
      int particleCount = 0;
      double minArea = Math.PI * minSize * minSize / 4;
      for (int key : counts.keySet()) {
         int val = counts.get(key);
         if (key < 2) {
            continue;
         }
         if (val < minArea) {
            continue;
         }

         areaSum += val;
         particleCount += 1;
      }
      return (particleCount > 2) ? areaSum / particleCount : 0;
   }

   public enum Method {
      @SerializedName("Do nothing")
      DO_NOTHING,
      @SerializedName("Threshold")
      THRESHOLD,
      @SerializedName("Invert")
      INVERT,
      @SerializedName("Fill gaps")
      FILL_GAPS,
      @SerializedName("Distance transform")
      DISTANCE_TRANSFORM,
      @SerializedName("Watershed")
      WATERSHED,
      @SerializedName("ConnectedComponent")
      CONNECTED_COMPONENT,
      @SerializedName("Erode")
      ERODE,
      @SerializedName("Dilate")
      DILATE,
      @SerializedName("Opening")
      OPENING,
      @SerializedName("Closing")
      CLOSING,
      @SerializedName("Mean filter")
      MEAN_FILTER,
      @SerializedName("Gaussian filter")
      GAUSSIAN_FILTER,
      @SerializedName("Binomial filter")
      BINOMIAL_FILTER,
      @SerializedName("Median filter")
      MEDIAN_FILTER,
      @SerializedName("Normalize")
      NORMALIZE,
      @SerializedName("Add")
      ADD,
      @SerializedName("Subtract")
      SUBTRACT,
      @SerializedName("And")
      AND,
      @SerializedName("Or")
      OR,
      @SerializedName("Xor")
      XOR
   }

   /**
    * Object for priority queues
    * x - Column index of pixel
    * y - Row index of pixel
    * id - Identifier of object
    * priority - Value to sort object
    */
   static class PriorityObject {
      int x; // column
      int y; // y-position
      int id; // segment-id
      int priority; // priority

      PriorityObject(int x, int y, int id, int priority) {
         this.x = x;
         this.y = y;
         this.id = id;
         this.priority = priority;
      }
   }

   /**
    * Comparator that sorts Priority objects in decreasing (non-increasing) order.
    */
   static class SortDecreasing implements Comparator<PriorityObject> {
      @Override
      public int compare(PriorityObject o0, PriorityObject o2) {
         if (o0.priority > o2.priority) {
            return -1;
         } else if (o0.priority < o2.priority) {
            return 1;
         }
         return 0;
      }
   }

   /**
    * Comparator that sorts Priority objects in increasing (non-decreasing) order.
    */
   static class SortIncreasing implements Comparator<PriorityObject> {
      @Override
      public int compare(PriorityObject o0, PriorityObject o2) {
         if (o0.priority > o2.priority) {
            return 1;
         } else if (o0.priority < o2.priority) {
            return -1;
         }
         return 0;
      }
   }
}