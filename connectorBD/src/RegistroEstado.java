import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegistroEstado extends JFrame {
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JComboBox<String> comboEstadoRegistro;
    private DefaultListModel<String> modeloLista;

    // Datos de conexión
    static final String URL = "jdbc:mysql://localhost:3306/LABBDCASO11";
    static final String USUARIO = "root";
    static final String CONTRASEÑA = "UJKMjuandi128980";

    private Connection conexion;

    public RegistroEstado() {
        conectarBD();
        setTitle("Registro de Estado de Registro");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel de entrada
        JPanel panelEntrada = new JPanel(new GridLayout(3, 2, 10, 10));
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Registro de Estado"));

        txtCodigo = new JTextField();
        txtNombre = new JTextField();
        comboEstadoRegistro = new JComboBox<>(new String[]{"Activo", "Inactivo", "Eliminado"});

        panelEntrada.add(new JLabel("Código (Ej: A1):"));
        panelEntrada.add(txtCodigo);
        panelEntrada.add(new JLabel("Nombre del Estado:"));
        panelEntrada.add(txtNombre);
        panelEntrada.add(new JLabel("Estado del Registro:"));
        panelEntrada.add(comboEstadoRegistro);

        add(panelEntrada, BorderLayout.NORTH);

        // Lista
        modeloLista = new DefaultListModel<>();
        JList<String> listaEstados = new JList<>(modeloLista);
        JScrollPane scrollPane = new JScrollPane(listaEstados);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Tabla: gzz_estado_registro"));
        add(scrollPane, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnAdicionar = new JButton("Adicionar");
        JButton btnSalir = new JButton("Salir");
        JButton btnEliminar = new JButton("Eliminar");  
        
        
        panelBotones.add(btnEliminar);
        panelBotones.add(btnAdicionar);
        panelBotones.add(btnSalir);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos
        btnAdicionar.addActionListener(e -> insertarEstado());
        btnSalir.addActionListener(e -> System.exit(0));
        btnEliminar.addActionListener(e -> eliminarEstado());


        cargarEstados();
    }

    private void conectarBD() {
        try {
            conexion = DriverManager.getConnection(URL, USUARIO, CONTRASEÑA);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos: " + e.getMessage());
            System.exit(1);
        }
    }

    private void insertarEstado() {
        try {
            String codigo = txtCodigo.getText().trim();
            String nombre = txtNombre.getText().trim();
            String estado = comboEstadoRegistro.getSelectedItem().toString(); // Texto completo

            if (codigo.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Completa todos los campos.");
                return;
            }

            String sql = "INSERT INTO gzz_estado_registro (EstRegCod, EstRegNom, EstRegRegEst) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, codigo);
                ps.setString(2, nombre);
                ps.setString(3, estado);

                int filas = ps.executeUpdate();
                if (filas > 0) {
                    modeloLista.addElement("Código: " + codigo + " | Nombre: " + nombre + " | Estado: " + estado);
                    txtCodigo.setText("");
                    txtNombre.setText("");
                    comboEstadoRegistro.setSelectedIndex(0);
                    JOptionPane.showMessageDialog(this, "Estado registrado correctamente.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al insertar estado: " + ex.getMessage());
        }
    }

    private void cargarEstados() {
        modeloLista.clear();
        String sql = "SELECT * FROM gzz_estado_registro";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String cod = rs.getString("EstRegCod");
                String nom = rs.getString("EstRegNom");
                String est = rs.getString("EstRegRegEst");
                modeloLista.addElement("Código: " + cod + " | Nombre: " + nom + " | Estado: " + est);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage());
        }
    }
    private void eliminarEstado() {
    String codigo = txtCodigo.getText().trim();
    if (codigo.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ingresa el código del estado a eliminar.");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar el estado con código: " + codigo + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    String sql = "DELETE FROM gzz_estado_registro WHERE EstRegCod = ?";
    try (PreparedStatement ps = conexion.prepareStatement(sql)) {
        ps.setString(1, codigo);
        int filas = ps.executeUpdate();
        if (filas > 0) {
            JOptionPane.showMessageDialog(this, "Estado eliminado correctamente.");
            cargarEstados(); // Actualiza la lista
            txtCodigo.setText("");
            txtNombre.setText("");
            comboEstadoRegistro.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró ningún estado con ese código.");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistroEstado().setVisible(true));
    }
}
