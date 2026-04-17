package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.plugins.dropletcontrol.AbstractFormatterFactory;

public class ActionDevicePanel extends JPanel {
   private final JComboBox<String> devicesDropdown;
   private String currentDeviceName;
   private String currentDeviceType;
   private String nextStepTrue;
   private String nextStepFalse;

   private final String[] deviceNames;
   private final String[] deviceTypes;
   private JLabel valueLabel;
   private final JFormattedTextField valueField;
   private final JComboBox<String> flowrateDropdown;
   private final JComboBox<String> pressureDropdown;
   private JumpPanel jumpPanel;

   public ActionDevicePanel(String[] deviceNames, String[] deviceTypes, int nSteps) {
      this(deviceNames[0], deviceNames, deviceTypes, 0, nSteps,
            nSteps);
   }

   public ActionDevicePanel(String device, String[] deviceNames,
                            String[] deviceTypes, double value,
                            int nextStep, int nSteps) {
      this.setLayout(new MigLayout("insets 2"));
      this.deviceNames = deviceNames;
      this.deviceTypes = deviceTypes;

      flowrateDropdown = new JComboBox<>(new String[]{ "uL/s", "nL/m", "uL/h", "mL/s", "mL/m",
            "mL/h"});
      pressureDropdown = new JComboBox<>(new String[] {"Pa", "kPa", "hPa", "bar", "mbar", "atm",
            "psi"});

      devicesDropdown = new JComboBox<>(deviceNames);
      devicesDropdown.setSelectedItem(device);
      devicesDropdown.addActionListener(e -> {
         updateDevice();
      });

      valueField = new JFormattedTextField();
      valueField.setValue(value);
      valueField.setFormatterFactory(new AbstractFormatterFactory());

      jumpPanel = new JumpPanel("Next: ", nextStep, nSteps);
   }

   private void updateDevice() {
      int idx = devicesDropdown.getSelectedIndex();
      currentDeviceName = deviceNames[idx];
      currentDeviceType = deviceTypes[idx];
      if (Objects.equals(currentDeviceType, "PressurePump")) {
         valueLabel = new JLabel("Pressure: ");
      }
      else {
         valueLabel = new JLabel("Flow rate: ");
      }
      redraw();
   }

   private void redraw() {
      this.add(devicesDropdown);
      this.add(valueLabel);
      this.add(valueField);
      if (Objects.equals(currentDeviceType, "PressurePump")) {
         this.add(pressureDropdown);
      }
      else {
         this.add(flowrateDropdown);
      }
      this.add(jumpPanel);
   }
}
