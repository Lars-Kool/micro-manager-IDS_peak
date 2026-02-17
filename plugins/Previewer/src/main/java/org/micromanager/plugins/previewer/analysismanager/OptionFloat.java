package org.micromanager.plugins.previewer.analysismanager;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Objects;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;
import net.miginfocom.swing.MigLayout;

public class OptionFloat extends JPanel implements OptionInterface, PropertyChangeListener {
   private final JFormattedTextField textField;
   private final AnalysisParameter parameter;

   /**
    * Constructor of OptionFloat. OptionFloat is a JPanel containing a JFormattedTextField that
    * will format the input as Double.
    *
    * @param parameter
    */
   public OptionFloat(AnalysisParameter parameter) {
      this.setLayout(new MigLayout("insets 0"));
      this.parameter = parameter;
      ImageAnalysisEventHandler.getInstance().addListener(this);

      this.add(new JLabel(parameter.getName()));
      NumberFormat format = NumberFormat.getNumberInstance();
      format.setMaximumFractionDigits(3);
      format.setMinimumIntegerDigits(1);
      NumberFormatter formatter = new NumberFormatter(format);

      textField = new JFormattedTextField(formatter);
      textField.setValue(parameter.getValue());
      textField.setColumns(8);
      textField.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               double newValue = Double.parseDouble(textField.getText());
               parameter.setValue(newValue);
            }
         }
      });
      this.add(textField);
   }

   /**
    * Getter of the parameter label
    *
    * @return String label of parameter
    */
   public String getLabel() {
      return parameter.getName();
   }

   /**
    * Getter of the value of the selected item. This will be a Double, but to keep universality
    * in the OptionInterface, the String will be returned as Object.
    *
    * @return Value of the selected item
    */
   public Object getValue() {
      return parameter.getValue();
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
            || !Objects.equals(this, evt.getSource())) {
         return;
      }
      textField.setValue(evt.getNewValue());
   }
}
