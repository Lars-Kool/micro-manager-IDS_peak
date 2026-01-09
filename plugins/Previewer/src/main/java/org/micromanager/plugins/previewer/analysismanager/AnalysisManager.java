package org.micromanager.plugins.previewer.analysismanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import org.micromanager.LogManager;
import org.micromanager.Studio;

public class AnalysisManager {
   private final HashSet<String> sequenceNames;
   private final Gson gson;
   private final Studio studio;
   private final LogManager logManager;
   private final PropertyChangeSupport support;
   private ArrayList<AnalysisSequence> sequences;
   private AnalysisSequence currentSequence;

   public AnalysisManager(Studio studio) {
      this.studio = studio;
      logManager = studio.getLogManager();
      gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
      support = new PropertyChangeSupport(this);

      sequences = new ArrayList<>();
      sequences = loadFromFile("AnalysisSequences.json");
      if (sequences == null) {
         sequences = generateEmptySequence();
      }
      verifyParameters();
      sequenceNames = getSequenceNames();
      currentSequence = sequences.get(0);
   }

   public void addListener(PropertyChangeListener listener) {
      support.addPropertyChangeListener(listener);
   }

   public void removeListener(PropertyChangeListener listener) {
      support.removePropertyChangeListener(listener);
   }

   private ArrayList<AnalysisSequence> generateEmptySequence() {
      ArrayList<AnalysisSequence> temp = new ArrayList<>();
      temp.add(new AnalysisSequence("Do nothing"));
      return temp;
   }

   private void verifyParameters() {
      for (AnalysisSequence seq : sequences) {
         for (AnalysisStep step : seq.steps) {
            if (!step.hasParameters()) {
               continue;
            }
            for (AnalysisParameter parameter : step.parameters) {
               if (parameter.getType() == AnalysisParameter.ParameterType.INTEGER) {
                  parameter.setValue(((Double) parameter.getValue()).intValue());
               }
            }
            step.isBinary = ImageAnalysis.binaryMethods.contains(step.method);
         }
         seq.updateReferences();
      }
   }

   private ArrayList<AnalysisSequence> loadFromFile(String path) {
      File file = new File(path);
      if (!file.exists()) {
         return null;
      }

      StringBuilder contents = new StringBuilder();
      try (Scanner reader = new Scanner(file)) {
         while (reader.hasNextLine()) {
            contents.append(reader.nextLine());
         }
      } catch (Exception e) {
         studio.getLogManager().logError(e);
         return null;
      }

      // Defining correct type for Gson to read (ArrayList<AnalysisSequence>) is unstable
      // in Gson. Instead, read as immutable array, and use that to initialize the mutable
      // list.
      Gson gson = new Gson();
      gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
      if (contents.toString().isEmpty()) {
         return null;
      }
      AnalysisSequence[] temp = gson.fromJson(contents.toString(), AnalysisSequence[].class);
      ArrayList<AnalysisSequence> analysisSequences = new ArrayList<>(Arrays.asList(temp));
      return analysisSequences;
   }

   private HashSet<String> getSequenceNames() {
      HashSet<String> temp = new HashSet<>();
      for (AnalysisSequence seq : sequences) {
         temp.add(seq.sequenceName);
      }
      return temp;
   }

   public void saveToFile() {
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
      try (BufferedWriter writer = new BufferedWriter(new FileWriter("AnalysisSequences.json"))) {
         writer.write(content);
      } catch (Exception e) {
         logManager.logMessage("Previewer exception: Could not save to file.");
         logManager.logMessage(Arrays.toString(e.getStackTrace()));
      }
   }

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
      support.firePropertyChange("AnalysisManager", null, this);
      return true;
   }

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
      support.firePropertyChange("AnalysisManager", null, this);
      return true;
   }

   public ArrayList<AnalysisSequence> getSequences() {
      return sequences;
   }

   public AnalysisSequence getSequence(int i) {
      return sequences.get(i);
   }

   public AnalysisSequence getCurrentSequence() {
      return currentSequence;
   }

   public void setCurrentSequence(int i) {
      currentSequence = sequences.get(i);
   }

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
      support.firePropertyChange("AnalysisManager", null, this);
      return true;
   }

   public int size() {
      return sequences.size();
   }

   public ArrayList<int[]> getImageStack(int[] src, int width, int height) {
      ArrayList<int[]> imgs = new ArrayList<>();
      int[] img = src.clone();
      imgs.add(img.clone());
      for (int i = 0; i < currentSequence.steps.size(); i++) {
         if (currentSequence.references.containsKey(i)) {
            for (int forward : currentSequence.references.get(i)) {
               currentSequence.steps.get(forward).img = img.clone();
            }
         }
         imgs.add(currentSequence.steps.get(i).executeStep(img, width, height, true).clone());
      }
      return imgs;
   }

   public int[] analyze(int[] src, int width, int height) {
      currentSequence.execute(src, width, height);
      return src;
   }

   public void setParameterValue(AnalysisParameter parameter, Object value) {
      if (parameter.getValue() == value) {
         return;
      }
      parameter.setValue(value);
      currentSequence.updateReferences();
      support.firePropertyChange("Parameter changed", null, value);
   }
}