package view;

import connection.Koneksi;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FormLogin extends JFrame {

    // ── Palette kopi ──────────────────────────────
    static final Color ESPRESSO  = new Color(0x1A0F0A);
    static final Color ROAST     = new Color(0x2D1810);
    static final Color MAHOGANY  = new Color(0x4A2518);
    static final Color CARAMEL   = new Color(0xC8803A);
    static final Color LATTE     = new Color(0xD4A96A);
    static final Color CREAM     = new Color(0xF5EDD8);
    static final Color MUTED     = new Color(0x8B6347);

    JTextField     txtUsername;
    JPasswordField txtPassword;
    JButton        btnLogin;
    JLabel         lblConnStatus;

    public FormLogin() {
        setTitle("Café POS — Login");
        setSize(420, 580);
        setUndecorated(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, ESPRESSO, getWidth(), getHeight(), ROAST);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(200, 128, 58, 28));
                g2.fillOval(-60, -60, 260, 260);
                g2.setColor(new Color(200, 128, 58, 18));
                g2.fillOval(getWidth() - 120, getHeight() - 120, 200, 200);
                g2.dispose();
            }
        };
        root.setLayout(new GridBagLayout());
        setContentPane(root);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ROAST);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(MAHOGANY);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 20, 20));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 36, 36, 36));
        card.setPreferredSize(new Dimension(330, 460));

        // ── Logo ──────────────────────────────────
        JPanel logoBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoBox.setOpaque(false);
        JPanel logoRound = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARAMEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        logoRound.setOpaque(false);
        logoRound.setPreferredSize(new Dimension(60, 60));
        logoRound.setLayout(new GridBagLayout());
        JLabel emojiLbl = new JLabel("\u2615");
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        logoRound.add(emojiLbl);
        logoBox.add(logoRound);

        JLabel title = new JLabel("Café POS", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(CREAM);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("POINT OF SALE SYSTEM", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblUser = makeFieldLabel("USERNAME");
        txtUsername = new JTextField();
        styleField(txtUsername);

        JLabel lblPass = makeFieldLabel("PASSWORD");
        txtPassword = new JPasswordField();
        styleField(txtPassword);

        btnLogin = makeCaramelButton("Masuk ke Sistem");

        // ── Connection status pill ─────────────────
        lblConnStatus = new JLabel("\u25CF  Memeriksa koneksi...");
        lblConnStatus.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblConnStatus.setForeground(MUTED);
        lblConnStatus.setAlignmentX(CENTER_ALIGNMENT);

        card.add(logoBox);
        card.add(Box.createVerticalStrut(16));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(14));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(22));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));
        card.add(lblConnStatus);

        root.add(card);

        btnLogin.addActionListener(e -> login());
        txtPassword.addActionListener(e -> login());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());

        // Check connection asynchronously after UI shows
        SwingUtilities.invokeLater(this::testConnection);
    }

    private void testConnection() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                try {
                    Connection c = Koneksi.getConnection();
                    return c != null && c.isValid(3);
                } catch (Exception e) { return false; }
            }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        lblConnStatus.setText("\u25CF  Database terhubung");
                        lblConnStatus.setForeground(new Color(0x63C58A));
                    } else {
                        lblConnStatus.setText("\u25CF  Tidak dapat terhubung ke database");
                        lblConnStatus.setForeground(new Color(0xE24B4A));
                    }
                } catch (Exception e) {
                    lblConnStatus.setText("\u25CF  Koneksi error");
                    lblConnStatus.setForeground(new Color(0xE24B4A));
                }
            }
        };
        worker.execute();
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setBackground(MAHOGANY);
        field.setForeground(CREAM);
        field.setCaretColor(CARAMEL);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x5A3020), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARAMEL, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x5A3020), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    static JButton makeCaramelButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setText(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(CARAMEL);
        btn.setForeground(ESPRESSO);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(LATTE); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(CARAMEL); }
            @Override public void mousePressed(MouseEvent e) { btn.setBackground(MUTED); }
            @Override public void mouseReleased(MouseEvent e){ btn.setBackground(CARAMEL); }
        });
        return btn;
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = String.valueOf(txtPassword.getPassword());

        if (username.isEmpty()) { showError("Username tidak boleh kosong"); return; }
        if (password.isEmpty()) { showError("Password tidak boleh kosong"); return; }

        btnLogin.setEnabled(false);
        btnLogin.setText("Masuk...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            String errMsg = null;
            @Override protected Boolean doInBackground() {
                try {
                    Connection conn = Koneksi.requireConnection();
                    String sql = "SELECT * FROM users WHERE username=? AND password=?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, username);
                    pst.setString(2, password);
                    ResultSet rs = pst.executeQuery();
                    return rs.next();
                } catch (Exception ex) {
                    errMsg = ex.getMessage();
                    return false;
                }
            }
            @Override protected void done() {
                btnLogin.setEnabled(true);
                btnLogin.setText("Masuk ke Sistem");
                try {
                    if (errMsg != null) {
                        showError("Koneksi gagal: " + errMsg);
                    } else if (get()) {
                        new FormKasir().setVisible(true);
                        dispose();
                    } else {
                        showError("Username / Password salah");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    }
                } catch (Exception e) {
                    showError("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }
}
