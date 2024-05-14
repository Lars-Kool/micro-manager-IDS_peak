package org.micromanager.plugins.framecombiner;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;
import net.miginfocom.swing.MigLayout;
import org.micromanager.PropertyMap;
import org.micromanager.PropertyMaps;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.internal.utils.WindowPositioning;
import org.micromanager.multichannelshading.MultiChannelShading;

// Imports for MMStudio internal packages
// Plugins should not access internal packages, to ensure modularity and
// maintainability. However, this plugin code is older than the current
// MMStudio API, so it still uses internal classes and interfaces. New code
// should not imitate this practice.

public class FrameCombinerConfigurator extends JFrame implements ProcessorConfigurator {

   private static final String PROCESSOR_DIMENSION = "Dimension";
   private static final String USE_WHOLE_STACK = "useWholeStack";
   private static final String PROCESSOR_ALGO = "Algorithm to apply on stack images";
   private static final String NUMBER_TO_PROCESS = "Number of images to process";
   private static final String CHANNEL_TO_AVOID = "Avoid Channel(s) (eg. 1,2 or 1-5)";

   private final Studio studio_;
   private final PropertyMap settings_;
   private JComboBox<String> processorDimensionBox_;
   private JCheckBox useWholeStackCheckBox_;
   private JFormattedTextField numberOfImagesToProcessField_;
   private JComboBox<String> processorAlgoBox_;
   private JFormattedTextField channelsToAvoidField_;

   public FrameCombinerConfigurator(PropertyMap settings, Studio studio) {
      studio_ = studio;
      settings_ = settings;

      initComponents();
      loadSettingValue();

      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(200, 200);
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);
      super.setVisible(true);
   }

   private void initComponents() {
      super.setLayout(new MigLayout("flowx, fill, insets 8"));
      super.setTitle(MultiChannelShading.MENUNAME);
      setTitle("FrameCombiner");

      final JPanel jPanel1 = new JPanel();
      final NumberFormat format = NumberFormat.getInstance();
      final NumberFormatter formatter = new NumberFormatter(format);
      formatter.setValueClass(Integer.class);
      formatter.setMinimum(1);
      formatter.setMaximum(Integer.MAX_VALUE);
      formatter.setCommitsOnValidEdit(true);
      formatter.setAllowsInvalid(false);
      processorDimensionBox_ = new JComboBox<>();
      useWholeStackCheckBox_ = new JCheckBox();
      numberOfImagesToProcessField_ = new JFormattedTextField(formatter);
      processorAlgoBox_ = new JComboBox<>();

      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      jPanel1.setAutoscrolls(true);
      jPanel1.setLayout(new MigLayout("flowx, fill, insets 8, gapy 15"));

      jPanel1.add(new JLabel("Dimension to process"));
      JLabel useWholeStackLabel = new JLabel("Use whole stack");
      JLabel numberOfImagesToProcessLabel = new JLabel("Number of images to process");

      processorDimensionBox_.addItem(FrameCombinerPlugin.PROCESSOR_DIMENSION_TIME);
      processorDimensionBox_.addItem(FrameCombinerPlugin.PROCESSOR_DIMENSION_Z);
      processorDimensionBox_.addActionListener(e -> {
         if (Objects.equals(processorDimensionBox_.getSelectedItem(),
                 FrameCombinerPlugin.PROCESSOR_DIMENSION_TIME)) {
            useWholeStackCheckBox_.setEnabled(false);
            useWholeStackLabel.setEnabled(false);
            numberOfImagesToProcessField_.setEnabled(true);
            numberOfImagesToProcessLabel.setEnabled(true);
         } else if (Objects.equals(processorDimensionBox_.getSelectedItem(),
                 FrameCombinerPlugin.PROCESSOR_DIMENSION_Z)) {
            useWholeStackCheckBox_.setEnabled(true);
            useWholeStackLabel.setEnabled(true);
            numberOfImagesToProcessField_.setEnabled(!useWholeStackCheckBox_.isSelected());
            numberOfImagesToProcessLabel.setEnabled(useWholeStackCheckBox_.isSelected());
         }
      });
      jPanel1.add(processorDimensionBox_, "wrap");

      jPanel1.add(useWholeStackLabel);
      useWholeStackCheckBox_.addActionListener(e -> {
         numberOfImagesToProcessField_.setEnabled(!useWholeStackCheckBox_.isSelected());
         numberOfImagesToProcessLabel.setEnabled(!useWholeStackCheckBox_.isSelected());
      });
      jPanel1.add(useWholeStackCheckBox_, "wrap");

      jPanel1.add(numberOfImagesToProcessLabel);
      numberOfImagesToProcessField_.setName("_");
      jPanel1.add(numberOfImagesToProcessField_, "growx, wrap");

      jPanel1.add(new JLabel("Algorithm to apply on image stack"));

      processorAlgoBox_.addItem(FrameCombinerPlugin.PROCESSOR_ALGO_MEAN);
      //processorAlgoBox_.addItem(FrameCombinerPlugin.PROCESSOR_ALGO_MEDIAN);
      processorAlgoBox_.addItem(FrameCombinerPlugin.PROCESSOR_ALGO_SUM);
      processorAlgoBox_.addItem(FrameCombinerPlugin.PROCESSOR_ALGO_MAX);
      processorAlgoBox_.addItem(FrameCombinerPlugin.PROCESSOR_ALGO_MIN);
      jPanel1.add(processorAlgoBox_, "wrap");

      jPanel1.add(new JLabel(
              "<html>Avoid Channel(s) (zero-based)<br/><p style=\"text-align: center;\">"
              + "eg. 1,2 or 1-5 (no space)</p></html>"));

      channelsToAvoidField_ = new JFormattedTextField();
      channelsToAvoidField_.setName("_");
      jPanel1.add(channelsToAvoidField_, "growx, wrap");

      super.add(jPanel1, "wrap");
      pack();
   }


   private void loadSettingValue() {
      String processorDimension = studio_.profile().getSettings(FrameCombinerConfigurator.class)
              .getString(PROCESSOR_DIMENSION, FrameCombinerPlugin.PROCESSOR_DIMENSION_TIME);
      processorDimensionBox_.setSelectedItem(settings_.getString(
            "processorDimension", processorDimension));
      boolean useWholeStack = studio_.profile().getSettings(FrameCombinerConfigurator.class)
              .getBoolean(USE_WHOLE_STACK, false);
      useWholeStackCheckBox_.setSelected(settings_.getBoolean(
            "useWholeStack", useWholeStack));
      String processorAlgo = studio_.profile().getSettings(FrameCombinerConfigurator.class)
              .getString(PROCESSOR_ALGO, FrameCombinerPlugin.PROCESSOR_ALGO_MEAN);
      processorAlgoBox_.setSelectedItem(settings_.getString(
            "processorAlgo", processorAlgo));
      int numberOfImagesToProcess =  studio_.profile().getSettings(FrameCombinerConfigurator.class)
              .getInteger(NUMBER_TO_PROCESS, 10);
      numberOfImagesToProcessField_.setText(Integer.toString(settings_.getInteger(
            "numerOfImagesToProcess", numberOfImagesToProcess)));
      String channelsToAvoid = studio_.profile().getSettings(FrameCombinerConfigurator.class)
              .getString(CHANNEL_TO_AVOID, "");
      channelsToAvoidField_.setText(settings_.getString(
            "channelsToAvoidField", channelsToAvoid));
   }

   @Override
   public void showGUI() {
      pack();
      setVisible(true);
   }

   @Override
   public void cleanup() {
      dispose();
   }

   @Override
   public PropertyMap getSettings() {
      // Save preferences now.
      studio_.profile().getSettings(FrameCombinerConfigurator.class)
              .putString(PROCESSOR_ALGO, (String) processorAlgoBox_.getSelectedItem());
      studio_.profile().getSettings(FrameCombinerConfigurator.class)
              .putBoolean(USE_WHOLE_STACK, useWholeStackCheckBox_.isSelected());
      studio_.profile().getSettings(FrameCombinerConfigurator.class)
              .putString(PROCESSOR_DIMENSION, (String ) processorDimensionBox_.getSelectedItem());
      Integer numberOfImagesToProcess = Integer.parseInt(numberOfImagesToProcessField_.getText());
      studio_.profile().getSettings(FrameCombinerConfigurator.class)
              .putInteger(NUMBER_TO_PROCESS, numberOfImagesToProcess);
      studio_.profile().getSettings(FrameCombinerConfigurator.class)
              .putString(CHANNEL_TO_AVOID, channelsToAvoidField_.getText());

      PropertyMap.Builder builder = PropertyMaps.builder();
      builder.putString("processorDimension", (String) processorDimensionBox_.getSelectedItem());
      builder.putBoolean("useWholeStack", useWholeStackCheckBox_.isSelected());
      builder.putString("processorAlgo", (String) processorAlgoBox_.getSelectedItem());
      builder.putInteger("numerOfImagesToProcess",
            Integer.parseInt(numberOfImagesToProcessField_.getText()));
      builder.putString("channelsToAvoid", channelsToAvoidField_.getText());
      return builder.build();
   }

   /**
    * Stores settings when the window is closing.
    *
    * @param evt the window event that is not used.
    */
   public void windowClosing(WindowEvent evt) {
      getSettings();
   }

}