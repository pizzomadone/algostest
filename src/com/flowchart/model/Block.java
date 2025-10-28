package com.flowchart.model;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Rappresenta un blocco nel diagramma a blocchi
 */
public class Block implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private BlockType type;
    private String text;
    private Point position;
    private Dimension size;
    private List<Connection> outgoingConnections;
    private boolean highlighted;
    private boolean visible;

    public Block(BlockType type, String text, Point position) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.text = text;
        this.position = position;
        this.size = new Dimension(120, 60);
        this.outgoingConnections = new ArrayList<>();
        this.highlighted = false;
        this.visible = true;
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

    public void addConnection(Connection connection) {
        outgoingConnections.add(connection);
    }

    public void removeConnection(Connection connection) {
        outgoingConnections.remove(connection);
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
