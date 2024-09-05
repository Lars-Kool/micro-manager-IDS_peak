package org.micromanager.plugins.MapPositions;

import javax.swing.table.AbstractTableModel;

public class DataTable extends AbstractTableModel {
    private final String[] columnNames;
    private Object[][] data;

    DataTable(double[][] data, String[] columnNames) {
        this.columnNames = columnNames;
        this.data = new Object[0][0];
    }

    DataTable(Object[][] data, String[] columnNames) {
        this.columnNames = columnNames;
        this.data = new Object[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                this.data[i][j] = data[i][j];
            }
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a checkbox.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }


}
