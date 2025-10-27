package com.flowchart.model;

import java.io.Serializable;

/**
 * Rappresenta una connessione tra due blocchi
 */
public class Connection implements Serializable {
    private static final long serialVersionUID = 1L;

    private Block sourceBlock;
    private Block targetBlock;
    private String label; // Per le connessioni condizionali (es: "SI", "NO")

    public Connection(Block sourceBlock, Block targetBlock) {
        this(sourceBlock, targetBlock, "");
    }

    public Connection(Block sourceBlock, Block targetBlock, String label) {
        this.sourceBlock = sourceBlock;
        this.targetBlock = targetBlock;
        this.label = label;
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

    @Override
    public String toString() {
        return sourceBlock.getId() + " -> " + targetBlock.getId() +
               (label.isEmpty() ? "" : " (" + label + ")");
    }
}
