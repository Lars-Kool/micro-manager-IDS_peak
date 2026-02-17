package org.micromanager.plugins.previewer.analysismanager;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import net.miginfocom.swing.MigLayout;

/**
 * StepPanel class. It extends JPanel and adds a user input box for each of the parameters in
 * this step.
 */
public class StepPanel extends JPanel {
   private final AnalysisStep step;

   /**
    * Constructor for StepPanel
    *
    * @param step AnalysisStep this panel is representing
    */
   public StepPanel(AnalysisStep step) {
      this.setLayout(new MigLayout("insets 0"));
      Border border = BorderFactory.createTitledBorder(step.name);
      this.setBorder(border);

      this.step = step;
      if (!step.hasParameters()) {
         this.add(new JLabel("This step has no input parameters."));
      }
      for (AnalysisParameter parameter : step.parameters) {
         switch (parameter.getType()) {
            case INTEGER:
               this.add(new OptionInteger(parameter), "wrap");
               break;
            case FLOAT:
               this.add(new OptionFloat(parameter), "wrap");
               break;
            case ENUM:
               this.add(new OptionDropdown(parameter), "wrap");
               break;
            default:
               break;
         }
      }
   }

   /**
    * Getter for the AnalysisStep corresponding to this panel
    *
    * @return AnalysisStep
    */
   public AnalysisStep getStep() {
      return step;
   }
}
