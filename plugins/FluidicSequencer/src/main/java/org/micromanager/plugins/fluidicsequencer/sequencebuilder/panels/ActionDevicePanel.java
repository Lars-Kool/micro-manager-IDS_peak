package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class ActionDevicePanel extends JPanel {
   private final JLabel idxLabel;
   private final JComboBox<String> devicesDropdown;
   private String currentDeviceName;
   private String currentDeviceType;

   private final String[] deviceNames;
   private final String[] deviceTypes;
   private JLabel valueLabel;
   private final JFormattedTextField
   private final JComboBox<String> flowrateDropdown;
   private final JComboBox<String> pressureDropdown;

   public ActionDevicePanel(int actionIdx, String[] deviceNames, String[] deviceTypes) {
      this(actionIdx, deviceNames[0], deviceNames, deviceTypes, 0, actionIdx + 1);
   }

   public ActionDevicePanel(int actionIdx, String device, String[] deviceNames,
                            String[] deviceTypes, double value,
                            int nextStep) {
      this.setLayout(new MigLayout("insets 2"));
      this.deviceNames = deviceNames;
      this.deviceTypes = deviceTypes;

      idxLabel = new JLabel(actionIdx + ": ");
      devicesDropdown = new JComboBox<>(deviceNames);
      devicesDropdown.setSelectedItem(device);
      devicesDropdown.addActionListener(e -> {
         updateDevice();
      });
   }

   private void updateDevice() {
      int idx = devicesDropdown.getSelectedIndex();
      currentDeviceName = deviceNames[idx];
      currentDeviceType = deviceTypes[idx];
      if (Objects.equals(currentDeviceType, "PressurePump")) {
         valueLabel = new JLabel("Pressure: ");

      }

      redraw();
   }

   private void redraw() {
      this.add(idxLabel);
      this.add(devicesDropdown);
      this.add(valueLabel);

   }
}
