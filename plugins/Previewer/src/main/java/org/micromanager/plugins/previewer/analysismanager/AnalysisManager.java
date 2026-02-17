package org.micromanager.plugins.previewer.analysismanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import org.micromanager.LogManager;
import org.micromanager.Studio;

/**
 * Singleton class of manager for Image Analysis
 */
public class AnalysisManager implements PropertyChangeListener {
   private static AnalysisManager INSTANCE;
   private final ImageAnalysisEventHandler eventHandler;

   private final ArrayList<String> sequenceNames;
   private final Gson gson;
   private final Studio studio;
   private final LogManager logManager;
   private ArrayList<AnalysisSequence> sequences;

   /**
    * Private constructor
    *
    * @param studio Micro-Manager Studio instance
    */
   private AnalysisManager(Studio studio) {
      this.studio = studio;
      logManager = studio.getLogManager();
      gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
      sequences = new ArrayList<>();
      sequences = loadFromFile("AnalysisSequences.json");
      if (sequences == null) {
         generateEmptySequence();
      }
      verifyParameters();
      sequenceNames = getSequenceNames();

      eventHandler = ImageAnalysisEventHandler.getInstance();
   }

   /**
    * Getter of AnalysisManager instance.
    *
    * @param studio Micro-Manager Studio instance
    *
    * @return AnalysisManager instance
    */
   public static AnalysisManager getInstance(Studio studio) {
      if (INSTANCE == null) {
         INSTANCE = new AnalysisManager(studio);
      }
      return INSTANCE;
   }

   /**
    * Resets 'sequences' to an empty sequence (containing a single 'Do nothing' sequence)
    */
   private void generateEmptySequence() {
      sequences = new ArrayList<>();
      sequences.add(new AnalysisSequence("Do nothing"));
      saveToFile();
   }

   /**
    * This method verifies the types of parameters after Gson loads them from a json file.
    * Gson loads every number as Double, but some methods require casting to Integer, which Java
    * cannot do implicitly. Hence, we need to explicitly cast Double instances to Integer
    * instances where required.
    */
   private void verifyParameters() {
      for (AnalysisSequence seq : sequences) {
         for (AnalysisStep step : seq.steps) {
            if (!step.hasParameters()) {
               continue;
            }
            step.isBinary = ImageAnalysis.binaryMethods.contains(step.method);
            for (AnalysisParameter parameter : step.parameters) {
               if (parameter.getType() == AnalysisParameter.ParameterType.INTEGER) {
                  parameter.setValue(((Double) parameter.getValue()).intValue());
               }
            }
         }
      }
   }

   /**
    * Loads sequences from file. If no file can be found, or an exception is thrown, it loads an
    * empty sequence instead.
    *
    * @param path Path to json with sequences
    */
   private void loadFromFile(String path) {
      File file = new File(path);
      if (!file.exists()) {
         generateEmptySequence();
         return;
      }

      StringBuilder contents = new StringBuilder();
      try (Scanner reader = new Scanner(file)) {
         while (reader.hasNextLine()) {
            contents.append(reader.nextLine());
         }
      } catch (Exception e) {
         studio.getLogManager().logError(e);
         generateEmptySequence();
         return;
      }

      // Defining correct type for Gson to read (ArrayList<AnalysisSequence>) is unstable
      // in Gson. Instead, read as immutable array, and use that to initialize the mutable
      // list.
      Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
      if (contents.toString().isEmpty()) {
         generateEmptySequence();
         return;
      }
      AnalysisSequence[] temp = gson.fromJson(contents.toString(), AnalysisSequence[].class);
      sequences = new ArrayList<>(Arrays.asList(temp));
   }

   /**
    * Returns a list of sequence names.
    *
    * @return
    */
   public ArrayList<String> getSequenceNames() {
      ArrayList<String> temp = new ArrayList<>();
      for (AnalysisSequence seq : sequences) {
         temp.add(seq.sequenceName);
      }
      return temp;
   }

   /**
    * Saves ArrayList of sequences to a json file using Gson.
    */
   private void saveToFile() {
      File file = new File("AnalysisSequences.json");
      // Delete existing file
      if (file.exists()) {
         if (!file.delete()) {
            logManager.logMessage("Previewer error: Could not edit old config.");
            return;
         }
      }

      // Create new file
      try {
         if (!file.createNewFile()) {
            logManager.logMessage("Previewer error: Could not open file.");
            return;
         }
      } catch (Exception e) {
         logManager.logMessage("Previewer exception: Could not save file");
         logManager.logMessage(Arrays.toString(e.getStackTrace()));
         return;
      }

      String content = gson.toJson(sequences);
      if (Objects.equals(content, "null")) {
         studio.getLogManager().logMessage("Could not serialize sequences.");
         return;
      }
      try (BufferedWriter writer = new BufferedWriter(new FileWriter("AnalysisSequences.json"))) {
         writer.write(content);
      } catch (Exception e) {
         logManager.logMessage("Previewer exception: Could not save to file.");
         logManager.logMessage(Arrays.toString(e.getStackTrace()));
      }
   }

   /**
    * Appends sequence to list of sequences.
    *
    * @param seq AnalysisSequence to be appended.
    *
    * @return Boolean flag indicating success (true == success, false == fail)
    */
   public boolean addSequence(AnalysisSequence seq) {
      if (sequenceNames.contains(seq.sequenceName)) {
         studio.alerts().postAlert("Sequence Manager alert", null, "Sequence name already exists.");
         return false;
      }
      if (seq.sequenceName.isEmpty()) {
         studio.alerts().postAlert("Sequence Manager alert", null, "Sequence name cannot be empty"
               + ".");
         return false;
      }
      this.sequences.add(seq);
      saveToFile();
      eventHandler.firePropertyChange("Sequence list changed", this, null, null);
      return true;
   }

   /**
    * Removes sequence from list of sequences
    *
    * @param idx Index of sequence to be removed
    *
    * @return Boolean flag indicating success (true == success, false == fail)
    */
   public boolean removeSequence(int idx) {
      if (idx < 0) {
         studio.alerts().postAlert("Sequence Manager alert.", null, "New sequence cannot be "
               + "deleted. Simply click 'Cancel' to go back.");
         return false;
      }
      if (idx >= sequences.size()) {
         logManager.logMessage("Sequence Manager warning: Provided sequence id out of range.");
         return false;
      }
      this.sequences.remove(idx);
      saveToFile();
      eventHandler.firePropertyChange("Sequence list changed", this, null, null);
      return true;
   }

   /**
    * Overwrites sequence at index i with AnalysisSequence seq
    *
    * @param i   Index of sequence to be overwritten
    * @param seq Analysis sequence to be added
    *
    * @return Boolean flag indicating success (true == success, false == fail)
    */
   public boolean setSequence(int i, AnalysisSequence seq) {
      if (sequenceNames.contains(seq.sequenceName)
            && !Objects.equals(sequences.get(i).sequenceName, seq.sequenceName)) {
         studio.alerts().postAlert("Sequence Manager alert", null, "Sequence name already exists.");
         return false;
      }
      if (seq.sequenceName.isEmpty()) {
         studio.alerts().postAlert("Sequence Manager alert", null, "Sequence name cannot be empty"
               + ".");
         return false;
      }
      sequences.set(i, seq);
      saveToFile();
      eventHandler.firePropertyChange("Sequence list changed", this, null, null);
      return true;
   }

   // TODO: Should we return a copy (which will be inaccessible, and thus immutable) ?

   /**
    * Getter for AnalysisSequence from list
    *
    * @param i Index of sequence to be retrieved
    *
    * @return AnalysisSequence requested
    *
    * @throws ArrayIndexOutOfBoundsException when i is out of bounds with respect to the
    *                                        sequences array.
    */
   public AnalysisSequence getSequence(int i) {
      return sequences.get(i);
   }

   /**
    * Get size of sequences list
    *
    * @return Number of sequences in list
    */
   public int size() {
      return sequences.size();
   }

   /**
    * ImageAnalysisEventHandler calls this method when an event is fired.
    *
    * @param evt A PropertyChangeEvent object describing the event source
    *            and the property that has changed.
    */
   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      switch (evt.getPropertyName()) {
         case "Closing":
            saveToFile();
            break;
         default:
            break;
      }
   }
}