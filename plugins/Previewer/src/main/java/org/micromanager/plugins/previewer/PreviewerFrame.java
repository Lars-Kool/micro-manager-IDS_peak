package org.micromanager.plugins.previewer;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import mmcorej.DeviceType;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.RewritableDatastore;
import org.micromanager.display.DisplayWindow;

public class PreviewerFrame extends JFrame implements PropertyChangeListener {
   private final Studio studio;
   private final DropdownOption analysisSelector;
   private final JButton liveButton;
   private final JButton imageButton;
   private PreviewerPanel panel;

   private int[] rawImage;
   private ArrayList<int[]> imgs;
   private int width;
   private int height;

   private ArrayList<PreviewerPanel> panels;

   private final Coords.Builder builder;
   private final RewritableDatastore datastore;
   private DisplayWindow displayWindow;

   public PreviewerFrame(Studio studio) {
      this.setTitle("Image analysis previewer.");
      this.setLayout(new MigLayout());
      this.studio = studio;
      this.studio.events().registerForEvents(this);

      datastore = studio.data().createRewritableRAMDatastore();
      builder = studio.data().coordsBuilder();

      initializePanels();

      String[] names = new String[panels.size()];
      for (int i = 0; i < names.length; i++) {
         names[i] = panels.get(i).getName();
      }
      analysisSelector = new DropdownOption("Analysis", names);
      analysisSelector.addPropertyChangeListener(this);
      panel = panels.get(analysisSelector.getSelectedIndex());

      liveButton = new JButton("Refresh image");
      liveButton.addActionListener(e -> {
         rawImage = intArrayFromLive();
         if (rawImage == null) {
            return;
         }
         if (displayWindow == null) {
            displayWindow = studio.displays().createDisplay(datastore);
         }
         analyze();
         addImageToWindow(imgs.get(panel.getDisplayIndex()));
      });
      imageButton = new JButton("Import image");
      imageButton.addActionListener(e -> {
         BufferedImage img = bufferedImageFromFile();
         if (img == null) {
            return;
         }
         if (displayWindow == null) {
            displayWindow = studio.displays().createDisplay(datastore);
         }
         rawImage = bufferedImageToIntArray(img);
         analyze();
         addImageToWindow(imgs.get(panel.getDisplayIndex()));
      });

      redraw();
      this.pack();
   }

   private void initializePanels() {
      ImageAnalysis.Method[] watershedBrightfield = new ImageAnalysis.Method[] {
            ImageAnalysis.Method.INVERT,
            ImageAnalysis.Method.THRESHOLD,
            ImageAnalysis.Method.FILL_GAPS,
            ImageAnalysis.Method.DISTANCE_TRANSFORM,
            ImageAnalysis.Method.WATERSHED,
            ImageAnalysis.Method.ADD_EDGES
      };

      ImageAnalysis.Method[] watershedFluorescence = new ImageAnalysis.Method[] {
            ImageAnalysis.Method.THRESHOLD,
            ImageAnalysis.Method.DISTANCE_TRANSFORM,
            ImageAnalysis.Method.WATERSHED
      };

      panels = new ArrayList<>();
      panels.add(new PreviewerPanel(studio, this, "Watershed Brightfield", watershedBrightfield));
      panels.add(new PreviewerPanel(studio, this, "Watershed Fluorescence", watershedFluorescence));
   }

   private BufferedImage bufferedImageFromFile() {
      JFileChooser chooser = new JFileChooser();
      FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image files", "jpg", "gif", "tif", "png");
      chooser.setFileFilter(filter);
      int ret = chooser.showOpenDialog(getParent());
      if (JFileChooser.APPROVE_OPTION != ret) {
         return null;
      }

      BufferedImage img = null;
      try {
         img = ImageIO.read(new File(String.valueOf(chooser.getSelectedFile())));
      } catch (IOException ex) {
         studio.getLogManager().logError(ex, "Could not read the image.");
         return null;
      }
      if (img == null) {
         studio.getLogManager().logMessage("No valid image was read.");
         return null;
      }
      return img;
   }

   private int[] intArrayFromLive() {
      if (studio.core().getLoadedDevicesOfType(DeviceType.CameraDevice).isEmpty()) {
         studio.getLogManager().logMessage("Could not snap an image, as no camera is loaded.");
         return null;
      }
      byte[] temp = studio.getSnapLiveManager().snap(false).get(0).getByteArray();
      width = (int) studio.core().getImageWidth();
      height = (int) studio.core().getImageHeight();
      return byteArrayToIntArray(temp);
   }

   private int[] byteArrayToIntArray(byte[] src) {
      int[] out = new int[src.length];
      for (int i = 0; i < out.length; i++) {
         out[i] = src[i] & 0xFF;
      }
      return out;
   }

   private int[] bufferedImageToIntArray(BufferedImage img) {
      width = img.getWidth();
      height = img.getHeight();
      int[] imgOut = new int[width * height];
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int val = img.getRGB(x, y);
            // ARGB to greyscale
            imgOut[y * width + x] =
                  (((val & 0xff) + ((val >> 8) & 0xff) + ((val >> 16) & 0xff)) / 3);
         }
      }
      return imgOut;
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      switch (evt.getPropertyName()) {
         case "Analysis":
            panel = panels.get(analysisSelector.getSelectedIndex());
            redraw();

            if (rawImage == null) {
               return;
            }
            analyze();
            addImageToWindow(imgs.get(panel.getDisplayIndex()));
            displayWindow.autostretch();
            break;
         case "Analysis step":
            addImageToWindow(imgs.get(panel.getDisplayIndex()));
            displayWindow.autostretch();
            break;
         case "ancestor":
            break;
         default:
            if (rawImage == null) {
               return;
            }
            analyze();
            addImageToWindow(imgs.get(panel.getDisplayIndex()));
            displayWindow.autostretch();
            break;
      }
   }

   public void analyze() {
      studio.getLogManager().logMessage("Analyze");
      imgs = new ArrayList<>();
      int[] src = rawImage.clone();
      imgs.add(src.clone());

      ArrayList<HashMap<String, Object>> data = panel.getData();
      for (HashMap<String, Object> temp : data) {
         executeMethod(src, width, height, temp);
         imgs.add(src.clone());
      }
   }

   private void executeMethod(int[] src, int width, int height, HashMap<String, Object> method) {
      switch ((ImageAnalysis.Method) method.get("Method")) {
         case THRESHOLD:
            ImageAnalysis.threshold(src, (int) method.get("ThresholdValue"), true);
            break;
         case INVERT:
            ImageAnalysis.invert(
                  src, Objects.equals(method.get("ShouldInvert"),"True"), true
            );
            break;
         case FILL_GAPS:
            ImageAnalysis.fillGaps(
                  src, Objects.equals(method.get("ShouldFillGaps"), "True"), width, height, true
            );
            break;
         case DISTANCE_TRANSFORM:
            ImageAnalysis.distanceTransform(
                  src, width, height, true
            );
            break;
         case WATERSHED:
            ImageAnalysis.watershed(
                  src, width, height,(Integer) method.get("MinimumDropletRadius"), true
            );
            break;
         case ADD_EDGES:
            ImageAnalysis.addEdges(
                  src, imgs.get((Integer) method.get("EdgeImage")).clone(), width, height,
                  (Integer) method.get("EdgeWidth"),
                        true
            );
            break;
         default:
            studio.getLogManager().logMessage("Image analysis method: " +
                  ((ImageAnalysis.Method) method.get("Method")).name() + " not implemented");
      }
   }

   private void addImageToWindow(int[] img) {
      byte[] temp = new byte[img.length];
      for (int i = 0; i < img.length; i++) {
         temp[i] = (byte) (img[i] & 0xFF);
      }
      try {
         datastore.putImage(studio.data().createImage(
               temp,
               width,
               height,
               1, 1,
               builder.build(),
               null
         ));
      } catch (Exception e) {
         studio.getLogManager().logError(e, "Could not add image to display.");
      }
   }

   private void redraw() {
      this.getContentPane().removeAll();

      this.add(analysisSelector, "wrap");
      this.add(panel, "wrap");
      this.add(liveButton);
      this.add(imageButton);
      this.validate();
      this.repaint();
      this.pack();
   }

   public ArrayList<HashMap<String, Object>> getData() {
      return panel.getData();
   }
}