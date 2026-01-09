package org.micromanager.plugins.previewer;

import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;
import org.micromanager.plugins.previewer.analysismanager.AnalysisManager;
import org.micromanager.plugins.previewer.analysismanager.AnalysisParameter;
import org.micromanager.plugins.previewer.analysismanager.AnalysisStep;

public class PreviewerAnalysisPanel extends JPanel {
   private final AnalysisStep step;
   private final ArrayList<JPanel> panels;
   private final PreviewerFrame mainFrame;
   private final AnalysisManager analysisManager;
   private final Studio studio;

   public PreviewerAnalysisPanel(Studio studio, PreviewerFrame frame,
                                 AnalysisManager analysisManager, AnalysisStep step) {
      this.setLayout(new MigLayout("insets 0"));
      Border border = BorderFactory.createTitledBorder(step.name);
      this.setBorder(border);

      this.studio = studio;
      this.mainFrame = frame;
      this.analysisManager = analysisManager;
      this.step = step;

      panels = new ArrayList<>();
      addOptions();
      for (JPanel panel : panels) {
         this.add(panel, "wrap");
         panel.addPropertyChangeListener(mainFrame);
      }
   }

   private void addOptions() {
      for (AnalysisParameter parameter : step.parameters) {
         switch (parameter.getType()) {
            case INTEGER:
               addIntegerOption(parameter);
               break;
            case FLOAT:
               addFloatOption(parameter);
               break;
            case ENUM:
               addDropdownOption(parameter);
               break;
            default:
               studio.getLogManager().logMessage("Previewer warning: Invalid parameter type '"
                     + parameter.getType().name() + "'");
               break;
         }
      }
   }

   public AnalysisStep getStep() {
      return step;
   }

   private void addIntegerOption(AnalysisParameter parameter) {
      panels.add(new OptionInteger(parameter, analysisManager));
   }

   private void addFloatOption(AnalysisParameter parameter) {
      panels.add(new OptionFloat(parameter, analysisManager));
   }

   private void addDropdownOption(AnalysisParameter parameter) {
      panels.add(new OptionDropdown(parameter, analysisManager));
   }
}
