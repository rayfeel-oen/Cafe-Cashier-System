package view;

import connection.Koneksi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

import static view.FormLogin.*;
import static view.FormKasir.*;

public class FormLaporan extends JFrame {

    Connection conn = Koneksi.getConnection();

    JTable tableLaporan;
    DefaultTableModel model;

    JLabel lblPendapatan;
    JLabel lblJumlahOrder;
    JLabel lblRataRata;

    static final NumberFormat IDR = NumberFormat.getInstance(new Locale("id", "ID"));

    public FormLaporan() {
        setTitle("Café POS — Laporan Harian");
        setSize(780, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = darkPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        // ── Top Bar ───────────────────────────────
        root.add(buildTopBar(), BorderLayout.NORTH);

        // ── Content ───────────────────────────────
        JPanel content = darkPanel();
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Stat cards
        content.add(buildStatCards(), BorderLayout.NORTH);

        // Tabel
        content.add(buildTableSection(), BorderLayout.CENTER);

        root.add(content, BorderLayout.CENTER);

        // ── Bottom Nav ────────────────────────────
        root.add(buildNavBar(), BorderLayout.SOUTH);

        tampilLaporan();
        hitungStat();
    }

    // ── TOP BAR ───────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(ROAST);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, MAHOGANY),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("📊  Laporan Harian Café");
        title.setFont(new Font("Georgia", Font.BOLD, 18));
        title.setForeground(CREAM);
        bar.add(title, BorderLayout.WEST);

        JButton btnRefresh = makeOutlineButton("🔄  Refresh");
        btnRefresh.addActionListener(e -> {
            tampilLaporan();
            hitungStat();
        });
        bar.add(btnRefresh, BorderLayout.EAST);
        return bar;
    }

    // ── STAT CARDS ────────────────────────────────
    private JPanel buildStatCards() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        lblPendapatan = new JLabel("Rp 0");
        lblJumlahOrder = new JLabel("0");
        lblRataRata = new JLabel("Rp 0");

        panel.add(buildStatCard("☕  Total Pendapatan", lblPendapatan, "Hari ini"));
        panel.add(buildStatCard("🧾  Jumlah Order", lblJumlahOrder, "Transaksi"));
        panel.add(buildStatCard("📈  Rata-rata", lblRataRata, "Per transaksi"));

        return panel;
    }

    private JPanel buildStatCard(String labelText, JLabel valueLabel, String subText) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ROAST);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(MAHOGANY);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        valueLabel.setForeground(CARAMEL);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel(subText);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sub.setForeground(MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lbl);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(sub);

        return card;
    }

    // ── TABLE SECTION ─────────────────────────────
    private JPanel buildTableSection() {
        JPanel panel = darkPanel();
        panel.setLayout(new BorderLayout());

        JLabel lbl = sectionTitle("Riwayat Transaksi");
        panel.add(lbl, BorderLayout.NORTH);

        model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        model.addColumn("ID Order");
        model.addColumn("Tanggal");
        model.addColumn("Total");
        model.addColumn("Bayar");
        model.addColumn("Kembalian");

        tableLaporan = new JTable(model);
        styleTable(tableLaporan);
        tableLaporan.getColumnModel().getColumn(0).setMaxWidth(80);

        // Warna pada kolom total = caramel, kembalian = hijau
        DefaultTableCellRenderer totalRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (val instanceof Number) setText("Rp " + IDR.format(((Number) val).longValue()));
                setForeground(sel ? CREAM : CARAMEL);
                return this;
            }
        };
        DefaultTableCellRenderer kembalianRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (val instanceof Number) setText("Rp " + IDR.format(((Number) val).longValue()));
                setForeground(sel ? CREAM : new Color(0x63C58A));
                return this;
            }
        };

        tableLaporan.getColumnModel().getColumn(2).setCellRenderer(totalRenderer);
        tableLaporan.getColumnModel().getColumn(3).setCellRenderer(totalRenderer);
        tableLaporan.getColumnModel().getColumn(4).setCellRenderer(kembalianRenderer);

        // ID Order format #0001
        tableLaporan.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (val instanceof Number)
                    setText(String.format("#%04d", ((Number) val).intValue()));
                setForeground(sel ? CREAM : LATTE);
                return this;
            }
        });

        panel.add(styledScrollPane(tableLaporan), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        nav.setBackground(ROAST);
        nav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, MAHOGANY));

        JButton bKasir = makeOutlineButton("🧾  Kasir");
        bKasir.addActionListener(e -> { new FormKasir().setVisible(true); dispose(); });

        JButton bMenu = makeOutlineButton("📋  Kelola Menu");
        bMenu.addActionListener(e -> { new FormMenu().setVisible(true); dispose(); });

        JButton bLogout = makeDangerButton("🚪  Logout");
        bLogout.addActionListener(e -> { new FormLogin().setVisible(true); dispose(); });

        nav.add(bKasir);
        nav.add(bMenu);
        nav.add(Box.createHorizontalStrut(20));
        nav.add(bLogout);
        return nav;
    }

    // ── LOGIC ─────────────────────────────────────

    private void tampilLaporan() {
        model.setRowCount(0);
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT * FROM orders ORDER BY tanggal DESC");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_order"),
                    rs.getString("tanggal"),
                    rs.getDouble("total"),
                    rs.getDouble("bayar"),
                    rs.getDouble("kembalian")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hitungStat() {
        try {
            Statement st = conn.createStatement();

            // Total pendapatan
            ResultSet rs1 = st.executeQuery(
                "SELECT COALESCE(SUM(total),0) AS pendapatan FROM orders");
            if (rs1.next()) {
                double total = rs1.getDouble("pendapatan");
                lblPendapatan.setText("Rp " + IDR.format((long) total));
            }

            // Jumlah order
            ResultSet rs2 = st.executeQuery("SELECT COUNT(*) AS jumlah FROM orders");
            if (rs2.next()) {
                int jumlah = rs2.getInt("jumlah");
                lblJumlahOrder.setText(String.valueOf(jumlah));

                // Rata-rata
                if (jumlah > 0) {
                    ResultSet rs3 = st.executeQuery(
                        "SELECT COALESCE(AVG(total),0) AS rata FROM orders");
                    if (rs3.next()) {
                        lblRataRata.setText("Rp " + IDR.format((long) rs3.getDouble("rata")));
                    }
                } else {
                    lblRataRata.setText("Rp 0");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
