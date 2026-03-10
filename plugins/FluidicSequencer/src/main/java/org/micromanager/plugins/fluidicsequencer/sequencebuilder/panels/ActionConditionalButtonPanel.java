package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

public class ActionConditionalButtonPanel extends JPanel {
   private JumpPanel jumpTruePanel;
   private JumpPanel jumpFalsePanel;

   public ActionConditionalButtonPanel(int nSteps) {
      this(nSteps, nSteps, nSteps);
   }

   public ActionConditionalButtonPanel(int nextTrue, int nextFalse,
                                        int nSteps) {
      this.setLayout(new MigLayout("insets 2"));
      jumpTruePanel = new JumpPanel("Next if true: ", nextTrue, nSteps);
      jumpFalsePanel = new JumpPanel("Next if False: ", nextFalse, nSteps);
      redraw();
   }

   private void redraw() {
      this.add(jumpTruePanel);
      this.add(jumpFalsePanel);
   }
}
