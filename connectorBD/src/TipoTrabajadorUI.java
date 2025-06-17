import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TipoTrabajadorUI extends JFrame {
    private JTextField codField, nombreField;
    private JComboBox<String> estadoBox;
    private JTable table;
    private DefaultTableModel model;

    private final String DB_URL = "jdbc:mysql://localhost:3306/LABBDCASO11";
    private final String DB_USER = "root";
    private final String DB_PASS = "UJKMjuandi128980";

    public TipoTrabajadorUI() {
        setTitle("r1z_tipo_trabajador");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel de entrada de datos
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Registro de Trabajadores"));

        codField = new JTextField(5);
        nombreField = new JTextField(15);
        estadoBox = new JComboBox<>(new String[]{"A", "I", "E"});  // Activo, Inactivo, Eliminado

        inputPanel.add(new JLabel("Código:"));
        inputPanel.add(codField);
        inputPanel.add(new JLabel("Nombre:"));
        inputPanel.add(nombreField);
        inputPanel.add(new JLabel("Estado:"));
        inputPanel.add(estadoBox);

        add(inputPanel, BorderLayout.NORTH);

        // Formatear el código a dos dígitos al perder foco
        codField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String txt = codField.getText().trim();
                if (!txt.isEmpty()) {
                    try {
                        int n = Integer.parseInt(txt);
                        codField.setText(String.format("%02d", n));
                    } catch (NumberFormatException ignored) { }
                }
            }
        });

        // Tabla de datos
        model = new DefaultTableModel(new String[]{"Código", "Nombre", "Estado"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Cargar datos al iniciar y permitir selección automática
        cargarDatos();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = table.getSelectedRow();
                if (fila >= 0) {
                    codField.setText(model.getValueAt(fila, 0).toString());
                    nombreField.setText(model.getValueAt(fila, 1).toString());
                    estadoBox.setSelectedItem(model.getValueAt(fila, 2).toString());
                }
            }
        });

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        String[] botones = {"Adicionar", "Actualizar", "Eliminar", "Activar(A)", "Desactivar(I)", "Eliminador(E)", "Salir"};
        for (String txt : botones) {
            JButton btn = new JButton(txt);
            btn.addActionListener(this::buttonClicked);
            buttonPanel.add(btn);
        }
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void cargarDatos() {
        model.setRowCount(0);
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TipTraCod, TipTraNom, TipTraRegEst FROM r1z_tipo_trabajador")) {

            while (rs.next()) {
                String cod = String.format("%02d", rs.getInt("TipTraCod"));
                model.addRow(new Object[]{cod, rs.getString("TipTraNom"), rs.getString("TipTraRegEst")});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage());
        }
    }

    private void buttonClicked(ActionEvent e) {
    String action = ((JButton) e.getSource()).getText();

    try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
        if ("Salir".equals(action)) {
            dispose();
            return;
        }

        String codTxt = codField.getText().trim();
        if (codTxt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Código vacío.");
            return;
        }
        int cod = Integer.parseInt(codTxt);
        String cod2 = String.format("%02d", cod);
        String nombre = nombreField.getText().trim();
        String estado = estadoBox.getSelectedItem().toString();

        PreparedStatement ps;

        switch (action) {
            case "Adicionar" -> {
                ps = con.prepareStatement("INSERT INTO r1z_tipo_trabajador (TipTraCod, TipTraNom, TipTraRegEst) VALUES (?, ?, ?)");
                ps.setInt(1, cod);
                ps.setString(2, nombre);
                ps.setString(3, estado);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registro agregado exitosamente.");
        }

            case "Actualizar" -> {
    // Primero, obtener el estado actual del registro
    PreparedStatement psCheck = con.prepareStatement("SELECT TipTraRegEst FROM r1z_tipo_trabajador WHERE TipTraCod = ?");
    psCheck.setInt(1, cod);
    ResultSet rs = psCheck.executeQuery();

    if (rs.next()) {
        String estadoActual = rs.getString("TipTraRegEst");

        // Si el estado NO es activo
        if (!estadoActual.equals("A")) {
            int opcion = JOptionPane.showConfirmDialog(this,
                "El registro no está activo. ¿Deseas activarlo para poder actualizarlo?",
                "Confirmación de Activación",
                JOptionPane.YES_NO_OPTION);

            if (opcion == JOptionPane.YES_OPTION) {
    // Reactivar el registro (cambiar estado a 'A')
                PreparedStatement psActivar = con.prepareStatement("UPDATE r1z_tipo_trabajador SET TipTraRegEst = 'A' WHERE TipTraCod = ?");
                psActivar.setInt(1, cod);
                psActivar.executeUpdate();

    // También actualizar el estado en el input visual
                estadoBox.setSelectedItem("A");
                estado = "A"; // <-- Asegura que se use el estado correcto al actualizar
            } else {
                JOptionPane.showMessageDialog(this, "Actualización cancelada.");
                break;
            }

        }

        // Proceder con la actualización
        ps = con.prepareStatement("UPDATE r1z_tipo_trabajador SET TipTraNom = ?, TipTraRegEst = ? WHERE TipTraCod = ?");
        ps.setString(1, nombre);
        ps.setString(2, estado); // Ya será 'A' si fue reactivado
        ps.setInt(3, cod);
        ps.executeUpdate();

        JOptionPane.showMessageDialog(this, "Registro actualizado correctamente.");
    } else {
        JOptionPane.showMessageDialog(this, "Código no encontrado.");
    }
}

            case "Eliminar" -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "¿Está seguro de que desea eliminar este registro?",
                        "Confirmar Eliminación",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    ps = con.prepareStatement("DELETE FROM r1z_tipo_trabajador WHERE TipTraCod = ?");
                    ps.setInt(1, cod);
                    ps.executeUpdate();
                } else {
                    return;
                }
            }
            case "Activar" -> actualizarEstado(con, cod, "A");
            case "Desactivar" -> actualizarEstado(con, cod, "I");
            case "Eliminador" -> actualizarEstado(con, cod, "E");
        }

        cargarDatos();
        limpiarCampos();
        codField.setText(cod2);
    } catch (SQLException | NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
    }
}


    private void actualizarEstado(Connection con, int cod, String est) throws SQLException {
        PreparedStatement ps = con.prepareStatement("UPDATE r1z_tipo_trabajador SET TipTraRegEst = ? WHERE TipTraCod = ?");
        ps.setString(1, est);
        ps.setInt(2, cod);
        ps.executeUpdate();
    }

    private void limpiarCampos() {
        codField.setText("");
        nombreField.setText("");
        estadoBox.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TipoTrabajadorUI::new);
    }
}
