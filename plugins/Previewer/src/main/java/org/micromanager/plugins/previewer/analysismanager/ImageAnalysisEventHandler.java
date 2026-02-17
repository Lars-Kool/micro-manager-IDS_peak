package org.micromanager.plugins.previewer.analysismanager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class that handles events for the RT Image Analysis package
 */
public class ImageAnalysisEventHandler {
   private static ImageAnalysisEventHandler INSTANCE;
   List<WeakReference<PropertyChangeListener>> listeners;

   // Private constructor
   private ImageAnalysisEventHandler() {
      listeners = new ArrayList<>();
   }

   // Static getter of instance
   public static ImageAnalysisEventHandler getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ImageAnalysisEventHandler();
      }
      return INSTANCE;
   }

   /**
    * Subscribe listener to events
    *
    * @param listener
    */
   public void addListener(PropertyChangeListener listener) {
      listeners.add(new WeakReference<>(listener));
   }

   /**
    * Unsubscribe listener to events
    *
    * @param listener
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
