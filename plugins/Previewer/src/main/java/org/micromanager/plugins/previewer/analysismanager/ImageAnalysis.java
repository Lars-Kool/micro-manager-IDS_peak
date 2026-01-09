package org.micromanager.plugins.previewer.analysismanager;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

public class ImageAnalysis {
   public static ArrayList<Method> binaryMethods = new ArrayList<>(Arrays.asList(
         Method.ADD,
         Method.SUBTRACT,
         Method.AND,
         Method.OR,
         Method.XOR
   ));

   static int[][] getNeighbors(String connectivity) {
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

   static int[] threshold(int[] src, int thresholdValue, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] = (out[i] >= thresholdValue) ? 1 : 0;
      }
      return out;
   }

   static int[] invert(int[] src, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();

      for (int i = 0; i < out.length; i++) {
         out[i] = 255 - out[i];
      }
      return out;
   }

   static int[] fillGaps(int[] src, int width, int height, boolean inPlace) {
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

   static int[] distanceTransform(int[] src, int width, int height, boolean inPlace) {
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

   static PriorityQueue<PriorityObject> findPeaks(int[] src, int width, int height,
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
         return null;
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

   static int[] watershed(int[] src, int width, int height, int minDropletSize,
                          String connectivity, boolean inPlace) {
      int[] out = new int[src.length];

      PriorityQueue<PriorityObject> queue = findPeaks(src, width, height, minDropletSize);
      if (queue == null) {
         return new int[src.length];
      }

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

            if (
                  // To be assigned to a segment
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
      if (inPlace) {
         System.arraycopy(out, 0, src, 0, src.length);
      }
      return out;
   }

   static int[] addEdges(int[] src, int[] edges, int width, int height, int edgeWidth,
                         boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      int[][] neighbors = getNeighbors("8");

      PriorityQueue<PriorityObject> queue = new PriorityQueue<>(new SortIncreasing());
      for (int y = 1; y < height - 1; y++) {
         for (int x = 1; x < width - 1; x++) {
            if (edges[y * width + x] != 1) {
               continue;
            }
            for (int[] neighbor : neighbors) {
               int idx = out[(y + neighbor[0]) * width + neighbor[1] + x];
               if (idx == 0) {
                  continue;
               }
               queue.add(new PriorityObject(
                     x,
                     y,
                     idx,
                     0
               ));
               edges[y * width + x] = 0;
               break;
            }
         }
      }

      while (!queue.isEmpty()) {
         PriorityObject curr = queue.remove();
         // Already visited, or max edge width reached
         if (
               curr.x < 0 || curr.x >= width  // out of bounds x
                     || curr.y < 0 || curr.y >= height  // out of bounds y
                     || out[curr.y * width + curr.x] != 0  // Pixel already set
         ) {
            continue;
         }
         out[curr.y * width + curr.x] = curr.id;

         for (int[] neighbor : neighbors) {
            int x = curr.x + neighbor[1];
            int y = curr.y + neighbor[0];

            if (edges[y * width + x] == 1 && curr.priority < edgeWidth) {
               queue.add(new PriorityObject(
                     x,
                     y,
                     curr.id,
                     curr.priority + 1
               ));
               edges[y * width + x] = 0;
            }
         }
      }
      return out;
   }

   static int[] connectedComponent(int[] src, int width, int height, String connectivity,
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

   static int[] erode(int[] src, int width, int height, int sigma,
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

   static int[] dilate(int[] src, int width, int height, int sigma, String connectivity,
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

   static int[] opening(int[] src, int width, int height, int sigma, String connectivity,
                        boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      out = erode(out, width, height, sigma, connectivity, inPlace);
      out = dilate(out, width, height, sigma, connectivity, inPlace);
      return out;
   }

   static int[] closing(int[] src, int width, int height, int sigma, String connectivity,
                        boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      out = dilate(out, width, height, sigma, connectivity, inPlace);
      out = erode(out, width, height, sigma, connectivity, inPlace);
      return out;
   }

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

   static int[] meanFilter(int[] src, int width, int height, int sigma, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      out = meanFilterPass(out, width, height, sigma, inPlace);
      // Inverse width and height, as image is transposed!
      out = meanFilterPass(out, height, width, sigma, inPlace);
      return out;
   }

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

   private static double[] getGaussianKernel(double sigma, int cutoff) {
      double[] output = new double[2 * cutoff + 1];
      for (int i = -cutoff; i < cutoff + 1; i++) {
         output[i + cutoff] = Math.pow(Math.E, (double) -(i * i) / (2 * sigma * sigma));
      }
      return output;
   }

   static int[] gaussianFilter(int[] src, int width, int height, double sigma,
                               int cutoff, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      double[] kernel = getGaussianKernel(sigma, cutoff);
      out = applyKernel(out, width, height, kernel, true);
      // Inverse width and height, as image is transposed!
      out = applyKernel(out, height, width, kernel, true);
      return out;
   }

   static double[] getBinomialKernel(int sigma) {
      double[] kernel = new double[2 * sigma + 1];
      kernel[0] = 1;
      for (int i = 0; i < 2 * sigma + 1; i++) {
         for (int j = i; j > 0; j--) {
            kernel[j] = kernel[j] + kernel[j - 1];
         }
      }
      return kernel;
   }

   static int[] binomialFilter(int[] src, int width, int height, int sigma, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      double[] kernel = getBinomialKernel(sigma);
      out = applyKernel(out, width, height, kernel, true);
      // Inverse width and height, as image is transposed!
      out = applyKernel(out, height, width, kernel, true);
      return out;
   }

   static int[] medianFilter(int[] src, int width, int height, int sigma, boolean inPlace) {
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

   static int[] normalize(int[] src, boolean inPlace) {
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

   static int[] transpose(int[] src, int width, int height, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      int[] temp = src.clone();
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            out[x * height + y] = temp[y * width + x];
         }
      }
      return out;
   }

   // Region indexes start with 2 (0 is reserved for background,
   // 1 is reserved for foreground before segmenting)
   private static int countRegions(int[] src) {
      int maxVal = 0;
      for (int val : src) {
         maxVal = Math.max(val, maxVal);
      }
      return maxVal - 2;
   }

   // Binary methods
   static int[] add(int[] src, int[] binImg, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] += binImg[i];
      }
      return out;
   }

   static int[] subtract(int[] src, int[] binImg, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] -= binImg[i];
      }
      return out;
   }

   static int[] and(int[] src, int[] mask, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] &= mask[i];
      }
      return out;
   }

   static int[] or(int[] src, int[] mask, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] |= mask[i];
      }
      return out;
   }

   static int[] xor(int[] src, int[] mask, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      for (int i = 0; i < out.length; i++) {
         out[i] ^= mask[i];
      }
      return out;
   }

   public enum Method {
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
      // Math (addition/subtraction/multiplication)
   }

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