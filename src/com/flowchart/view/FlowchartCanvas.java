package com.flowchart.view;

import com.flowchart.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Canvas per disegnare e gestire il diagramma a blocchi
 */
public class FlowchartCanvas extends JPanel {
    private FlowchartModel model;
    private Block selectedBlock;
    private Block draggedBlock;
    private Point dragOffset;
    private Block connectionSourceBlock;
    private Point currentMousePos;
    private String connectionLabel = "";
    private Connection selectedConnection;
    private double zoomFactor = 1.0;
    private JButton zoomInButton;
    private JButton zoomOutButton;

    public FlowchartCanvas(FlowchartModel model) {
        this.model = model;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        setLayout(null); // Layout assoluto per posizionare i pulsanti

        setupMouseListeners();
        setupKeyListeners();
        setupZoomButtons();
    }

    private void setupZoomButtons() {
        // Pulsante Zoom In
        zoomInButton = new JButton("+");
        zoomInButton.setFont(new Font("Arial", Font.BOLD, 20));
        zoomInButton.setFocusable(false);
        zoomInButton.addActionListener(e -> zoomIn());
        add(zoomInButton);

        // Pulsante Zoom Out
        zoomOutButton = new JButton("-");
        zoomOutButton.setFont(new Font("Arial", Font.BOLD, 20));
        zoomOutButton.setFocusable(false);
        zoomOutButton.addActionListener(e -> zoomOut());
        add(zoomOutButton);

        // Posiziona i pulsanti quando il componente viene ridimensionato
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
        // Aggiungi listener per i tasti
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (selectedConnection != null) {
                        // Elimina la connessione selezionata
                        for (Block block : model.getBlocks()) {
                            if (block.getOutgoingConnections().remove(selectedConnection)) {
                                selectedConnection = null;
                                repaint();
                                break;
                            }
                        }
                    } else if (selectedBlock != null) {
                        deleteSelectedBlock();
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
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                currentMousePos = e.getPoint();
                if (connectionSourceBlock != null) {
                    repaint();
                }
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
        Block clickedBlock = model.getBlockAt(worldPos.x, worldPos.y);

        if (SwingUtilities.isRightMouseButton(e)) {
            // Click destro: inizia connessione
            if (clickedBlock != null) {
                connectionSourceBlock = clickedBlock;
                currentMousePos = worldPos;
            }
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            // Click sinistro: seleziona blocco o connessione
            if (clickedBlock != null) {
                selectedBlock = clickedBlock;
                selectedConnection = null;
                draggedBlock = selectedBlock;
                dragOffset = new Point(
                    worldPos.x - selectedBlock.getPosition().x,
                    worldPos.y - selectedBlock.getPosition().y
                );
            } else {
                // Se non è un blocco, prova a selezionare una connessione
                selectedConnection = getConnectionAt(worldPos.x, worldPos.y);
                selectedBlock = null;
            }
        }

        requestFocusInWindow(); // Necessario per ricevere eventi tastiera
        repaint();
    }

    private Connection getConnectionAt(int x, int y) {
        // Trova la connessione più vicina al punto cliccato
        for (Block block : model.getBlocks()) {
            for (Connection conn : block.getOutgoingConnections()) {
                // Usa gli stessi metodi di drawConnections per gestire sia blocchi che midpoint
                Point source = conn.getSourceBlock().getConnectionPoint(conn.getSourceEdge());
                Point target = conn.getTargetPoint(); // Gestisce sia blocchi che midpoint

                // Calcola la distanza dalla linea
                double distance = distanceFromLine(x, y, source.x, source.y, target.x, target.y);
                if (distance < 10) { // Tolleranza di 10 pixel
                    return conn;
                }
            }
        }
        return null;
    }

    private Connection getConnectionMidpointAt(int x, int y) {
        // Trova se il punto è vicino a una pallina (punto medio di una connessione)
        for (Block block : model.getBlocks()) {
            for (Connection conn : block.getOutgoingConnections()) {
                Point midpoint = conn.getMidpoint();

                // Calcola distanza dal punto medio
                double distance = Math.sqrt(
                    (x - midpoint.x) * (x - midpoint.x) +
                    (y - midpoint.y) * (y - midpoint.y)
                );

                if (distance < 15) { // Tolleranza di 15 pixel (raggio della pallina + margine)
                    return conn;
                }
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

    private void handleMouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) && connectionSourceBlock != null) {
            Point worldPos = screenToWorld(e.getPoint());
            // Controlla se il rilascio è vicino a una pallina (punto medio di una connessione)
            Connection midpointConnection = getConnectionMidpointAt(worldPos.x, worldPos.y);

            if (midpointConnection != null) {
                // Crea una connessione che punta direttamente al pallino blu (midpoint)
                Connection connection = new Connection(connectionSourceBlock, midpointConnection, connectionLabel);
                connectionSourceBlock.addConnection(connection);
                connectionLabel = "";
                connectionSourceBlock = null;
                repaint();
                return;
            }

            // Comportamento normale: connetti a un blocco
            Block targetBlock = model.getBlockAt(worldPos.x, worldPos.y);

            if (targetBlock != null && targetBlock != connectionSourceBlock) {
                // Per blocchi decisionali e cicli, chiedi l'etichetta
                if (connectionSourceBlock.getType() == BlockType.DECISION) {
                    String[] options = {"SI", "NO", "Personalizza"};
                    int choice = JOptionPane.showOptionDialog(
                        this,
                        "Scegli l'etichetta per la connessione:",
                        "Etichetta Connessione",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                    );

                    if (choice == 0) {
                        connectionLabel = "SI";
                    } else if (choice == 1) {
                        connectionLabel = "NO";
                    } else if (choice == 2) {
                        connectionLabel = JOptionPane.showInputDialog(
                            this,
                            "Inserisci etichetta:",
                            "SI"
                        );
                        if (connectionLabel == null) connectionLabel = "";
                    }
                } else if (connectionSourceBlock.getType() == BlockType.FOR_LOOP ||
                           connectionSourceBlock.getType() == BlockType.WHILE_LOOP ||
                           connectionSourceBlock.getType() == BlockType.DO_WHILE_LOOP) {
                    String[] options = {"CORPO (SI)", "ESCI (NO)", "Personalizza"};
                    int choice = JOptionPane.showOptionDialog(
                        this,
                        "Scegli l'etichetta per la connessione del ciclo:",
                        "Etichetta Connessione Ciclo",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                    );

                    if (choice == 0) {
                        connectionLabel = "SI";
                    } else if (choice == 1) {
                        connectionLabel = "NO";
                    } else if (choice == 2) {
                        connectionLabel = JOptionPane.showInputDialog(
                            this,
                            "Inserisci etichetta:",
                            "SI"
                        );
                        if (connectionLabel == null) connectionLabel = "";
                    }
                }

                Connection connection = new Connection(connectionSourceBlock, targetBlock, connectionLabel);
                connectionSourceBlock.addConnection(connection);
                connectionLabel = "";
            }
            connectionSourceBlock = null;
        }

        draggedBlock = null;
        repaint();
    }

    private void handleMouseDragged(MouseEvent e) {
        Point worldPos = screenToWorld(e.getPoint());
        // Aggiorna sempre la posizione del mouse per le connessioni in corso
        currentMousePos = worldPos;

        if (draggedBlock != null) {
            Point newPos = new Point(
                worldPos.x - dragOffset.x,
                worldPos.y - dragOffset.y
            );
            draggedBlock.setPosition(newPos);
        }

        repaint();
    }

    private void handleDoubleClick(MouseEvent e) {
        Point worldPos = screenToWorld(e.getPoint());
        Block clickedBlock = model.getBlockAt(worldPos.x, worldPos.y);
        if (clickedBlock != null) {
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

    public void addBlock(BlockType type) {
        String defaultText = "";
        switch (type) {
            case START:
                defaultText = "Inizio";
                break;
            case END:
                defaultText = "Fine";
                break;
            case PROCESS:
                defaultText = "x = 0";
                break;
            case DECISION:
                defaultText = "x > 0";
                break;
            case INPUT:
                defaultText = "x";
                break;
            case OUTPUT:
                defaultText = "x";
                break;
            case FOR_LOOP:
                defaultText = "i=0; i<10; i=i+1";
                break;
            case WHILE_LOOP:
                defaultText = "i < 10";
                break;
            case DO_WHILE_LOOP:
                defaultText = "i < 10";
                break;
        }

        Block block = new Block(type, defaultText, new Point(50, 50));
        model.addBlock(block);
        repaint();
    }

    public void deleteSelected() {
        if (selectedConnection != null) {
            // Elimina la connessione selezionata
            for (Block block : model.getBlocks()) {
                if (block.getOutgoingConnections().remove(selectedConnection)) {
                    selectedConnection = null;
                    repaint();
                    return;
                }
            }
        } else if (selectedBlock != null) {
            // Elimina il blocco selezionato
            model.removeBlock(selectedBlock);
            selectedBlock = null;
            repaint();
        }
    }

    public void deleteSelectedBlock() {
        // Manteniamo per compatibilità, ma ora chiama deleteSelected
        deleteSelected();
    }

    public void clearAll() {
        model.clear();
        selectedBlock = null;
        connectionSourceBlock = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Applica lo zoom
        g2d.scale(zoomFactor, zoomFactor);

        // Disegna le connessioni
        drawConnections(g2d);

        // Disegna la connessione in corso
        if (connectionSourceBlock != null && currentMousePos != null) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
            // Calcola dinamicamente il lato in base alla posizione del mouse
            String sourceEdge = Connection.calculateBestEdge(connectionSourceBlock, currentMousePos);
            Point sourcePoint = connectionSourceBlock.getConnectionPoint(sourceEdge);
            g2d.drawLine(sourcePoint.x, sourcePoint.y, currentMousePos.x, currentMousePos.y);
            g2d.setStroke(new BasicStroke(1));
        }

        // Disegna i blocchi
        for (Block block : model.getBlocks()) {
            drawBlock(g2d, block);
        }
    }

    private void drawConnections(Graphics2D g2d) {
        for (Block block : model.getBlocks()) {
            for (Connection conn : block.getOutgoingConnections()) {
                // Usa i lati salvati nella connessione
                Point source = conn.getSourceBlock().getConnectionPoint(conn.getSourceEdge());
                Point target = conn.getTargetPoint(); // Gestisce sia blocchi che midpoint

                // Se la connessione è selezionata, evidenziala
                if (conn == selectedConnection) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(4));
                } else {
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(2));
                }

                // Disegna la linea (manhattan se necessario)
                Point arrowStart = drawConnectionLine(g2d, source, target);

                // Disegna la freccia
                drawArrow(g2d, arrowStart, target);

                // Disegna la pallina a metà della connessione solo se NON punta a un midpoint
                if (!conn.isTargetingMidpoint()) {
                    Point midpoint = conn.getMidpoint();
                    g2d.setColor(new Color(33, 150, 243)); // Blu
                    g2d.fillOval(midpoint.x - 8, midpoint.y - 8, 16, 16); // Cerchio di raggio 8
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawOval(midpoint.x - 8, midpoint.y - 8, 16, 16); // Bordo bianco
                    g2d.setStroke(new BasicStroke(1));
                }

                // Disegna l'etichetta se presente (spostata leggermente per non sovrapporsi alla pallina)
                if (!conn.getLabel().isEmpty()) {
                    int midX = (source.x + target.x) / 2;
                    int midY = (source.y + target.y) / 2;
                    g2d.setColor(Color.RED);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString(conn.getLabel(), midX + 12, midY - 12);
                }
            }
        }

        g2d.setStroke(new BasicStroke(1));
    }

    /**
     * Disegna la linea di connessione (diretta o manhattan style).
     * Restituisce il punto da cui inizia la freccia (l'ultimo punto prima del target).
     */
    private Point drawConnectionLine(Graphics2D g2d, Point source, Point target) {
        int dx = target.x - source.x;
        int dy = target.y - source.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Se la distanza è molto piccola, usa linea diretta
        if (distance < 50) {
            g2d.drawLine(source.x, source.y, target.x, target.y);
            return source;
        }

        // Calcola l'angolo di inclinazione
        double angle = Math.abs(Math.atan2(dy, dx));
        double angleDegrees = Math.toDegrees(angle);

        // Se l'angolo è vicino a 0, 90, 180, 270 gradi (+/- 30 gradi), usa linea diretta
        boolean isNearlyHorizontal = angleDegrees < 30 || angleDegrees > 150;
        boolean isNearlyVertical = (angleDegrees > 60 && angleDegrees < 120);

        if (isNearlyHorizontal || isNearlyVertical) {
            // Linea diretta
            g2d.drawLine(source.x, source.y, target.x, target.y);
            return source;
        }

        // Usa manhattan style: linee ortogonali
        // Determina se iniziare con segmento orizzontale o verticale in base ai lati
        boolean startHorizontal = Math.abs(dx) > Math.abs(dy);

        Point mid;
        if (startHorizontal) {
            // Vai prima orizzontalmente, poi verticalmente
            int midX = source.x + dx / 2;
            mid = new Point(midX, source.y);
            Point corner = new Point(midX, target.y);

            g2d.drawLine(source.x, source.y, mid.x, mid.y);
            g2d.drawLine(mid.x, mid.y, corner.x, corner.y);
            g2d.drawLine(corner.x, corner.y, target.x, target.y);

            return corner;
        } else {
            // Vai prima verticalmente, poi orizzontalmente
            int midY = source.y + dy / 2;
            mid = new Point(source.x, midY);
            Point corner = new Point(target.x, midY);

            g2d.drawLine(source.x, source.y, mid.x, mid.y);
            g2d.drawLine(mid.x, mid.y, corner.x, corner.y);
            g2d.drawLine(corner.x, corner.y, target.x, target.y);

            return corner;
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

        // Imposta il colore
        Color blockColor = Color.decode(block.getType().getColor());
        if (block == selectedBlock) {
            g2d.setColor(blockColor.darker());
        } else if (block.isHighlighted()) {
            // Colore giallo brillante per indicare esecuzione
            g2d.setColor(new Color(255, 235, 59));
        } else {
            g2d.setColor(blockColor);
        }

        // Disegna la forma in base al tipo
        switch (block.getType()) {
            case START:
            case END:
                // Ovale
                g2d.fillOval(pos.x, pos.y, size.width, size.height);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(pos.x, pos.y, size.width, size.height);
                g2d.setStroke(new BasicStroke(1));
                break;

            case DECISION:
                // Rombo
                int[] xPoints = {
                    pos.x + size.width / 2,
                    pos.x + size.width,
                    pos.x + size.width / 2,
                    pos.x
                };
                int[] yPoints = {
                    pos.y,
                    pos.y + size.height / 2,
                    pos.y + size.height,
                    pos.y + size.height / 2
                };
                g2d.fillPolygon(xPoints, yPoints, 4);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawPolygon(xPoints, yPoints, 4);
                g2d.setStroke(new BasicStroke(1));
                break;

            case INPUT:
            case OUTPUT:
                // Parallelogramma
                int offset = 15;
                int[] xPointsIO = {
                    pos.x + offset,
                    pos.x + size.width,
                    pos.x + size.width - offset,
                    pos.x
                };
                int[] yPointsIO = {
                    pos.y,
                    pos.y,
                    pos.y + size.height,
                    pos.y + size.height
                };
                g2d.fillPolygon(xPointsIO, yPointsIO, 4);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawPolygon(xPointsIO, yPointsIO, 4);
                g2d.setStroke(new BasicStroke(1));

                // Disegna icona I o O nella parte destra del parallelogramma
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.setColor(new Color(255, 255, 255, 180)); // Bianco semi-trasparente
                String icon = block.getType() == BlockType.INPUT ? "I" : "O";
                FontMetrics fm = g2d.getFontMetrics();
                int iconWidth = fm.stringWidth(icon);
                // Posiziona l'icona in alto a destra
                g2d.drawString(icon, pos.x + size.width - iconWidth - 20, pos.y + 26);
                break;

            case FOR_LOOP:
                // Esagono con etichetta FOR
                int wFor = size.width;
                int hFor = size.height;
                int indentFor = 20;
                int[] xPointsFor = {
                    pos.x + indentFor,
                    pos.x + wFor - indentFor,
                    pos.x + wFor,
                    pos.x + wFor - indentFor,
                    pos.x + indentFor,
                    pos.x
                };
                int[] yPointsFor = {
                    pos.y,
                    pos.y,
                    pos.y + hFor / 2,
                    pos.y + hFor,
                    pos.y + hFor,
                    pos.y + hFor / 2
                };
                g2d.fillPolygon(xPointsFor, yPointsFor, 6);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawPolygon(xPointsFor, yPointsFor, 6);

                // Etichetta FOR in alto a sinistra
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.drawString("FOR", pos.x + 5, pos.y + 12);
                g2d.setStroke(new BasicStroke(1));
                break;

            case WHILE_LOOP:
                // Esagono con etichetta WHILE
                int wWhile = size.width;
                int hWhile = size.height;
                int indentWhile = 20;
                int[] xPointsWhile = {
                    pos.x + indentWhile,
                    pos.x + wWhile - indentWhile,
                    pos.x + wWhile,
                    pos.x + wWhile - indentWhile,
                    pos.x + indentWhile,
                    pos.x
                };
                int[] yPointsWhile = {
                    pos.y,
                    pos.y,
                    pos.y + hWhile / 2,
                    pos.y + hWhile,
                    pos.y + hWhile,
                    pos.y + hWhile / 2
                };
                g2d.fillPolygon(xPointsWhile, yPointsWhile, 6);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawPolygon(xPointsWhile, yPointsWhile, 6);

                // Etichetta WHILE in alto a sinistra
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.drawString("WHILE", pos.x + 5, pos.y + 12);

                // Freccia di ritorno a sinistra per indicare il ciclo
                g2d.setColor(new Color(255, 255, 255, 150));
                g2d.setStroke(new BasicStroke(2));
                // Disegna freccia curva sul lato sinistro
                g2d.drawArc(pos.x - 15, pos.y + 10, 15, hWhile - 20, 90, 180);
                // Punta freccia verso l'alto
                g2d.drawLine(pos.x - 15, pos.y + 15, pos.x - 10, pos.y + 10);
                g2d.drawLine(pos.x - 15, pos.y + 15, pos.x - 15, pos.y + 20);
                g2d.setStroke(new BasicStroke(1));
                break;

            case DO_WHILE_LOOP:
                // Esagono con doppia linea in basso per distinguerlo
                int wDo = size.width;
                int hDo = size.height;
                int indentDo = 20;
                int[] xPointsDo = {
                    pos.x + indentDo,
                    pos.x + wDo - indentDo,
                    pos.x + wDo,
                    pos.x + wDo - indentDo,
                    pos.x + indentDo,
                    pos.x
                };
                int[] yPointsDo = {
                    pos.y,
                    pos.y,
                    pos.y + hDo / 2,
                    pos.y + hDo,
                    pos.y + hDo,
                    pos.y + hDo / 2
                };
                g2d.fillPolygon(xPointsDo, yPointsDo, 6);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawPolygon(xPointsDo, yPointsDo, 6);

                // Doppia linea in basso per distinguere DO-WHILE
                g2d.drawLine(pos.x + indentDo, pos.y + hDo - 5, pos.x + wDo - indentDo, pos.y + hDo - 5);

                // Etichetta DO-WHILE in alto a sinistra
                g2d.setFont(new Font("Arial", Font.BOLD, 9));
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.drawString("DO-WHILE", pos.x + 5, pos.y + 12);

                // Freccia di ritorno a sinistra per indicare il ciclo
                g2d.setColor(new Color(255, 255, 255, 150));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawArc(pos.x - 15, pos.y + 10, 15, hDo - 20, 90, 180);
                g2d.drawLine(pos.x - 15, pos.y + 15, pos.x - 10, pos.y + 10);
                g2d.drawLine(pos.x - 15, pos.y + 15, pos.x - 15, pos.y + 20);
                g2d.setStroke(new BasicStroke(1));
                break;

            case PROCESS:
            default:
                // Rettangolo
                g2d.fillRect(pos.x, pos.y, size.width, size.height);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(pos.x, pos.y, size.width, size.height);
                g2d.setStroke(new BasicStroke(1));
                break;
        }

        // Se il blocco è evidenziato (in esecuzione), disegna un bordo spesso lampeggiante
        if (block.isHighlighted()) {
            g2d.setColor(new Color(255, 215, 0)); // Oro
            g2d.setStroke(new BasicStroke(5));
            // Disegna un rettangolo esterno come indicatore
            g2d.drawRect(pos.x - 5, pos.y - 5, size.width + 10, size.height + 10);
            g2d.setStroke(new BasicStroke(1));
        }

        // Disegna il testo
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        drawCenteredString(g2d, block.getText(), pos.x, pos.y, size.width, size.height);
    }

    private void drawCenteredString(Graphics2D g2d, String text, int x, int y, int width, int height) {
        FontMetrics metrics = g2d.getFontMetrics();

        // Split text into lines if too long
        String[] words = text.split(" ");
        java.util.List<String> lines = new java.util.ArrayList<>();
        String currentLine = "";

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (metrics.stringWidth(testLine) < width - 10) {
                currentLine = testLine;
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine);
                }
                currentLine = word;
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }

        int lineHeight = metrics.getHeight();
        int totalHeight = lineHeight * lines.size();
        int startY = y + (height - totalHeight) / 2 + metrics.getAscent();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int stringWidth = metrics.stringWidth(line);
            int stringX = x + (width - stringWidth) / 2;
            int stringY = startY + i * lineHeight;
            g2d.drawString(line, stringX, stringY);
        }
    }

    public Block getSelectedBlock() {
        return selectedBlock;
    }
}
