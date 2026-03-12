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

   public ActionWaitPanel() {
      this(0, "s");
   }

   public ActionWaitPanel(double time, String unit) {
      this.setLayout(new MigLayout("insets 2"));

      waitLabel = new JLabel("Wait for: ");
      timeField = new JFormattedTextField();
      timeField.setFormatterFactory(new AbstractFormatterFactory());
      timeField.setValue(time);
      unitDropdown = new JComboBox<>(new String[] {"s", "m", "h"});
      unitDropdown.setSelectedItem(unit);
      redraw();
   }

   private void redraw() {
      this.removeAll();
      this.add(waitLabel);
      this.add(timeField);
      this.add(unitDropdown);
   }
}
