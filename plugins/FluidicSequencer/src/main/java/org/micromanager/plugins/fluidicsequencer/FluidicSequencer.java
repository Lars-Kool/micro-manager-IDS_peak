package org.micromanager.plugins.fluidicsequencer;

import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

@Plugin(type = MenuPlugin.class)
public class FluidicSequencer implements SciJavaPlugin, MenuPlugin {
   private Studio studio_;
   private SequencerFrame frame_;

   @Override
   public void setContext(Studio studio) {
      studio_ = studio;
   }

   @Override
   public void onPluginSelected() {
      if (frame_ == null) {
        // We have never before shown our GUI, so now we need to create it.
         frame_ = new SequencerFrame(studio_);
      }
      frame_.setVisible(true);
   }

   @Override
   public String getSubMenu() {
      return "Developer Tools";
   }

   @Override
   public String getName() {
      return "Fluidic Sequencer";
   }

   @Override
   public String getHelpText() {
      return "Tool to develop and execute a sequence of fluidic controls.";
   }

   @Override
   public String getVersion() {
      return "0.1";
   }

   @Override
   public String getCopyright() {
      return "Institut Pierre-Gilles de Gennes (IPGG), 2025-2026";
   }
}
