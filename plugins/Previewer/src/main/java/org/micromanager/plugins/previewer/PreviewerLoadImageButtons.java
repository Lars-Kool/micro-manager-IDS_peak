package org.micromanager.plugins.previewer;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import mmcorej.DeviceType;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;

public class PreviewerLoadImageButtons extends JPanel {
   private final Studio studio;
   private final JButton liveButton;
   private final JButton imageButton;

   private final PropertyChangeSupport support;

   private int[] img;
   private int width;
   private int height;

   public PreviewerLoadImageButtons(Studio studio) {
      this.studio = studio;
      this.setLayout(new MigLayout());

      support = new PropertyChangeSupport(this);

      liveButton = new JButton("Refresh image");
      liveButton.addActionListener(e -> {
         intArrayFromLive();
         if (img == null) {
            return;
         }
         support.firePropertyChange("New image loaded", null, img);
      });
      this.add(liveButton);

      imageButton = new JButton("Import image");
      imageButton.addActionListener(e -> {
         intArrayFromFile();
         if (img == null) {
            return;
         }
         support.firePropertyChange("New image loaded", null, img);
      });
      this.add(imageButton);
   }

   public int[] getImg() {
      return img;
   }

   public int getImgWidth() {
      return width;
   }

   public int getImgHeight() {
      return height;
   }

   public void addListener(PropertyChangeListener listener) {
      support.addPropertyChangeListener(listener);
   }

   public void removeListener(PropertyChangeListener listener) {
      support.removePropertyChangeListener(listener);
   }

   private void intArrayFromFile() {
      BufferedImage bufferedImage = bufferedImageFromFile();
      if (bufferedImage == null) {
         return;
      }
      bufferedImageToIntArray(bufferedImage);
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

   private void intArrayFromLive() {
      if (studio.core().getLoadedDevicesOfType(DeviceType.CameraDevice).isEmpty()) {
         studio.getLogManager().logMessage("Could not snap an image, as no camera is loaded.");
         img = null;
         return;
      }
      byte[] temp = studio.getSnapLiveManager().snap(false).get(0).getByteArray();
      width = (int) studio.core().getImageWidth();
      height = (int) studio.core().getImageHeight();
      img = byteArrayToIntArray(temp);
   }

   private int[] byteArrayToIntArray(byte[] src) {
      int[] out = new int[src.length];
      for (int i = 0; i < out.length; i++) {
         out[i] = src[i] & 0xFF;
      }
      return out;
   }

   private void bufferedImageToIntArray(BufferedImage image) {
      width = image.getWidth();
      height = image.getHeight();
      img = new int[width * height];
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int val = image.getRGB(x, y);
            // ARGB to greyscale
            img[y * width + x] =
                  (((val & 0xff) + ((val >> 8) & 0xff) + ((val >> 16) & 0xff)) / 3);
         }
      }
   }
}
