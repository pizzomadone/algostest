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
        // Rimuovi tutte le connessioni associate al blocco
        for (Block b : blocks) {
            List<Connection> toRemove = new ArrayList<>();
            for (Connection conn : b.getOutgoingConnections()) {
                if (conn.getTargetBlock().equals(block) || conn.getSourceBlock().equals(block)) {
                    toRemove.add(conn);
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
