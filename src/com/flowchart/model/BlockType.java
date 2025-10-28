package com.flowchart.model;

/**
 * Enumerazione dei tipi di blocchi disponibili nel diagramma a blocchi
 */
public enum BlockType {
    START("Inizio", "#2E7D32"),
    END("Fine", "#C62828"),
    PROCESS("Processo", "#1565C0"),
    DECISION("Decisione", "#E65100"),
    INPUT("Input", "#6A1B9A"),
    OUTPUT("Output", "#4A148C"),
    FOR_LOOP("Ciclo FOR", "#00838F"),
    WHILE_LOOP("Ciclo WHILE", "#00695C"),
    DO_WHILE_LOOP("Ciclo DO-WHILE", "#004D40"),
    MERGE("Merge", "#FFFFFF"),
    LOOP_BODY("Corpo Ciclo", "#009688");

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
