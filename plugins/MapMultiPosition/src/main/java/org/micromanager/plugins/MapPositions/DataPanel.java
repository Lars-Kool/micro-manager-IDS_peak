package org.micromanager.plugins.MapPositions;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class DataPanel extends JPanel {
    private DataTable dataTable;
    JScrollPane scrollPane;

    DataPanel(String title, String[] columns) {
        this.setBorder(BorderFactory.createTitledBorder("Light power"));
        this.setLayout(new MigLayout("fill, insets 2, gap 2, flowx"));
        this.setBorder(BorderFactory.createTitledBorder(title));

        dataTable = new DataTable(new double[0][0], columns);
        JTable table = new JTable(dataTable);
        scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        this.add(scrollPane);
    }

    DataPanel(String title, DataTable dataTable) {
        this.setBorder(BorderFactory.createTitledBorder("Light power"));
        this.setLayout(new MigLayout("fill, insets 2, gap 2, flowx"));
        this.setBorder(BorderFactory.createTitledBorder(title));

        this.dataTable = dataTable;
        JTable table = new JTable(dataTable);
        scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        this.add(scrollPane);
    }

    private void updateScrollPane() {
        if (scrollPane != null) {
            this.remove(scrollPane);
        }
        JTable table = new JTable(dataTable);
        scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        this.add(scrollPane);
    }



    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
        updateScrollPane();
    }
}
