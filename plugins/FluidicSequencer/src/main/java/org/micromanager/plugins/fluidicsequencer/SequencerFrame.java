package org.micromanager.plugins.fluidicsequencer;

import java.awt.Toolkit;
import java.util.ArrayList;
import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import org.micromanager.Studio;
import org.micromanager.internal.utils.WindowPositioning;
import org.micromanager.plugins.fluidicsequencer.sequencebuilder.FluidicSequence;

public class SequencerFrame extends JFrame {
   private final Studio studio;
   private ArrayList<FluidicSequence> sequences;


   public SequencerFrame(Studio studio) {
      this.setTitle("Fluidic Sequencer");
      this.setLayout(new MigLayout());
      this.studio = studio;

      super.setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/org/micromanager/icons/microscope.gif")));
      super.setLocation(100, 100);
      WindowPositioning.setUpLocationMemory(this, this.getClass(), null);

      super.pack();
   }
}
