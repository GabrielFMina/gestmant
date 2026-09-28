package gesmant.vista;

import gesmant.datos.UsuarioDAO;
import gesmant.modelo.Usuario;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** CU01 - Iniciar sesion. */
public class LoginVista extends JFrame {

    private final JTextField txtUsuario = new JTextField(15);
    private final JPasswordField txtClave = new JPasswordField(15);

    public LoginVista() {
        super("GesMant - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel campos = new JPanel(new GridLayout(2, 2, 5, 5));
        campos.add(new JLabel("Usuario:"));
        campos.add(txtUsuario);
        campos.add(new JLabel("Clave:"));
        campos.add(txtClave);

        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JLabel("Sistema de órdenes de trabajo de mantenimiento"), BorderLayout.NORTH);
        panel.add(campos, BorderLayout.CENTER);
        panel.add(btnIngresar, BorderLayout.SOUTH);

        add(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        try {
            Usuario u = new UsuarioDAO().login(txtUsuario.getText().trim(), new String(txtClave.getPassword()));
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Usuario o clave incorrectos.", "Acceso denegado",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if ("ADMINISTRADOR".equals(u.getRol())) {
                new AdminVista(u).setVisible(true);
            } else {
                new TecnicoVista(u).setVisible(true);
            }
            dispose();
        } catch (Exception ex) {
            Util.error(this, ex);
        }
    }
}
