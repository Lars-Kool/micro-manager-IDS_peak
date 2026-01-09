package org.micromanager.plugins.previewer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;
import net.miginfocom.swing.MigLayout;
import org.micromanager.plugins.previewer.analysismanager.AnalysisManager;
import org.micromanager.plugins.previewer.analysismanager.AnalysisParameter;

public class OptionInteger extends JPanel implements OptionInterface {
   private final JFormattedTextField textField;
   private final AnalysisParameter parameter;

   public OptionInteger(AnalysisParameter parameter, AnalysisManager analysisManager) {
      this.setLayout(new MigLayout("insets 0"));
      this.parameter = parameter;

      this.add(new JLabel(parameter.getName()));
      NumberFormat format = NumberFormat.getIntegerInstance();
      NumberFormatter formatter = new NumberFormatter(format);
      formatter.setMinimum(0);
      formatter.setMaximum(255);

      textField = new JFormattedTextField(formatter);
      textField.setValue(parameter.getValue());
      textField.setColumns(5);
      textField.addKeyListener(new KeyAdapter() {
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               int newValue = Integer.parseInt(textField.getText());
               analysisManager.setParameterValue(parameter, newValue);
               textField.setValue(newValue);
            }
         }
      });
      this.add(textField);
   }

   public String getLabel() {
      return parameter.getName();
   }

   public Object getValue() {
      return parameter.getValue();
   }
}
