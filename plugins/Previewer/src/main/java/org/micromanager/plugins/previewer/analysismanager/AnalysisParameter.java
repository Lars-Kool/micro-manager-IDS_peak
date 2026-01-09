package org.micromanager.plugins.previewer.analysismanager;

import com.google.gson.annotations.SerializedName;

public class AnalysisParameter {
   private String name;
   private ParameterType type;
   private Object value;
   private String[] options;

   public AnalysisParameter() {
   }

   public AnalysisParameter(String name, ParameterType type, Object value) {
      this.name = name;
      this.type = type;
      this.value = value;
   }

   public AnalysisParameter(String name, ParameterType type, String value, String[] options) {
      this.name = name;
      this.type = type;
      this.value = value;
      this.options = options;
   }

   public String getName() {
      return this.name;
   }

   public ParameterType getType() {
      return this.type;
   }

   public Object getValue() {
      return this.value;
   }

   protected void setValue(Object value) {
      this.value = value;
   }

   public String[] getOptions() {
      return options;
   }

   protected void setOptions(String[] options) {
      this.options = options;
   }

   public AnalysisParameter copy() {
      if (type == ParameterType.ENUM) {
         return new AnalysisParameter(name, type, (String) value, options);
      }
      return new AnalysisParameter(name, type, value);
   }

   public enum ParameterType {
      @SerializedName("INTEGER")
      INTEGER,
      @SerializedName("FLOAT")
      FLOAT,
      @SerializedName("ENUM")
      ENUM
   }
}
