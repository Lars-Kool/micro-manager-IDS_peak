package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;

public class BuilderPanel extends JPanel {
   private final Studio studio;
   ArrayList<ActionPanel> actionPanels;

   public BuilderPanel(Studio studio) {
      this.setLayout(new MigLayout());
      this.studio = studio;
   }
}
