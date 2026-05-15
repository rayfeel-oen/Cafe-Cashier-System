package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GenshinPin extends JPanel {

    // ── Gem colour presets ────────────────────────
    public enum Element {
        CRYO   ("#a8f0ff", "#38b8e8", "#0a5fa8", "#60d0ff"),   // ice-blue
        PYRO   ("#ffe8a8", "#e8a020", "#7a4800", "#ffb040"),   // fire-amber
        ANEMO  ("#a8f5d0", "#28c87a", "#0a5a34", "#50e0a0"),   // wind-green
        ELECTRO("#d8a8ff", "#9040e0", "#3a1060", "#b060ff"),   // lightning-purple
        HYDRO  ("#a8d8ff", "#2880e0", "#062860", "#50a8ff");   // water-blue

        final String hi, mid, deep, rim;
        Element(String hi, String mid, String deep, String rim) {
            this.hi = hi; this.mid = mid; this.deep = deep; this.rim = rim;
        }
    }

    // ── Config ───────────────────────────────────
    private static final int   W          = 54;   // pin bounding width
    private static final int   H          = 68;   // pin bounding height (inc. tip)
    private static final int   MARGIN     = 18;   // distance from frame edge
    private static final float HOVER_SCALE = 1.18f;

    private final Element element;
    private boolean hovered = false;
    private float   pulse   = 0f;   // 0..1 glow animation
    private Timer   pulseTimer;

    // ── Factory / attach ─────────────────────────
    public static void attach(JFrame frame) {
        attach(frame, Element.PYRO);   // default: warm gold — matches café theme
    }

    public static void attach(JFrame frame, Element element) {
        GenshinPin pin = new GenshinPin(element);

        // Swap to JLayeredPane so the pin floats above everything
        JLayeredPane layered = frame.getLayeredPane();

        // Position: bottom-right, sized to W × H + hover breathing room
        int pw = W + 20, ph = H + 20;
        layered.add(pin, JLayeredPane.PALETTE_LAYER);

        // Reposition on resize
        ComponentAdapter resizer = new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                Insets ins = frame.getInsets();
                int x = frame.getWidth()  - ins.right  - pw - MARGIN;
                int y = frame.getHeight() - ins.bottom - ph - MARGIN;
                pin.setBounds(x, y, pw, ph);
            }
        };
        frame.addComponentListener(resizer);
        // Trigger once immediately so pin shows up before first resize
        resizer.componentResized(null);
    }

    // ── Constructor ──────────────────────────────
    public GenshinPin(Element element) {
        this.element = element;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                // Cycle element on click — fun Easter egg
                JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(GenshinPin.this),
                    "Café POS  v1.0\nMade with ☕ & Anemo energy",
                    "About", JOptionPane.PLAIN_MESSAGE);
            }
        });

        // Subtle pulse animation (glow breathes every 2s)
        pulseTimer = new Timer(30, e -> {
            pulse = (pulse + 0.025f) % (2 * (float) Math.PI);
            repaint();
        });
        pulseTimer.start();
    }

    // ── Painting ─────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,    RenderingHints.VALUE_STROKE_PURE);

        float scale = hovered ? HOVER_SCALE : 1.0f;
        int cx = getWidth()  / 2;
        int cy = getHeight() / 2 - 4;  // slight upward centre

        g2.translate(cx, cy);
        g2.scale(scale, scale);

        drawPin(g2);
        g2.dispose();
    }

    private void drawPin(Graphics2D g2) {
        // ── Outer glow ring (pulse) ───────────────
        float glowAlpha = 0.10f + 0.08f * (float) Math.sin(pulse);
        Color rimColor  = hex(element.rim);
        g2.setColor(new Color(rimColor.getRed(), rimColor.getGreen(), rimColor.getBlue(),
                              (int)(glowAlpha * 255)));
        g2.fillOval(-W/2 - 4, -H/2 + 2, W + 8, W + 8);

        // ── Shadow ────────────────────────────────
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillOval(-W/2 + 2, -H/2 + 6, W - 4, W - 2);

        // ── Gold outer body (teardrop) ────────────
        drawTeardrop(g2, W/2, H/2 - 4,
                     new GradientPaint(-W/2, -H/2, hex("#fff3b0"), W/2, H/2, hex("#7a5200")));

        // ── Dark inner circle ─────────────────────
        int ir = W/2 - 5;
        g2.setColor(hex("#0a1f38"));
        g2.fillOval(-ir, -H/2 + 8, ir*2, ir*2);
        g2.setColor(hex("#c49000"));
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawOval(-ir, -H/2 + 8, ir*2, ir*2);

        // ── Filigree cross-arms ───────────────────
        g2.setColor(hex("#e8b800"));
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int armR = ir + 3;
        drawPetal(g2, 0, -H/2 + 4,    0, -H/2 + 14);   // top
        drawPetal(g2, 0, -H/2 + ir*2 + 8, 0, -H/2 + ir*2); // bottom
        drawPetal(g2, -armR, -H/2 + ir + 8, -ir + 1, -H/2 + ir + 8); // left
        drawPetal(g2,  armR, -H/2 + ir + 8,  ir - 1, -H/2 + ir + 8); // right

        // ── Corner accent dots ────────────────────
        g2.setColor(hex("#f0d060"));
        float dotR = 2.4f;
        fillCircle(g2, 0,    -H/2 + 2,          dotR);
        fillCircle(g2, 0,    -H/2 + ir*2 + 12,  dotR);
        fillCircle(g2, -armR, -H/2 + ir + 8,    dotR);
        fillCircle(g2,  armR, -H/2 + ir + 8,    dotR);

        // ── Elemental gem ─────────────────────────
        int gr = ir - 4;
        int gy = -H/2 + ir + 8;   // gem centre y
        RadialGradientPaint gem = new RadialGradientPaint(
            new Point2D.Float(-gr * 0.2f, gy - gr * 0.3f),
            gr * 1.1f,
            new float[]{0f, 0.45f, 1f},
            new Color[]{hex(element.hi), hex(element.mid), hex(element.deep)}
        );
        g2.setPaint(gem);
        g2.fillOval(-gr, gy - gr, gr*2, gr*2);

        // Gem rim
        g2.setColor(hex(element.rim));
        g2.setStroke(new BasicStroke(1.1f));
        g2.drawOval(-gr, gy - gr, gr*2, gr*2);

        // Gem facet lines
        g2.setColor(new Color(hex(element.hi).getRed(), hex(element.hi).getGreen(),
                              hex(element.hi).getBlue(), 130));
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawLine(0, gy - gr,  gr/2, gy);
        g2.drawLine(0, gy - gr, -gr/2, gy);
        g2.drawLine(0, gy + gr,  gr/2, gy);
        g2.drawLine(0, gy + gr, -gr/2, gy);

        // Gem highlight
        g2.setPaint(new GradientPaint(-gr/2f, gy - gr/2f,
                                       new Color(255,255,255,180),
                                       0, gy,
                                       new Color(255,255,255,0)));
        g2.fillOval(-gr/2, gy - gr + 2, gr/2, gr/3);
    }

    // ── Teardrop shape ────────────────────────────
    private void drawTeardrop(Graphics2D g2, int rw, int rh, Paint paint) {
        // Body: rounded top oval + pointy bottom tip
        Path2D path = new Path2D.Float();
        int bw = rw, bh = rh - 10;
        path.moveTo(0, rh - 2);   // tip
        path.curveTo(-bw + 6, rh - 14, -bw, 0, -bw, -bh/2);
        path.curveTo(-bw, -bh, bw, -bh, bw, -bh/2);
        path.curveTo(bw, 0, bw - 6, rh - 14, 0, rh - 2);
        path.closePath();

        g2.setPaint(paint);
        g2.fill(path);
        g2.setColor(hex("#a07800"));
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(path);
    }

    // ── Decorative petal between two points ───────
    private void drawPetal(Graphics2D g2, float x1, float y1, float x2, float y2) {
        float mx = (x1+x2)/2, my = (y1+y2)/2;
        float dx = (y2-y1)*0.4f, dy = (x1-x2)*0.4f;
        Path2D p = new Path2D.Float();
        p.moveTo(x1, y1);
        p.quadTo(mx+dx, my+dy, x2, y2);
        p.quadTo(mx-dx, my-dy, x1, y1);
        p.closePath();
        g2.fill(p);
        g2.draw(p);
    }

    private void fillCircle(Graphics2D g2, float cx, float cy, float r) {
        g2.fillOval((int)(cx-r), (int)(cy-r), (int)(r*2), (int)(r*2));
    }

    private static Color hex(String h) {
        return Color.decode(h);
    }
}
