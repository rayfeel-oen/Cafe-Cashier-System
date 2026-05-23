package view;

import connection.Koneksi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

import static view.FormLogin.*;
import static view.FormKasir.*;

public class FormLaporan extends JFrame {

    JTable tableLaporan;
    DefaultTableModel model;

    JLabel lblPendapatan;
    JLabel lblJumlahOrder;
    JLabel lblRataRata;

    static final NumberFormat IDR = NumberFormat.getInstance(new Locale("id", "ID"));

    private Connection getConn() throws SQLException {
        return Koneksi.requireConnection();
    }

    public FormLaporan() {
        setTitle("Café POS — Laporan Transaksi");
        setSize(880, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = darkPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        root.add(buildTopBar(), BorderLayout.NORTH);

        JPanel content = darkPanel();
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        content.add(buildStatCards(), BorderLayout.NORTH);
        content.add(buildTableSection(), BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);

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

        JLabel title = new JLabel("\uD83D\uDCCA  Laporan Transaksi Café");
        title.setFont(new Font("Georgia", Font.BOLD, 18));
        title.setForeground(CREAM);
        bar.add(title, BorderLayout.WEST);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        eastPanel.setOpaque(false);

        JButton btnHapus = makeDangerButton("\uD83D\uDDD1  Hapus Riwayat");
        btnHapus.addActionListener(e -> hapusRiwayat());

        JButton btnRefresh = makeOutlineButton("\uD83D\uDD04  Refresh");
        btnRefresh.addActionListener(e -> { tampilLaporan(); hitungStat(); });

        eastPanel.add(btnHapus);
        eastPanel.add(btnRefresh);
        bar.add(eastPanel, BorderLayout.EAST);
        return bar;
    }

    // ── STAT CARDS ────────────────────────────────
    private JPanel buildStatCards() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        lblPendapatan  = new JLabel("Rp 0");
        lblJumlahOrder = new JLabel("0");
        lblRataRata    = new JLabel("Rp 0");

        panel.add(buildStatCard("\u2615  Total Pendapatan", lblPendapatan, "Semua waktu"));
        panel.add(buildStatCard("\uD83E\uDDFE  Jumlah Order",    lblJumlahOrder, "Transaksi"));
        panel.add(buildStatCard("\uD83D\uDCC8  Rata-rata",        lblRataRata,    "Per transaksi"));
        return panel;
    }

    private JPanel buildStatCard(String labelText, JLabel valueLabel, String subText) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

        // Header row: judul + tombol hapus selected
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lbl = new JLabel("  Riwayat Transaksi");
        lbl.setFont(new Font("Georgia", Font.BOLD, 14));
        lbl.setForeground(LATTE);
        headerRow.add(lbl, BorderLayout.WEST);

        JButton btnHapusBaris = makeDangerButton("\uD83D\uDDD1  Hapus Transaksi Ini");
        btnHapusBaris.addActionListener(e -> hapusTransaksiTerpilih());
        headerRow.add(btnHapusBaris, BorderLayout.EAST);

        panel.add(headerRow, BorderLayout.NORTH);

        model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        model.addColumn("ID Order");
        model.addColumn("Tanggal");
        model.addColumn("Waktu");
        model.addColumn("Total");
        model.addColumn("Bayar");
        model.addColumn("Kembalian");

        tableLaporan = new JTable(model);
        styleTable(tableLaporan);
        tableLaporan.getColumnModel().getColumn(0).setMaxWidth(75);
        tableLaporan.getColumnModel().getColumn(1).setPreferredWidth(100);
        tableLaporan.getColumnModel().getColumn(2).setPreferredWidth(80);

        // ID Order renderer → #0001
        tableLaporan.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (val instanceof Number)
                    setText(String.format("#%04d", ((Number) val).intValue()));
                setForeground(sel ? CREAM : LATTE);
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        // Tanggal renderer — format: Senin, 23 Mei 2026
        tableLaporan.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            final String[] HARI = {"Minggu","Senin","Selasa","Rabu","Kamis","Jumat","Sabtu"};
            final String[] BULAN = {"","Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des"};
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (val != null) {
                    try {
                        java.sql.Timestamp ts = java.sql.Timestamp.valueOf(val.toString());
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(ts);
                        int hari  = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
                        int tgl   = cal.get(java.util.Calendar.DAY_OF_MONTH);
                        int bln   = cal.get(java.util.Calendar.MONTH) + 1;
                        int tahun = cal.get(java.util.Calendar.YEAR);
                        setText(HARI[hari] + ", " + tgl + " " + BULAN[bln] + " " + tahun);
                    } catch (Exception ex) {
                        setText(val.toString().substring(0, Math.min(10, val.toString().length())));
                    }
                }
                setForeground(sel ? CREAM : LATTE);
                return this;
            }
        });

        // Waktu renderer — format: 14:35:22
        tableLaporan.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (val != null) {
                    try {
                        java.sql.Timestamp ts = java.sql.Timestamp.valueOf(val.toString());
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(ts);
                        setText(String.format("%02d:%02d:%02d",
                            cal.get(java.util.Calendar.HOUR_OF_DAY),
                            cal.get(java.util.Calendar.MINUTE),
                            cal.get(java.util.Calendar.SECOND)));
                    } catch (Exception ex) {
                        setText(val.toString().length() > 11 ? val.toString().substring(11, Math.min(19, val.toString().length())) : "");
                    }
                }
                setForeground(sel ? CREAM : MUTED);
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        // Rupiah renderers
        DefaultTableCellRenderer rupiahRenderer = new DefaultTableCellRenderer() {
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
        tableLaporan.getColumnModel().getColumn(3).setCellRenderer(rupiahRenderer);
        tableLaporan.getColumnModel().getColumn(4).setCellRenderer(rupiahRenderer);
        tableLaporan.getColumnModel().getColumn(5).setCellRenderer(kembalianRenderer);

        // Alternating row bg
        tableLaporan.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                // Delegate to column-specific renderer first
                Component comp = t.getColumnModel().getColumn(c)
                    .getCellRenderer() != null
                    ? t.getColumnModel().getColumn(c).getCellRenderer()
                        .getTableCellRendererComponent(t, val, sel, foc, r, c)
                    : super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (!sel && comp instanceof JComponent) {
                    comp.setBackground(r % 2 == 0
                        ? new Color(0x2D1810)
                        : new Color(0x271510));
                }
                return comp;
            }
        });

        panel.add(styledScrollPane(tableLaporan), BorderLayout.CENTER);
        return panel;
    }

    // ── NAV BAR ───────────────────────────────────
    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        nav.setBackground(ROAST);
        nav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, MAHOGANY));

        JButton bKasir   = makeOutlineButton("\uD83E\uDDFE  Kasir");
        bKasir.addActionListener(e -> { new FormKasir().setVisible(true); dispose(); });

        JButton bMenu    = makeOutlineButton("\uD83D\uDCCB  Kelola Menu");
        bMenu.addActionListener(e -> { new FormMenu().setVisible(true); dispose(); });

        JButton bLogout  = makeDangerButton("\uD83D\uDEAA  Logout");
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
            ResultSet rs = getConn().createStatement().executeQuery(
                "SELECT id_order, tanggal, total, bayar, kembalian FROM orders ORDER BY tanggal DESC");
            while (rs.next()) {
                String tanggal = rs.getString("tanggal");
                model.addRow(new Object[]{
                    rs.getInt("id_order"),
                    tanggal,   // kolom Tanggal
                    tanggal,   // kolom Waktu (renderer pisahkan sendiri)
                    rs.getDouble("total"),
                    rs.getDouble("bayar"),
                    rs.getDouble("kembalian")
                });
            }
        } catch (Exception e) {
            showDbError(e);
        }
    }

    private void hitungStat() {
        try {
            Connection conn = getConn();
            Statement st = conn.createStatement();

            ResultSet rs1 = st.executeQuery("SELECT COALESCE(SUM(total),0) AS p FROM orders");
            if (rs1.next()) lblPendapatan.setText("Rp " + IDR.format((long) rs1.getDouble("p")));

            ResultSet rs2 = st.executeQuery("SELECT COUNT(*) AS j FROM orders");
            if (rs2.next()) {
                int j = rs2.getInt("j");
                lblJumlahOrder.setText(String.valueOf(j));
                if (j > 0) {
                    ResultSet rs3 = st.executeQuery("SELECT COALESCE(AVG(total),0) AS r FROM orders");
                    if (rs3.next()) lblRataRata.setText("Rp " + IDR.format((long) rs3.getDouble("r")));
                } else {
                    lblRataRata.setText("Rp 0");
                }
            }
        } catch (Exception e) {
            showDbError(e);
        }
    }

    /** Hapus SATU transaksi yang dipilih di tabel + kembalikan stok */
    private void hapusTransaksiTerpilih() {
        int baris = tableLaporan.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih transaksi yang ingin dihapus terlebih dahulu!",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idOrder = (int) model.getValueAt(baris, 0);
        String tanggal = model.getValueAt(baris, 1).toString().substring(0, 10);

        int confirm = JOptionPane.showConfirmDialog(this,
            "<html>Hapus transaksi <b>#" + String.format("%04d", idOrder) + "</b> (" + tanggal + ")?<br>"
            + "Stok menu akan dikembalikan sesuai qty order ini.</html>",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection conn = getConn();

            // Kembalikan stok dulu sebelum hapus
            PreparedStatement pstStok = conn.prepareStatement(
                "UPDATE menu m JOIN order_details od ON m.id_menu = od.id_menu " +
                "SET m.stok = m.stok + od.qty WHERE od.id_order = ?");
            pstStok.setInt(1, idOrder);
            pstStok.executeUpdate();

            // Hapus detail lalu order
            PreparedStatement pstDetail = conn.prepareStatement("DELETE FROM order_details WHERE id_order=?");
            pstDetail.setInt(1, idOrder);
            pstDetail.executeUpdate();

            PreparedStatement pstOrder = conn.prepareStatement("DELETE FROM orders WHERE id_order=?");
            pstOrder.setInt(1, idOrder);
            pstOrder.executeUpdate();

            tampilLaporan();
            hitungStat();
            JOptionPane.showMessageDialog(this,
                "\u2705 Transaksi #" + String.format("%04d", idOrder) + " berhasil dihapus.\nStok menu telah dikembalikan.");
        } catch (Exception e) {
            showDbError(e);
        }
    }

    /** Hapus SEMUA riwayat transaksi (reset total) + kembalikan stok */
    private void hapusRiwayat() {
        int jumlah = model.getRowCount();
        if (jumlah == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada riwayat transaksi.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>\u26A0 PERHATIAN!</b><br><br>"
            + "Ini akan menghapus <b>" + jumlah + " transaksi</b> secara permanen.<br>"
            + "Stok menu akan dikembalikan ke kondisi sebelum transaksi.<br><br>"
            + "Tindakan ini <b>tidak bisa dibatalkan</b>.<br><br>"
            + "Lanjutkan?</html>",
            "Konfirmasi Hapus Semua", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Konfirmasi kedua — ketik kata kunci
        String input = JOptionPane.showInputDialog(this,
            "<html>Ketik <b>HAPUS SEMUA</b> untuk konfirmasi:</html>",
            "Konfirmasi Akhir", JOptionPane.WARNING_MESSAGE);
        if (input == null || !input.trim().equals("HAPUS SEMUA")) {
            JOptionPane.showMessageDialog(this, "Penghapusan dibatalkan.", "Batal", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Connection conn = getConn();

            // Kembalikan semua stok dulu
            conn.createStatement().executeUpdate(
                "UPDATE menu m JOIN order_details od ON m.id_menu = od.id_menu " +
                "SET m.stok = m.stok + od.qty");

            conn.createStatement().executeUpdate("DELETE FROM order_details");
            conn.createStatement().executeUpdate("DELETE FROM orders");

            tampilLaporan();
            hitungStat();
            JOptionPane.showMessageDialog(this,
                "\u2705 Semua riwayat transaksi telah dihapus.\nStok menu telah dikembalikan.");
        } catch (Exception e) {
            showDbError(e);
        }
    }

    private void showDbError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) msg = "Koneksi ke database terputus.";
        JOptionPane.showMessageDialog(this,
            "<html><b>Terjadi kesalahan:</b><br><br>" + msg + "</html>",
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
