package org.micromanager.plugins.previewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;

public class PreviewerPanel extends JPanel implements PropertyChangeListener {
   private final String name;
   private final ArrayList<AnalysisPanel> panels;
   private final ArrayList<HashMap<String, Object>> data;
   private ArrayList<int[]> imgs;
   private final PreviewerFrame parent;
   private final DropdownOption analysisSelector;
   private final PropertyChangeSupport support;
   private final Studio studio;

   private int[] src;
   private int width;
   private int height;

   public PreviewerPanel(Studio studio, PreviewerFrame parent, String name,
                         ImageAnalysis.Method[] methods) {
      this.setLayout(new MigLayout("insets 0"));
      this.parent = parent;
      this.name = name;
      this.studio = studio;

      data = new ArrayList<>();
      panels = new ArrayList<>();
      support = new PropertyChangeSupport(this);
      support.addPropertyChangeListener(parent);


      String[] methodNames = new String[methods.length + 1];
      methodNames[0] = "RAW";
      for (int i = 0; i < methods.length; i++) {
         methodNames[i + 1] = methods[i].name();
      }
      analysisSelector = new DropdownOption("Analysis step", methodNames);
      analysisSelector.addPropertyChangeListener(this);

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

   public void analyze() {
      int[] temp = src.clone();
      imgs = new ArrayList<>();
      imgs.add(temp.clone());
      for (AnalysisPanel panel : panels) {
         panel.analyze(temp, width, height);
         imgs.add(temp.clone());
      }
   }

   public void setImg(int[] src, int width, int height) {
      this.src = src;
      this.width = width;
      this.height = height;
   }

   public void draw() {
      this.add(analysisSelector, "wrap");
      for (AnalysisPanel panel : panels) {
         this.add(panel, "wrap");
         panel.addPropertyChangeListener(this);
         panel.addPropertyChangeListener(parent);
      }
   }

   public byte[] getImg() {
      int[] img = imgs.get(analysisSelector.getSelectedIndex());
      byte[] out = new byte[img.length];
      for (int i = 0; i < out.length; i++) {
         out[i] = (byte) (img[i] & 0xFF);
      }
      return out;
   }

   public int[] getRawImg(int idx) {
      return imgs.get(idx);
   }

   public String getName() {
      return name;
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      if (src == null || Objects.equals(evt.getPropertyName(), "ancestor")) {
         return;
      }
      if (!Objects.equals(evt.getPropertyName(), "Analysis step")) {
         analyze();
      }
      support.firePropertyChange("imageUpdated", -1, 1);
   }
}
