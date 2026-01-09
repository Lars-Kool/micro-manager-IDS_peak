package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import javax.swing.JButton;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class ActionPanel extends JPanel {

   public ActionPanel(BuilderPanel parent) {
      this.setLayout(new MigLayout());

      JButton moveUpButton = new JButton("^");
      moveUpButton.addActionListener(e -> {
         parent.moveUp(this);
      });
   }
}
