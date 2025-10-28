package com.flowchart.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce il layout ad albero del flowchart con posizionamento automatico verticale
 */
public class FlowchartTree {
    private Block root;
    private static final int VERTICAL_SPACING = 100;
    private static final int HORIZONTAL_SPACING = 200;
    private static final int START_X = 400;
    private static final int START_Y = 50;

    public FlowchartTree() {
        // Crea blocco START
        root = new Block(BlockType.START, "Inizio", new Point(START_X, START_Y));

        // Crea blocco END
        Block endBlock = new Block(BlockType.END, "Fine", new Point(START_X, START_Y + VERTICAL_SPACING));

        // Connette START → END
        Connection conn = new Connection(root, endBlock, "");
        root.addConnection(conn);
    }

    public Block getRoot() {
        return root;
    }

    /**
     * Inserisce un nuovo blocco in mezzo a una connessione esistente
     */
    public Block insertBlockInConnection(Connection conn, BlockType type, String text) {
        Block sourceBlock = conn.getSourceBlock();
        Block targetBlock = conn.getTargetBlock();

        // Crea nuovo blocco a metà tra source e target
        int midX = (sourceBlock.getPosition().x + targetBlock.getPosition().x) / 2;
        int midY = (sourceBlock.getPosition().y + targetBlock.getPosition().y) / 2;
        Block newBlock = new Block(type, text, new Point(midX, midY));

        // Rimuove la vecchia connessione
        sourceBlock.getOutgoingConnections().remove(conn);

        // Crea nuove connessioni: source → nuovo → target
        Connection conn1 = new Connection(sourceBlock, newBlock, "");
        Connection conn2 = new Connection(newBlock, targetBlock, "");
        sourceBlock.addConnection(conn1);
        newBlock.addConnection(conn2);

        // Ricalcola layout per spostare tutto verso il basso
        recalculateLayout();

        return newBlock;
    }

    /**
     * Rimuove un blocco e riconnette i blocchi circostanti
     */
    public void removeBlock(Block block) {
        if (block.getType() == BlockType.START || block.getType() == BlockType.END) {
            return; // Non rimuovere START o END
        }

        // Trova chi punta a questo blocco
        Block parent = findParent(block);
        if (parent == null) return;

        // Trova dove punta questo blocco
        if (block.getOutgoingConnections().isEmpty()) return;
        Block child = block.getOutgoingConnections().get(0).getTargetBlock();

        // Rimuovi le vecchie connessioni
        parent.getOutgoingConnections().removeIf(c -> c.getTargetBlock() == block);
        block.getOutgoingConnections().clear();

        // Riconnetti parent → child
        Connection newConn = new Connection(parent, child, "");
        parent.addConnection(newConn);

        // Ricalcola layout
        recalculateLayout();
    }

    /**
     * Trova il blocco parent che punta a questo blocco
     */
    private Block findParent(Block target) {
        return findParentRecursive(root, target);
    }

    private Block findParentRecursive(Block current, Block target) {
        for (Connection conn : current.getOutgoingConnections()) {
            if (conn.getTargetBlock() == target) {
                return current;
            }
            Block found = findParentRecursive(conn.getTargetBlock(), target);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * Raccoglie tutti i blocchi dell'albero
     */
    public List<Block> getAllBlocks() {
        List<Block> blocks = new ArrayList<>();
        collectBlocksRecursive(root, blocks);
        return blocks;
    }

    private void collectBlocksRecursive(Block current, List<Block> blocks) {
        if (blocks.contains(current)) return;
        blocks.add(current);
        for (Connection conn : current.getOutgoingConnections()) {
            collectBlocksRecursive(conn.getTargetBlock(), blocks);
        }
    }

    /**
     * Raccoglie tutte le connessioni dell'albero
     */
    public List<Connection> getAllConnections() {
        List<Connection> connections = new ArrayList<>();
        collectConnectionsRecursive(root, connections);
        return connections;
    }

    private void collectConnectionsRecursive(Block current, List<Connection> connections) {
        for (Connection conn : current.getOutgoingConnections()) {
            if (!connections.contains(conn)) {
                connections.add(conn);
                collectConnectionsRecursive(conn.getTargetBlock(), connections);
            }
        }
    }

    /**
     * Ricalcola il layout di tutti i blocchi
     */
    private void recalculateLayout() {
        layoutBlockRecursive(root, START_X, START_Y, 0);
    }

    private int layoutBlockRecursive(Block block, int x, int y, int depth) {
        block.setPosition(new Point(x, y));

        List<Connection> outgoing = block.getOutgoingConnections();
        if (outgoing.isEmpty()) {
            return y;
        }

        int nextY = y + VERTICAL_SPACING;

        // Se ha una sola connessione (flusso lineare)
        if (outgoing.size() == 1) {
            Connection conn = outgoing.get(0);
            return layoutBlockRecursive(conn.getTargetBlock(), x, nextY, depth);
        }

        // Se ha più connessioni (ramificazione)
        int currentX = x - (outgoing.size() - 1) * HORIZONTAL_SPACING / 2;
        int maxY = nextY;

        for (Connection conn : outgoing) {
            int branchEndY = layoutBlockRecursive(conn.getTargetBlock(), currentX, nextY, depth + 1);
            maxY = Math.max(maxY, branchEndY);
            currentX += HORIZONTAL_SPACING;
        }

        return maxY;
    }
}
