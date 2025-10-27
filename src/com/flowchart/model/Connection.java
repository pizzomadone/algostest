package com.flowchart.model;

import java.awt.Point;
import java.io.Serializable;

/**
 * Rappresenta una connessione tra due blocchi
 */
public class Connection implements Serializable {
    private static final long serialVersionUID = 1L;

    private Block sourceBlock;
    private Block targetBlock;
    private Connection targetMidpointConnection; // Se non null, la connessione punta al midpoint di questa connessione
    private String label; // Per le connessioni condizionali (es: "SI", "NO")
    private String sourceEdge; // Lato da cui esce la connessione: "top", "bottom", "left", "right"
    private String targetEdge; // Lato in cui entra la connessione: "top", "bottom", "left", "right"

    public Connection(Block sourceBlock, Block targetBlock) {
        this(sourceBlock, targetBlock, "");
    }

    public Connection(Block sourceBlock, Block targetBlock, String label) {
        this.sourceBlock = sourceBlock;
        this.targetBlock = targetBlock;
        this.label = label;
        // Calcola automaticamente i lati migliori
        calculateBestEdges();
    }

    public Connection(Block sourceBlock, Block targetBlock, String label, String sourceEdge, String targetEdge) {
        this.sourceBlock = sourceBlock;
        this.targetBlock = targetBlock;
        this.label = label;
        this.sourceEdge = sourceEdge;
        this.targetEdge = targetEdge;
    }

    /**
     * Costruttore per connessioni che puntano al midpoint di un'altra connessione
     */
    public Connection(Block sourceBlock, Connection targetMidpoint, String label) {
        this.sourceBlock = sourceBlock;
        this.targetMidpointConnection = targetMidpoint;
        this.targetBlock = null;
        this.label = label;
        // Calcola il lato di uscita
        Point midpoint = targetMidpoint.getMidpoint();
        this.sourceEdge = calculateBestEdge(sourceBlock, midpoint);
        this.targetEdge = null; // Non serve per midpoint
    }

    /**
     * Calcola automaticamente i lati migliori per connettere i due blocchi
     */
    private void calculateBestEdges() {
        Point sourceCenter = new Point(
            sourceBlock.getPosition().x + sourceBlock.getSize().width / 2,
            sourceBlock.getPosition().y + sourceBlock.getSize().height / 2
        );
        Point targetCenter = new Point(
            targetBlock.getPosition().x + targetBlock.getSize().width / 2,
            targetBlock.getPosition().y + targetBlock.getSize().height / 2
        );

        double dx = targetCenter.x - sourceCenter.x;
        double dy = targetCenter.y - sourceCenter.y;

        // Determina il lato del source block
        if (Math.abs(dx) > Math.abs(dy)) {
            // Connessione prevalentemente orizzontale
            sourceEdge = dx > 0 ? "right" : "left";
        } else {
            // Connessione prevalentemente verticale
            sourceEdge = dy > 0 ? "bottom" : "top";
        }

        // Determina il lato del target block (opposto rispetto alla direzione)
        if (Math.abs(dx) > Math.abs(dy)) {
            // Connessione prevalentemente orizzontale
            targetEdge = dx > 0 ? "left" : "right";
        } else {
            // Connessione prevalentemente verticale
            targetEdge = dy > 0 ? "top" : "bottom";
        }
    }

    public Block getSourceBlock() {
        return sourceBlock;
    }

    public Block getTargetBlock() {
        return targetBlock;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSourceEdge() {
        return sourceEdge;
    }

    public String getTargetEdge() {
        return targetEdge;
    }

    public Connection getTargetMidpointConnection() {
        return targetMidpointConnection;
    }

    public boolean isTargetingMidpoint() {
        return targetMidpointConnection != null;
    }

    /**
     * Restituisce il punto medio della connessione (dove disegnare la pallina)
     */
    public Point getMidpoint() {
        Point source = sourceBlock.getConnectionPoint(sourceEdge);
        Point target;

        if (isTargetingMidpoint()) {
            // Se punta a un midpoint, usa quel midpoint come target
            target = targetMidpointConnection.getMidpoint();
        } else {
            // Altrimenti usa il punto di connessione del blocco target
            target = targetBlock.getConnectionPoint(targetEdge);
        }

        return new Point((source.x + target.x) / 2, (source.y + target.y) / 2);
    }

    /**
     * Restituisce il punto finale della connessione (dove termina la freccia)
     */
    public Point getTargetPoint() {
        if (isTargetingMidpoint()) {
            return targetMidpointConnection.getMidpoint();
        } else {
            return targetBlock.getConnectionPoint(targetEdge);
        }
    }

    /**
     * Calcola il lato migliore da cui uscire in base alla posizione del mouse
     */
    public static String calculateBestEdge(Block block, Point mousePos) {
        Point blockCenter = new Point(
            block.getPosition().x + block.getSize().width / 2,
            block.getPosition().y + block.getSize().height / 2
        );

        double dx = mousePos.x - blockCenter.x;
        double dy = mousePos.y - blockCenter.y;

        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? "right" : "left";
        } else {
            return dy > 0 ? "bottom" : "top";
        }
    }

    @Override
    public String toString() {
        return sourceBlock.getId() + " -> " + targetBlock.getId() +
               (label.isEmpty() ? "" : " (" + label + ")");
    }
}

