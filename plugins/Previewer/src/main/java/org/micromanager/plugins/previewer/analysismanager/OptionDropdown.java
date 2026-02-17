package org.micromanager.plugins.previewer.analysismanager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.plugins.previewer.OptionInterface;

public class OptionDropdown extends JPanel implements OptionInterface, PropertyChangeListener {
   private final JComboBox<String> comboBox;
   private final String label;
   private final AnalysisParameter parameter;

   /**
    * Constructor of OptionDropdown. An OptionDropdown is a JPanel that contains a JCombobox that
    * allows the user to set an Enum AnalysisParameter.
    *
    * @param parameter Enum AnalysisParameter
    */
   public OptionDropdown(AnalysisParameter parameter) {
      this.setLayout(new MigLayout("insets 0"));
      this.label = parameter.getName();
      this.parameter = parameter;
      ImageAnalysisEventHandler.getInstance().addListener(this);

      this.add(new JLabel(label));
      comboBox = new JComboBox<>(parameter.getOptions());
      comboBox.setSelectedItem(parameter.getValue());
      comboBox.addActionListener(e -> {
         parameter.setValue(comboBox.getSelectedItem());
      });
      this.add(comboBox);
   }

   /**
    * Getter of the label of the parameter
    *
    * @return
    */
   public String getLabel() {
      return label;
   }

   /**
    * Getter of the value of the selected item. This will be a String, but to keep universality
    * in the OptionInterface, the String will be returned as Object.
    *
    * @return Value of the selected item
    */
   public Object getValue() {
      return comboBox.getSelectedItem();
   }

   /**
    * Getter of the index of the selected item.
    *
    * @return Index of the selected item
    */
   public int getIdex() {
      return comboBox.getSelectedIndex();
   }

   /**
    * ImageAnalysisEventHandler calls this method when an event is fired.
    *
    * @param evt A PropertyChangeEvent object describing the event source
    *            and the property that has changed.
    */
   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      if (!Objects.equals(evt.getPropertyName(), "Parameter changed")
            || !Objects.equals(parameter, evt.getSource())) {
         return;
      }
      comboBox.setSelectedItem(evt.getNewValue());
   }
}
