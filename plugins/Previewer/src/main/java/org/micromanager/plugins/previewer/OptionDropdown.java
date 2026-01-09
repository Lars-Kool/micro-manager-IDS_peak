package org.micromanager.plugins.previewer;

import java.beans.PropertyChangeSupport;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.plugins.previewer.analysismanager.AnalysisManager;
import org.micromanager.plugins.previewer.analysismanager.AnalysisParameter;

public class OptionDropdown extends JPanel implements OptionInterface {
   private final JComboBox<String> comboBox;
   private final String label;

   public OptionDropdown(AnalysisParameter parameter, AnalysisManager analysisManager) {
      this.setLayout(new MigLayout("insets 0"));
      this.label = parameter.getName();

      this.add(new JLabel(label));
      comboBox = new JComboBox<>(parameter.getOptions());
      comboBox.addActionListener(e -> {
         analysisManager.setParameterValue(parameter, comboBox.getSelectedItem());
      });
      this.add(comboBox);
   }

   public OptionDropdown(String label, String defaultOption, String[] options,
                         PreviewerFrame frame) {
      this.setLayout(new MigLayout("insets 0"));
      this.label = label;
      PropertyChangeSupport support = new PropertyChangeSupport(this);
      support.addPropertyChangeListener(frame);

      this.add(new JLabel(label));
      comboBox = new JComboBox<>(options);
      comboBox.addActionListener(e -> {
         support.firePropertyChange(label, null, comboBox.getSelectedIndex());
      });
      // comboBox.setSelectedItem(defaultOption);
      this.add(comboBox);
   }

   public String getSelected() {
      return (String) comboBox.getSelectedItem();
   }

   public int getSelectedIndex() {
      return comboBox.getSelectedIndex();
   }

   public String getLabel() {
      return label;
   }

   public Object getValue() {
      return comboBox.getSelectedItem();
   }
}
