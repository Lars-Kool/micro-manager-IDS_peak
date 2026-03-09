package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/*
   Events:
   - Parameter changed: AnalysisParameter changed value
      source: AnalysisParameter that changed value
      oldValue: previous value
      newValue: updated value
   - Step changed: One of the parameters of this step changed
      source: AnalysisStep that changed
      oldValue: null
      newValue: null
   - Sequence changed: One of the steps in this sequence changed
      source: AnalysisSequence that changed
      oldValue: null
      newValue: null
   - Sequence list changed: AnalysisManager updated the list of sequences
      source: AnalysisManager
      oldValue: null
      newValue: null
 */

/**
 * Singleton class that handles events for the RT Image Analysis package
 */
public class SequencerEventHandler {
   private static SequencerEventHandler INSTANCE;
   List<WeakReference<PropertyChangeListener>> listeners;

   // Private constructor
   private SequencerEventHandler() {
      listeners = new ArrayList<>();
   }

   // Static getter of instance
   public static SequencerEventHandler getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new SequencerEventHandler();
      }
      return INSTANCE;
   }

   /**
    * Subscribe listener to events
    *
    * @param listener PropertyChangeListener to subscribe
    */
   public void addListener(PropertyChangeListener listener) {
      listeners.add(new WeakReference<>(listener));
   }

   /**
    * Unsubscribe listener to events
    *
    * @param listener PropertyChangeListener to unsubscribe
    */
   public void removeListener(PropertyChangeListener listener) {
      for (int i = 0; i < listeners.size(); i++) {
         if (listeners.get(i).get() == listener) {
            listeners.remove(i);
            break;
         }
      }
   }

   /**
    * Fire event. If there is no evident way to pass an oldValue and/or newValue, null can be
    * passed instead.
    *
    * @param name     Name of event, main way to check if event is relevant
    * @param source   Object firing the event. Allows to act only on relevent objects
    * @param oldValue Value to be replaced (can be null)
    * @param newValue New value (can be null)
    */
   public void firePropertyChange(String name, Object source, Object oldValue, Object newValue) {
      PropertyChangeEvent pce = new PropertyChangeEvent(source, name, oldValue, newValue);
      for (int i = 0; i < listeners.size(); i++) {
         PropertyChangeListener pcl = listeners.get(i).get();
         if (pcl != null) {
            pcl.propertyChange(pce);
         }
      }
   }
}
