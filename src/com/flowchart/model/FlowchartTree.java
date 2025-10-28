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

        // GESTIONE SPECIALE per MERGE connections (archi del blocco decisionale)
        if (conn.isMergeConnection()) {
            // Determina in quale ramo siamo (preserva dall'originale o usa sourceEdge)
            String branch = conn.getMergeBranch();
            if (branch == null) {
                branch = "left".equals(conn.getSourceEdge()) ? "left" : "right";
            }

            // Inserisco il blocco sul segmento verticale
            // Calcolo posizione X sulla linea verticale (dipende dal ramo)
            int blockX;
            Point sourcePos = sourceBlock.getPosition();
            int horizontalOffset = 60;

            if ("left".equals(branch)) {
                // Ramo sinistro: X è a sinistra del rombo
                blockX = sourcePos.x + 60 - horizontalOffset - 60; // Centro blocco sulla linea verticale sinistra
            } else {
                // Ramo destro: X è a destra del rombo
                blockX = sourcePos.x + 60 + horizontalOffset - 60; // Centro blocco sulla linea verticale destra
            }

            // Y: metà tra source e target
            int blockY = (sourcePos.y + targetBlock.getPosition().y) / 2;

            Block newBlock = new Block(type, text, new Point(blockX, blockY));

            // Ricrea le connessioni mantenendo il routing Manhattan E il ramo
            // source (rombo o blocco precedente) → newBlock
            Connection conn1 = new Connection(sourceBlock, newBlock, conn.getLabel(), conn.getSourceEdge(), "top");
            conn1.setMergeConnection(true);
            conn1.setMergeBranch(branch); // Propaga il ramo
            sourceBlock.addConnection(conn1);

            // newBlock → target (pallino o prossimo blocco)
            Connection conn2 = new Connection(newBlock, targetBlock, "", "bottom", conn.getTargetEdge());
            conn2.setMergeConnection(true);
            conn2.setMergeBranch(branch); // Propaga il ramo
            newBlock.addConnection(conn2);

            recalculateLayout();
            return newBlock;
        }

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

            // Calcola posizione del pallino: CENTRATO sotto il rombo
            // Rombo: posizione (midX, midY), dimensione 120x60
            // Centro del rombo: (midX + 60, midY + 30)
            // Pallino: dimensione 20x20, deve avere centro uguale al centro X del rombo
            // Quindi pallino.x = midX + 60 - 10 = midX + 50
            int pallinoX = midX + 50;
            int pallinoY = midY + VERTICAL_SPACING;
            Block mergePoint = new Block(BlockType.MERGE, "", new Point(pallinoX, pallinoY));
            mergePoint.setSize(new java.awt.Dimension(20, 20));
            mergePoint.setVisible(true);

            // source → decision
            Connection toDecision = new Connection(sourceBlock, decisionBlock, "");
            sourceBlock.addConnection(toDecision);

            // decision → merge (SI - esce dal vertice SINISTRO)
            // Routing: sinistra → giù → destra → pallino
            Connection siToMerge = new Connection(decisionBlock, mergePoint, "SI", "left", "left");
            siToMerge.setMergeConnection(true);
            siToMerge.setMergeBranch("left"); // Ramo sinistro
            decisionBlock.addConnection(siToMerge);

            // decision → merge (NO - esce dal vertice DESTRO)
            // Routing: destra → giù → sinistra → pallino
            Connection noToMerge = new Connection(decisionBlock, mergePoint, "NO", "right", "right");
            noToMerge.setMergeConnection(true);
            noToMerge.setMergeBranch("right"); // Ramo destro
            decisionBlock.addConnection(noToMerge);

            // merge → target (dal basso del pallino verso l'alto del target)
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

        // I MERGE points non vengono riposizionati dal layout automatico
        // Mantengono la posizione manuale impostata quando sono stati creati
        if (block.getType() == BlockType.MERGE) {
            // Continua il layout per i blocchi successivi senza spostare questo
            List<Connection> outgoing = block.getOutgoingConnections();
            if (!outgoing.isEmpty()) {
                Connection conn = outgoing.get(0);
                Block target = conn.getTargetBlock();
                if (target != null && !visited.contains(target)) {
                    // Continua dal merge point con Y dopo il merge
                    int mergeY = block.getPosition().y;
                    return layoutBlockRecursive(target, x, mergeY + VERTICAL_SPACING, depth, visited);
                }
            }
            return block.getPosition().y;
        }

        block.setPosition(new Point(x, y));

        // Se è un blocco DECISION, trova e posiziona il merge point
        if (block.getType() == BlockType.DECISION) {
            // Trova il merge point seguendo le connessioni (potrebbe non essere diretto)
            Block mergePoint = findMergePointFromDecision(block);
            if (mergePoint != null) {
                // Calcola Y del merge point: deve essere sotto tutti i blocchi intermedi
                int maxY = y + 60; // Parte dal vertice inferiore del rombo

                // Trova il blocco più in basso tra i rami SI e NO
                for (Connection conn : block.getOutgoingConnections()) {
                    int branchMaxY = findMaxYInBranch(conn.getTargetBlock(), visited);
                    maxY = Math.max(maxY, branchMaxY);
                }

                // Posiziona il merge point centrato sotto il rombo e sotto tutti i blocchi
                int pallinoX = x + 50;
                int pallinoY = maxY + 40; // 40 pixel sotto l'ultimo blocco
                mergePoint.setPosition(new Point(pallinoX, pallinoY));
            }
        }

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

        // Se ha più connessioni forward (ramificazione - blocco decision)
        int currentX = x - (forwardConnections.size() - 1) * HORIZONTAL_SPACING / 2;
        int maxY = nextY;

        for (Connection conn : forwardConnections) {
            int branchEndY = layoutBlockRecursive(conn.getTargetBlock(), currentX, nextY, depth + 1, visited);
            maxY = Math.max(maxY, branchEndY);
            currentX += HORIZONTAL_SPACING;
        }

        return maxY;
    }

    /**
     * Trova il merge point collegato a un blocco DECISION
     */
    private Block findMergePointFromDecision(Block decision) {
        // Segue le connessioni fino a trovare un MERGE
        for (Connection conn : decision.getOutgoingConnections()) {
            Block current = conn.getTargetBlock();
            while (current != null) {
                if (current.getType() == BlockType.MERGE) {
                    return current;
                }
                // Segue la prima connessione non backward
                Block next = null;
                for (Connection c : current.getOutgoingConnections()) {
                    if (!c.isBackwardConnection() && c.getTargetBlock() != null) {
                        next = c.getTargetBlock();
                        break;
                    }
                }
                current = next;
            }
        }
        return null;
    }

    /**
     * Trova la Y massima (blocco più in basso) in un ramo prima del merge point
     */
    private int findMaxYInBranch(Block start, List<Block> visited) {
        if (start == null || start.getType() == BlockType.MERGE) {
            return 0;
        }

        int maxY = start.getPosition().y + start.getSize().height;

        // Segue le connessioni non backward
        for (Connection conn : start.getOutgoingConnections()) {
            if (!conn.isBackwardConnection()) {
                Block target = conn.getTargetBlock();
                if (target != null && target.getType() != BlockType.MERGE && !visited.contains(target)) {
                    int branchY = findMaxYInBranch(target, visited);
                    maxY = Math.max(maxY, branchY);
                }
            }
        }

        return maxY;
    }
}
