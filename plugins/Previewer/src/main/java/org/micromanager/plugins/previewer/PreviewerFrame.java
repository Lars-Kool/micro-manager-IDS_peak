package org.micromanager.plugins.previewer;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.RewritableDatastore;
import org.micromanager.display.DisplayWindow;
import org.micromanager.internal.utils.WindowPositioning;
import org.micromanager.plugins.previewer.analysismanager.AnalysisManager;
import org.micromanager.plugins.previewer.analysismanager.AnalysisSequence;

public class PreviewerFrame extends JFrame implements PropertyChangeListener, Runnable {
   private final Studio studio;
   private final AnalysisManager analysisManager;
   private final Coords.Builder builder;
   private final RewritableDatastore datastore;
   private final AtomicBoolean isRunning;
   private OptionDropdown analysisSelector;
   private PreviewerPanel panel;
   private PreviewerLoadImageButtons buttonPanel;
   private JButton newButton;
   private JButton editButton;
   private JLabel elapsedLabel;
   private JTextField elapsedTextfield;
   private int[] rawImage;
   private ArrayList<int[]> imgs;
   private int width;
   private int height;
   private DisplayWindow displayWindow;
   private SequenceGeneratorFrame sequenceGeneratorFrame;
   private Thread analysisThread;

   public PreviewerFrame(Studio studio) {
      this.setTitle("Image analysis previewer.");
      this.setLayout(new MigLayout());
      this.studio = studio;

      analysisManager = new AnalysisManager(studio);
      analysisManager.addListener(this);
      panel =
            new PreviewerPanel(studio, this, analysisManager, analysisManager.getCurrentSequence());

      datastore = studio.data().createRewritableRAMDatastore();
      builder = studio.data().coordsBuilder();
      isRunning = new AtomicBoolean(false);

      initializeFinalComponents();
      updateSequenceNames();

      this.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            analysisManager.saveToFile();
            super.windowClosing(e);
         }
      });

      redraw();
      this.pack();
      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(100, 100);
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);
      this.studio.events().registerForEvents(this);
   }

   private void updateSequenceNames() {
      String[] names = new String[analysisManager.size()];
      int i = 0;
      for (AnalysisSequence sequence : analysisManager.getSequences()) {
         names[i] = sequence.sequenceName;
         i++;
      }
      analysisSelector = new OptionDropdown("Analysis", names[0], names, this);
   }

   private void initializeFinalComponents() {
      newButton = new JButton("New");
      newButton.addActionListener(e -> {
         SequenceGeneratorFrame sequenceGeneratorFrame =
               new SequenceGeneratorFrame(-1, analysisManager);
         sequenceGeneratorFrame.setVisible(true);
      });

      editButton = new JButton("Edit");
      editButton.addActionListener(e -> {
         if (sequenceGeneratorFrame != null) {
            return;
         }
         sequenceGeneratorFrame = new SequenceGeneratorFrame(
               analysisSelector.getSelectedIndex(),
               analysisManager
         );
         sequenceGeneratorFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
               sequenceGeneratorFrame = null;
               super.windowClosing(e);
            }
         });
         sequenceGeneratorFrame.setVisible(true);
      });

      elapsedLabel = new JLabel("Processing time (ms):");
      elapsedTextfield = new JTextField("0");
      elapsedTextfield.setColumns(8);
      elapsedTextfield.setEditable(false);
      buttonPanel = new PreviewerLoadImageButtons(studio);
      buttonPanel.addListener(this);
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      switch (evt.getPropertyName()) {
         case "Analysis":
            analysisManager.setCurrentSequence(analysisSelector.getSelectedIndex());
            panel = new PreviewerPanel(studio, this, analysisManager,
                  analysisManager.getCurrentSequence());
            redraw();

            if (rawImage == null) {
               return;
            }
            analyze();
            addImageToWindow(imgs.get(panel.getDisplayIndex()));
            displayWindow.autostretch();
            break;
         case "Analysis step":
            if (rawImage == null) {
               return;
            }
            addImageToWindow(imgs.get(panel.getDisplayIndex()));
            displayWindow.autostretch();
            break;
         case "New image loaded":
            rawImage = buttonPanel.getImg();
            width = buttonPanel.getImgWidth();
            height = buttonPanel.getImgHeight();
            analyze();
            addImageToWindow(imgs.get(panel.getDisplayIndex()));
            displayWindow.autostretch();
            break;
         case "AnalysisManager":
            updateSequenceNames();
            redraw();
            break;
         case "Parameter changed":
            if (rawImage == null) {
               return;
            }
            analyze();
            addImageToWindow(imgs.get(panel.getDisplayIndex()));
            displayWindow.autostretch();
            break;
         default:
            break;
      }
   }

   public void analyze() {
      imgs = analysisManager.getImageStack(rawImage, width, height);
      if (isRunning.get()) {
         isRunning.set(false);
      }
      if (analysisThread != null) {
         analysisThread = new Thread(this);
      }
      analysisThread = new Thread(this);
      analysisThread.start();
   }

   private double timeSequence() {
      long count = 0;
      for (int i = 0; i < 10; i++) {
         if (!isRunning.get()) {
            return -1;
         }
         int[] src = rawImage.clone();
         Instant start = Instant.now();
         analysisManager.analyze(src, width, height);
         Instant end = Instant.now();
         count += Duration.between(start, end).toNanos();
      }
      return (double) (count) / 1E7;
   }

   public void run() {
      isRunning.set(true);
      elapsedTextfield.setText("Running...");
      double time = timeSequence();
      elapsedTextfield.setText(String.valueOf(time));
      isRunning.set(false);
   }

   private void addImageToWindow(int[] img) {
      if (displayWindow == null) {
         displayWindow = studio.displays().createDisplay(datastore);
      }

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

      this.add(analysisSelector, "split");
      this.add(newButton, "split");
      this.add(editButton, "wrap");
      this.add(panel, "wrap");
      this.add(elapsedLabel, "split");
      this.add(elapsedTextfield, "wrap");
      this.add(buttonPanel);
      this.validate();
      this.repaint();
      this.pack();
   }

}