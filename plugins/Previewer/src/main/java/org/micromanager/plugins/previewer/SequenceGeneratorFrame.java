package org.micromanager.plugins.previewer;

import java.awt.Toolkit;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.micromanager.internal.utils.WindowPositioning;
import org.micromanager.plugins.previewer.analysismanager.AnalysisManager;
import org.micromanager.plugins.previewer.analysismanager.AnalysisSequence;
import org.micromanager.plugins.previewer.analysismanager.AnalysisStep;
import org.micromanager.plugins.previewer.analysismanager.ImageAnalysis;

public class SequenceGeneratorFrame extends JFrame {
   private final String sequenceName = "";

   private final AnalysisManager analysisManager;
   private final AnalysisSequence sequence;

   private final JTextField nameTextField;
   private final ArrayList<SequenceGeneratorPanel> panels;

   private final JButton deleteButton;
   private final JButton cancelButton;
   private final JButton replaceButton;
   private final JButton confirmButton;

   public SequenceGeneratorFrame(int idx, AnalysisManager analysisManager) {
      this.setTitle("Sequence generator");
      this.setLayout(new MigLayout());
      this.analysisManager = analysisManager;

      nameTextField = new JTextField();
      nameTextField.setColumns(19);

      if (idx < 0) {
         sequence = new AnalysisSequence("");
      } else {
         sequence = analysisManager.getSequence(idx).copy();
         nameTextField.setText(sequence.sequenceName);
      }

      panels = new ArrayList<>();
      for (AnalysisStep step : sequence.steps) {
         panels.add(new SequenceGeneratorPanel(this, step.method.name(),
               ImageAnalysis.Method.values()));
      }
      if (panels.isEmpty()) {
         panels.add(new SequenceGeneratorPanel(this, ImageAnalysis.Method.values()));
         sequence.addMethod(panels.get(0).getMethod());
      }

      deleteButton = new JButton("Delete");
      deleteButton.addActionListener(e -> {
         if (analysisManager.removeSequence(idx)) {
            this.dispose();
         }
      });

      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(e -> {
         this.dispose();
      });

      replaceButton = new JButton("Replace");
      replaceButton.addActionListener(e -> {
         updateName();
         updateForwardReferences();
         if (analysisManager.setSequence(idx, sequence)) {
            this.dispose();
         }
      });

      confirmButton = new JButton("Add");
      confirmButton.addActionListener(e -> {
         updateName();
         updateForwardReferences();
         if (analysisManager.addSequence(sequence)) {
            this.dispose();
         }
      });
      this.redraw();

      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(100, 100);
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);
   }

   public void addItem(SequenceGeneratorPanel panel) {
      int i = panels.indexOf(panel);
      panels.add(i + 1, new SequenceGeneratorPanel(this, ImageAnalysis.Method.values()));
      sequence.steps.add(i + 1, new AnalysisStep(panel.getMethod()));
      redraw();
   }

   public void removeItem(SequenceGeneratorPanel panel) {
      if (panels.size() == 1) {
         return;
      }
      int idx = panels.indexOf(panel);
      sequence.steps.remove(idx);
      panels.remove(panel);
      redraw();
   }

   public void moveUp(SequenceGeneratorPanel panel) {
      int idx = panels.indexOf(panel);
      if (idx <= 0) {
         return;
      } // Already at the top

      AnalysisStep step = sequence.steps.remove(idx);
      sequence.steps.add(idx - 1, step);

      panels.remove(idx);
      panels.add(idx - 1, panel);
      redraw();
   }

   public void moveDown(SequenceGeneratorPanel panel) {
      int idx = panels.indexOf(panel);
      if (idx >= panels.size() - 1) {
         return;
      } // Already at the top

      AnalysisStep step = sequence.steps.remove(idx);
      sequence.steps.add(idx + 1, step);

      panels.remove(idx);
      panels.add(idx + 1, panel);
      redraw();
   }

   public void updateItem(SequenceGeneratorPanel panel) {
      int idx = panels.indexOf(panel);
      if (idx < 0 || idx >= panels.size()) {
         return;
      }
      sequence.steps.set(idx, new AnalysisStep(panel.getMethod()));
   }

   private void updateForwardReferences() {
      ArrayList<AnalysisStep> steps = sequence.steps;
      for (int i = 0; i < steps.size(); i++) {
         if (steps.get(i).isBinary) {
            int j = (int) steps.get(i).parameters.get(0).getValue();
            if (!sequence.references.containsKey(j)) {
               sequence.references.put(j, new ArrayList<>());
            }
            sequence.references.get(j).add(i);
         }
      }
   }

   private void updateName() {
      sequence.sequenceName = nameTextField.getText();
   }

   private void redraw() {
      this.getContentPane().removeAll();

      this.add(new JLabel("Name:"), "split");
      this.add(nameTextField, "split");
      this.add(deleteButton, "wrap");

      for (SequenceGeneratorPanel panel : panels) {
         this.add(panel, "wrap");
      }

      this.add(cancelButton, "split");
      this.add(replaceButton, "split");
      this.add(confirmButton, "split");

      this.validate();
      this.repaint();
      this.pack();
   }
}
