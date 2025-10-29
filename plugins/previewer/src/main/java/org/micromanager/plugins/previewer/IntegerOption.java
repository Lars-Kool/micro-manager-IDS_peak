package org.micromanager.plugins.previewer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;
import net.miginfocom.swing.MigLayout;

public class IntegerOption extends JPanel {
   private int value;
   private final JFormattedTextField textField;
   private final PropertyChangeSupport support;

   public IntegerOption(String label, int defaultValue) {
      this.setLayout(new MigLayout("insets 0"));
      support = new PropertyChangeSupport(this);

      this.add(new JLabel(label));
      NumberFormat format = NumberFormat.getIntegerInstance();
      NumberFormatter formatter = new NumberFormatter(format);
      formatter.setMinimum(0);
      formatter.setMaximum(255);

      textField = new JFormattedTextField(formatter);
      textField.setValue(defaultValue);
      textField.setColumns(5);
      textField.addKeyListener(new KeyAdapter() {
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               int oldValue = value;
               value = Integer.parseInt(textField.getText());
               textField.setValue(value);
               support.firePropertyChange(label, oldValue, value);
            }
         }
      });
      this.add(textField);
   }

   public int getValue() {
      return value;
   }

   public void addPropertyChangeListener(PropertyChangeListener pcl) {
      support.addPropertyChangeListener(pcl);
   }

   public void removePropertyChangeListener(PropertyChangeListener pcl) {
      support.removePropertyChangeListener(pcl);
   }
}
