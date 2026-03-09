package org.micromanager.plugins.fluidicsequencer;

import java.awt.Toolkit;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;
import org.micromanager.internal.utils.WindowPositioning;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.BuilderPanel;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.Sequence;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.SequenceManager;

public class SequencerFrame extends JFrame {
   private final Studio studio;
   private final SequenceManager sequenceManager;
   BuilderPanel builderPanel;
   JComboBox<String> sequenceCombobox;
   JButton newSequenceButton;
   JButton saveSequenceButton;
   JButton deleteSequenceButton;
   JTextField sequenceNameTextField;
   private Sequence currSequence;

   public SequencerFrame(Studio studio) {
      this.setTitle("Fluidic Sequencer");
      this.setLayout(new MigLayout());
      this.studio = studio;

      sequenceManager = new SequenceManager(studio);
      sequenceCombobox = new JComboBox<>(sequenceManager.getSequenceNames());
      sequenceCombobox.addActionListener(e -> {
         currSequence = sequenceManager.getSequence((String) sequenceCombobox.getSelectedItem());
      });
      currSequence = sequenceManager.getSequence((String) sequenceCombobox.getSelectedItem());


      newSequenceButton = new JButton("New");
      newSequenceButton.addActionListener(e -> {
         if (!shouldChangeSequence()) {
            return;
         }
         currSequence = new Sequence("");
         builderPanel = new BuilderPanel(studio, currSequence);
         sequenceNameTextField.setText("");
         redraw();
      });

      saveSequenceButton = new JButton("Save");
      saveSequenceButton.addActionListener(e -> {
         // Invalid name
         String name = sequenceNameTextField.getText();
         if (name.isEmpty()) {
            studio.alerts().postAlert("Invalid sequence name", null, "Please provide a name for "
                  + "the sequence.");
            return;
         }

         // Check if name already exists
         if (sequenceManager.sequenceNameExists(name)) {
            int input = JOptionPane.showConfirmDialog(null, "Do you want to overwrite the "
                  + "existing sequence?");
            if (input != 0) {
               return;
            }
         }

         // Remove old sequence if renamed
         if (!name.equals(currSequence.sequenceName)) {
            sequenceManager.removeSequence(currSequence.sequenceName);
            currSequence.sequenceName = name;
         }

         // Save new sequence
         sequenceManager.addSequence(currSequence, true);
      });


      deleteSequenceButton = new JButton("Delete");
      deleteSequenceButton.addActionListener(e -> {
         int input = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this "
               + "sequence?");
         if (input != 0) {
            return;
         }
         sequenceManager.removeSequence(currSequence.sequenceName);

         String[] sequenceNames = sequenceManager.getSequenceNames();
         if (sequenceNames.length == 0) {
            currSequence = new Sequence("");
            sequenceNameTextField.setText("");
         } else {
            currSequence = sequenceManager.getSequence(sequenceManager.getSequenceNames()[0]);
         }
         builderPanel = new BuilderPanel(studio, currSequence);
         redraw();
      });

      sequenceNameTextField = new JTextField(currSequence.sequenceName);
      sequenceNameTextField.setColumns(15);

      builderPanel = new BuilderPanel(studio, currSequence);

      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(100, 100);
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);

      this.redraw();
   }

   private boolean shouldChangeSequence() {
      if (builderPanel.isModifiedSinceLastSave()) {
         return true;
      }
      int input = JOptionPane.showConfirmDialog(null, "Modification to current sequence "
            + "are not yet saved. Any modifications will be lost.\nDo you want to save "
            + "before continuing?");
      if (input == 2) {
         return false;
      }
      if (input == 0) {
         sequenceManager.addSequence(currSequence, true);
      }
      return true;
   }

   private void updateSequenceSelector() {
      String[] sequenceNames = sequenceManager.getSequenceNames();
      sequenceCombobox = new JComboBox<>();
      sequenceCombobox.addActionListener(e -> {
         if (!shouldChangeSequence()) {
            return;
         }
         currSequence =
               sequenceManager.getSequence((String) sequenceCombobox.getSelectedItem());
         builderPanel = new BuilderPanel(studio, currSequence);
         redraw();
      });
   }

   private void redraw() {
      this.getContentPane().removeAll();

      this.add(new JLabel("Sequence: "), "split");
      this.add(sequenceCombobox, "split");
      this.add(newSequenceButton, "split");
      this.add(saveSequenceButton, "split");
      this.add(deleteSequenceButton, "wrap");

      this.add(new JLabel("Name: "), "split");
      this.add(sequenceNameTextField, "wrap");

      this.add(builderPanel);

      this.revalidate();
      this.repaint();
      this.pack();
   }
}
