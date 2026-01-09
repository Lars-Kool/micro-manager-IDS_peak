package org.micromanager.plugins.previewer;

import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.micromanager.plugins.previewer.analysismanager.ImageAnalysis;

public class SequenceGeneratorPanel extends JPanel {
   private final ImageAnalysis.Method[] options;
   private final JComboBox<String> optionsDropdown;

   public SequenceGeneratorPanel(SequenceGeneratorFrame parent, String currOption,
                                 ImageAnalysis.Method[] options) {
      this.options = options;

      JButton addButton = new JButton("+");
      addButton.addActionListener(e -> parent.addItem(this));
      this.add(addButton);

      JButton removeButton = new JButton("-");
      removeButton.addActionListener(e -> parent.removeItem(this));
      this.add(removeButton);

      Vector<String> optionsString = new Vector<>();
      for (ImageAnalysis.Method option : options) {
         optionsString.add(option.toString());
      }
      optionsDropdown = new JComboBox<>(optionsString);
      optionsDropdown.addActionListener(e -> {
         parent.updateItem(this);
      });
      optionsDropdown.setSelectedItem(currOption);
      this.add(optionsDropdown);

      JButton upButton = new JButton("^");
      upButton.addActionListener(e -> {
         parent.moveUp(this);
      });
      this.add(upButton);

      JButton downButton = new JButton("v");
      downButton.addActionListener(e -> {
         parent.moveDown(this);
      });
      this.add(downButton);
   }

   public SequenceGeneratorPanel(SequenceGeneratorFrame parent, ImageAnalysis.Method[] options) {
      this(parent, String.valueOf(options[0]), options);
   }

   public ImageAnalysis.Method getMethod() {
      return options[optionsDropdown.getSelectedIndex()];
   }
}

