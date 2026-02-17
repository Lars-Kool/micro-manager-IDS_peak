package org.micromanager.plugins.previewer.analysismanager;

import com.google.gson.annotations.SerializedName;

public class AnalysisParameter {
   private final transient ImageAnalysisEventHandler eventHandler;
   private String name;
   private ParameterType type;
   private Object value;
   private String[] options;

   /**
    * Empty constructor, for Gson use only!
    */
   public AnalysisParameter() {
      eventHandler = ImageAnalysisEventHandler.getInstance();
   }

   /**
    * AnalysisParameter constructor, only for Integer and Float use (there is a different
    * constructor for Enum types, as they require a list of options).
    *
    * @param name  Name of parameter
    * @param type  Type of parameter (Integer or Float)
    * @param value Value of parameter (depending on type)
    */
   public AnalysisParameter(String name, ParameterType type, Object value) {
      this.name = name;
      this.type = type;
      this.value = value;
      this.eventHandler = ImageAnalysisEventHandler.getInstance();
   }

   /**
    * AnalysisParameter constructor for Enum type
    *
    * @param name  Name of parameter
    * @param type  Type of parameter (Enum)
    * @param value Value of parameter (String)
    */
   public AnalysisParameter(String name, ParameterType type, String value, String[] options) {
      this.name = name;
      this.type = type;
      this.value = value;
      this.options = options;
      this.eventHandler = ImageAnalysisEventHandler.getInstance();
   }

   /**
    * Getter of the parameter name
    *
    * @return String of name of parameter
    */
   public String getName() {
      return this.name;
   }

   /**
    * Getter of the parameter type
    *
    * @return Type of parameter
    */
   public ParameterType getType() {
      return this.type;
   }

   /**
    * Getter of the parameter value
    *
    * @return Value of the parameter (Integer, Double or String)
    */
   public Object getValue() {
      return this.value;
   }

   /**
    * Setter of parameter value
    *
    * @param value New value of the parameter (Integer, Double or String)
    */
   public void setValue(Object value) {
      if (value == this.value) {
         return;
      }
      Object oldValue = this.value;
      this.value = value;
      eventHandler.firePropertyChange("Parameter changed", this, oldValue, value);
   }

   /**
    * Getter of Enum options
    *
    * @return String array with possible options
    */
   public String[] getOptions() {
      return options;
   }

   /**
    * Setter of Enum options
    *
    * @param options String array with available options
    */
   protected void setOptions(String[] options) {
      this.options = options;
   }

   /**
    * Deep-copy of this parameter. Generates a copy of this parameter without
    * any reference/aliasing to this parameter.
    *
    * @return Deep-copy of this parameter.
    */
   public AnalysisParameter copy() {
      if (type == ParameterType.ENUM) {
         return new AnalysisParameter(name, type, (String) value, options);
      }
      return new AnalysisParameter(name, type, value);
   }

   /**
    * Available types of parameters
    */
   public enum ParameterType {
      @SerializedName("INTEGER")
      INTEGER,
      @SerializedName("FLOAT")
      FLOAT,
      @SerializedName("ENUM")
      ENUM
   }
}
