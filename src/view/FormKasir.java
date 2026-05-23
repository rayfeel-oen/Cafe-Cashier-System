package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import connection.Koneksi;
import static view.FormLogin.CARAMEL;
import static view.FormLogin.CREAM;
import static view.FormLogin.ESPRESSO;
import static view.FormLogin.LATTE;
import static view.FormLogin.MAHOGANY;
import static view.FormLogin.MUTED;
import static view.FormLogin.ROAST;
import static view.FormLogin.makeCaramelButton;

public class FormKasir extends JFrame {

    DefaultTableModel modelMenu;
    DefaultTableModel modelKeranjang;
    JTable tableMenu;
    JTable tableKeranjang;

    JLabel lblTotal;
    JTextField txtBayar;
    JLabel lblKembalian;

    JButton btnTambah;
    JButton btnHapus;
    JButton btnSimpan;

    // Status bar label for connection feedback
    JLabel lblStatus;

    static final NumberFormat IDR = NumberFormat.getInstance(new Locale("id", "ID"));

    /** Get a fresh connection each time, with auto-reconnect */
    private Connection getConn() throws SQLException {
        return Koneksi.requireConnection();
    }

    public FormKasir() {
        setTitle("Café POS — Kasir");
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = darkPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        root.add(buildTopBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildMenuPanel(), buildCartPanel());
        split.setDividerLocation(600);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(ESPRESSO);
        root.add(split, BorderLayout.CENTER);

        root.add(buildNavBar(), BorderLayout.SOUTH);

        tampilMenu();

        // ── Genshin Impact pin (bottom-right corner) ──
        GenshinPin.attach(this);
    }

    // ── TOP BAR ───────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(ROAST);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, MAHOGANY),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("\u2615  Sistem Kasir Café");
        title.setFont(new Font("Georgia", Font.BOLD, 18));
        title.setForeground(CREAM);
        bar.add(title, BorderLayout.WEST);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        eastPanel.setOpaque(false);

        // Connection status indicator
        lblStatus = new JLabel("\u25CF  Terhubung");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(0x63C58A));
        eastPanel.add(lblStatus);

        JLabel clock = new JLabel();
        clock.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clock.setForeground(MUTED);
        eastPanel.add(clock);

        bar.add(eastPanel, BorderLayout.EAST);

        Timer timer = new Timer(1000, e -> {
            clock.setText(new java.util.Date().toString().substring(11, 19)
                    + "   " + java.time.LocalDate.now());
            checkConnectionStatus();
        });
        timer.start();
        timer.getActionListeners()[0].actionPerformed(null);

        return bar;
    }

    private void checkConnectionStatus() {
        try {
            Connection c = Koneksi.getConnection();
            boolean alive = c != null && c.isValid(1);
            if (alive) {
                lblStatus.setText("\u25CF  Terhubung");
                lblStatus.setForeground(new Color(0x63C58A));
            } else {
                lblStatus.setText("\u25CF  Koneksi Terputus");
                lblStatus.setForeground(new Color(0xE24B4A));
            }
        } catch (Exception ex) {
            lblStatus.setText("\u25CF  Koneksi Terputus");
            lblStatus.setForeground(new Color(0xE24B4A));
        }
    }

    // ── PANEL MENU (kiri) ─────────────────────────
    private JPanel buildMenuPanel() {
        JPanel panel = darkPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 8));

        JLabel lbl = sectionTitle("Daftar Menu");
        panel.add(lbl, BorderLayout.NORTH);

        modelMenu = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        modelMenu.addColumn("ID");
        modelMenu.addColumn("Nama");
        modelMenu.addColumn("Kategori");
        modelMenu.addColumn("Harga");

        tableMenu = new JTable(modelMenu);
        styleTable(tableMenu);
        tableMenu.getColumnModel().getColumn(0).setMaxWidth(40);
        tableMenu.getColumnModel().getColumn(2).setMaxWidth(100);

        // Kategori badge renderer
        tableMenu.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                String cat = val != null ? val.toString().toLowerCase() : "";
                if (sel) {
                    setBackground(MAHOGANY); setForeground(CARAMEL);
                } else if (cat.contains("minuman")) {
                    setBackground(new Color(0x1A2A3A)); setForeground(new Color(0x6BAED6));
                } else if (cat.contains("makanan")) {
                    setBackground(new Color(0x1A2A0A)); setForeground(new Color(0x74C476));
                } else {
                    setBackground(new Color(0x2D1810)); setForeground(LATTE);
                }
                return this;
            }
        });

        tableMenu.getColumnModel().getColumn(3).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val,
                        boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                    if (val instanceof Number) {
                        setText("Rp " + IDR.format(((Number) val).longValue()));
                    }
                    setForeground(sel ? CREAM : CARAMEL);
                    return this;
                }
            }
        );

        JScrollPane sp = styledScrollPane(tableMenu);
        panel.add(sp, BorderLayout.CENTER);

        btnTambah = makeCaramelButton("\u2795  Tambah ke Keranjang");
        // Also allow double-click on row to add
        tableMenu.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) tambahKeKeranjang();
            }
        });

        JPanel btnWrap = darkPanel();
        btnWrap.setBorder(new EmptyBorder(10, 0, 0, 0));
        btnWrap.add(btnTambah);
        panel.add(btnWrap, BorderLayout.SOUTH);

        btnTambah.addActionListener(e -> tambahKeKeranjang());

        return panel;
    }

    // ── PANEL KERANJANG (kanan) ───────────────────
    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ROAST);
        panel.setBorder(new EmptyBorder(16, 8, 16, 16));

        JLabel lbl = sectionTitle("Keranjang");
        panel.add(lbl, BorderLayout.NORTH);

        modelKeranjang = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        modelKeranjang.addColumn("ID");
        modelKeranjang.addColumn("Nama");
        modelKeranjang.addColumn("Harga");
        modelKeranjang.addColumn("Qty");
        modelKeranjang.addColumn("Subtotal");

        tableKeranjang = new JTable(modelKeranjang);
        styleTable(tableKeranjang);
        tableKeranjang.getColumnModel().getColumn(0).setMaxWidth(36);
        tableKeranjang.getColumnModel().getColumn(3).setMaxWidth(40);

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
        tableKeranjang.getColumnModel().getColumn(2).setCellRenderer(rupiahRenderer);
        tableKeranjang.getColumnModel().getColumn(4).setCellRenderer(rupiahRenderer);

        JScrollPane sp = styledScrollPane(tableKeranjang);
        panel.add(sp, BorderLayout.CENTER);

        JPanel pay = buildPaymentSection();
        panel.add(pay, BorderLayout.SOUTH);

        return panel;
    }

    // ── PAYMENT SECTION ───────────────────────────
    private JPanel buildPaymentSection() {
        JPanel pay = new JPanel();
        pay.setBackground(ROAST);
        pay.setLayout(new BoxLayout(pay, BoxLayout.Y_AXIS));
        pay.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, MAHOGANY),
            new EmptyBorder(14, 0, 0, 0)
        ));

        JPanel rowTotal = payRow();
        JLabel lblTotalLbl = payLabel("TOTAL");
        lblTotal = new JLabel("Rp 0");
        lblTotal.setFont(new Font("Georgia", Font.BOLD, 20));
        lblTotal.setForeground(CARAMEL);
        rowTotal.add(lblTotalLbl);
        rowTotal.add(Box.createHorizontalGlue());
        rowTotal.add(lblTotal);
        pay.add(rowTotal);
        pay.add(Box.createVerticalStrut(10));

        JLabel lblBayarLbl = payLabel("JUMLAH UANG DITERIMA");
        lblBayarLbl.setAlignmentX(LEFT_ALIGNMENT);
        pay.add(lblBayarLbl);
        pay.add(Box.createVerticalStrut(5));

        txtBayar = new JTextField();
        styleField(txtBayar);
        txtBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtBayar.setAlignmentX(LEFT_ALIGNMENT);
        txtBayar.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { hitungKembalian(); }
        });
        pay.add(txtBayar);
        pay.add(Box.createVerticalStrut(10));

        JPanel rowKembalian = payRow();
        JLabel lblKembalianLbl = payLabel("KEMBALIAN");
        lblKembalian = new JLabel("Rp 0");
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKembalian.setForeground(new Color(0x63C58A));
        rowKembalian.add(lblKembalianLbl);
        rowKembalian.add(Box.createHorizontalGlue());
        rowKembalian.add(lblKembalian);
        pay.add(rowKembalian);
        pay.add(Box.createVerticalStrut(14));

        JPanel btnRow = darkPanel();
        btnRow.setBackground(ROAST);
        btnRow.setLayout(new GridLayout(1, 2, 8, 0));

        btnHapus = makeOutlineButton("\uD83D\uDDD1  Hapus Item");
        btnHapus.addActionListener(e -> hapusKeranjang());

        btnSimpan = makeCaramelButton("\uD83D\uDCBE  Simpan");
        btnSimpan.addActionListener(e -> simpanTransaksi());

        btnRow.add(btnHapus);
        btnRow.add(btnSimpan);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        pay.add(btnRow);

        return pay;
    }

    // ── NAVIGATION BAR ────────────────────────────
    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        nav.setBackground(ROAST);
        nav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, MAHOGANY));

        JButton bMenu = makeOutlineButton("\uD83D\uDCCB  Kelola Menu");
        bMenu.addActionListener(e -> { new FormMenu().setVisible(true); dispose(); });

        JButton bLaporan = makeOutlineButton("\uD83D\uDCCA  Laporan");
        bLaporan.addActionListener(e -> { new FormLaporan().setVisible(true); dispose(); });

        JButton bLogout = makeDangerButton("\uD83D\uDEAA  Logout");
        bLogout.addActionListener(e -> { new FormLogin().setVisible(true); dispose(); });

        nav.add(bMenu);
        nav.add(bLaporan);
        nav.add(Box.createHorizontalStrut(20));
        nav.add(bLogout);
        return nav;
    }

    // ── LOGIC METHODS ─────────────────────────────

    private void tampilMenu() {
        modelMenu.setRowCount(0);
        try {
            // Use DISTINCT on nama_menu + kategori to avoid duplicates shown in UI
            // But still pull by id_menu correctly
            ResultSet rs = getConn().createStatement().executeQuery(
                "SELECT id_menu, nama_menu, kategori, harga FROM menu ORDER BY kategori, nama_menu"
            );
            while (rs.next()) {
                modelMenu.addRow(new Object[]{
                    rs.getInt("id_menu"),
                    rs.getString("nama_menu"),
                    rs.getString("kategori"),
                    rs.getDouble("harga")
                });
            }
        } catch (Exception e) {
            showDbError(e);
        }
    }

    private void tambahKeKeranjang() {
        int baris = tableMenu.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih menu terlebih dahulu!\n(Klik satu kali pada baris menu, atau klik dua kali untuk langsung tambah)",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id       = (int)    modelMenu.getValueAt(baris, 0);
            String nama  = (String) modelMenu.getValueAt(baris, 1);
            double harga = (double) modelMenu.getValueAt(baris, 3);

            String qtyStr = JOptionPane.showInputDialog(this,
                "Masukkan Qty untuk: " + nama, "Input", JOptionPane.QUESTION_MESSAGE);
            if (qtyStr == null) return; // user cancelled

            qtyStr = qtyStr.trim();
            if (qtyStr.isEmpty()) return;

            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Qty harus lebih dari 0!", "Perhatian", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── FIX: merge if same menu already in cart ──────────────
            for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                if ((int) modelKeranjang.getValueAt(i, 0) == id) {
                    int newQty = (int) modelKeranjang.getValueAt(i, 3) + qty;
                    modelKeranjang.setValueAt(newQty, i, 3);
                    modelKeranjang.setValueAt(harga * newQty, i, 4);
                    hitungTotal();
                    // Highlight the updated row
                    tableKeranjang.setRowSelectionInterval(i, i);
                    tableKeranjang.scrollRectToVisible(tableKeranjang.getCellRect(i, 0, true));
                    return;
                }
            }

            modelKeranjang.addRow(new Object[]{ id, nama, harga, qty, harga * qty });
            // Select the newly added row
            int newRow = modelKeranjang.getRowCount() - 1;
            tableKeranjang.setRowSelectionInterval(newRow, newRow);
            tableKeranjang.scrollRectToVisible(tableKeranjang.getCellRect(newRow, 0, true));
            hitungTotal();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Qty harus berupa angka bulat!", "Format Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void hapusKeranjang() {
        int baris = tableKeranjang.getSelectedRow();
        if (baris < 0) {
            // Provide clear guidance: the user must click a row in the CART table
            JOptionPane.showMessageDialog(this,
                "<html>Pilih item di tabel <b>Keranjang</b> (sebelah kanan)<br>"
                + "yang ingin dihapus terlebih dahulu!</html>",
                "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nama = modelKeranjang.getValueAt(baris, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Hapus \"" + nama + "\" dari keranjang?",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        modelKeranjang.removeRow(baris);
        hitungTotal();

        // Auto-select next row if possible
        int total = modelKeranjang.getRowCount();
        if (total > 0) {
            int sel = Math.min(baris, total - 1);
            tableKeranjang.setRowSelectionInterval(sel, sel);
        }
    }

    private void hitungTotal() {
        double total = 0;
        for (int i = 0; i < tableKeranjang.getRowCount(); i++) {
            total += (double) tableKeranjang.getValueAt(i, 4);
        }
        lblTotal.setText("Rp " + IDR.format((long) total));
        hitungKembalian();
    }

    private void hitungKembalian() {
        try {
            double total = parseTotal();
            double bayar = Double.parseDouble(txtBayar.getText().trim());
            double kembalian = bayar - total;
            lblKembalian.setText("Rp " + IDR.format((long) kembalian));
            lblKembalian.setForeground(kembalian >= 0
                ? new Color(0x63C58A) : new Color(0xE24B4A));
        } catch (Exception e) {
            lblKembalian.setText("Rp 0");
            lblKembalian.setForeground(new Color(0x63C58A));
        }
    }

    private void simpanTransaksi() {
        if (modelKeranjang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!", "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double total = parseTotal();
            String bayarText = txtBayar.getText().trim();
            if (bayarText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan jumlah bayar!", "Perhatian", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double bayar = Double.parseDouble(bayarText);
            if (bayar < total) {
                JOptionPane.showMessageDialog(this,
                    "Uang pembayaran kurang!\nKurang: Rp " + IDR.format((long)(total - bayar)),
                    "Pembayaran Tidak Cukup", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double kembalian = bayar - total;

            Connection conn = getConn();

            PreparedStatement pstOrder = conn.prepareStatement(
                "INSERT INTO orders(tanggal, total, bayar, kembalian) VALUES(NOW(),?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            );
            pstOrder.setDouble(1, total);
            pstOrder.setDouble(2, bayar);
            pstOrder.setDouble(3, kembalian);
            pstOrder.executeUpdate();

            ResultSet rs = pstOrder.getGeneratedKeys();
            int idOrder = rs.next() ? rs.getInt(1) : 0;

            for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                int idMenu = (int) tableKeranjang.getValueAt(i, 0);
                int qty    = (int) tableKeranjang.getValueAt(i, 3);

                PreparedStatement pstDetail = conn.prepareStatement(
                    "INSERT INTO order_details(id_order, id_menu, qty, subtotal) VALUES(?,?,?,?)"
                );
                pstDetail.setInt(1, idOrder);
                pstDetail.setInt(2, idMenu);
                pstDetail.setInt(3, qty);
                pstDetail.setDouble(4, (double) tableKeranjang.getValueAt(i, 4));
                pstDetail.executeUpdate();

                // ── Kurangi stok menu sesuai qty yang dipesan ──
                PreparedStatement pstStok = conn.prepareStatement(
                    "UPDATE menu SET stok = stok - ? WHERE id_menu = ? AND stok >= ?"
                );
                pstStok.setInt(1, qty);
                pstStok.setInt(2, idMenu);
                pstStok.setInt(3, qty);
                int updated = pstStok.executeUpdate();
                if (updated == 0) {
                    // Stok tidak cukup — batalkan SELURUH transaksi
                    String namaMenu = tableKeranjang.getValueAt(i, 1).toString();

                    // Kembalikan stok yang sudah dikurangi di iterasi sebelumnya
                    PreparedStatement pstRestore = conn.prepareStatement(
                        "UPDATE menu m JOIN order_details od ON m.id_menu = od.id_menu " +
                        "SET m.stok = m.stok + od.qty WHERE od.id_order = ?");
                    pstRestore.setInt(1, idOrder);
                    pstRestore.executeUpdate();

                    // Hapus detail & order
                    conn.prepareStatement("DELETE FROM order_details WHERE id_order=" + idOrder).executeUpdate();
                    conn.prepareStatement("DELETE FROM orders WHERE id_order=" + idOrder).executeUpdate();

                    // Cek stok aktual untuk info ke kasir
                    int stokAktual = 0;
                    try {
                        PreparedStatement pstCek = conn.prepareStatement("SELECT stok FROM menu WHERE id_menu=?");
                        pstCek.setInt(1, idMenu);
                        ResultSet rsCek = pstCek.executeQuery();
                        if (rsCek.next()) stokAktual = rsCek.getInt("stok");
                    } catch (Exception ignored) {}

                    JOptionPane.showMessageDialog(this,
                        "<html>\u26A0 <b>Transaksi Dibatalkan!</b><br><br>"
                        + "Stok <b>\"" + namaMenu + "\"</b> tidak mencukupi.<br>"
                        + "Sisa stok: <b>" + stokAktual + "</b> &nbsp;|&nbsp; Dibutuhkan: <b>" + qty + "</b><br><br>"
                        + "Kurangi qty atau minta admin tambah stok.</html>",
                        "Stok Tidak Cukup \u2014 Order Dibatalkan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            cetakStruk(idOrder, total, bayar, kembalian);

            modelKeranjang.setRowCount(0);
            lblTotal.setText("Rp 0");
            txtBayar.setText("");
            lblKembalian.setText("Rp 0");

        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void cetakStruk(int idOrder, double total, double bayar, double kembalian) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557\n");
        sb.append("\u2551     \u2615  CAF\u00C9 POS SYSTEM      \u2551\n");
        sb.append("\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D\n\n");
        sb.append("  ID Order : #").append(String.format("%04d", idOrder)).append("\n\n");
        sb.append("  \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n");
        for (int i = 0; i < tableKeranjang.getRowCount(); i++) {
            sb.append("  ").append(tableKeranjang.getValueAt(i, 1))
              .append(" x").append(tableKeranjang.getValueAt(i, 3)).append("\n");
            sb.append("  Rp ").append(IDR.format((long)(double)tableKeranjang.getValueAt(i, 4))).append("\n\n");
        }
        sb.append("  \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n");
        sb.append("  Total     : Rp ").append(IDR.format((long) total)).append("\n");
        sb.append("  Bayar     : Rp ").append(IDR.format((long) bayar)).append("\n");
        sb.append("  Kembalian : Rp ").append(IDR.format((long) kembalian)).append("\n\n");
        sb.append("  \u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\n");
        sb.append("        TERIMA KASIH! \u2615\n");
        sb.append("  \u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBackground(ROAST);
        area.setForeground(CREAM);
        area.setEditable(false);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(340, 340));

        JOptionPane.showMessageDialog(this, sp, "Struk Pembayaran", JOptionPane.PLAIN_MESSAGE);
    }

    private void showDbError(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.contains("null")) {
            msg = "Koneksi ke database terputus.\nSilakan periksa MySQL server dan restart aplikasi.";
        }
        JOptionPane.showMessageDialog(this,
            "<html><b>Terjadi kesalahan database:</b><br><br>"
            + msg.replace("\n", "<br>") + "</html>",
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    private double parseTotal() {
        String text = lblTotal.getText().replace("Rp ", "").replaceAll("[^0-9]", "");
        return text.isEmpty() ? 0 : Double.parseDouble(text);
    }

    // ── STYLE HELPERS ─────────────────────────────

    static JPanel darkPanel() {
        JPanel p = new JPanel();
        p.setBackground(ESPRESSO);
        return p;
    }

    static JLabel sectionTitle(String text) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JPanel bar = new JPanel();
        bar.setBackground(CARAMEL);
        bar.setPreferredSize(new Dimension(3, 16));

        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("Georgia", Font.BOLD, 14));
        lbl.setForeground(LATTE);

        wrap.add(bar, BorderLayout.WEST);
        wrap.add(lbl, BorderLayout.CENTER);
        wrap.setBorder(new EmptyBorder(0, 0, 12, 0));
        return lbl;
    }

    static void styleTable(JTable table) {
        table.setBackground(new Color(0x2D1810));
        table.setForeground(CREAM);
        table.setSelectionBackground(MAHOGANY);
        table.setSelectionForeground(CARAMEL);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(32);
        table.setGridColor(new Color(0x3A1F12));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setBorder(null);

        JTableHeader header = table.getTableHeader();
        header.setBackground(MAHOGANY);
        header.setForeground(MUTED);
        header.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x5A3020)));
    }

    static JScrollPane styledScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(MAHOGANY, 1));
        sp.setBackground(ESPRESSO);
        sp.getViewport().setBackground(new Color(0x2D1810));
        return sp;
    }

    static void styleField(JTextField field) {
        field.setBackground(MAHOGANY);
        field.setForeground(CREAM);
        field.setCaretColor(CARAMEL);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x5A3020), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
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

    static JButton makeOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(MAHOGANY);
        btn.setForeground(LATTE);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x5A3020), 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setForeground(CARAMEL);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARAMEL, 1),
                    BorderFactory.createEmptyBorder(8, 14, 8, 14)));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setForeground(LATTE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x5A3020), 1),
                    BorderFactory.createEmptyBorder(8, 14, 8, 14)));
            }
        });
        return btn;
    }

    static JButton makeDangerButton(String text) {
        JButton btn = makeOutlineButton(text);
        btn.setForeground(new Color(0xE24B4A));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE24B4A, true).darker(), 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(0x3A1010)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(MAHOGANY); }
        });
        return btn;
    }

    static JPanel payRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    static JLabel payLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(MUTED);
        return lbl;
    }
}
