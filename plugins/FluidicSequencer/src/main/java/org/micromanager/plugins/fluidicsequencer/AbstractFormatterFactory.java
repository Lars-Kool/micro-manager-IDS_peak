package org.micromanager.plugins.dropletcontrol;

import javax.swing.JFormattedTextField;

public class AbstractFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
   @Override
   public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
      return new AbstractFormatter();
   }
}
