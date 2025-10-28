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

        // Rimuove la vecchia connessione
        sourceBlock.getOutgoingConnections().remove(conn);

        // Crea nuovo blocco a metà tra source e target
        int midX = (sourceBlock.getPosition().x + targetBlock.getPosition().x) / 2;
        int midY = (sourceBlock.getPosition().y + targetBlock.getPosition().y) / 2;

        // Gestione speciale per cicli (WHILE, FOR, DO-WHILE)
        if (type == BlockType.WHILE_LOOP || type == BlockType.FOR_LOOP || type == BlockType.DO_WHILE_LOOP) {
            Block loopBlock = new Block(type, text, new Point(midX, midY));
            Block bodyBlock = new Block(BlockType.LOOP_BODY, "corpo", new Point(midX, midY + VERTICAL_SPACING));

            // Connessioni per il ciclo:
            // source → loopBlock
            Connection toLoop = new Connection(sourceBlock, loopBlock, "");
            sourceBlock.addConnection(toLoop);

            // loopBlock → bodyBlock (SI - entra nel corpo)
            Connection toBody = new Connection(loopBlock, bodyBlock, "SI", false);
            loopBlock.addConnection(toBody);

            // bodyBlock → loopBlock (freccia di ritorno) - MARCATA COME BACKWARD
            Connection backToLoop = new Connection(bodyBlock, loopBlock, "", true);
            bodyBlock.addConnection(backToLoop);

            // loopBlock → targetBlock (NO - esce dal ciclo)
            Connection exitLoop = new Connection(loopBlock, targetBlock, "NO");
            loopBlock.addConnection(exitLoop);

            recalculateLayout();
            return loopBlock;
        }

        // Gestione speciale per decisioni (IF)
        if (type == BlockType.DECISION) {
            Block decisionBlock = new Block(type, text, new Point(midX, midY));

            // Crea punto di merge VISIBILE come pallino - CENTRATO SOTTO il rombo
            // Il rombo ha dimensione 120x60, quindi centro a (midX + 60, midY + 30)
            // Pallino di 20x20 deve avere centro a (midX + 60, ...)
            // Posizione pallino (angolo alto-sinistra) = (midX + 60 - 10, ...) = (midX + 50, ...)
            int pallinoX = midX + 50; // Centra il pallino rispetto al rombo
            int pallinoY = midY + VERTICAL_SPACING;
            Block mergePoint = new Block(BlockType.MERGE, "", new Point(pallinoX, pallinoY));
            mergePoint.setSize(new java.awt.Dimension(20, 20)); // Pallino piccolo
            mergePoint.setVisible(true);

            // Connessioni:
            // source → decision
            Connection toDecision = new Connection(sourceBlock, decisionBlock, "");
            sourceBlock.addConnection(toDecision);

            // decision → merge (SI - esce dal vertice SINISTRO del rombo)
            Connection siToMerge = new Connection(decisionBlock, mergePoint, "SI", "left", "top");
            siToMerge.setMergeConnection(true);
            decisionBlock.addConnection(siToMerge);

            // decision → merge (NO - esce dal vertice DESTRO del rombo)
            Connection noToMerge = new Connection(decisionBlock, mergePoint, "NO", "right", "top");
            noToMerge.setMergeConnection(true);
            decisionBlock.addConnection(noToMerge);

            // merge → target (freccia esce dal BASSO del pallino e va in ALTO del target)
            Connection mergeToTarget = new Connection(mergePoint, targetBlock, "", "bottom", "top");
            mergePoint.addConnection(mergeToTarget);

            recalculateLayout();
            return decisionBlock;
        }

        // Caso normale: inserimento semplice
        Block newBlock = new Block(type, text, new Point(midX, midY));

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
            Block target = conn.getTargetBlock();
            if (target != null && !blocks.contains(target)) {
                collectBlocksRecursive(target, blocks);
            }
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
        List<Block> visited = new ArrayList<>();
        layoutBlockRecursive(root, START_X, START_Y, 0, visited);
    }

    private int layoutBlockRecursive(Block block, int x, int y, int depth, List<Block> visited) {
        // Evita cicli infiniti (per le frecce di ritorno dei loop)
        if (visited.contains(block)) {
            return y;
        }
        visited.add(block);

        block.setPosition(new Point(x, y));

        List<Connection> outgoing = block.getOutgoingConnections();
        if (outgoing.isEmpty()) {
            return y;
        }

        // Filtra le connessioni backward (frecce di ritorno dei loop)
        List<Connection> forwardConnections = new ArrayList<>();
        for (Connection conn : outgoing) {
            if (!conn.isBackwardConnection() && conn.getTargetBlock() != null) {
                forwardConnections.add(conn);
            }
        }

        if (forwardConnections.isEmpty()) {
            return y;
        }

        int nextY = y + VERTICAL_SPACING;

        // Se ha una sola connessione forward (flusso lineare)
        if (forwardConnections.size() == 1) {
            Connection conn = forwardConnections.get(0);
            return layoutBlockRecursive(conn.getTargetBlock(), x, nextY, depth, visited);
        }

        // Se ha più connessioni forward (ramificazione)
        int currentX = x - (forwardConnections.size() - 1) * HORIZONTAL_SPACING / 2;
        int maxY = nextY;

        for (Connection conn : forwardConnections) {
            int branchEndY = layoutBlockRecursive(conn.getTargetBlock(), currentX, nextY, depth + 1, visited);
            maxY = Math.max(maxY, branchEndY);
            currentX += HORIZONTAL_SPACING;
        }

        return maxY;
    }
}
