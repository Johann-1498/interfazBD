import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PrestamoTipo extends JFrame {

    private JTextField txtCodigo, txtNombre;
    private JComboBox<String> comboEstado;
    private JTable tabla;
    private DefaultTableModel modelo;
    private Connection conexion;

    public PrestamoTipo() {
        setTitle("Gestión de Tipo de Préstamo");
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel superior con campos
        JPanel panelCampos = new JPanel(new GridLayout(3, 2, 5, 5));
        panelCampos.setBorder(BorderFactory.createTitledBorder("Datos del Tipo de Préstamo"));

        txtCodigo = new JTextField(10);
        txtNombre = new JTextField(15);
        comboEstado = new JComboBox<>();

        panelCampos.add(new JLabel("Código:"));
        panelCampos.add(txtCodigo);
        panelCampos.add(new JLabel("Nombre:"));
        panelCampos.add(txtNombre);
        panelCampos.add(new JLabel("Estado:"));
        panelCampos.add(comboEstado);

        add(panelCampos, BorderLayout.NORTH);

        // Panel central con botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout());

        String[] etiquetas = {
            "Adicionar", "Actualizar", "Eliminar",
            "Activar", "Desactivar", "Eliminador", "Salir"
        };

        for (String texto : etiquetas) {
            JButton boton = new JButton(texto);
            boton.setPreferredSize(new Dimension(110, 25));
            panelBotones.add(boton);

            switch (texto) {
                case "Adicionar" -> boton.addActionListener(e -> adicionar());
                case "Actualizar" -> boton.addActionListener(e -> actualizar());
                case "Eliminar" -> boton.addActionListener(e -> eliminarFisico());
                case "Activar" -> boton.addActionListener(e -> cambiarEstado("1"));
                case "Desactivar" -> boton.addActionListener(e -> cambiarEstado("0"));
                case "Eliminador" -> boton.addActionListener(e -> cambiarEstado("2"));
                case "Salir" -> boton.addActionListener(e -> dispose());
            }
        }

        add(panelBotones, BorderLayout.CENTER);

        // Tabla inferior
        modelo = new DefaultTableModel(new String[]{"Código", "Nombre", "Estado"}, 0);
        tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(600, 150));
        add(scroll, BorderLayout.SOUTH);

        conectarBD();
        cargarEstados();
        cargarTabla();
    }

    private void conectarBD() {
        try {
            conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/LABBDCASO11", "root", "UJKMjuandi128980"
            );
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al conectar BD: " + e.getMessage());
            System.exit(1);
        }
    }

    private void cargarEstados() {
        comboEstado.removeAllItems();
        String sql = "SELECT EstRegCod FROM gzz_estado_registro";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comboEstado.addItem(rs.getString("EstRegCod"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar estados: " + e.getMessage());
        }
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        String sql = "SELECT * FROM f2z_tipo_prestamo";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("TipoPreCod"),
                    rs.getString("TipPreNom"),
                    rs.getString("PreRegTipEst")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar tabla: " + e.getMessage());
        }
    }

    private void adicionar() {
        try {
            String cod = txtCodigo.getText().trim();
            String nom = txtNombre.getText().trim();
            String est = (String) comboEstado.getSelectedItem();

            if (cod.isEmpty() || nom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe completar todos los campos.");
                return;
            }

            String sql = "INSERT INTO f2z_tipo_prestamo (TipoPreCod, TipPreNom, PreRegTipEst) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(cod));
                ps.setString(2, nom);
                ps.setString(3, est);
                ps.executeUpdate();
            }
            cargarTabla();
            JOptionPane.showMessageDialog(this, "Registro añadido.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al insertar: " + e.getMessage());
        }
    }

    private void actualizar() {
        try {
            String cod = txtCodigo.getText().trim();
            String nom = txtNombre.getText().trim();
            String est = (String) comboEstado.getSelectedItem();

            String sql = "UPDATE f2z_tipo_prestamo SET TipPreNom = ?, PreRegTipEst = ? WHERE TipoPreCod = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, nom);
                ps.setString(2, est);
                ps.setInt(3, Integer.parseInt(cod));
                ps.executeUpdate();
            }
            cargarTabla();
            JOptionPane.showMessageDialog(this, "Registro actualizado.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
        }
    }

    private void eliminarFisico() {
        try {
            String cod = txtCodigo.getText().trim();
            String sql = "DELETE FROM f2z_tipo_prestamo WHERE TipoPreCod = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(cod));
                ps.executeUpdate();
            }
            cargarTabla();
            JOptionPane.showMessageDialog(this, "Registro eliminado.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
        }
    }

    private void cambiarEstado(String nuevoEstado) {
        try {
            String cod = txtCodigo.getText().trim();
            String sql = "UPDATE f2z_tipo_prestamo SET PreRegTipEst = ? WHERE TipoPreCod = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, Integer.parseInt(cod));
                ps.executeUpdate();
            }
            cargarTabla();
            JOptionPane.showMessageDialog(this, "Estado actualizado.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cambiar estado: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrestamoTipo().setVisible(true));
    }
}
