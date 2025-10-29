package org.micromanager.plugins.previewer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class ImageAnalysis {
   public enum Method {
      THRESHOLD,
      INVERT,
      FILL_GAPS,
      DISTANCE_TRANSFORM,
      WATERSHED,
      ADD_EDGES,
      MEAN_FILTER
      // Median Filter (Actual 2D filter, maybe too expensive)
      // Gaussian blur
      // Dilate
      // Erosion
      // Opening
      // Closing
   }

   static int[][] getNeighbors(int connectivity) {
      if (connectivity == 8) {
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

   static int[] invert(int[] src, boolean shouldInvert, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      if (!shouldInvert) {
         return out;
      }
      for (int i = 0; i < out.length; i++) {
         out[i] = 255 - out[i];
      }
      return out;
   }

   static int[] fillGaps(int[] src, boolean shouldFill, int width, int height, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      if (!shouldFill) {
         return out;
      }

      // Set all background pixels to temporary value (2)
      for (int i = 0; i < out.length; i++) {
         if (out[i] == 0) {
            out[i] = 2;
         }
      }


      // Add all edge background pixels to a queue (these are known to be outside
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
            out[i] = 255;
         }
         if (out[i] == 1) {
            out[i] = 0;
         }
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
                  && src[i * width + j] >= src[(i - 1) * width + (j - 1)] // One above, one left
                  && src[i * width + j] >= src[(i - 1) * width + j] // One above
                  && src[i * width + j] >= src[(i - 1) * width + (j + 1)] // One above, right
                  && src[i * width + j] >= src[i * width + (j - 2)] // Two left
                  && src[i * width + j] >= src[i * width + (j - 1)] // One left
                  && src[i * width + j] >= src[i * width + (j + 1)] // One right
                  && src[i * width + j] >= src[i * width + (j + 2)] // Two right
                  && src[i * width + j] >= src[(i + 1) * width + (j - 1)] // One below, one left
                  && src[i * width + j] >= src[(i + 1) * width + j] // One below
                  && src[i * width + j] >= src[(i + 1) * width + (j + 1)] // One below, one right
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

   static int[] watershed(int[] src, int width, int height, int minDropletSize, boolean inPlace) {
      int[] out = new int[src.length];

      PriorityQueue<PriorityObject> queue = findPeaks(src, width, height, minDropletSize);
      if (queue == null) {
         return new int[src.length];
      }

      double[] sizes = new double[queue.size()];
      boolean[] isBulkDroplet = new boolean[queue.size()];
      Arrays.fill(isBulkDroplet, true);

      int[][] neighbors = getNeighbors(8);
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
      int[][] neighbors = getNeighbors(8);

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
               curr.x < 0 || curr.x >= width
               || curr.y < 0 || curr.y >= height
               || out[curr.y * width + curr.x] != 0
               || curr.priority >= edgeWidth
         ) {
            continue;
         }
         out[curr.y * width + curr.x] = curr.id;

         for (int[] neighbor : neighbors) {
            int x = curr.x + neighbor[1];
            int y = curr.y + neighbor[0];

            if (edges[y * width + x] == 1) {
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
      int[][] neighbors = getNeighbors(Integer.parseInt(connectivity));

      int id = 2;

      Queue<Integer> queue = new LinkedList<>();
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

   static int[] meanFilter(int[] src, int width, int height, int sigma, boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      out = meanFilterPass(out, width, height, sigma, inPlace);
      out = meanFilterPass(out, width, height, sigma, inPlace);
      return out;
   }

   private static int[] meanFilterPass(int[] src, int width, int height, int sigma,
                                       boolean inPlace) {
      int[] out = (inPlace) ? src : src.clone();
      int partialSum = 0;
      int size = 2 * sigma + 1;
      for (int y = 0; y < height; y++) {
         // Initialize window
         partialSum = 0;
         for (int i = -sigma; i < sigma + 1; i++) {
            partialSum += (i < 0) ? src[y * width] : src[y * width + i];
         }
         // Bulk
         for (int x = 0; x < width; x++) {
            out[x * height + y] = partialSum / size;
            partialSum += (x + sigma + 1 >= width)
                  ? src[y * width] : src[y * width + x + sigma + 1];
            partialSum -= (x - sigma < 0) ? src[y * width] : src[y * width + x - sigma];
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
            return -2;
         } else if (o0.priority < o2.priority) {
            return 0;
         }
         return -1;
      }
   }

   static class SortIncreasing implements Comparator<PriorityObject> {
      @Override
      public int compare(PriorityObject o0, PriorityObject o2) {
         if (o0.priority > o2.priority) {
            return 0;
         } else if (o0.priority < o2.priority) {
            return -2;
         }
         return -1;
      }
   }
}


