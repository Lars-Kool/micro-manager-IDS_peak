package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class JumpPanel extends JPanel {
   private JComboBox<String> nextDropdown;

   private int nextStep;

   public JumpPanel(String label, int nextStep, int nSteps) {
      this.setLayout(new MigLayout("insets 2"));
      this.nextStep = nextStep;
      this.add(new JLabel(label));

      ArrayList<String> nextValues = new ArrayList<>();
      for (int i = 0; i < nSteps; i++) {
         nextValues.add(String.valueOf(i));
      }
      nextValues.add("End");
      nextDropdown = new JComboBox<>(nextValues.toArray(new String[0]));
      if (nextStep >= nSteps) {
         this.nextStep = nSteps;
      }
      nextDropdown.setSelectedItem(nextStep);
      nextDropdown.addActionListener(e -> {
         this.nextStep = nextDropdown.getSelectedIndex();
      });
      this.add(nextDropdown);
   }
   public String getNextStep() {
      return nextDropdown.getItemAt(nextStep);
   }
}
