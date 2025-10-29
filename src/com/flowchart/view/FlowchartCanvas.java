package com.flowchart.view;

import com.flowchart.model.*;
import static com.flowchart.layout.LayoutConstants.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Canvas for rendering and interacting with the flowchart diagram.
 * Handles drawing blocks, connections, and user interactions (clicks, hovers, edits).
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

            // Per merge connections, controlla tutti i segmenti del routing Manhattan
            if (conn.isMergeConnection()) {
                if (isPointOnMergeConnection(x, y, source, target, conn)) {
                    return conn;
                }
            } else {
                // Per connessioni normali, linea retta
                double distance = distanceFromLine(x, y, source.x, source.y, target.x, target.y);
                if (distance < 10) {
                    return conn;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a point is on a merge connection (decision block branch).
     * Handles Manhattan routing with horizontal and vertical segments.
     */
    private boolean isPointOnMergeConnection(int px, int py, Point source, Point target, Connection conn) {
        String branch = conn.getMergeBranch();
        boolean isLeftBranch = "left".equals(branch);
        boolean isTargetMergePoint = (conn.getTargetBlock().getType() == BlockType.MERGE);
        boolean isSourceDecision = (conn.getSourceBlock().getType() == BlockType.DECISION);

        // Click tolerance in pixels
        final int CLICK_TOLERANCE = 10;

        if (isLeftBranch) {
            int leftX = isSourceDecision ? (source.x - BRANCH_HORIZONTAL_OFFSET) : source.x;

            if (isTargetMergePoint) {
                int mergeCenterX = conn.getTargetBlock().getPosition().x + conn.getTargetBlock().getSize().width / 2;
                int mergeCenterY = conn.getTargetBlock().getPosition().y + conn.getTargetBlock().getSize().height / 2;

                if (isSourceDecision) {
                    // From decision diamond: horizontal → vertical → horizontal
                    if (distanceFromLine(px, py, source.x, source.y, leftX, source.y) < CLICK_TOLERANCE) return true;
                    if (distanceFromLine(px, py, leftX, source.y, leftX, mergeCenterY) < CLICK_TOLERANCE) return true;
                    if (distanceFromLine(px, py, leftX, mergeCenterY, mergeCenterX, mergeCenterY) < CLICK_TOLERANCE) return true;
                } else {
                    // From intermediate block: vertical → horizontal
                    if (distanceFromLine(px, py, source.x, source.y, source.x, mergeCenterY) < CLICK_TOLERANCE) return true;
                    if (distanceFromLine(px, py, source.x, mergeCenterY, mergeCenterX, mergeCenterY) < CLICK_TOLERANCE) return true;
                }
            } else {
                if (isSourceDecision) {
                    // From decision diamond: horizontal → vertical
                    if (distanceFromLine(px, py, source.x, source.y, leftX, source.y) < CLICK_TOLERANCE) return true;
                    if (distanceFromLine(px, py, leftX, source.y, leftX, target.y) < CLICK_TOLERANCE) return true;
                } else {
                    // From intermediate block: only vertical
                    if (distanceFromLine(px, py, source.x, source.y, source.x, target.y) < CLICK_TOLERANCE) return true;
                }
            }
        } else {
            // RIGHT BRANCH
            int rightX = isSourceDecision ? (source.x + BRANCH_HORIZONTAL_OFFSET) : source.x;

            if (isTargetMergePoint) {
                int mergeCenterX = conn.getTargetBlock().getPosition().x + conn.getTargetBlock().getSize().width / 2;
                int mergeCenterY = conn.getTargetBlock().getPosition().y + conn.getTargetBlock().getSize().height / 2;

                if (isSourceDecision) {
                    // From decision diamond: horizontal → vertical → horizontal
                    if (distanceFromLine(px, py, source.x, source.y, rightX, source.y) < CLICK_TOLERANCE) return true;
                    if (distanceFromLine(px, py, rightX, source.y, rightX, mergeCenterY) < CLICK_TOLERANCE) return true;
                    if (distanceFromLine(px, py, rightX, mergeCenterY, mergeCenterX, mergeCenterY) < CLICK_TOLERANCE) return true;
                } else {
                    // From intermediate block: vertical → horizontal
                    if (distanceFromLine(px, py, source.x, source.y, source.x, mergeCenterY) < CLICK_TOLERANCE) return true;
                    if (distanceFromLine(px, py, source.x, mergeCenterY, mergeCenterX, mergeCenterY) < CLICK_TOLERANCE) return true;
                }
            } else {
                if (isSourceDecision) {
                    // From decision diamond: horizontal → vertical
                    if (distanceFromLine(px, py, source.x, source.y, rightX, source.y) < CLICK_TOLERANCE) return true;
                    if (distanceFromLine(px, py, rightX, source.y, rightX, target.y) < CLICK_TOLERANCE) return true;
                } else {
                    // From intermediate block: only vertical
                    if (distanceFromLine(px, py, source.x, source.y, source.x, target.y) < CLICK_TOLERANCE) return true;
                }
            }
        }

        return false;
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

        // Disegna blocchi (solo quelli visibili)
        for (Block block : tree.getAllBlocks()) {
            if (block.isVisible()) {
                drawBlock(g2d, block);
            }
        }
    }

    private void drawConnections(Graphics2D g2d) {
        for (Connection conn : tree.getAllConnections()) {
            Block sourceBlock = conn.getSourceBlock();
            Block targetBlock = conn.getTargetBlock();

            Point source = sourceBlock.getConnectionPoint(conn.getSourceEdge());
            Point target = targetBlock.getConnectionPoint(conn.getTargetEdge());

            // Evidenzia se hover
            if (conn == hoveredConnection) {
                g2d.setColor(new Color(0, 120, 215));
                g2d.setStroke(new BasicStroke(4));
            } else {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
            }

            // Determina il tipo di connessione
            boolean isBackward = conn.isBackwardConnection();
            boolean isMerge = conn.isMergeConnection();

            if (isBackward) {
                // Usa Manhattan routing per frecce di ritorno (loop)
                drawManhattanConnection(g2d, source, target);
                drawArrow(g2d, source, target, true);
            } else if (isMerge) {
                // Usa Manhattan routing per frecce verso merge point
                drawMergeConnection(g2d, source, target, sourceBlock, targetBlock, conn.getMergeBranch());
                drawArrow(g2d, source, target, false);
            } else {
                // Linea diretta per frecce normali
                g2d.drawLine(source.x, source.y, target.x, target.y);
                drawArrow(g2d, source, target, false);
            }

            // Disegna label (SI/NO) se presente
            String label = conn.getLabel();
            if (label != null && !label.isEmpty()) {
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                int labelX, labelY;

                if (isBackward) {
                    // Per frecce di ritorno, posiziona label a destra
                    labelX = source.x + 80;
                    labelY = (source.y + target.y) / 2;
                } else {
                    // Per frecce normali, posiziona label vicino alla source
                    labelX = source.x + 10;
                    labelY = source.y + 20;
                }

                // Background bianco per leggibilità
                int labelWidth = fm.stringWidth(label);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(labelX - 2, labelY - fm.getAscent(), labelWidth + 4, fm.getHeight());

                // Disegna label
                g2d.setColor(Color.BLACK);
                g2d.drawString(label, labelX, labelY);
            }

            g2d.setStroke(new BasicStroke(1));
        }
    }

    private void drawManhattanConnection(Graphics2D g2d, Point source, Point target) {
        // Manhattan routing per frecce che tornano indietro (loop)
        int offset = 60; // Quanto spostare a destra

        // 1. Vai verso il basso un po'
        int midY1 = source.y + 30;
        g2d.drawLine(source.x, source.y, source.x, midY1);

        // 2. Vai a destra
        int rightX = source.x + offset;
        g2d.drawLine(source.x, midY1, rightX, midY1);

        // 3. Vai verso l'alto fino al livello del target
        int midY2 = target.y - 30;
        g2d.drawLine(rightX, midY1, rightX, midY2);

        // 4. Vai a sinistra verso il target
        g2d.drawLine(rightX, midY2, target.x, midY2);

        // 5. Vai verso il target finale
        g2d.drawLine(target.x, midY2, target.x, target.y);
    }

    /**
     * Draws a merge connection using Manhattan routing.
     * Handles connections from decision blocks to merge points or intermediate blocks.
     *
     * @param branch "left" or "right" indicating which branch of the decision block
     */
    private void drawMergeConnection(Graphics2D g2d, Point source, Point target,
                                     Block sourceBlock, Block targetBlock, String branch) {
        boolean isLeftBranch = "left".equals(branch);
        boolean isTargetMergePoint = (targetBlock.getType() == BlockType.MERGE);
        boolean isSourceDecision = (sourceBlock.getType() == BlockType.DECISION);

        if (isLeftBranch) {
            // LEFT BRANCH (YES)
            int leftX = isSourceDecision ? (source.x - BRANCH_HORIZONTAL_OFFSET) : source.x;

            if (isTargetMergePoint) {
                // Draw to merge point center
                int mergeCenterX = targetBlock.getPosition().x + targetBlock.getSize().width / 2;
                int mergeCenterY = targetBlock.getPosition().y + targetBlock.getSize().height / 2;

                if (isSourceDecision) {
                    // From decision: horizontal → vertical → horizontal
                    g2d.drawLine(source.x, source.y, leftX, source.y);  // Go LEFT
                    g2d.drawLine(leftX, source.y, leftX, mergeCenterY);  // Go DOWN
                    g2d.drawLine(leftX, mergeCenterY, mergeCenterX, mergeCenterY);  // Go RIGHT to merge
                } else {
                    // From intermediate block: vertical → horizontal
                    g2d.drawLine(source.x, source.y, source.x, mergeCenterY);  // Go DOWN
                    g2d.drawLine(source.x, mergeCenterY, mergeCenterX, mergeCenterY);  // Go RIGHT to merge
                }
            } else {
                // Draw to intermediate block
                if (isSourceDecision) {
                    // From decision: horizontal → vertical
                    g2d.drawLine(source.x, source.y, leftX, source.y);  // Go LEFT
                    g2d.drawLine(leftX, source.y, leftX, target.y);  // Go DOWN to target
                } else {
                    // From intermediate block: only vertical
                    g2d.drawLine(source.x, source.y, source.x, target.y);
                }
            }
        } else {
            // RIGHT BRANCH (NO)
            int rightX = isSourceDecision ? (source.x + BRANCH_HORIZONTAL_OFFSET) : source.x;

            if (isTargetMergePoint) {
                // Draw to merge point center
                int mergeCenterX = targetBlock.getPosition().x + targetBlock.getSize().width / 2;
                int mergeCenterY = targetBlock.getPosition().y + targetBlock.getSize().height / 2;

                if (isSourceDecision) {
                    // From decision: horizontal → vertical → horizontal
                    g2d.drawLine(source.x, source.y, rightX, source.y);  // Go RIGHT
                    g2d.drawLine(rightX, source.y, rightX, mergeCenterY);  // Go DOWN
                    g2d.drawLine(rightX, mergeCenterY, mergeCenterX, mergeCenterY);  // Go LEFT to merge
                } else {
                    // From intermediate block: vertical → horizontal
                    g2d.drawLine(source.x, source.y, source.x, mergeCenterY);  // Go DOWN
                    g2d.drawLine(source.x, mergeCenterY, mergeCenterX, mergeCenterY);  // Go LEFT to merge
                }
            } else {
                // Draw to intermediate block
                if (isSourceDecision) {
                    // From decision: horizontal → vertical
                    g2d.drawLine(source.x, source.y, rightX, source.y);  // Go RIGHT
                    g2d.drawLine(rightX, source.y, rightX, target.y);  // Go DOWN to target
                } else {
                    // From intermediate block: only vertical
                    g2d.drawLine(source.x, source.y, source.x, target.y);
                }
            }
        }
    }

    private void drawArrow(Graphics2D g2d, Point source, Point target, boolean isBackward) {
        int arrowSize = 10;
        double angle;

        if (isBackward) {
            // Per frecce di ritorno, la punta dell'arco punta verso il basso (dall'alto)
            angle = Math.PI / 2; // 90 gradi (verso il basso)
        } else {
            // Per frecce normali, calcola l'angolo dalla source al target
            angle = Math.atan2(target.y - source.y, target.x - source.x);
        }

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
            case MERGE:
                // Disegna un pallino blu per il merge point
                g2d.setColor(new Color(0, 120, 215));
                g2d.fillOval(pos.x, pos.y, size.width, size.height);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(pos.x, pos.y, size.width, size.height);
                g2d.setStroke(new BasicStroke(1));
                return; // Non disegnare testo per i pallini merge
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
