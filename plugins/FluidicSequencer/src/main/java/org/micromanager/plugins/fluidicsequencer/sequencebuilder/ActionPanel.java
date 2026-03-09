package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;
import net.miginfocom.swing.MigLayout;

public class ActionPanel extends JPanel {
   private static final String[] pressurUnits = new String[] {"Pa", "kPa", "hPa", "bar", "mbar",
         "atm", "psi"};
   private static final String[] flowrateUnits = new String[] {"uL_s", "uL_m", "uL_h", "mL_s",
         "mL_m", "mL_h"};
   private static final String[] durationUnits = new String[] {"s", "m", "h"};
   private final SequencerEventHandler eventHandler;

   private final SequenceAction action;
   private final JButton addButton;
   private final JButton removeButton;
   private final JButton upButton;
   private final JButton downButton;
   private final JLabel deviceLabel;
   private final JComboBox<String> devicesDropdown;
   private final JComboBox<String> startDropdown;
   private final JLabel valueLabel;
   private final JFormattedTextField valueTextField;
   private final JComboBox<String> pressureUnitDropdown;
   private final JComboBox<String> flowrateUnitDropdown;
   private final JLabel durationLabel;
   private final JFormattedTextField durationTextField;
   private final JComboBox<String> durationUnitDropdown;

   public ActionPanel(String[] deviceNames, ActionType[] deviceActions) {
      this(new SequenceAction("Wait", ActionType.WAIT), deviceNames, deviceActions);
   }

   public ActionPanel(SequenceAction action, String[] deviceNames, ActionType[] deviceActions) {
      this.setLayout(new MigLayout("insets 2"));
      this.eventHandler = SequencerEventHandler.getInstance();

      this.action = action;
      addButton = new JButton("+");
      addButton.addActionListener(e -> {
         eventHandler.firePropertyChange("Add action", this, null, null);
      });
      removeButton = new JButton("-");
      removeButton.addActionListener(e -> {
         eventHandler.firePropertyChange("Remove action", this, null, null);
      });
      upButton = new JButton("^");
      upButton.addActionListener(e -> {
         eventHandler.firePropertyChange("Move up action", this, null, null);
      });
      downButton = new JButton("v");
      downButton.addActionListener(e -> {
         eventHandler.firePropertyChange("Move down action", this, null, null);
      });

      ArrayList<String> startNames = new ArrayList<>();
      for (StartType type : StartType.values()) {
         startNames.add(type.label);
      }
      startDropdown = new JComboBox<>(startNames.toArray(new String[0]));

      deviceLabel = new JLabel("Device:");
      devicesDropdown = new JComboBox<>(deviceNames);
      // Set name before action listener to prevent activation
      devicesDropdown.setSelectedItem(action.deviceName);
      devicesDropdown.addActionListener(e -> {
         action.deviceName = (String) devicesDropdown.getSelectedItem();
         int idx = devicesDropdown.getSelectedIndex();
         action.actionType = deviceActions[idx];
         setActionLabelText();
         redraw();
      });

      NumberFormat format = NumberFormat.getNumberInstance();
      format.setMinimumIntegerDigits(1);
      format.setMaximumFractionDigits(3);
      NumberFormatter formatter = new NumberFormatter(format);

      valueLabel = new JLabel();
      setActionLabelText();
      valueTextField = new JFormattedTextField(formatter);
      valueTextField.setColumns(8);
      valueTextField.setValue(action.value);
      valueTextField.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               action.value = (Double) valueTextField.getValue();
               eventHandler.firePropertyChange("Action value changed", this, null, null);
            }
         }
      });

      pressureUnitDropdown = new JComboBox<>(pressurUnits);
      pressureUnitDropdown.setSelectedItem("kPa");
      pressureUnitDropdown.addActionListener(e -> {
         action.valueUnit = ValueUnit.valueOf((String) pressureUnitDropdown.getSelectedItem());
         eventHandler.firePropertyChange("Action value changed", this, null, null);
      });

      flowrateUnitDropdown = new JComboBox<>(flowrateUnits);
      flowrateUnitDropdown.setSelectedItem("uL_s");
      flowrateUnitDropdown.addActionListener(e -> {
         action.valueUnit = ValueUnit.valueOf((String) flowrateUnitDropdown.getSelectedItem());
         eventHandler.firePropertyChange("Action value changed", this, null, null);
      });

      durationLabel = new JLabel("Duration:");
      durationTextField = new JFormattedTextField(format);
      durationTextField.setColumns(8);
      durationTextField.setValue(action.duration);
      durationTextField.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               action.duration = (Double) durationTextField.getValue();
               eventHandler.firePropertyChange("Action value changed", this, null, null);
            }
         }
      });

      durationUnitDropdown = new JComboBox<>(durationUnits);
      durationUnitDropdown.setSelectedItem("s");
      durationUnitDropdown.addActionListener(e -> {
         action.durationUnit =
               DurationUnit.valueOf((String) durationUnitDropdown.getSelectedItem());
         eventHandler.firePropertyChange("Action value changed", this, null, null);
      });
      redraw();
   }

   private void setActionLabelText() {
      switch (action.actionType) {
         case SET_PRESSURE:
            valueLabel.setText("Set pressure (kPa):");
            break;
         case SET_FLOWRATE:
            valueLabel.setText("Set flowrate (uL/s):");
            break;
         case SET_POSITION:
            valueLabel.setText("Set position:");
            break;
         default:
            break;
      }
   }

   private void redraw() {
      this.removeAll();

      this.add(addButton);
      this.add(removeButton);
      this.add(upButton);
      this.add(downButton);

      this.add(deviceLabel);
      this.add(devicesDropdown);

      this.add(startDropdown);

      if (action.actionType != ActionType.WAIT) {
         this.add(valueLabel);
         this.add(valueTextField);
         if (action.actionType == ActionType.SET_PRESSURE) {
            this.add(pressureUnitDropdown);
         } else {
            this.add(flowrateUnitDropdown);
         }
      }

      this.add(durationLabel);
      this.add(durationTextField);
      this.add(durationUnitDropdown);

      this.validate();
      this.repaint();
      JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
      if (frame != null) {
         frame.pack();
      }
   }

   public SequenceAction getAction() {
      return action;
   }
}
