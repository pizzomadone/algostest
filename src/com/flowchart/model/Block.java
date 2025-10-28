package com.flowchart.model;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Rappresenta un blocco nel diagramma a blocchi
 * VERSIONE RISTRUTTURATA: Grafo bidirezionale con incoming e outgoing connections
 */
public class Block implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private BlockType type;
    private String text;
    private Point position;
    private Dimension size;
    private boolean highlighted;
    private boolean visible;

    // GRAFO BIDIREZIONALE: liste separate per IN e OUT
    private final List<Connection> outgoingConnections;
    private final List<Connection> incomingConnections;

    public Block(BlockType type, String text, Point position) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.text = text;
        this.position = position;
        this.size = new Dimension(120, 60);
        this.highlighted = false;
        this.visible = true;
        this.outgoingConnections = new ArrayList<>();
        this.incomingConnections = new ArrayList<>();
    }

    /**
     * Aggiunge una connessione in USCITA e aggiorna automaticamente il target
     */
    public void addConnection(Connection conn) {
        if (conn.getSourceBlock() != this) return; // Sicurezza

        if (!outgoingConnections.contains(conn)) {
            outgoingConnections.add(conn);
            Block target = conn.getTargetBlock();
            if (target != null && target != this) {
                target.addIncomingConnectionInternal(conn);
            }
        }
    }

    /**
     * Aggiunge una connessione in ENTRATA (uso interno, chiamato da addConnection)
     */
    private void addIncomingConnectionInternal(Connection conn) {
        if (conn.getTargetBlock() != this) return;
        if (!incomingConnections.contains(conn)) {
            incomingConnections.add(conn);
        }
    }

    /**
     * Rimuove una connessione in USCITA e aggiorna automaticamente il target
     */
    public void removeConnection(Connection conn) {
        if (outgoingConnections.remove(conn)) {
            Block target = conn.getTargetBlock();
            if (target != null && target != this) {
                target.removeIncomingConnectionInternal(conn);
            }
        }
    }

    /**
     * Rimuove una connessione in ENTRATA (uso interno)
     */
    private void removeIncomingConnectionInternal(Connection conn) {
        incomingConnections.remove(conn);
    }

    /**
     * Pulisce tutte le connessioni del blocco
     */
    public void clearConnections() {
        // Copia per evitare ConcurrentModificationException
        new ArrayList<>(outgoingConnections).forEach(this::removeConnection);
        new ArrayList<>(incomingConnections).forEach(conn -> {
            Block source = conn.getSourceBlock();
            if (source != null && source != this) {
                source.removeConnection(conn);
            }
        });
        outgoingConnections.clear();
        incomingConnections.clear();
    }

    public String getId() {
        return id;
    }

    public BlockType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Dimension getSize() {
        return size;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }

    public List<Connection> getOutgoingConnections() {
        return outgoingConnections;
    }

    public List<Connection> getIncomingConnections() {
        return incomingConnections;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean contains(Point point) {
        return point.x >= position.x && point.x <= position.x + size.width &&
               point.y >= position.y && point.y <= position.y + size.height;
    }

    public Point getConnectionPoint(String direction) {
        int centerX = position.x + size.width / 2;
        int centerY = position.y + size.height / 2;

        switch (direction.toLowerCase()) {
            case "top":
                return new Point(centerX, position.y);
            case "bottom":
                return new Point(centerX, position.y + size.height);
            case "left":
                return new Point(position.x, centerY);
            case "right":
                return new Point(position.x + size.width, centerY);
            default:
                return new Point(centerX, position.y + size.height);
        }
    }

    @Override
    public String toString() {
        return type.getDisplayName() + ": " + text;
    }
}
