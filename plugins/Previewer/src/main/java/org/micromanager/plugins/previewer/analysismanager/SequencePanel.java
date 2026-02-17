package org.micromanager.plugins.previewer.analysismanager;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 * SequencePanel class, which extends JPanel. It contains a JPanel for each of the steps in this
 * sequence.
 */
public class SequencePanel extends JPanel {
   private final AnalysisSequence sequence;

   public SequencePanel(AnalysisSequence sequence) {
      this.setLayout(new MigLayout("insets 0"));
      this.sequence = sequence;

      for (AnalysisStep step : sequence.steps) {
         this.add(new StepPanel(step), "wrap");
      }
   }

   /**
    * Getter of the AnalysisSequence that this panel represents
    *
    * @return AnalysisSequence
    */
   public AnalysisSequence getSequence() {
      return sequence;
   }
}