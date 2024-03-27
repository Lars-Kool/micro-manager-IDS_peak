/**
 * MistFrame.java
 *
 * <p>This module shows an example of creating a GUI (Graphical User Interface).
 * There are many ways to do this in Java; this particular example uses the
 * MigLayout layout manager, which has extensive documentation online.
 *
 * <p>Nico Stuurman, copyright UCSF, 2012, 2015
 *
 * <p>LICENSE: This file is distributed under the BSD license. License text is
 * included with the source distribution.
 *
 * <p>This file is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.
 *
 * <p>IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
 */

package org.micromanager.plugins.mist;

import com.google.common.eventbus.Subscribe;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatter;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;
import org.micromanager.assembledata.AssembleDataForm;
import org.micromanager.assembledata.DragDropListener;
import org.micromanager.data.Coords;
import org.micromanager.data.DataProvider;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.display.DataViewer;
import org.micromanager.display.internal.event.DataViewerAddedEvent;
import org.micromanager.display.internal.event.DataViewerWillCloseEvent;
import org.micromanager.events.ExposureChangedEvent;
import org.micromanager.internal.utils.FileDialogs;
import org.micromanager.internal.utils.WindowPositioning;
import org.micromanager.propertymap.MutablePropertyMapView;

// Imports for MMStudio internal packages
// Plugins should not access internal packages, to ensure modularity and
// maintainability. However, this plugin code is older than the current
// MMStudio API, so it still uses internal classes and interfaces. New code
// should not imitate this practice.


public class MistFrame extends JFrame {

   private Studio studio_;
   private final Font arialSmallFont_;
   private final MutablePropertyMapView profileSettings_;
   private static final String CHOOSEDIR = "ChooseDir";
   private static final String DIRNAME = "DirName";
   private final Dimension buttonSize_;
   private final JComboBox<String> dataSetBox_;
   private static final String DATAVIEWER = "DataViewer";
   private JTextField userText_;


   public MistFrame(Studio studio) {
      super("Mist Plugin");
      studio_ = studio;
      arialSmallFont_ = new Font("Arial", Font.PLAIN, 12);
      buttonSize_ = new Dimension(70, 21);
      profileSettings_ =
              studio_.profile().getSettings(AssembleDataForm.class);

      super.setLayout(new MigLayout("fill, insets 2, gap 2, flowx"));

      JLabel title = new JLabel("Stitch data based on Mist plugin locations");
      title.setFont(new Font("Arial", Font.BOLD, 14));
      super.add(title, "span, alignx center, wrap");

      final JLabel mistFileLabel = new JLabel("Mist file: ");
      super.add(mistFileLabel, "span3, split");

      final JTextField locationsField = new JTextField(35);
      locationsField.setFont(arialSmallFont_);
      locationsField.setText(profileSettings_.getString(DIRNAME,
              profileSettings_.getString(DIRNAME,
                      System.getProperty("user.home") + "/img-global-positions-0.txt")));
      locationsField.setHorizontalAlignment(JTextField.LEFT);
      super.add(locationsField);

      DragDropListener dragDropListener = new DragDropListener(locationsField);
      new DropTarget(this, dragDropListener);
      new DropTarget(locationsField, dragDropListener);

      final JButton locationsFieldButton =  makeButton(buttonSize_, arialSmallFont_);
      locationsFieldButton.setText("...");
      locationsFieldButton.addActionListener((ActionEvent evt) -> {
         File f = FileDialogs.openFile(this, "Mist Global Positions File",
                 new FileDialogs.FileType(
                 "Mist Global Positions File",
                 "Mist Global Positions File",
                 locationsField.getText(),
                 false,
                 "txt"));
         if (f != null) {
            locationsField.setText(f.getAbsolutePath());
            profileSettings_.putString(DIRNAME, f.getAbsolutePath());
         }
      });
      super.add(locationsFieldButton, "wrap");

      dataSetBox_ = new JComboBox<>();
      setupDataViewerBox(dataSetBox_, DATAVIEWER);
      super.add(new JLabel("Data Set:"));
      super.add(dataSetBox_, "wrap");



      JButton helpButton = new JButton("Help");
      helpButton.addActionListener((ActionEvent e) -> {
         new Thread(org.micromanager.internal.utils.GUIUtils.makeURLRunnable(
                 "https://micro-manager.org/wiki/MistData")).start();
      });
      super.add(helpButton, "span 2, split 2");

      final JButton assembleButton =  new JButton("Assemble");
      assembleButton.addActionListener((ActionEvent e) -> {
         Runnable runnable =
                 () -> assembleData(locationsField.getText(),
                         (String) dataSetBox_.getSelectedItem());
         new Thread(runnable).start();
      });
      super.add(assembleButton, "wrap");

      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(100, 100);
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);

      super.pack();

      // Registering this class for events means that its event handlers
      // (that is, methods with the @Subscribe annotation) will be invoked when
      // an event occurs. You need to call the right registerForEvents() method
      // to get events; this one is for the application-wide event bus, but
      // there's also Datastore.registerForEvents() for events specific to one
      // Datastore, and DisplayWindow.registerForEvents() for events specific
      // to one image display window.
      studio_.events().registerForEvents(this);
      studio_.displays().registerForEvents(this);
   }

   private final JButton makeButton(Dimension buttonSize, Font font) {
      JButton button = new JButton();
      button.setPreferredSize(buttonSize);
      button.setMinimumSize(buttonSize);
      button.setFont(font);
      button.setMargin(new Insets(0, 0, 0, 0));

      return button;
   }

   private void setupDataViewerBox(final JComboBox<String> box, final String key) {
      List<DataViewer> allDataViewers = studio_.displays().getAllDataViewers();
      String[] dataViewers = new String[allDataViewers.size()];
      for (int i = 0; i < allDataViewers.size(); i++) {
         dataViewers[i] = allDataViewers.get(i).getName();
      }
      for (ActionListener al : box.getActionListeners()) {
         box.removeActionListener(al);
      }
      box.setModel(new DefaultComboBoxModel<>(
              dataViewers));
      String dataViewer = profileSettings_.getString(key, "");
      box.setSelectedItem(dataViewer);
      profileSettings_.putString(key, (String) box.getSelectedItem());
      box.addActionListener((java.awt.event.ActionEvent evt) -> {
         profileSettings_.putString(key, (String)
                 box.getSelectedItem());
      });
      super.pack();
   }

   @Subscribe
   public void onDataViewerAddedEvent(DataViewerAddedEvent event) {
      setupDataViewerBox(dataSetBox_, DATAVIEWER);
   }

   @Subscribe
   public void onDataViewerClosing(DataViewerWillCloseEvent event) {
      setupDataViewerBox(dataSetBox_, DATAVIEWER);
   }


   private void addAxisSliders(DataProvider dp, DataViewer dw) {
      List<String> axes = dp.getAxes();
      // Note: MM uses 0-based indices in the code, but 1-based indices
      // for the UI.  To avoid confusion, this storage of the desired
      // limits for each axis is 0-based, and translation to 1-based is made
      // in the UI code
      final Map<String, Integer> mins = new HashMap<>();
      final Map<String, Integer> maxes = new HashMap<>();
      final List<JCheckBox> channelCheckBoxes = new ArrayList<>();
      boolean usesChannels = false;
      for (final String axis : axes) {
         if (axis.equals(Coords.CHANNEL)) {
            usesChannels = true;
            break;
         }
      }
      int nrNoChannelAxes = axes.size();
      ;
      if (usesChannels) {
         nrNoChannelAxes = nrNoChannelAxes - 1;
         List<String> channelNameList = dp.getSummaryMetadata().getChannelNameList();
         if (channelNameList.size() > 0) {
            super.add(new JLabel(Coords.C));
            ;
         }
         for (int i = 0; i < channelNameList.size(); i++) {
            String channelName = channelNameList.get(i);
            JCheckBox checkBox = new JCheckBox(channelName);
           // if (!settings.getStringList(UNSELECTED_CHANNELS, "").contains(channelName)) {
           //    checkBox.setSelected(true);
           // }
            channelCheckBoxes.add(checkBox);
            if (i == 0) {
               if (channelNameList.size() > 1) {
                  super.add(checkBox, "span 3, split " + channelNameList.size());
               } else {
                  super.add(checkBox, "wrap");
               }
            } else if (i == channelNameList.size() - 1) {
               super.add(checkBox, "wrap");
            } else {
               super.add(checkBox);
            }
         }
      }


      if (nrNoChannelAxes > 0) {
         super.add(new JLabel(" "));
         super.add(new JLabel("min"));
         super.add(new JLabel("max"), "wrap");

         for (final String axis : axes) {
            if (axis.equals(Coords.CHANNEL)) {
               continue;
            }
            if (dp.getNextIndex(axis) > 1) {
               mins.put(axis, 1);
               maxes.put(axis, dp.getNextIndex(axis));

               super.add(new JLabel(axis));
               SpinnerNumberModel model = new SpinnerNumberModel(1, 1,
                       (int) dp.getNextIndex(axis), 1);
               mins.put(axis, 0);
               final JSpinner minSpinner = new JSpinner(model);
               JFormattedTextField field =
                       (JFormattedTextField) minSpinner.getEditor().getComponent(0);
               DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
               formatter.setCommitsOnValidEdit(true);
               minSpinner.addChangeListener((ChangeEvent ce) -> {
                  // check to stay below max, this could be annoying at times
                  if ((Integer) minSpinner.getValue() > maxes.get(axis) + 1) {
                     minSpinner.setValue(maxes.get(axis) + 1);
                  }
                  mins.put(axis, (Integer) minSpinner.getValue() - 1);
                  try {
                     Coords coord = dw.getDisplayedImages().get(0).getCoords();
                     coord = coord.copyBuilder().index(axis, mins.get(axis)).build();
                     dw.setDisplayPosition(coord);
                  } catch (IOException ioe) {
                     studio_.logs().logError(ioe, "IOException in DuplicatorPlugin");
                  }
               });
               super.add(minSpinner, "wmin 60");

               model = new SpinnerNumberModel((int) dp.getNextIndex(axis),
                       1, (int) dp.getNextIndex(axis), 1);
               maxes.put(axis, dp.getNextIndex(axis) - 1);
               final JSpinner maxSpinner = new JSpinner(model);
               field = (JFormattedTextField) maxSpinner.getEditor().getComponent(0);
               formatter = (DefaultFormatter) field.getFormatter();
               formatter.setCommitsOnValidEdit(true);
               maxSpinner.addChangeListener((ChangeEvent ce) -> {
                  // check to stay above min
                  if ((Integer) maxSpinner.getValue() < mins.get(axis) + 1) {
                     maxSpinner.setValue(mins.get(axis) + 1);
                  }
                  maxes.put(axis, (Integer) maxSpinner.getValue() - 1);
                  try {
                     Coords coord = dw.getDisplayedImages().get(0).getCoords();
                     coord = coord.copyBuilder().index(axis, maxes.get(axis)).build();
                     dw.setDisplayPosition(coord);
                  } catch (IOException ioe) {
                     studio_.logs().logError(ioe, "IOException in DuplcatorPlugin");
                  }
               });
               super.add(maxSpinner, "wmin 60, wrap");
            }
         }
      }

      super.add(new JLabel("name"));
    //  final JTextField nameField = new JTextField(shortName);
     // super.add(nameField, "span2, grow, wrap");

      final JCheckBox saveBox = new JCheckBox("Save");
      final JLabel fileField = new JLabel("");
      final JButton chooserButton = new JButton("...");
      final JLabel saveMethod = new JLabel("Memory");
      saveBox.addActionListener(e -> {
         fileField.setEnabled(saveBox.isSelected());
         chooserButton.setEnabled(saveBox.isSelected());
         if (saveBox.isSelected()) {
     //       chooseDataLocation(ourFrame, fileField, saveMethod);
         } else {
            saveMethod.setText("Memory");
         }
      });
      saveBox.setSelected(false);
      fileField.setEnabled(saveBox.isSelected());
      chooserButton.setEnabled(saveBox.isSelected());
      chooserButton.addActionListener(e -> {
      //   chooseDataLocation(ourFrame, fileField, saveMethod);
      });

      super.add(saveBox);
      super.add(fileField, "wmin 420, span 2, grow");
   }

   private void assembleData(String locationsFile, String dataViewerName) {
      List<MistGlobalData> mistEntries = new ArrayList<>();
      DataViewer dataViewer = null;

      File mistFile = new File(locationsFile);
      if (!mistFile.exists()) {
         studio_.logs().showError("Mist global positions file not found: " + mistFile.getAbsolutePath());
         return;
      }
      try {
         // parse global position file into MistGlobalData objects
         BufferedReader br
                 = new BufferedReader(new FileReader(mistFile));
         String line;
         while ((line = br.readLine()) != null) {
            if (!line.startsWith("file: ")) {
               continue;
            }
            int fileNameEnd = line.indexOf(';');
            String fileName = line.substring(6, fileNameEnd);
            int siteNr = Integer.parseInt(fileName.substring(fileName.lastIndexOf('_') + 1,
                    fileName.length() - 8));
            int index = fileName.indexOf("MMStack_");
            int end = fileName.substring(index).indexOf("-") + index;
            String well = fileName.substring(index + 8, end);
            // x, y
            int posStart = line.indexOf("position: ") + 11;
            String lineEnd = line.substring(posStart);
            String xy = lineEnd.substring(0, lineEnd.indexOf(')'));
            String[] xySplit = xy.split(",");
            int positionX = Integer.parseInt(xySplit[0]);
            int positionY = Integer.parseInt(xySplit[1].trim());
            // row, column
            int gridStart = line.indexOf("grid: ") + 7;
            lineEnd = line.substring(gridStart);
            String rowCol = lineEnd.substring(0, lineEnd.indexOf(')'));
            String[] rowColSplit = rowCol.split(",");
            int row = Integer.parseInt(rowColSplit[0]);
            int col = Integer.parseInt(rowColSplit[1].trim());
            mistEntries.add(new MistGlobalData(
                    fileName, siteNr, well, positionX, positionY, row, col));
         }
      } catch (IOException e) {
         studio_.logs().showError("Error reading Mist global positions file: " + e.getMessage());
         return;
      } catch (NumberFormatException e) {
         studio_.logs().showError("Error parsing Mist global positions file: " + e.getMessage());
         return;
      }

      for (DataViewer dv : studio_.displays().getAllDataViewers()) {
         if (dv.getName().equals(dataViewerName)) {
            dataViewer = dv;
         }
      }
      if (dataViewer == null) {
         studio_.logs().showError("No Micro-Manager data set selected");
         return;
      }
      // calculate new image dimensions
      DataProvider dp = dataViewer.getDataProvider();
      int imWidth = dp.getSummaryMetadata().getImageWidth();
      int imHeight = dp.getSummaryMetadata().getImageHeight();
      int maxX = 0;
      int maxY = 0;
      for (MistGlobalData entry : mistEntries) {
         if (entry.getPositionX() > maxX) {
            maxX = entry.getPositionX();
         }
         if (entry.getPositionY() > maxY) {
            maxY = entry.getPositionY();
         }
      }

      int newWidth = maxX + imWidth;
      int newHeight = maxY + imHeight;

      try {
         // create datastore to hold the result
         Datastore newStore = studio_.data().createRAMDatastore();
         int newNrP = dp.getSummaryMetadata().getIntendedDimensions().getP() / mistEntries.size();
         Coords dims = dp.getSummaryMetadata().getIntendedDimensions();
         Coords.Builder cb = dims.copyBuilder().p(newNrP);
         newStore.setSummaryMetadata(dp.getSummaryMetadata().copyBuilder().imageHeight(newHeight)
                 .imageWidth(newWidth).intendedDimensions(cb.build())
                 .build());
         DataViewer dv = studio_.displays().createDisplay(newStore);
         Coords.Builder imgCb = studio_.data().coordsBuilder();
         for (int c = 0; c < 1; c++) {
            for (int t = 0; t < 1; t++) {
               for (int z = 0; z < 1; z++) {
                  for (int newP = 0; newP < 1; newP++) {
                     ImagePlus newImgPlus = IJ.createImage(
                             "Stitched image-" + newP, "16-bit black", newWidth, newHeight, 2);
                     for (int p = 0; p < newNrP; p++) {
                        Image img = dp.getImage(imgCb.c(c).t(t).z(z).p(newP * mistEntries.size() + p)
                                .build());
                        if (img != null) {
                           String posName = img.getMetadata().getPositionName("");
                           int siteNr = Integer.parseInt(posName.substring(posName.lastIndexOf('_')
                                   + 1));
                           for (MistGlobalData entry : mistEntries) {
                              if (entry.getSiteNr() == siteNr) {
                                 int x = entry.getPositionX();
                                 int y = entry.getPositionY();
                                 ImageProcessor ip = studio_.data().getImageJConverter().createProcessor(img);
                                 newImgPlus.getProcessor().insert(ip, x, y);
                              }
                           }
                        }
                     }
                     Image newImg = studio_.data().ij().createImage(newImgPlus.getProcessor(),
                             imgCb.c(c).t(t).z(z).p(newP).build(),
                             dp.getImage(imgCb.c(c).t(t).z(z).p(newP * mistEntries.size())
                                     .build()).getMetadata().copyBuilderWithNewUUID().build());
                     newStore.putImage(newImg);
                  }
               }
            }
         }
      } catch (IOException e) {
         studio_.logs().showError("Error creating new data store: " + e.getMessage());
         return;
      }
   }
}
