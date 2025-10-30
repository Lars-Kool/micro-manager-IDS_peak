package org.micromanager.plugins.previewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;

public class PreviewerPanel extends JPanel {
   private final String name;
   private final ArrayList<AnalysisPanel> panels;
   private final PreviewerFrame parent;
   private final DropdownOption analysisSelector;
   private final ImageAnalysis.Method[] methods;


   public PreviewerPanel(Studio studio, PreviewerFrame parent, String name,
                         ImageAnalysis.Method[] methods) {
      this.setLayout(new MigLayout("insets 0"));
      this.parent = parent;
      this.name = name;
      this.methods = methods;

      panels = new ArrayList<>();


      String[] methodNames = new String[methods.length + 1];
      methodNames[0] = "RAW";
      for (int i = 0; i < methods.length; i++) {
         methodNames[i + 1] = methods[i].name();
      }
      analysisSelector = new DropdownOption("Analysis step", methodNames);
      analysisSelector.addPropertyChangeListener(parent);

      for (int i = 0; i < methods.length; i++) {
         panels.add(new AnalysisPanel(
               studio,
               methodNames[i + 1],
               this,
               methods[i])
         );
      }
      draw();
   }


   private void draw() {
      this.add(analysisSelector, "wrap");
      for (AnalysisPanel panel : panels) {
         this.add(panel, "wrap");
         panel.addPropertyChangeListener(parent);
      }
   }

   public String getName() {
      return name;
   }

   public ArrayList<HashMap<String, Object>> getData() {
      ArrayList<HashMap<String, Object>> output = new ArrayList<>();
      for (int i = 0; i < methods.length; i++) {
         HashMap<String, Object> temp = panels.get(i).getData();
         temp.put("Method", methods[i]);
         output.add(temp);
      }
      return output;
   }

   public int getDisplayIndex() {
      return analysisSelector.getSelectedIndex();
   }

   public PreviewerFrame getFrame() {
      return parent;
   }
}
