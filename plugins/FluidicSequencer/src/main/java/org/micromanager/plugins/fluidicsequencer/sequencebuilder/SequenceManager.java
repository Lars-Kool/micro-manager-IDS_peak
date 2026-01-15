package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.micromanager.Studio;

public class SequenceManager {
   private final Studio studio;
   private final Gson gson;
   private final String savePath;
   private Map<String, Sequence> sequences;
   private final String currentSequence;

   public SequenceManager(Studio studio) {
      this.studio = studio;
      gson = new GsonBuilder().setPrettyPrinting().create();

      savePath = "Fluidic_sequences.json";
      loadFromFile(savePath);
      if (sequences == null) {
         generateEmptySequence();
      }
      currentSequence = sequences.values().iterator().next().sequenceName;
   }

   public boolean sequenceNameExists(String name) {
      return sequences.containsKey(name);
   }

   public String[] getSequenceNames() {
      return sequences.keySet().toArray(new String[0]);
   }

   public Sequence getSequence(String sequenceName) {
      return sequences.get(sequenceName).copy();
   }

   public void addSequence(Sequence sequence, boolean replace) {
      if (sequences.containsKey(sequence.sequenceName) && !replace) {
         studio.alerts().postAlert("Fluidic sequence alert", null, "Sequence name already exists.");
         return;
      }
      sequences.put(sequence.sequenceName, sequence);
      save();
   }

   public void removeSequence(String sequenceName) {
      sequences.remove(sequenceName);
      save();
   }

   public void save() {
      saveToFile(savePath);
   }

   private void loadFromFile(String path) {
      File file = new File(path);
      if (!file.exists()) {
         return;
      }

      StringBuilder contents = new StringBuilder();
      try (Scanner reader = new Scanner(file)) {
         while (reader.hasNextLine()) {
            contents.append(reader.nextLine());
         }
      } catch (Exception e) {
         studio.getLogManager().logMessage("Error while reading: " + path);
         studio.getLogManager().logError(e);
         return;
      }

      if (contents.toString().isEmpty()) {
         return;
      }

      Type type = new TypeToken<Map<String, Sequence>>() {
      }.getType();
      sequences = gson.fromJson(contents.toString(), type);
   }

   private void saveToFile(String path) {
      File file = new File(path);
      // Delete existing file
      if (file.exists()) {
         if (!file.delete()) {
            studio.getLogManager().logMessage("Could not edit old config.");
            return;
         }
      }

      // Create new file
      try {
         if (!file.createNewFile()) {
            studio.getLogManager().logMessage("File already exists and cannot be overwritten.");
            return;
         }
      } catch (Exception e) {
         studio.getLogManager().logMessage("Could not open file.");
         studio.getLogManager().logError(e);
      }

      // Save file
      String content = gson.toJson(sequences);
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
         writer.write(content);
      } catch (Exception e) {
         studio.getLogManager().logMessage("Could not write to file.");
         studio.getLogManager().logError(e);
      }
   }

   private void generateEmptySequence() {
      sequences = new HashMap<>();
      sequences.put("Unnamed", new Sequence("Unnamed"));
   }
}
