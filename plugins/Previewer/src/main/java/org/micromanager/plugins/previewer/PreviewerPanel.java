package org.micromanager.plugins.previewer;

import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;
import org.micromanager.plugins.previewer.analysismanager.AnalysisManager;
import org.micromanager.plugins.previewer.analysismanager.AnalysisSequence;
import org.micromanager.plugins.previewer.analysismanager.AnalysisStep;

public class PreviewerPanel extends JPanel {
   private final PreviewerFrame mainFrame;
   private final OptionDropdown analysisSelector;
   private final ArrayList<PreviewerAnalysisPanel> panels;
   private final AnalysisSequence sequence;

   public PreviewerPanel(Studio studio, PreviewerFrame frame,
                         AnalysisManager analysisManager, AnalysisSequence sequence) {
      this.setLayout(new MigLayout("insets 0"));
      this.mainFrame = frame;
      this.sequence = sequence;

      panels = new ArrayList<>();
      String[] stepNames = new String[sequence.steps.size() + 1];
      int i = 0;
      stepNames[i] = "RAW";
      for (AnalysisStep step : sequence.steps) {
         i++;
         PreviewerAnalysisPanel temp = new PreviewerAnalysisPanel(studio, mainFrame,
               analysisManager, step);
         if (!step.hasParameters()) {
            temp.add(new JLabel("No parameters."));
         }
         panels.add(temp);
         stepNames[i] = step.name;
      }

      analysisSelector = new OptionDropdown("Analysis step", stepNames[0], stepNames, mainFrame);
      draw();
   }


   private void draw() {
      this.add(analysisSelector, "wrap");
      for (PreviewerAnalysisPanel panel : panels) {
         this.add(panel, "wrap");
         panel.addPropertyChangeListener(mainFrame);
      }
   }

   public String getName() {
      return analysisSelector.getSelected();
   }

   public AnalysisSequence getSequence() {
      return sequence;
   }

   public int getDisplayIndex() {
      return analysisSelector.getSelectedIndex();
   }
}