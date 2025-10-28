package com.flowchart.view;

import com.flowchart.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Nuovo Canvas con sistema di layout verticale automatico
 */
public class FlowchartCanvas extends JPanel {
    private FlowchartTree tree;
    private FlowchartModel model;
    private Connection hoveredConnection;
    private Block selectedBlock;
    private double zoomFactor = 1.0;
    private JButton zoomInButton;
    private JButton zoomOutButton;

    public FlowchartCanvas(FlowchartModel model) {
        this.model = model;
        this.tree = new FlowchartTree();

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        setLayout(null);

        setupMouseListeners();
        setupKeyListeners();
        setupZoomButtons();
    }

    private void setupZoomButtons() {
        zoomInButton = new JButton("+");
        zoomInButton.setFont(new Font("Arial", Font.BOLD, 20));
        zoomInButton.setFocusable(false);
        zoomInButton.addActionListener(e -> zoomIn());
        add(zoomInButton);

        zoomOutButton = new JButton("-");
        zoomOutButton.setFont(new Font("Arial", Font.BOLD, 20));
        zoomOutButton.setFocusable(false);
        zoomOutButton.addActionListener(e -> zoomOut());
        add(zoomOutButton);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionZoomButtons();
            }
        });
    }

    private void positionZoomButtons() {
        int buttonSize = 40;
        int margin = 10;
        int x = getWidth() - buttonSize - margin;
        int yOut = getHeight() - buttonSize - margin;
        int yIn = yOut - buttonSize - 5;

        zoomInButton.setBounds(x, yIn, buttonSize, buttonSize);
        zoomOutButton.setBounds(x, yOut, buttonSize, buttonSize);
    }

    public void zoomIn() {
        if (zoomFactor < 2.0) {
            zoomFactor += 0.1;
            repaint();
        }
    }

    public void zoomOut() {
        if (zoomFactor > 0.5) {
            zoomFactor -= 0.1;
            repaint();
        }
    }

    private Point screenToWorld(Point screenPoint) {
        return new Point(
            (int) (screenPoint.x / zoomFactor),
            (int) (screenPoint.y / zoomFactor)
        );
    }

    private void setupKeyListeners() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (selectedBlock != null) {
                        tree.removeBlock(selectedBlock);
                        selectedBlock = null;
                        repaint();
                    }
                }
            }
        });
    }

    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick(e);
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private void handleMousePressed(MouseEvent e) {
        Point worldPos = screenToWorld(e.getPoint());

        // Controlla se ha cliccato su una freccia
        Connection clickedConn = getConnectionAt(worldPos.x, worldPos.y);
        if (clickedConn != null) {
            showInsertBlockMenu(clickedConn, e.getPoint());
            return;
        }

        // Controlla se ha cliccato su un blocco
        Block clickedBlock = getBlockAt(worldPos.x, worldPos.y);
        if (clickedBlock != null) {
            selectedBlock = clickedBlock;
            repaint();
            return;
        }

        selectedBlock = null;
        repaint();
        requestFocusInWindow();
    }

    private void handleMouseMoved(MouseEvent e) {
        Point worldPos = screenToWorld(e.getPoint());
        Connection conn = getConnectionAt(worldPos.x, worldPos.y);

        if (conn != hoveredConnection) {
            hoveredConnection = conn;
            setCursor(hoveredConnection != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }
    }

    private void handleDoubleClick(MouseEvent e) {
        Point worldPos = screenToWorld(e.getPoint());
        Block clickedBlock = getBlockAt(worldPos.x, worldPos.y);

        if (clickedBlock != null && clickedBlock.getType() != BlockType.START && clickedBlock.getType() != BlockType.END) {
            String newText = JOptionPane.showInputDialog(
                this,
                "Modifica testo del blocco:",
                clickedBlock.getText()
            );
            if (newText != null && !newText.trim().isEmpty()) {
                clickedBlock.setText(newText);
                repaint();
            }
        }
    }

    private void showInsertBlockMenu(Connection conn, Point screenPos) {
        JPopupMenu menu = new JPopupMenu();

        // Aggiungi opzioni per ogni tipo di blocco (esclusi START e END)
        addMenuItem(menu, "Processo", BlockType.PROCESS, conn);
        addMenuItem(menu, "Input", BlockType.INPUT, conn);
        addMenuItem(menu, "Output", BlockType.OUTPUT, conn);
        addMenuItem(menu, "Decisione", BlockType.DECISION, conn);
        addMenuItem(menu, "Ciclo FOR", BlockType.FOR_LOOP, conn);
        addMenuItem(menu, "Ciclo WHILE", BlockType.WHILE_LOOP, conn);
        addMenuItem(menu, "Ciclo DO-WHILE", BlockType.DO_WHILE_LOOP, conn);

        menu.show(this, screenPos.x, screenPos.y);
    }

    private void addMenuItem(JPopupMenu menu, String label, BlockType type, Connection conn) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(e -> {
            String text = JOptionPane.showInputDialog(
                this,
                "Inserisci il testo per il blocco " + label + ":",
                type.getDisplayName()
            );
            if (text != null && !text.trim().isEmpty()) {
                tree.insertBlockInConnection(conn, type, text);
                repaint();
            }
        });
        menu.add(item);
    }

    private Block getBlockAt(int x, int y) {
        for (Block block : tree.getAllBlocks()) {
            if (block.contains(new Point(x, y))) {
                return block;
            }
        }
        return null;
    }

    private Connection getConnectionAt(int x, int y) {
        for (Connection conn : tree.getAllConnections()) {
            Point source = conn.getSourceBlock().getConnectionPoint(conn.getSourceEdge());
            Point target = conn.getTargetBlock().getConnectionPoint(conn.getTargetEdge());

            double distance = distanceFromLine(x, y, source.x, source.y, target.x, target.y);
            if (distance < 10) {
                return conn;
            }
        }
        return null;
    }

    private double distanceFromLine(int px, int py, int x1, int y1, int x2, int y2) {
        double lineLength = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        if (lineLength == 0) return Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1));

        double t = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / (lineLength * lineLength)));
        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);

        return Math.sqrt((px - projX) * (px - projX) + (py - projY) * (py - projY));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Applica zoom
        g2d.scale(zoomFactor, zoomFactor);

        // Disegna connessioni
        drawConnections(g2d);

        // Disegna blocchi
        for (Block block : tree.getAllBlocks()) {
            drawBlock(g2d, block);
        }
    }

    private void drawConnections(Graphics2D g2d) {
        for (Connection conn : tree.getAllConnections()) {
            Point source = conn.getSourceBlock().getConnectionPoint(conn.getSourceEdge());
            Point target = conn.getTargetBlock().getConnectionPoint(conn.getTargetEdge());

            // Evidenzia se hover
            if (conn == hoveredConnection) {
                g2d.setColor(new Color(0, 120, 215));
                g2d.setStroke(new BasicStroke(4));
            } else {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
            }

            // Disegna linea
            g2d.drawLine(source.x, source.y, target.x, target.y);

            // Disegna freccia
            drawArrow(g2d, source, target);

            g2d.setStroke(new BasicStroke(1));
        }
    }

    private void drawArrow(Graphics2D g2d, Point source, Point target) {
        double angle = Math.atan2(target.y - source.y, target.x - source.x);
        int arrowSize = 10;

        int x1 = (int) (target.x - arrowSize * Math.cos(angle - Math.PI / 6));
        int y1 = (int) (target.y - arrowSize * Math.sin(angle - Math.PI / 6));
        int x2 = (int) (target.x - arrowSize * Math.cos(angle + Math.PI / 6));
        int y2 = (int) (target.y - arrowSize * Math.sin(angle + Math.PI / 6));

        g2d.drawLine(target.x, target.y, x1, y1);
        g2d.drawLine(target.x, target.y, x2, y2);
    }

    private void drawBlock(Graphics2D g2d, Block block) {
        Point pos = block.getPosition();
        Dimension size = block.getSize();
        BlockType type = block.getType();

        // Evidenzia se selezionato
        if (block == selectedBlock) {
            g2d.setColor(new Color(255, 235, 59, 100));
            g2d.fillRect(pos.x - 5, pos.y - 5, size.width + 10, size.height + 10);
        }

        // Disegna forma in base al tipo
        g2d.setColor(Color.decode(type.getColor()));

        switch (type) {
            case START:
            case END:
                g2d.fillOval(pos.x, pos.y, size.width, size.height);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(pos.x, pos.y, size.width, size.height);
                break;
            case DECISION:
                int[] xPoints = {pos.x + size.width / 2, pos.x + size.width, pos.x + size.width / 2, pos.x};
                int[] yPoints = {pos.y, pos.y + size.height / 2, pos.y + size.height, pos.y + size.height / 2};
                g2d.fillPolygon(xPoints, yPoints, 4);
                g2d.setColor(Color.BLACK);
                g2d.drawPolygon(xPoints, yPoints, 4);
                break;
            case INPUT:
            case OUTPUT:
                int[] xParallel = {pos.x + 20, pos.x + size.width, pos.x + size.width - 20, pos.x};
                int[] yParallel = {pos.y, pos.y, pos.y + size.height, pos.y + size.height};
                g2d.fillPolygon(xParallel, yParallel, 4);
                g2d.setColor(Color.BLACK);
                g2d.drawPolygon(xParallel, yParallel, 4);
                break;
            default:
                g2d.fillRect(pos.x, pos.y, size.width, size.height);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(pos.x, pos.y, size.width, size.height);
        }

        // Disegna testo
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        FontMetrics fm = g2d.getFontMetrics();
        String text = block.getText();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int textX = pos.x + (size.width - textWidth) / 2;
        int textY = pos.y + (size.height + textHeight) / 2 - fm.getDescent();
        g2d.drawString(text, textX, textY);
    }

    public FlowchartTree getTree() {
        return tree;
    }

    public void clearAll() {
        tree = new FlowchartTree();
        selectedBlock = null;
        hoveredConnection = null;
        repaint();
    }
}
