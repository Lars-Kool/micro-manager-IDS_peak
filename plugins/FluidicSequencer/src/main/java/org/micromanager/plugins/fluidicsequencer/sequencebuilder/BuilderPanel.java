package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import mmcorej.DeviceType;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;
import org.micromanager.plugins.fluidicsequencer.fluidicsequence.Sequence;
import org.micromanager.plugins.fluidicsequencer.fluidicsequence.SequencerEventHandler;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.panels.ActionPanel;

public class BuilderPanel extends JPanel implements PropertyChangeListener {
   private final Studio studio;
   private final SequencerEventHandler eventHandler;
   private final ArrayList<ActionPanel> actionPanels;
   private final Sequence sequence;
   protected boolean modifiedSinceLastSave;
   private String[] deviceNames;
   private ActionType[] deviceActions;

   public BuilderPanel(Studio studio, Sequence sequence) {
      this.setLayout(new MigLayout());
      this.studio = studio;
      this.sequence = sequence;
      this.modifiedSinceLastSave = false;
      this.setBorder(BorderFactory.createEmptyBorder());
      this.eventHandler = SequencerEventHandler.getInstance();

      updateDevices();
      actionPanels = new ArrayList<>();
      // for (SequenceAction action : sequence.actions) {
      for (int i = 0; i < sequence.actions.size(); i++) {
         actionPanels.add(new ActionPanel(i, sequence.actions.get(i), deviceNames, deviceActions));
      }
      redraw();
   }

   public boolean isModifiedSinceLastSave() {
      return modifiedSinceLastSave;
   }

   private void updateDevices() {
      ArrayList<String> devices = new ArrayList<>();
      ArrayList<ActionType> actions = new ArrayList<>();
      for (String dev : studio.core().getLoadedDevicesOfType(DeviceType.PressurePumpDevice)) {
         devices.add(dev);
         actions.add(ActionType.SET_PRESSURE);
      }
      for (String dev : studio.core().getLoadedDevicesOfType(DeviceType.VolumetricPumpDevice)) {
         devices.add(dev);
         actions.add(ActionType.SET_FLOWRATE);
      }
      for (String dev : studio.core().getLoadedDevicesOfType(DeviceType.StateDevice)) {
         devices.add(dev);
         actions.add(ActionType.SET_POSITION);
      }
      devices.add("Wait");
      actions.add(ActionType.WAIT);

      deviceNames = new String[devices.size()];
      deviceActions = new ActionType[devices.size()];
      for (int i = 0; i < devices.size(); i++) {
         deviceNames[i] = devices.get(i);
         deviceActions[i] = actions.get(i);
      }
   }

   protected void addItem(ActionPanel actionPanel) {
      int i = actionPanels.indexOf(actionPanel);
      actionPanels.add(i + 1, new ActionPanel(deviceNames, deviceActions));
      updateSequence();
      redraw();
   }

   protected void removeItem(ActionPanel actionPanel) {
      actionPanels.remove(actionPanel);
      if (actionPanels.isEmpty()) {
         actionPanels.add(new ActionPanel(deviceNames, deviceActions));
      }
      updateSequence();
      redraw();
   }

   protected void moveUp(ActionPanel actionPanel) {
      int i = actionPanels.indexOf(actionPanel);
      if (i <= 0) {
         return;
      } // Already at top

      actionPanels.remove(actionPanel);
      actionPanels.add(i - 1, actionPanel);
      updateSequence();
      redraw();
   }

   protected void moveDown(ActionPanel actionPanel) {
      int i = actionPanels.indexOf(actionPanel);
      if (i >= actionPanels.size() - 1) {
         return;
      } // Already at bottom

      actionPanels.remove(actionPanel);
      actionPanels.add(i + 1, actionPanel);
      updateSequence();
      redraw();
   }

   private void updateSequence() {
      ArrayList<SequenceAction> tempActions = new ArrayList<>();
      for (ActionPanel actionPanel : actionPanels) {
         tempActions.add(actionPanel.getAction());
      }
      sequence.actions = tempActions;
      modifiedSinceLastSave = true;
   }

   private void redraw() {
      this.removeAll();

      for (ActionPanel panel : actionPanels) {
         this.add(panel, "wrap");
      }

      this.validate();
      this.repaint();
      JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
      if (frame != null) {
         frame.pack();
      }
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      switch (evt.getPropertyName()) {
         case "Add action":
            addItem((ActionPanel) evt.getSource());
            modifiedSinceLastSave = true;
            break;
         case "Remove action":
            removeItem((ActionPanel) evt.getSource());
            modifiedSinceLastSave = true;
            break;
         case "Move up action":
            moveUp((ActionPanel) evt.getSource());
            modifiedSinceLastSave = true;
            break;
         case "Move down action":
            moveDown((ActionPanel) evt.getSource());
            modifiedSinceLastSave = true;
            break;
         case "Action value changed":
            modifiedSinceLastSave = true;
         default:
            break;
      }
   }
}
