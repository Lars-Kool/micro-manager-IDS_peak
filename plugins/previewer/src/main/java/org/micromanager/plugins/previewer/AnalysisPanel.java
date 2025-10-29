package org.micromanager.plugins.previewer;

import java.util.ArrayList;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;

public class AnalysisPanel extends JPanel {
   private final String name;
   private final ImageAnalysis.Method method;
   private final ArrayList<JPanel> panels;
   private final PreviewerPanel parent;
   private final Studio studio;

   public AnalysisPanel(Studio studio, String name, PreviewerPanel parent,
                        ImageAnalysis.Method method) {
      this.setLayout(new MigLayout("insets 0"));
      Border border = BorderFactory.createTitledBorder(name);
      this.setBorder(border);

      this.name = name;
      this.method = method;
      this.parent = parent;
      this.studio = studio;
      panels = new ArrayList<>();
      addOptions();
      for (JPanel panel : panels) {
         this.add(panel, "wrap");
         panel.addPropertyChangeListener(parent);
      }
   }

   public void analyze(int[] src, int width, int height) {
      switch (method) {
         case THRESHOLD:
            ImageAnalysis.threshold(src, ((IntegerOption) panels.get(0)).getValue(), true);
            break;
         case INVERT:
            ImageAnalysis.invert(
                  src,
                  Objects.equals(((DropdownOption) panels.get(0)).getSelected(), "True"),
                  true
            );
            break;
         case FILL_GAPS:
            ImageAnalysis.fillGaps(
                  src,
                  Objects.equals(((DropdownOption) panels.get(0)).getSelected(), "True"),
                  width, height, true
            );
            break;
         case DISTANCE_TRANSFORM:
            ImageAnalysis.distanceTransform(
                  src, width, height, true
            );
            break;
         case WATERSHED:
            ImageAnalysis.watershed(
                  src, width, height,
                  ((IntegerOption) panels.get(0)).getValue(),
                  true
            );
            break;
         case ADD_EDGES:
            ImageAnalysis.addEdges(
                  src,
                  parent.getRawImg(((IntegerOption) panels.get(0)).getValue()).clone(),
                  width, height,
                  ((IntegerOption) panels.get(1)).getValue(),
                  true
            );
            break;
         default:
            studio.getLogManager().logMessage("Image analysis algorithm not implemented.");
            break;
      }
   }

   private void addOptions() {
      switch (method) {
         case THRESHOLD:
            addIntegerOption("ThresholdValue", 0);
            break;
         case INVERT:
            addDropdownOption("ShouldInvert", new String[] {"True", "False"});
            break;
         case FILL_GAPS:
            addDropdownOption("ShouldFillGaps", new String[] {"True", "False"});
            break;
         case DISTANCE_TRANSFORM:
            addDropdownOption("Connectivity", new String[] {"4", "8"});
            break;
         case WATERSHED:
            addIntegerOption("MinimumDropletRadius", 0);
            addDropdownOption("Connectivity", new String[] {"4", "8"});
            break;
         case ADD_EDGES:
            addIntegerOption("EdgeImage", 0);
            addIntegerOption("EdgeWidth", 0);
            break;
         default:
            studio.getLogManager().logMessage("Algorithm not implemented.");
      }
   }

   private void addIntegerOption(String label, int defaultValue) {
      panels.add(new IntegerOption(label, defaultValue));
   }

   private void addDropdownOption(String label, String[] options) {
      panels.add(new DropdownOption(label, options));
   }
}
