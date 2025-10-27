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

    public FlowchartCanvas(FlowchartModel model) {
        this.model = model;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));

        setupMouseListeners();
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
        Block clickedBlock = model.getBlockAt(e.getX(), e.getY());

        if (SwingUtilities.isRightMouseButton(e)) {
            // Click destro: inizia connessione
            if (clickedBlock != null) {
                connectionSourceBlock = clickedBlock;
                currentMousePos = e.getPoint();
            }
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            // Click sinistro: seleziona e trascina
            selectedBlock = clickedBlock;
            if (selectedBlock != null) {
                draggedBlock = selectedBlock;
                dragOffset = new Point(
                    e.getX() - selectedBlock.getPosition().x,
                    e.getY() - selectedBlock.getPosition().y
                );
            }
        }

        repaint();
    }

    private void handleMouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) && connectionSourceBlock != null) {
            // Completa la connessione
            Block targetBlock = model.getBlockAt(e.getX(), e.getY());
            if (targetBlock != null && targetBlock != connectionSourceBlock) {
                // Per blocchi decisionali, chiedi l'etichetta
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
        if (draggedBlock != null) {
            Point newPos = new Point(
                e.getX() - dragOffset.x,
                e.getY() - dragOffset.y
            );
            draggedBlock.setPosition(newPos);
            repaint();
        }
    }

    private void handleDoubleClick(MouseEvent e) {
        Block clickedBlock = model.getBlockAt(e.getX(), e.getY());
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
        }

        Block block = new Block(type, defaultText, new Point(50, 50));
        model.addBlock(block);
        repaint();
    }

    public void deleteSelectedBlock() {
        if (selectedBlock != null) {
            model.removeBlock(selectedBlock);
            selectedBlock = null;
            repaint();
        }
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

        // Disegna le connessioni
        drawConnections(g2d);

        // Disegna la connessione in corso
        if (connectionSourceBlock != null && currentMousePos != null) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
            Point sourcePoint = connectionSourceBlock.getConnectionPoint("bottom");
            g2d.drawLine(sourcePoint.x, sourcePoint.y, currentMousePos.x, currentMousePos.y);
            g2d.setStroke(new BasicStroke(1));
        }

        // Disegna i blocchi
        for (Block block : model.getBlocks()) {
            drawBlock(g2d, block);
        }
    }

    private void drawConnections(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        for (Block block : model.getBlocks()) {
            for (Connection conn : block.getOutgoingConnections()) {
                Point source = conn.getSourceBlock().getConnectionPoint("bottom");
                Point target = conn.getTargetBlock().getConnectionPoint("top");

                // Disegna la linea
                g2d.drawLine(source.x, source.y, target.x, target.y);

                // Disegna la freccia
                drawArrow(g2d, source, target);

                // Disegna l'etichetta se presente
                if (!conn.getLabel().isEmpty()) {
                    int midX = (source.x + target.x) / 2;
                    int midY = (source.y + target.y) / 2;
                    g2d.setColor(Color.RED);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString(conn.getLabel(), midX + 5, midY - 5);
                    g2d.setColor(Color.BLACK);
                }
            }
        }

        g2d.setStroke(new BasicStroke(1));
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
            g2d.setColor(Color.YELLOW);
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
                g2d.drawOval(pos.x, pos.y, size.width, size.height);
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
                g2d.drawPolygon(xPoints, yPoints, 4);
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
                g2d.drawPolygon(xPointsIO, yPointsIO, 4);
                break;

            case PROCESS:
            default:
                // Rettangolo
                g2d.fillRect(pos.x, pos.y, size.width, size.height);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(pos.x, pos.y, size.width, size.height);
                break;
        }

        // Disegna il testo
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
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
