package org.micromanager.plugins.MapPositions;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.linear.RealMatrix;
import org.micromanager.*;
import org.micromanager.internal.utils.WindowPositioning;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private final Studio studio_;
    private final JFileChooser fc = new JFileChooser();
    private final PositionListManager positionListManager;
    private final String XYStage;

    // Actual data
    private DataTable dataTable;
    private DataTable referenceTable;
    private final MatrixTransformations matTrans = new MatrixTransformations();

    // JSwing objects
    private DataPanel dataPanel;
    private DataPanel referencePanel;

    public MainFrame(Studio studio) {
        super("Droplet size controller GUI");
        super.setLayout(new MigLayout("fill, insets 2, gap 2, flowx"));
        super.setResizable(false);
        super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        studio_ = studio;
        positionListManager = studio_.getPositionListManager();
        XYStage = studio_.core().getXYStageDevice();

        configureWindows();
        configureButtons();

        super.setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/org/micromanager/icons/microscope.gif")));
        super.setLocation(100, 100);
        WindowPositioning.setUpLocationMemory(this, this.getClass(), null);
        this.pack();
    }

    private DataTable getData(Path path) {
        Object[][] data;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            data = new Object[lines.size()][];
            for (int i = 0; i < lines.size(); i++) {
                int idx = lines.get(i).indexOf(",");
                data[i] = new Object[]{
                        Double.parseDouble(lines.get(i).substring(0, idx)),
                        Double.parseDouble(lines.get(i).substring(idx + 1)),
                        i < 3 ? Boolean.TRUE : Boolean.FALSE
                };
            }
        }
        catch (Exception e) { return null; }
        return new DataTable(data, new String[]{"X", "Y", "Reference"});
    }

    private void loadData() {
        fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = Paths.get(fc.getSelectedFile().getPath());
            dataTable = getData(path);
            dataPanel.setDataTable(dataTable);
            this.revalidate();
            this.repaint();
        }
    }

    private double[][] getReferenceMatrix() {
        if (referenceTable == null) {
            return null;
        }
        return new double[][] {
                {(double) referenceTable.getValueAt(0,0), (double) referenceTable.getValueAt(1,0), (double) referenceTable.getValueAt(2,0)},
                {(double) referenceTable.getValueAt(0,1), (double) referenceTable.getValueAt(1,1), (double) referenceTable.getValueAt(2,1)},
                {1, 1, 1}
        };
    }

    private ImmutablePair<double[][], double[][]> getAlignmentMatrix() {
        if (dataTable == null) { return null; }

        int idx = 0;
        double[][] alignmentMatrix = new double[3][3];
        double[][] toMapMatrix = new double[3][dataTable.getRowCount()-3];
        for (int i = 0; i < dataTable.getRowCount(); i++) {
            if ((Boolean) dataTable.getValueAt(i, 2)) {
                alignmentMatrix[0][idx] = (double) dataTable.getValueAt(i, 0);
                alignmentMatrix[1][idx] = (double) dataTable.getValueAt(i, 1);
                alignmentMatrix[2][idx] = 1;
                idx++;
            }
            else {
                toMapMatrix[0][i-idx] = (double) dataTable.getValueAt(i, 0);
                toMapMatrix[1][i-idx] = (double) dataTable.getValueAt(i, 1);
                toMapMatrix[2][i-idx] = 1;
            }
        }

        if (idx != 3) {
            return null;
        }
        return new ImmutablePair<>(alignmentMatrix, toMapMatrix);
    }

    private void addReferencePositions() {
        PositionList positionList = positionListManager.getPositionList();
        if (positionList.getNumberOfPositions() < 3) {
            studio_.alerts().postAlert(
                    "Not enough reference points.",
                    null,
                    "Please make sure that there are 3 points marked in the position list.");
            return;
        }

        Double[][] referenceMatrix = new Double[3][2];
        for (int i = 0; i < 3; i++) {
            referenceMatrix[i][0] = positionList.getPosition(i).getX();
            referenceMatrix[i][1] = positionList.getPosition(i).getY();
        }
        referenceTable = new DataTable(referenceMatrix, new String[]{"X", "Y"});
        referencePanel.setDataTable(referenceTable);
        this.revalidate();
        this.repaint();

        positionList.clearAllPositions();
        positionListManager.setPositionList(positionList);
    }

    private void transformPositions(Boolean clearPositions) {
        double[][] referenceMatrix = getReferenceMatrix();
        if (referenceMatrix == null) {
            studio_.alerts().postAlert(
                    "No reference points loaded.",
                    null,
                    "Please load 3 reference points before adding/replacing positions");
            return;
        }

        ImmutablePair<double[][], double[][]> alignmentData = getAlignmentMatrix();
        if (alignmentData == null) {
            studio_.alerts().postAlert(
                    "No positions loaded.",
                    null,
                    "Please load at least 3 positions, and select 3 positions as reference, before adding/replacing positions");
            return;
        }

        double[][] alignmentMatrix = alignmentData.getLeft();
        double[][] toMapMatrix = alignmentData.getRight();
        RealMatrix remapMatrix = matTrans.remap(alignmentMatrix, referenceMatrix);
        toMapMatrix = matTrans.transform(toMapMatrix, remapMatrix);

        PositionList positionList = positionListManager.getPositionList();
        if (clearPositions) { positionList.clearAllPositions(); }
        for (int i = 0; i < toMapMatrix[0].length; i++) {
            MultiStagePosition temp = new MultiStagePosition();
            temp.add(StagePosition.create2D(
                    XYStage,
                    toMapMatrix[0][i],
                    toMapMatrix[1][i]
            ));
            positionList.addPosition(temp);
        }
        positionListManager.setPositionList(positionList);
    }

    private void configureWindows() {
        referencePanel = new DataPanel("Reference positions", new String[] {"X", "Y"});
        this.add(referencePanel);

        dataPanel = new DataPanel("Positions of interest", new String[] {"X", "Y", "Reference"});
        this.add(dataPanel, "wrap");
    }

    private void configureButtons() {
        JButton addReferencePositionsButton = new JButton("Get reference positions");
        addReferencePositionsButton.addActionListener(e -> addReferencePositions());
        this.add(addReferencePositionsButton, "span 2, split 4");

        JButton loadPositionsButton = new JButton("Load positions");
        loadPositionsButton.addActionListener(e -> loadData());
        this.add(loadPositionsButton);

        JButton addPositionsButton = new JButton("Add positions to PositionList");
        addPositionsButton.addActionListener(e -> transformPositions(false));
        this.add(addPositionsButton);

        JButton replacePositionsButton = new JButton("Replace positions to PositionList");
        replacePositionsButton.addActionListener(e -> transformPositions(true));
        this.add(replacePositionsButton);
    }
}

