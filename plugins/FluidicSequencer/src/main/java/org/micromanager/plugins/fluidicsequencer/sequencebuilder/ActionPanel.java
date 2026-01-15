package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
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
   private final SequenceAction action;
   private final JButton addButton;
   private final JButton removeButton;
   private final JButton upButton;
   private final JButton downButton;
   private final JLabel deviceLabel;
   private final JComboBox<String> devicesDropdown;
   private final JLabel valueLabel;
   private final JFormattedTextField valueTextField;
   private final JComboBox<String> pressureUnitDropdown;
   private final JComboBox<String> flowrateUnitDropdown;
   private final JLabel durationLabel;
   private final JFormattedTextField durationTextField;
   private final JComboBox<String> durationUnitDropdown;

   public ActionPanel(BuilderPanel parent, String[] deviceNames, ActionType[] deviceActions) {
      this(parent, new SequenceAction("Wait", ActionType.WAIT), deviceNames, deviceActions);
   }

   public ActionPanel(BuilderPanel parent, SequenceAction action, String[] deviceNames,
                      ActionType[] deviceActions) {
      this.setLayout(new MigLayout("insets 2"));

      this.action = action;
      addButton = new JButton("+");
      addButton.addActionListener(e -> parent.addItem(this));
      removeButton = new JButton("-");
      removeButton.addActionListener(e -> parent.removeItem(this));
      upButton = new JButton("^");
      upButton.addActionListener(e -> parent.moveUp(this));
      downButton = new JButton("v");
      downButton.addActionListener(e -> parent.moveDown(this));

      deviceLabel = new JLabel("Device:");
      devicesDropdown = new JComboBox<>(deviceNames);
      // Set name before action listener to prevent activation
      devicesDropdown.setSelectedItem(action.deviceName);
      devicesDropdown.addActionListener(e -> {
         action.deviceName = (String) devicesDropdown.getSelectedItem();
         int idx = devicesDropdown.getSelectedIndex();
         action.type = deviceActions[idx];
         setActionLabelText();
         redraw();
         parent.modifiedSinceLastSave = true;
      });
      setActionLabelText();

      NumberFormat format = NumberFormat.getNumberInstance();
      format.setMinimumIntegerDigits(1);
      format.setMaximumFractionDigits(3);
      NumberFormatter formatter = new NumberFormatter(format);

      valueLabel = new JLabel();
      valueTextField = new JFormattedTextField(formatter);
      valueTextField.setColumns(8);
      valueTextField.setValue(action.value);
      valueTextField.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               action.value = (Double) valueTextField.getValue();
               parent.modifiedSinceLastSave = true;
            }
         }
      });

      pressureUnitDropdown = new JComboBox<>(pressurUnits);
      pressureUnitDropdown.setSelectedItem("kPa");
      pressureUnitDropdown.addActionListener(e -> {
         action.valueUnit = ValueUnit.valueOf((String) pressureUnitDropdown.getSelectedItem());
         parent.modifiedSinceLastSave = true;
      });

      flowrateUnitDropdown = new JComboBox<>(flowrateUnits);
      flowrateUnitDropdown.setSelectedItem("uL_s");
      flowrateUnitDropdown.addActionListener(e -> {
         action.valueUnit = ValueUnit.valueOf((String) flowrateUnitDropdown.getSelectedItem());
         parent.modifiedSinceLastSave = true;
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
               parent.modifiedSinceLastSave = true;
            }
         }
      });

      durationUnitDropdown = new JComboBox<>(durationUnits);
      durationUnitDropdown.setSelectedItem("s");
      durationUnitDropdown.addActionListener(e -> {
         action.durationUnit =
               DurationUnit.valueOf((String) durationUnitDropdown.getSelectedItem());
         parent.modifiedSinceLastSave = true;
      });
      redraw();
   }

   private void setActionLabelText() {
      switch (action.type) {
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

      if (action.type != ActionType.WAIT) {
         this.add(valueLabel);
         this.add(valueTextField);
         if (action.type == ActionType.SET_PRESSURE) {
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
