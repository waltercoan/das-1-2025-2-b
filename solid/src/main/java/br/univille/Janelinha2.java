package br.univille;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class Janelinha2 extends JFrame{
    private JTable tabelinha;
    
    public Janelinha2() {
        setTitle("Tabela");
        setSize(500,500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var modelo = new ModeloTabela();

        tabelinha = new JTable(modelo);
        add(new JScrollPane(tabelinha));
        setVisible(true);
    }
    public static void main(String[] args) {
        new Janelinha2();
    }

}
