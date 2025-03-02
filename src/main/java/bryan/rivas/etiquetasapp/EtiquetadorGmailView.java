package bryan.rivas.etiquetasapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class EtiquetadorGmailView extends JFrame {
    private final EtiquetadorGmail etiquetador = new EtiquetadorGmail();

    public EtiquetadorGmailView() {
        setTitle("Administrador de Etiquetas de Gmail");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        initUI();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        String[] columnNames = {"Asunto", "Remitente", "Etiqueta"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);

        JButton etiquetarButton = new JButton("Etiquetar Correos");
        etiquetarButton.addActionListener(e -> etiquetarCorreos(tableModel)); // Acción del botón

        JPanel panel = new JPanel();
        panel.add(etiquetarButton);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
    }

    private void etiquetarCorreos(DefaultTableModel tableModel) {
        SwingWorker<Map<String, List<String>>, Integer> worker = new SwingWorker<>() {
            @Override
            protected Map<String, List<String>> doInBackground() throws Exception {
                etiquetador.etiquetarCorreos();
                return etiquetador.obtenerCorreosEtiquetados();
            }

            @Override
            protected void done() {
                try {
                    Map<String, List<String>> correos = get();
                    agregarFilasATabla(tableModel, correos);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(EtiquetadorGmailView.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void agregarFilasATabla(DefaultTableModel tableModel, Map<String, List<String>> correos) {
        for (Map.Entry<String, List<String>> entry : correos.entrySet()) {
            String etiqueta = entry.getKey();
            for (String info : entry.getValue()) {
                String[] partes = info.split(" \\| ");
                tableModel.addRow(new Object[]{
                        partes[0].replace("Asunto: ", "").trim(),
                        partes[1].replace("Remitente: ", "").trim(),
                        etiqueta
                });
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new EtiquetadorGmailView().setVisible(true));
    }
}
