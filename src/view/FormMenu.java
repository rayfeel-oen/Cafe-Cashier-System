package view;

import connection.Koneksi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

import static view.FormLogin.*;
import static view.FormKasir.*;

public class FormMenu extends JFrame {

    // ── Extra accent colours ──────────────────────
    private static final Color GOLD        = new Color(0xD4A017);
    private static final Color JADE        = new Color(0x3DAA6A);
    private static final Color JADE_DARK   = new Color(0x0D2416);
    private static final Color AZURE       = new Color(0x4A9ECC);
    private static final Color AZURE_DARK  = new Color(0x0A1C28);
    private static final Color SURFACE     = new Color(0x231209);
    private static final Color CARD_BG     = new Color(0x2A150C);
    private static final Color DIVIDER     = new Color(0x3E1F11);
    private static final Color ACCENT_GLOW = new Color(0xC8803A, true);

    JTable tableMenu;
    DefaultTableModel model;

    JTextField txtNama;
    JTextField txtKategori;
    JTextField txtHarga;
    JTextField txtStok;

    JButton btnTambah;
    JButton btnEdit;
    JButton btnHapus;
    JButton btnClear;

    // field labels for floating-label effect
    private JLabel lblStatusBar;

    private Connection getConn() throws SQLException {
        return Koneksi.requireConnection();
    }

    public FormMenu() {
        setTitle("Café POS — Kelola Menu");
        setSize(960, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 560));

        // ── Root panel with painted background ──
        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // deep gradient
                GradientPaint gp = new GradientPaint(0, 0, SURFACE, getWidth(), getHeight(), new Color(0x1A0A04));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // subtle top-left warm glow
                RadialGradientPaint rg = new RadialGradientPaint(
                    new Point(0, 0), 400f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0xC8803A, false) {
                        { // inline tweak alpha
                        }
                        // Use 20 alpha
                    }, new Color(0x1A0A04, false)}
                );
                // simpler warm tint circle
                g2.setColor(new Color(200, 128, 58, 20));
                g2.fillOval(-100, -100, 500, 500);
                g2.dispose();
            }
        };
        root.setLayout(new BorderLayout(0, 0));
        setContentPane(root);

        root.add(buildTopBar(), BorderLayout.NORTH);

        // ── Main split ──
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildFormPanel(), buildTablePanel());
        split.setDividerLocation(290);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(SURFACE);
        root.add(split, BorderLayout.CENTER);

        root.add(buildNavBar(), BorderLayout.SOUTH);

        tampilData();
    }

    // ══ TOP BAR ══════════════════════════════════════════════════════════════
    private JPanel buildTopBar() {
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x3A1C0E), getWidth(), 0, new Color(0x2D1208));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // bottom accent line with gradient
                GradientPaint lp = new GradientPaint(0, getHeight()-1, CARAMEL, getWidth()/2, getHeight()-1, GOLD);
                g2.setPaint(lp);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setLayout(new BorderLayout());
        bar.setBorder(new EmptyBorder(14, 24, 14, 24));
        bar.setPreferredSize(new Dimension(0, 64));

        // Left: icon + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        // Coffee cup badge
        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, CARAMEL, getWidth(), getHeight(), GOLD);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(36, 36));
        badge.setLayout(new GridBagLayout());
        JLabel iconLbl = new JLabel("\u2615");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        badge.add(iconLbl);

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Kelola Menu");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(CREAM);
        JLabel sub = new JLabel("CAFÉ POINT OF SALE");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        sub.setForeground(MUTED);
        titleGroup.add(title);
        titleGroup.add(sub);

        left.add(badge);
        left.add(titleGroup);
        bar.add(left, BorderLayout.WEST);

        // Right: stats pill (item count updated on refresh)
        lblStatusBar = new JLabel("0 item dalam daftar");
        lblStatusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatusBar.setForeground(MUTED);
        bar.add(lblStatusBar, BorderLayout.EAST);

        return bar;
    }

    // ══ FORM PANEL ════════════════════════════════════════════════════════════
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0x240E07));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // right border
                g2.setColor(DIVIDER);
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 18));

        // Section header
        panel.add(buildSectionHeader("FORM INPUT MENU", "\u270F"));
        panel.add(Box.createVerticalStrut(16));

        // Fields with card-style
        txtNama     = addStyledField(panel, "Nama Menu",    "contoh: Espresso");
        txtKategori = addStyledField(panel, "Kategori",     "Minuman / Makanan");
        txtHarga    = addStyledField(panel, "Harga (Rp)",   "contoh: 25000");
        txtStok     = addStyledField(panel, "Stok",         "contoh: 50");

        panel.add(Box.createVerticalStrut(20));

        // Buttons
        btnTambah = buildPrimaryButton("\u2795  Tambah Menu", CARAMEL, ESPRESSO);
        btnTambah.addActionListener(e -> tambahData());
        btnTambah.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(btnTambah);
        panel.add(Box.createVerticalStrut(8));

        btnEdit = buildIconButton("\u270F  Perbarui Data", new Color(0x4A9ECC), ESPRESSO);
        btnEdit.addActionListener(e -> editData());
        btnEdit.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(btnEdit);
        panel.add(Box.createVerticalStrut(8));

        btnHapus = buildIconButton("\uD83D\uDDD1  Hapus Terpilih", new Color(0xE24B4A), CREAM);
        btnHapus.addActionListener(e -> hapusData());
        btnHapus.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(btnHapus);
        panel.add(Box.createVerticalStrut(8));

        btnClear = buildGhostButton("\u21BA  Bersihkan Form");
        btnClear.addActionListener(e -> clearForm());
        btnClear.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(btnClear);

        panel.add(Box.createVerticalGlue());

        // Tips card at bottom
        panel.add(buildTipsCard());

        outer.add(panel, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildSectionHeader(String text, String icon) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.setAlignmentX(LEFT_ALIGNMENT);

        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, CARAMEL, 0, getHeight(), GOLD);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(3, 20));
        bar.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(LATTE);
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        p.add(bar);
        p.add(lbl);
        return p;
    }

    private JTextField addStyledField(JPanel parent, String label, String placeholder) {
        // Label
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(4));

        // Field with rounded card feel
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isOpaque() { return false; }
        };
        field.setBackground(CARD_BG);
        field.setForeground(CREAM);
        field.setCaretColor(CARAMEL);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER, 1),
            BorderFactory.createEmptyBorder(9, 12, 9, 12)
        ));
        field.setToolTipText(placeholder);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARAMEL, 1),
                    BorderFactory.createEmptyBorder(9, 12, 9, 12)));
                lbl.setForeground(CARAMEL);
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DIVIDER, 1),
                    BorderFactory.createEmptyBorder(9, 12, 9, 12)));
                lbl.setForeground(MUTED);
            }
        });

        parent.add(field);
        parent.add(Box.createVerticalStrut(14));
        return field;
    }

    private JButton buildPrimaryButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, getBackground().brighter(), 0, getHeight(), getBackground());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // glow effect
                g2.setColor(new Color(getBackground().getRed(), getBackground().getGreen(), getBackground().getBlue(), 40));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setText(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
            @Override public void mousePressed(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseReleased(MouseEvent e){ btn.setBackground(bg); }
        });
        return btn;
    }

    private JButton buildIconButton(String text, Color accent, Color textColor) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // accent border
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setText(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(CARD_BG);
        btn.setForeground(accent);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
            }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(CARD_BG); }
        });
        return btn;
    }

    private JButton buildGhostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setForeground(MUTED);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(LATTE); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(MUTED); }
        });
        return btn;
    }

    private JPanel buildTipsCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1E0D06));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(0xC8803A, false) {});
                // left accent strip
                GradientPaint gp = new GradientPaint(0, 0, CARAMEL, 0, getHeight(), GOLD);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, 3, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 14, 10, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("\uD83D\uDCA1  Tips Penggunaan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 10));
        title.setForeground(CARAMEL);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel t1 = new JLabel("• Klik baris tabel untuk memilih data");
        JLabel t2 = new JLabel("• Isi semua kolom sebelum menekan Tambah");
        for (JLabel t : new JLabel[]{t1, t2}) {
            t.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            t.setForeground(MUTED);
            t.setAlignmentX(LEFT_ALIGNMENT);
        }

        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(t1);
        card.add(t2);
        return card;
    }

    // ══ TABLE PANEL ═══════════════════════════════════════════════════════════
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(SURFACE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 16, 16, 20));

        // Header row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleGroup.setOpaque(false);
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, CARAMEL, 0, getHeight(), GOLD);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(3, 22));
        JLabel tableLbl = new JLabel("  Daftar Menu Café");
        tableLbl.setFont(new Font("Georgia", Font.BOLD, 16));
        tableLbl.setForeground(CREAM);
        titleGroup.add(bar);
        titleGroup.add(tableLbl);
        headerRow.add(titleGroup, BorderLayout.WEST);

        // Search hint label
        JLabel hint = new JLabel("Klik baris untuk edit");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hint.setForeground(new Color(0x5A3525));
        headerRow.add(hint, BorderLayout.EAST);

        panel.add(headerRow, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        model.addColumn("ID");
        model.addColumn("Nama Menu");
        model.addColumn("Kategori");
        model.addColumn("Harga");
        model.addColumn("Stok");

        tableMenu = new JTable(model) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(0x231009));
                }
                return c;
            }
        };
        styleEnhancedTable(tableMenu);
        tableMenu.getColumnModel().getColumn(0).setMaxWidth(44);
        tableMenu.getColumnModel().getColumn(0).setMinWidth(44);
        tableMenu.getColumnModel().getColumn(4).setMaxWidth(70);

        // Kategori badge renderer
        tableMenu.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
                wrapper.setOpaque(true);
                wrapper.setBackground(sel ? MAHOGANY : (row % 2 == 0 ? CARD_BG : new Color(0x231009)));

                JLabel badge = new JLabel(val != null ? " " + val.toString() + " " : "");
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setOpaque(true);
                badge.setBorder(new EmptyBorder(3, 7, 3, 7));
                String cat = val != null ? val.toString().toLowerCase() : "";
                if (sel) {
                    badge.setBackground(MAHOGANY); badge.setForeground(CARAMEL);
                } else if (cat.contains("minuman")) {
                    badge.setBackground(AZURE_DARK); badge.setForeground(AZURE);
                } else if (cat.contains("makanan")) {
                    badge.setBackground(JADE_DARK); badge.setForeground(JADE);
                } else {
                    badge.setBackground(new Color(0x2D1810)); badge.setForeground(LATTE);
                }
                wrapper.add(badge);
                return wrapper;
            }
        });

        // Harga renderer
        tableMenu.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (val instanceof Number) {
                    setText("Rp " + FormKasir.IDR.format(((Number) val).longValue()));
                }
                setForeground(sel ? CREAM : GOLD);
                setBackground(sel ? MAHOGANY : (r % 2 == 0 ? CARD_BG : new Color(0x231009)));
                setOpaque(true);
                return this;
            }
        });

        // Stok renderer with colour coding
        tableMenu.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                setHorizontalAlignment(CENTER);
                setBackground(sel ? MAHOGANY : (r % 2 == 0 ? CARD_BG : new Color(0x231009)));
                setOpaque(true);
                if (!sel && val instanceof Number) {
                    int stok = ((Number) val).intValue();
                    if (stok <= 5)       setForeground(new Color(0xE24B4A));
                    else if (stok <= 15) setForeground(GOLD);
                    else                 setForeground(JADE);
                } else {
                    setForeground(CREAM);
                }
                return this;
            }
        });

        // Row click → populate form
        tableMenu.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = tableMenu.getSelectedRow();
                if (row >= 0) {
                    txtNama.setText(model.getValueAt(row, 1).toString());
                    txtKategori.setText(model.getValueAt(row, 2).toString());
                    txtHarga.setText(model.getValueAt(row, 3).toString());
                    txtStok.setText(model.getValueAt(row, 4).toString());
                }
            }
        });

        // Scroll pane
        JScrollPane sp = new JScrollPane(tableMenu);
        sp.setBorder(BorderFactory.createLineBorder(DIVIDER, 1));
        sp.setBackground(CARD_BG);
        sp.getViewport().setBackground(CARD_BG);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = MAHOGANY;
                this.trackColor = SURFACE;
            }
        });
        panel.add(sp, BorderLayout.CENTER);

        // Legend bar
        panel.add(buildLegendBar(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildLegendBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        bar.setOpaque(false);

        bar.add(legendDot(AZURE, "Minuman"));
        bar.add(legendDot(JADE, "Makanan"));
        bar.add(legendDot(new Color(0xE24B4A), "Stok kritis (\u22645)"));
        bar.add(legendDot(GOLD, "Stok rendah (\u226415)"));
        return bar;
    }

    private JPanel legendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 2, 8, 8);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(10, 12));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(MUTED);
        p.add(dot); p.add(lbl);
        return p;
    }

    private void styleEnhancedTable(JTable table) {
        table.setBackground(CARD_BG);
        table.setForeground(CREAM);
        table.setSelectionBackground(new Color(0x5A3020));
        table.setSelectionForeground(CARAMEL);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setGridColor(new Color(0x2E1308));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBorder(null);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0x1E0A04));
        header.setForeground(MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 10));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(val != null ? val.toString().toUpperCase() : "");
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
                lbl.setForeground(MUTED);
                lbl.setBackground(new Color(0x1E0A04));
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 6));
                return lbl;
            }
        });
    }

    // ══ NAV BAR ═══════════════════════════════════════════════════════════════
    private JPanel buildNavBar() {
        JPanel nav = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0x1E0A04));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(DIVIDER);
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        nav.setOpaque(false);
        nav.setLayout(new BorderLayout());
        nav.setBorder(new EmptyBorder(8, 16, 8, 16));
        nav.setPreferredSize(new Dimension(0, 52));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JButton bKasir   = buildNavButton("\uD83E\uDDFE  Kasir",   LATTE);
        JButton bLaporan = buildNavButton("\uD83D\uDCCA  Laporan", LATTE);
        bKasir.addActionListener(e   -> { new FormKasir().setVisible(true); dispose(); });
        bLaporan.addActionListener(e -> { new FormLaporan().setVisible(true); dispose(); });
        left.add(bKasir);
        left.add(bLaporan);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        JButton bLogout = buildNavButton("\uD83D\uDEAA  Logout", new Color(0xE24B4A));
        bLogout.addActionListener(e -> { new FormLogin().setVisible(true); dispose(); });
        right.add(bLogout);

        nav.add(left, BorderLayout.WEST);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    private JButton buildNavButton(String text, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(new Color(0x2D1208));
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER, 1),
            BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(MAHOGANY);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 1),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0x2D1208));
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DIVIDER, 1),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)));
            }
        });
        return btn;
    }

    // ══ DATABASE LOGIC ════════════════════════════════════════════════════════

    private void tampilData() {
        model.setRowCount(0);
        try {
            ResultSet rs = getConn().createStatement().executeQuery(
                "SELECT id_menu, nama_menu, kategori, harga, stok FROM menu ORDER BY kategori, nama_menu"
            );
            int count = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_menu"),
                    rs.getString("nama_menu"),
                    rs.getString("kategori"),
                    rs.getDouble("harga"),
                    rs.getInt("stok")
                });
                count++;
            }
            lblStatusBar.setText(count + " item dalam daftar");
            lblStatusBar.setForeground(MUTED);
        } catch (Exception e) {
            showDbError(e);
        }
    }

    private void tambahData() {
        if (txtNama.getText().isEmpty() || txtKategori.getText().isEmpty()
                || txtHarga.getText().isEmpty() || txtStok.getText().isEmpty()) {
            showWarning("Semua field wajib diisi!");
            return;
        }
        try {
            String nama     = txtNama.getText().trim();
            String kategori = txtKategori.getText().trim();
            double harga    = Double.parseDouble(txtHarga.getText().trim());
            int    stok     = Integer.parseInt(txtStok.getText().trim());
            Connection conn = getConn();

            PreparedStatement pstCek = conn.prepareStatement(
                "SELECT COUNT(*) FROM menu WHERE LOWER(nama_menu) = LOWER(?)");
            pstCek.setString(1, nama);
            ResultSet rsCek = pstCek.executeQuery();
            rsCek.next();
            if (rsCek.getInt(1) > 0) {
                showWarning("<html>Menu <b>\"" + nama + "\"</b> sudah ada!<br>Gunakan nama yang berbeda.</html>");
                return;
            }

            ResultSet rsId = conn.createStatement().executeQuery("SELECT COALESCE(MAX(id_menu), 0) + 1 FROM menu");
            rsId.next();
            int newId = rsId.getInt(1);

            PreparedStatement pst = conn.prepareStatement(
                "INSERT INTO menu(id_menu, nama_menu, kategori, harga, stok) VALUES(?,?,?,?,?)");
            pst.setInt(1, newId); pst.setString(2, nama); pst.setString(3, kategori);
            pst.setDouble(4, harga); pst.setInt(5, stok);
            pst.executeUpdate();
            clearForm(); tampilData();
            JOptionPane.showMessageDialog(this, "\u2705 Menu berhasil ditambah! (ID: " + newId + ")");
        } catch (NumberFormatException e) {
            showWarning("Harga dan Stok harus berupa angka!");
        } catch (Exception e) { showDbError(e); }
    }

    private void editData() {
        int row = tableMenu.getSelectedRow();
        if (row < 0) { showWarning("Pilih data yang ingin diedit!"); return; }
        try {
            int id = (int) model.getValueAt(row, 0);
            PreparedStatement pst = getConn().prepareStatement(
                "UPDATE menu SET nama_menu=?, kategori=?, harga=?, stok=? WHERE id_menu=?");
            pst.setString(1, txtNama.getText().trim());
            pst.setString(2, txtKategori.getText().trim());
            pst.setDouble(3, Double.parseDouble(txtHarga.getText()));
            pst.setInt(4, Integer.parseInt(txtStok.getText()));
            pst.setInt(5, id);
            pst.executeUpdate();
            clearForm(); tampilData();
            JOptionPane.showMessageDialog(this, "\u2705 Data berhasil diperbarui!");
        } catch (NumberFormatException e) {
            showWarning("Harga dan Stok harus berupa angka!");
        } catch (Exception e) { showDbError(e); }
    }

    private void hapusData() {
        int row = tableMenu.getSelectedRow();
        if (row < 0) { showWarning("Pilih data yang ingin dihapus!"); return; }
        String nama = model.getValueAt(row, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin menghapus menu \"" + nama + "\"?",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement pst = getConn().prepareStatement("DELETE FROM menu WHERE id_menu=?");
            pst.setInt(1, (int) model.getValueAt(row, 0));
            pst.executeUpdate();
            clearForm(); tampilData();
            JOptionPane.showMessageDialog(this, "\u2705 Data berhasil dihapus!");
        } catch (Exception e) { showDbError(e); }
    }

    private void clearForm() {
        txtNama.setText(""); txtKategori.setText("");
        txtHarga.setText(""); txtStok.setText("");
        tableMenu.clearSelection();
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Perhatian", JOptionPane.WARNING_MESSAGE);
    }

    private void showDbError(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.contains("null")) {
            msg = "Koneksi ke database terputus.\nSilakan periksa MySQL server.";
        }
        JOptionPane.showMessageDialog(this,
            "<html><b>Terjadi kesalahan database:</b><br><br>" + msg + "</html>",
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
