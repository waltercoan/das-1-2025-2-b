package br.univille;

import javax.swing.table.AbstractTableModel;

public class ModeloTabela extends AbstractTableModel{

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
       
        return true;
    }

    @Override
    public int getRowCount() {
        return 5;
    }

    @Override
    public int getColumnCount() {
        return 10;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return "Java lindo";
    }

}
