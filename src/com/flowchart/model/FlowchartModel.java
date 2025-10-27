package com.flowchart.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Modello del diagramma a blocchi che contiene tutti i blocchi e le connessioni
 */
public class FlowchartModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Block> blocks;
    private Map<String, Object> variables;

    public FlowchartModel() {
        this.blocks = new ArrayList<>();
        this.variables = new HashMap<>();
    }

    public void addBlock(Block block) {
        blocks.add(block);
    }

    public void removeBlock(Block block) {
        // Prima trova tutte le connessioni che saranno eliminate
        List<Connection> connectionsToRemove = new ArrayList<>();
        for (Block b : blocks) {
            for (Connection conn : b.getOutgoingConnections()) {
                // Controlla se la connessione coinvolge il blocco da rimuovere
                boolean shouldRemove = false;

                // Controlla se il source è il blocco da rimuovere
                if (conn.getSourceBlock().equals(block)) {
                    shouldRemove = true;
                }

                // Controlla se il target è il blocco da rimuovere (solo se non punta a midpoint)
                if (!conn.isTargetingMidpoint() && conn.getTargetBlock() != null && conn.getTargetBlock().equals(block)) {
                    shouldRemove = true;
                }

                if (shouldRemove) {
                    connectionsToRemove.add(conn);
                }
            }
        }

        // Rimuovi tutte le connessioni associate al blocco
        for (Block b : blocks) {
            List<Connection> toRemove = new ArrayList<>();
            for (Connection conn : b.getOutgoingConnections()) {
                // Rimuovi la connessione se è nella lista
                if (connectionsToRemove.contains(conn)) {
                    toRemove.add(conn);
                    continue;
                }

                // Rimuovi anche le connessioni che puntano ai pallini blu delle connessioni eliminate
                if (conn.isTargetingMidpoint()) {
                    Connection targetMidpoint = conn.getTargetMidpointConnection();
                    if (connectionsToRemove.contains(targetMidpoint)) {
                        toRemove.add(conn);
                    }
                }
            }
            b.getOutgoingConnections().removeAll(toRemove);
        }

        blocks.remove(block);
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public Block getBlockAt(int x, int y) {
        for (int i = blocks.size() - 1; i >= 0; i--) {
            Block block = blocks.get(i);
            if (block.contains(new java.awt.Point(x, y))) {
                return block;
            }
        }
        return null;
    }

    public Block getStartBlock() {
        for (Block block : blocks) {
            if (block.getType() == BlockType.START) {
                return block;
            }
        }
        return null;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public void clearVariables() {
        variables.clear();
    }

    public void clear() {
        blocks.clear();
        variables.clear();
    }
}
