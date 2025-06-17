import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TrabajadorTipo extends JFrame {
    private JTextField codField, nombreField;
    private JComboBox<String> estadoBox;
    private JTable table;
    private DefaultTableModel model;

    private final String DB_URL = "jdbc:mysql://localhost:3306/LABBDCASO11";
    private final String DB_USER = "root";
    private final String DB_PASS = "UJKMjuandi128980";

    public TrabajadorTipo() {
        setTitle("Gestión Tipo Trabajador");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel superior: entrada de datos
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Datos"));

        codField = new JTextField(5);
        nombreField = new JTextField(15);
        estadoBox = new JComboBox<>(new String[]{"A", "I", "*"});  // Activo, Inactivo, Eliminado

        inputPanel.add(new JLabel("Código:"));
        inputPanel.add(codField);
        inputPanel.add(new JLabel("Nombre:"));
        inputPanel.add(nombreField);
        inputPanel.add(new JLabel("Estado:"));
        inputPanel.add(estadoBox);

        add(inputPanel, BorderLayout.NORTH);

        // Tabla
        model = new DefaultTableModel(new String[]{"Código", "Nombre", "Estado"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(1, 7, 5, 5));
        String[] botones = {"Adicionar", "Actualizar", "Eliminar", "Activar", "Desactivar", "Eliminador", "Salir"};
        for (String b : botones) {
            JButton btn = new JButton(b);
            buttonPanel.add(btn);
            btn.addActionListener(this::buttonClicked);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        cargarDatos();
        setVisible(true);
    }

    private void cargarDatos() {
        model.setRowCount(0);
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM r1z_tipo_trabajador")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("TipTraCod"),
                        rs.getString("TipTraNom"),
                        rs.getString("TipTraRegEst")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buttonClicked(ActionEvent e) {
        String action = ((JButton) e.getSource()).getText();
        int cod = Integer.parseInt(codField.getText().trim());
        String nombre = nombreField.getText().trim();
        String estado = estadoBox.getSelectedItem().toString();

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps;
            switch (action) {
                case "Adicionar":
                    ps = con.prepareStatement("INSERT INTO r1z_tipo_trabajador (TipTraCod, TipTraNom, TipTraRegEst) VALUES (?, ?, ?)");
                    ps.setInt(1, cod);
                    ps.setString(2, nombre);
                    ps.setString(3, estado);
                    ps.executeUpdate();
                    break;
                case "Actualizar":
                    ps = con.prepareStatement("UPDATE r1z_tipo_trabajador SET TipTraNom = ?, TipTraRegEst = ? WHERE TipTraCod = ?");
                    ps.setString(1, nombre);
                    ps.setString(2, estado);
                    ps.setInt(3, cod);
                    ps.executeUpdate();
                    break;
                case "Eliminar":
                    ps = con.prepareStatement("DELETE FROM r1z_tipo_trabajador WHERE TipTraCod = ?");
                    ps.setInt(1, cod);
                    ps.executeUpdate();
                    break;
                case "Activar":
                    actualizarEstado(con, cod, "A");
                    break;
                case "Desactivar":
                    actualizarEstado(con, cod, "I");
                    break;
                case "Eliminador":
                    actualizarEstado(con, cod, "*");
                    break;
                case "Salir":
                    dispose();
                    return;
            }
            cargarDatos();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void actualizarEstado(Connection con, int cod, String nuevoEstado) throws SQLException {
        PreparedStatement ps = con.prepareStatement("UPDATE r1z_tipo_trabajador SET TipTraRegEst = ? WHERE TipTraCod = ?");
        ps.setString(1, nuevoEstado);
        ps.setInt(2, cod);
        ps.executeUpdate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TrabajadorTipo::new);
    }
}
