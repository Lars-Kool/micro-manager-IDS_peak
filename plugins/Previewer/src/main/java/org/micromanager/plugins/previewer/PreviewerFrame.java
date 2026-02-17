package org.micromanager.plugins.previewer;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import org.micromanager.plugins.previewer.analysismanager.AnalysisStep;
import org.micromanager.plugins.previewer.analysismanager.ImageAnalysisEventHandler;
import org.micromanager.plugins.previewer.analysismanager.SequencePanel;

public class PreviewerFrame extends JFrame implements PropertyChangeListener, Runnable {
   private final Coords.Builder builder;
   private final RewritableDatastore datastore;
   private final AtomicBoolean isRunning;
   private final Studio studio;
   private final AnalysisManager analysisManager;
   private final ImageAnalysisEventHandler eventHandler;
   private SequencePanel sequencePanel;
   private PreviewerLoadImageButtons buttonPanel;
   private int[] rawImage;
   private ArrayList<int[]> imgs;
   private int width;
   private int height;
   private DisplayWindow displayWindow;
   private SequenceGeneratorFrame sequenceGeneratorFrame;
   private Thread analysisThread;
   private AnalysisSequence currentSequence;
   private JComboBox<String> sequenceSelector;
   private JButton newButton;
   private JButton editButton;
   private JComboBox<String> stepSelector;
   private JLabel elapsedLabel;
   private JTextField elapsedTextfield;

   public PreviewerFrame(Studio studio) {
      this.setTitle("Image analysis previewer.");
      this.setLayout(new MigLayout());
      this.studio = studio;
      this.eventHandler = ImageAnalysisEventHandler.getInstance();
      eventHandler.addListener(this);

      analysisManager = AnalysisManager.getInstance(studio);
      currentSequence = analysisManager.getSequence(0);
      sequencePanel = new SequencePanel(currentSequence);

      datastore = studio.data().createRewritableRAMDatastore();
      builder = studio.data().coordsBuilder();
      isRunning = new AtomicBoolean(false);

      initializeFinalComponents();
      createAnalysisSelector();
      createStepSelector();

      this.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            eventHandler.firePropertyChange("Closing", this, null, null);
            super.windowClosing(e);
         }
      });

      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(100, 100);
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);
      this.studio.events().registerForEvents(this);

      redraw();
      this.pack();
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
               sequenceSelector.getSelectedIndex(),
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
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      switch (evt.getPropertyName()) {
         case "Sequence changed":
            // No need to update if a non-active sequence is modified, an up-to-date version will
            // be generated upon selection of that sequence
            if (Objects.equals(evt.getSource(), currentSequence) && rawImage != null) {
               analyze();
               updateDisplayedImage();
            }
            break;
         // TODO: make sure image is reanalyzed when current sequence is deleted
         case "Sequence list changed":
            String currentSequenceName = currentSequence.sequenceName;
            createAnalysisSelector();
            int idx =
                  ((DefaultComboBoxModel<?>) sequenceSelector.getModel()).getIndexOf(
                        currentSequenceName);
            sequenceSelector.setSelectedIndex(idx);
            redraw();
            break;
         case "sequenceSelector changed":
            currentSequence = analysisManager.getSequence(sequenceSelector.getSelectedIndex());
            sequencePanel = new SequencePanel(currentSequence);
            createStepSelector();
            redraw();

            if (rawImage != null) {
               analyze();
               updateDisplayedImage();
            }
            break;
         case "stepSelector changed":
            if (rawImage != null) {
               updateDisplayedImage();
            }
            break;
         case "rawImage changed":
            rawImage = buttonPanel.getImg();
            width = buttonPanel.getImgWidth();
            height = buttonPanel.getImgHeight();
            analyze();
            updateDisplayedImage();
            break;
         default:
            break;
      }
   }

   private void createAnalysisSelector() {
      ArrayList<String> names = analysisManager.getSequenceNames();
      sequenceSelector = new JComboBox<>(names.toArray(new String[0]));
      sequenceSelector.addActionListener(e -> {
         eventHandler.firePropertyChange("sequenceSelector changed", this, null, null);
      });
   }

   private void createStepSelector() {
      ArrayList<String> stepNames = new ArrayList<>();
      stepNames.add("RAW");
      for (AnalysisStep step : currentSequence.steps) {
         stepNames.add(step.name);
      }
      stepSelector = new JComboBox<>(stepNames.toArray(new String[0]));
      stepSelector.addActionListener(e -> {
         eventHandler.firePropertyChange("stepSelector changed", this, null, null);
      });
   }

   private void analyze() {
      imgs = currentSequence.executeStack(rawImage, width, height);
      if (isRunning.get()) {
         isRunning.set(false);
      }
      if (analysisThread != null) {
         analysisThread = new Thread(this);
      }
      analysisThread = new Thread(this);
      analysisThread.start();
   }

   private double timeAnalysis() {
      long count = 0;
      for (int i = 0; i < 10; i++) {
         if (!isRunning.get()) {
            return -1;
         }
         int[] src = rawImage.clone();
         Instant start = Instant.now();
         currentSequence.execute(src, width, height);
         Instant end = Instant.now();
         count += Duration.between(start, end).toNanos();
      }
      return (double) (count) / 1E7;
   }

   public void run() {
      isRunning.set(true);
      elapsedTextfield.setText("Running...");
      double time = timeAnalysis();
      elapsedTextfield.setText(String.valueOf(time));
      isRunning.set(false);
   }

   private void updateDisplayedImage() {
      if (displayWindow == null) {
         displayWindow = studio.displays().createDisplay(datastore);
      }
      if (!displayWindow.isVisible()) {
         studio.getLogManager().logMessage("display invisible...");
      }

      int[] img = imgs.get(stepSelector.getSelectedIndex());

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
         return;
      }
      displayWindow.autostretch();
   }

   private void redraw() {
      this.getContentPane().removeAll();

      this.add(sequenceSelector, "split");
      this.add(newButton, "split");
      this.add(editButton, "wrap");
      this.add(new JLabel("Visualize step"), "split");
      this.add(stepSelector, "wrap");
      this.add(sequencePanel, "wrap");
      this.add(elapsedLabel, "split");
      this.add(elapsedTextfield, "wrap");
      this.add(buttonPanel);
      this.validate();
      this.repaint();
      this.pack();
   }

}