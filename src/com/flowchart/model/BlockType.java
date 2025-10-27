package com.flowchart.model;

/**
 * Enumerazione dei tipi di blocchi disponibili nel diagramma a blocchi
 */
public enum BlockType {
    START("Inizio", "#4CAF50"),
    END("Fine", "#F44336"),
    PROCESS("Processo", "#2196F3"),
    DECISION("Decisione", "#FF9800"),
    INPUT("Input", "#9C27B0"),
    OUTPUT("Output", "#9C27B0");

    private final String displayName;
    private final String color;

    BlockType(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}
