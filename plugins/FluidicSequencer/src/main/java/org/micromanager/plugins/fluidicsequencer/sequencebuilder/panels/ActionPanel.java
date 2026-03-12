package org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;
import net.miginfocom.swing.MigLayout;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.ActionType;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.DurationUnit;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.SequenceAction;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.SequencerEventHandler;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.StartType;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.ValueUnit;

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
   private final JLabel indexLabel;
   private final JComboBox<String> actionDropdown;
   private JPanel actionPanel;

   public ActionPanel(int i, SequenceAction action, String[] deviceNames,
                      ActionType[] deviceActions, int nSteps) {
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

      indexLabel = new JLabel(String.valueOf(i));
      actionDropdown = new JComboBox<>(new String[] { "Wait", "Set pump", "Update counter",
            "Conditional jump"});
      actionDropdown.setSelectedItem(action.actionType);
      actionDropdown.addActionListener(e -> {
         setActionPanel();
      });
      setActionPanel();
      redraw();
   }

   private void setActionPanel() {
      String temp = (String) actionDropdown.getSelectedItem();
      switch (Objects.requireNonNull(temp)) {
         case "Wait":
            actionPanel = new ActionWaitPanel(action.value, action.unit);
            break;
         default:
            break;
      }
   }


   public void setIndexLabel(int i) {
      indexLabel.setText(String.valueOf(i));
      // redraw(); // Not sure this is needed
   }

   private void redraw() {
      this.removeAll();

      this.add(addButton);
      this.add(removeButton);
      this.add(upButton);
      this.add(downButton);
      this.add(indexLabel);

      this.add(actionDropdown);
      this.add(actionPanel);

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
