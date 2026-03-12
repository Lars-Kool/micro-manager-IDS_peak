package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.micromanager.plugins.previewer.analysismanager.AnalysisManager;

public class ActionConditionalImagePanel extends JPanel {
   private final AnalysisManager analysisManager;
   private final JLabel analysisLabel;
   private final JComboBox<String> analysisDropdown;
   private final JLabel quantificationLabel;
   private final JComboBox<String> quantificationDropdown;
   private final JComboBox<String> comparatorDropdown;
   private final JFormattedTextField valueTextField;
   private JumpPanel jumpTruePanel;
   private JumpPanel jumpFalsePanel;

   public ActionConditionalImagePanel(int nSteps) {
      this("", "", 0, nSteps, nSteps, nSteps);
   }

   public ActionConditionalImagePanel(String seq, String quant, int value, int nextTrue,
                                      int nextFalse, int nSteps) {
      // analysisManager = AnalysisManager.getInstance(studio);
      analysisManager = null;

      analysisLabel = new JLabel("Analysis algorithm: ");
      analysisDropdown = new JComboBox<>(analysisManager.getSequenceNames().toArray(new String[0]));
      quantificationLabel = new JLabel("Quantification algorithm: ");
      // quantificationDropdown =
      //    new JComboBox<>(analysisManager.getQuantificationAlgorithms().toArray(new String[0]));
      quantificationDropdown = new JComboBox<>(new String[] { "Avg Size", "Count regions" });

      comparatorDropdown = new JComboBox<>(new String[] {">", ">=", "==", "!=", "<", "<="});

      valueTextField = new JFormattedTextField();
      valueTextField.setFormatterFactory(new org.micromanager.plugins.dropletcontrol.AbstractFormatterFactory());
      valueTextField.setValue(value);

      jumpTruePanel = new JumpPanel("Next if true: ", nextTrue, nSteps);
      jumpFalsePanel = new JumpPanel("Next if False: ", nextFalse, nSteps);
      redraw();
   }

   private void redraw() {
      this.add(analysisLabel);
      this.add(analysisDropdown);
      this.add(quantificationLabel);
      this.add(quantificationDropdown);
      this.add(comparatorDropdown);
      this.add(valueTextField);
      this.add(jumpTruePanel);
      this.add(jumpFalsePanel);
   }
}
