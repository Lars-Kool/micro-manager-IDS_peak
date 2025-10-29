package org.micromanager.plugins.previewer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class DropdownOption extends JPanel {
   private final JComboBox<String> comboBox;
   private String option;
   private final PropertyChangeSupport support;

   public DropdownOption(String label, String[] options) {
      this.setLayout(new MigLayout("insets 0"));
      support = new PropertyChangeSupport(this);

      this.add(new JLabel(label));
      comboBox = new JComboBox<>(options);
      comboBox.addActionListener(e -> {
         if (!Objects.equals(label, "Analysis step")) {
            support.firePropertyChange(label, option, comboBox.getSelectedItem());
         } else {
            // Old value is set to -1 to never interfere with the new value.
            // Since no property change is fired if oldValue == newValue
            support.firePropertyChange(label, -1, comboBox.getSelectedIndex());
         }
         option = (String) comboBox.getSelectedItem();
      });
      this.add(comboBox);
   }

   public void addPropertyChangeListener(PropertyChangeListener pcl) {
      support.addPropertyChangeListener(pcl);
   }

   public void removePropertyChangeListener(PropertyChangeListener pcl) {
      support.removePropertyChangeListener(pcl);
   }

   public String getSelected() {
      return (String) comboBox.getSelectedItem();
   }

   public int getSelectedIndex() {
      return comboBox.getSelectedIndex();
   }
}
