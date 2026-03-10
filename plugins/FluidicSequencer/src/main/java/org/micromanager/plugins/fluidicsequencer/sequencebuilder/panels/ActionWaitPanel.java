package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.plugins.dropletcontrol.AbstractFormatterFactory;

public class ActionWaitPanel extends JPanel {
   private final JLabel waitLabel;
   private final JFormattedTextField timeField;
   private final JComboBox<String> unitDropdown;
   private JumpPanel jumpPanel;

   public ActionWaitPanel(int nSteps) {
      this(0, nSteps, nSteps);
   }

   public ActionWaitPanel(double time, int nextStep, int nSteps) {
      this.setLayout(new MigLayout("insets 2"));

      waitLabel = new JLabel("Wait for: ");
      timeField = new JFormattedTextField();
      timeField.setFormatterFactory(new AbstractFormatterFactory());
      unitDropdown = new JComboBox<>(new String[] {"s", "m", "h"});
      jumpPanel = new JumpPanel("Next step: ", nextStep, nSteps);
      redraw();
   }

   private void redraw() {
      this.add(waitLabel);
      this.add(timeField);
      this.add(unitDropdown);
      this.add(jumpPanel);
   }
}
