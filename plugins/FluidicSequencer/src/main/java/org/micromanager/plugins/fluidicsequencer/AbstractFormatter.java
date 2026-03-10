package org.micromanager.plugins.dropletcontrol;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Objects;
import javax.swing.JFormattedTextField;

public class AbstractFormatter extends JFormattedTextField.AbstractFormatter {
   private final DecimalFormat defaultFormat = new DecimalFormat("0.###");
   private final DecimalFormat scientificFormat = new DecimalFormat("0.###E0");

   @Override
   public Object stringToValue(String text) throws ParseException {
      return Double.parseDouble(text);
   }

   @Override
   public String valueToString(Object value) throws ParseException {
      if (value == null) {
         throw new ParseException("Null is invalid value", 0);
      }
      double temp = (double) value;
      if (Objects.equals(value, 0.0) || (1E-2 < temp && 1E3 > temp)) {
         return defaultFormat.format(temp);
      }
      return scientificFormat.format(temp);
   }
}
