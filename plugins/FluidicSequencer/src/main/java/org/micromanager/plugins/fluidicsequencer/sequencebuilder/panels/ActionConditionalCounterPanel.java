package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.micromanager.plugins.dropletcontrol.AbstractFormatterFactory;

public class ActionConditionalCounterPanel extends JPanel {
   private final JLabel counterLabel;
   private final JTextField counterTextField;
   private final JComboBox<String> comparatorDropdown;
   private final JFormattedTextField valueTextField;
   private JumpPanel jumpTruePanel;
   private JumpPanel jumpFalsePanel;

   public ActionConditionalCounterPanel(int nSteps) {
      this("", 0, nSteps, nSteps, nSteps);
   }

   public ActionConditionalCounterPanel(String counterName, int value, int nextTrue, int nextFalse,
                                        int nSteps) {
      // conditionLabel = new JLabel("Condition: ");
      // String[] conditions = new String[ConditionAction.values().length];
      // for (int i = 0; i < ConditionAction.values().length; i++) {
      //    conditions[i] = ConditionAction.values()[i].label;
      // }
      // conditionDropdown = new JComboBox<>(conditions);

      counterLabel = new JLabel("Counter name: ");
      counterTextField = new JTextField();
      counterTextField.setColumns(10);
      counterTextField.setText(counterName);

      comparatorDropdown = new JComboBox<>(new String[] {">", ">=", "==", "!=", "<", "<="});

      valueTextField = new JFormattedTextField();
      valueTextField.setFormatterFactory(new AbstractFormatterFactory());
      valueTextField.setValue(value);

      jumpTruePanel = new JumpPanel("Next if true: ", nextTrue, nSteps);
      jumpFalsePanel = new JumpPanel("Next if False: ", nextFalse, nSteps);
      redraw();
   }

   private void redraw() {
      this.add(counterLabel);
      this.add(counterTextField);
      this.add(comparatorDropdown);
      this.add(valueTextField);
      this.add(jumpTruePanel);
      this.add(jumpFalsePanel);
   }
}
