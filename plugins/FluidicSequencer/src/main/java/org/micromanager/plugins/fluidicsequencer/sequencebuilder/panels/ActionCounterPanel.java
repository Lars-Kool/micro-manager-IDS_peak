package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ActionCounterPanel extends JPanel {
   private final JLabel counterLabel;
   private final JTextField counterField;
   private final JComboBox<String> actionDropdown;
   private final JFormattedTextField valueField;
   private JumpPanel jumpPanel;

   public ActionCounterPanel(int nSteps) {
      this("CounterName", CounterAction.SET_VALUE, 0, nSteps, nSteps);
   }

   public ActionCounterPanel(String counterName, CounterAction action, int value,
                             int nextStep, int nSteps) {
      counterLabel = new JLabel("Counter: ");
      counterField = new JTextField(counterName);
      counterField.setColumns(10);

      actionDropdown = new JComboBox<>(new String[] { CounterAction.SET_VALUE.label,
            CounterAction.INCREMENT.label, CounterAction.DECREMENT.label});
      actionDropdown.setSelectedItem(action.label);

      valueField = new JFormattedTextField();
      valueField.setValue(value);

      jumpPanel = new JumpPanel("Next step: ", nextStep, nSteps);
      redraw();
   }

   private void redraw() {
      this.add(counterLabel);
      this.add(counterField);
      this.add(actionDropdown);
      this.add(valueField);
      this.add(jumpPanel);
   }
}
