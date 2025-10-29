package com.flowchart.model;

import static com.flowchart.layout.LayoutConstants.*;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the flowchart tree structure and delegates layout calculations
 * to SimpleLayoutCalculator for proper positioning of all blocks.
 */
public class FlowchartTree {
    private Block root;
    private final SimpleLayoutCalculator layoutCalculator;

    public FlowchartTree() {
        this.layoutCalculator = new SimpleLayoutCalculator();

        // Create START block
        root = new Block(BlockType.START, "Inizio", new Point(START_X, START_Y));

        // Create END block
        Block endBlock = new Block(BlockType.END, "Fine", new Point(START_X, START_Y + VERTICAL_SPACING));

        // Connect START → END
        Connection conn = new Connection(root, endBlock, "");
        root.addConnection(conn);
    }

    public Block getRoot() {
        return root;
    }

    /**
     * Inserts a new block into an existing connection.
     * Handles special cases for decision branches, loops, and regular connections.
     */
    public Block insertBlockInConnection(Connection conn, BlockType type, String text) {
        Block sourceBlock = conn.getSourceBlock();
        Block targetBlock = conn.getTargetBlock();

        // Remove the old connection
        sourceBlock.removeConnection(conn);

        // SPECIAL HANDLING: Decision block branches (merge connections)
        if (conn.isMergeConnection()) {
            return insertBlockInBranch(conn, type, text, sourceBlock, targetBlock);
        }

        // Calculate initial position (midpoint between source and target)
        int midX = (sourceBlock.getPosition().x + targetBlock.getPosition().x) / 2;
        int midY = (sourceBlock.getPosition().y + targetBlock.getPosition().y) / 2;

        // SPECIAL HANDLING: Loop structures (WHILE, FOR, DO-WHILE)
        if (type == BlockType.WHILE_LOOP || type == BlockType.FOR_LOOP || type == BlockType.DO_WHILE_LOOP) {
            return insertLoopBlock(type, text, sourceBlock, targetBlock, midX, midY);
        }

        // SPECIAL HANDLING: Decision blocks (IF)
        if (type == BlockType.DECISION) {
            return insertDecisionBlock(text, sourceBlock, targetBlock, midX, midY);
        }

        // REGULAR CASE: Simple linear insertion
        Block newBlock = new Block(type, text, new Point(midX, midY));

        // Create connections: source → new → target
        Connection conn1 = new Connection(sourceBlock, newBlock, "");
        Connection conn2 = new Connection(newBlock, targetBlock, "");
        sourceBlock.addConnection(conn1);
        newBlock.addConnection(conn2);

        // Recalculate layout to adjust all positions
        recalculateLayout();

        return newBlock;
    }

    /**
     * Inserts a block within a decision branch (left or right).
     * Maintains the branch structure and merge connection properties.
     *
     * SPECIAL CASE: If inserting a DECISION block (nested IF), creates the complete
     * structure with diamond, two branches, and merge point.
     */
    private Block insertBlockInBranch(Connection conn, BlockType type, String text,
                                      Block sourceBlock, Block targetBlock) {
        // Determine which branch we're in (left or right)
        String branch = conn.getMergeBranch();
        if (branch == null) {
            branch = "left".equals(conn.getSourceEdge()) ? "left" : "right";
        }

        // Calculate position - blocks in branches will be positioned by layout engine
        // but we need initial position for block creation
        Point sourcePos = sourceBlock.getPosition();
        int blockX = sourcePos.x;
        int blockY = (sourcePos.y + targetBlock.getPosition().y) / 2;

        // SPECIAL CASE: Inserting a DECISION block (nested IF)
        // Must create complete IF structure: diamond + 2 branches + merge point
        if (type == BlockType.DECISION) {
            Block decisionBlock = new Block(BlockType.DECISION, text, new Point(blockX, blockY));

            // Create merge point for this nested IF
            // It will be positioned between the decision and the target
            int nestedMergeX = blockX + 50;
            int nestedMergeY = blockY + VERTICAL_SPACING;
            Block nestedMergePoint = new Block(BlockType.MERGE, "", new Point(nestedMergeX, nestedMergeY));
            nestedMergePoint.setSize(new Dimension(MERGE_POINT_SIZE, MERGE_POINT_SIZE));
            nestedMergePoint.setVisible(true);

            // Connect source → decision (maintaining branch info)
            Connection toDecision = new Connection(sourceBlock, decisionBlock, conn.getLabel(),
                                                   conn.getSourceEdge(), "top");
            toDecision.setMergeConnection(true);
            toDecision.setMergeBranch(branch);
            sourceBlock.addConnection(toDecision);

            // Create two branches from decision to its merge point
            // Left branch (YES)
            Connection yesToNestedMerge = new Connection(decisionBlock, nestedMergePoint, "SI", "left", "left");
            yesToNestedMerge.setMergeConnection(true);
            yesToNestedMerge.setMergeBranch("left");
            decisionBlock.addConnection(yesToNestedMerge);

            // Right branch (NO)
            Connection noToNestedMerge = new Connection(decisionBlock, nestedMergePoint, "NO", "right", "right");
            noToNestedMerge.setMergeConnection(true);
            noToNestedMerge.setMergeBranch("right");
            decisionBlock.addConnection(noToNestedMerge);

            // Connect nested merge point → target (continuing in the outer branch)
            Connection nestedMergeToTarget = new Connection(nestedMergePoint, targetBlock, "",
                                                             "bottom", conn.getTargetEdge());
            nestedMergeToTarget.setMergeConnection(true);
            nestedMergeToTarget.setMergeBranch(branch); // Stay in same outer branch
            nestedMergePoint.addConnection(nestedMergeToTarget);

            recalculateLayout();
            return decisionBlock;
        }

        // SPECIAL CASE: Inserting a LOOP block (nested loop in branch)
        // Must create complete loop structure: loop block + body + backward connection
        if (type == BlockType.WHILE_LOOP || type == BlockType.FOR_LOOP || type == BlockType.DO_WHILE_LOOP) {
            Block loopBlock = new Block(type, text, new Point(blockX, blockY));
            Block bodyBlock = new Block(BlockType.LOOP_BODY, "corpo", new Point(blockX, blockY + VERTICAL_SPACING));

            // Connect source → loop (maintaining branch info)
            Connection toLoop = new Connection(sourceBlock, loopBlock, conn.getLabel(),
                                              conn.getSourceEdge(), "top");
            toLoop.setMergeConnection(true);
            toLoop.setMergeBranch(branch);
            sourceBlock.addConnection(toLoop);

            // loopBlock → bodyBlock (YES - enter loop body)
            Connection toBody = new Connection(loopBlock, bodyBlock, "SI", false);
            loopBlock.addConnection(toBody);

            // bodyBlock → loopBlock (return arrow) - MARKED AS BACKWARD
            Connection backToLoop = new Connection(bodyBlock, loopBlock, "", true);
            bodyBlock.addConnection(backToLoop);

            // loopBlock → targetBlock (NO - exit loop, continuing in outer branch)
            Connection exitLoop = new Connection(loopBlock, targetBlock, "NO", "bottom", conn.getTargetEdge());
            exitLoop.setMergeConnection(true);
            exitLoop.setMergeBranch(branch);
            loopBlock.addConnection(exitLoop);

            recalculateLayout();
            return loopBlock;
        }

        // REGULAR CASE: Simple block insertion
        Block newBlock = new Block(type, text, new Point(blockX, blockY));

        // Recreate connections maintaining Manhattan routing and branch info
        // source (decision or previous block) → newBlock
        Connection conn1 = new Connection(sourceBlock, newBlock, conn.getLabel(), conn.getSourceEdge(), "top");
        conn1.setMergeConnection(true);
        conn1.setMergeBranch(branch); // Propagate branch info
        sourceBlock.addConnection(conn1);

        // newBlock → target (merge point or next block)
        Connection conn2 = new Connection(newBlock, targetBlock, "", "bottom", conn.getTargetEdge());
        conn2.setMergeConnection(true);
        conn2.setMergeBranch(branch); // Propagate branch info
        newBlock.addConnection(conn2);

        recalculateLayout();
        return newBlock;
    }

    /**
     * Inserts a loop block (WHILE, FOR, or DO-WHILE) with its loop body.
     */
    private Block insertLoopBlock(BlockType type, String text, Block sourceBlock, Block targetBlock,
                                   int midX, int midY) {
        Block loopBlock = new Block(type, text, new Point(midX, midY));
        Block bodyBlock = new Block(BlockType.LOOP_BODY, "corpo", new Point(midX, midY + VERTICAL_SPACING));

        // Loop connections:
        // source → loopBlock
        Connection toLoop = new Connection(sourceBlock, loopBlock, "");
        sourceBlock.addConnection(toLoop);

        // loopBlock → bodyBlock (YES - enter loop body)
        Connection toBody = new Connection(loopBlock, bodyBlock, "SI", false);
        loopBlock.addConnection(toBody);

        // bodyBlock → loopBlock (return arrow) - MARKED AS BACKWARD
        Connection backToLoop = new Connection(bodyBlock, loopBlock, "", true);
        bodyBlock.addConnection(backToLoop);

        // loopBlock → targetBlock (NO - exit loop)
        Connection exitLoop = new Connection(loopBlock, targetBlock, "NO");
        loopBlock.addConnection(exitLoop);

        recalculateLayout();
        return loopBlock;
    }

    /**
     * Inserts a decision block (IF) with left and right branches and a merge point.
     */
    private Block insertDecisionBlock(String text, Block sourceBlock, Block targetBlock,
                                       int midX, int midY) {
        Block decisionBlock = new Block(BlockType.DECISION, text, new Point(midX, midY));

        // Create merge point (blue dot) - positioned by layout engine
        // Initial position calculation: centered under the decision block
        // Decision block: position (midX, midY), size 120x60
        // Center X of decision: midX + 60
        // Merge point size: 20x20, so X position = midX + 60 - 10 = midX + 50
        int mergePointX = midX + 50;
        int mergePointY = midY + VERTICAL_SPACING;
        Block mergePoint = new Block(BlockType.MERGE, "", new Point(mergePointX, mergePointY));
        mergePoint.setSize(new Dimension(MERGE_POINT_SIZE, MERGE_POINT_SIZE));
        mergePoint.setVisible(true);

        // source → decision
        Connection toDecision = new Connection(sourceBlock, decisionBlock, "");
        sourceBlock.addConnection(toDecision);

        // decision → merge (YES - exits from LEFT vertex)
        // Routing: left → down → right → merge point
        Connection yesToMerge = new Connection(decisionBlock, mergePoint, "SI", "left", "left");
        yesToMerge.setMergeConnection(true);
        yesToMerge.setMergeBranch("left"); // Left branch
        decisionBlock.addConnection(yesToMerge);

        // decision → merge (NO - exits from RIGHT vertex)
        // Routing: right → down → left → merge point
        Connection noToMerge = new Connection(decisionBlock, mergePoint, "NO", "right", "right");
        noToMerge.setMergeConnection(true);
        noToMerge.setMergeBranch("right"); // Right branch
        decisionBlock.addConnection(noToMerge);

        // merge → target (from bottom of merge point to top of target)
        Connection mergeToTarget = new Connection(mergePoint, targetBlock, "", "bottom", "top");
        mergePoint.addConnection(mergeToTarget);

        recalculateLayout();
        return decisionBlock;
    }

    /**
     * Rimuove un blocco e riconnette i blocchi circostanti
     * Usa la struttura bidirezionale per trovare parent e child in modo efficiente
     */
    public void removeBlock(Block block) {
        if (block.getType() == BlockType.START || block.getType() == BlockType.END) {
            return; // Non rimuovere START o END
        }

        // Blocchi strutturali (DECISION, MERGE, LOOP_BODY) non possono essere rimossi singolarmente
        if (block.getType() == BlockType.MERGE || block.getType() == BlockType.LOOP_BODY) {
            return;
        }

        // Usa la struttura bidirezionale: trova chi punta a questo blocco
        List<Connection> incomingConns = block.getIncomingConnections();
        List<Connection> outgoingConns = block.getOutgoingConnections();

        // Caso standard: 1 IN e 1 OUT
        if (incomingConns.size() == 1 && outgoingConns.size() == 1) {
            Connection inConn = incomingConns.get(0);
            Connection outConn = outgoingConns.get(0);

            Block parent = inConn.getSourceBlock();
            Block child = outConn.getTargetBlock();

            // Rimuovi le vecchie connessioni usando il metodo corretto
            parent.removeConnection(inConn);
            block.removeConnection(outConn);

            // Crea la nuova connessione ereditando TUTTE le proprietà dalla connessione in ingresso
            Connection newConn = new Connection(parent, child, inConn.getLabel(),
                                                inConn.getSourceEdge(), outConn.getTargetEdge());

            // CRITICAL: Eredita le proprietà del ramo per preservare la struttura
            newConn.setMergeConnection(inConn.isMergeConnection());
            newConn.setMergeBranch(inConn.getMergeBranch());
            newConn.setBackwardConnection(inConn.isBackwardConnection());

            parent.addConnection(newConn);

            // Ricalcola layout
            recalculateLayout();
            return;
        }

        // Caso DECISION: ha 0 IN e 2 OUT (rimuovi tutto il blocco decisionale)
        if (block.getType() == BlockType.DECISION) {
            // Trova il merge point
            Block mergePoint = findMergePointFromDecision(block);
            if (mergePoint == null) return;

            // Trova il blocco che viene dopo il merge point
            if (mergePoint.getOutgoingConnections().isEmpty()) return;
            Block afterMerge = mergePoint.getOutgoingConnections().get(0).getTargetBlock();

            // Trova chi punta al decision block
            if (incomingConns.isEmpty()) return;
            Connection inConn = incomingConns.get(0);
            Block parent = inConn.getSourceBlock();

            // Pulisci tutte le connessioni del decision e del merge
            block.clearConnections();
            mergePoint.clearConnections();

            // Riconnetti parent direttamente ad afterMerge
            Connection newConn = new Connection(parent, afterMerge, "");
            parent.addConnection(newConn);

            recalculateLayout();
            return;
        }

        // Caso ciclo: rimuovi tutto il blocco ciclo
        if (block.getType() == BlockType.WHILE_LOOP ||
            block.getType() == BlockType.FOR_LOOP ||
            block.getType() == BlockType.DO_WHILE_LOOP) {

            // Trova il corpo del ciclo
            Block bodyBlock = null;
            Block exitBlock = null;

            for (Connection conn : outgoingConns) {
                if (conn.getTargetBlock().getType() == BlockType.LOOP_BODY) {
                    bodyBlock = conn.getTargetBlock();
                } else if (!conn.isBackwardConnection()) {
                    exitBlock = conn.getTargetBlock();
                }
            }

            if (exitBlock == null) return;

            // Trova il parent
            if (incomingConns.isEmpty()) return;
            Connection inConn = incomingConns.get(0);
            Block parent = inConn.getSourceBlock();

            // Pulisci le connessioni
            block.clearConnections();
            if (bodyBlock != null) {
                bodyBlock.clearConnections();
            }

            // Riconnetti parent direttamente a exitBlock
            Connection newConn = new Connection(parent, exitBlock, "");
            parent.addConnection(newConn);

            recalculateLayout();
        }
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
     * Recalculates the layout of all blocks using the SimpleLayoutCalculator.
     * This is called whenever the structure changes (block insertion/deletion).
     */
    private void recalculateLayout() {
        layoutCalculator.recalculateLayout(root);
    }

    /**
     * Finds the merge point block connected to a decision block.
     * Follows the connections until a MERGE block is found.
     */
    private Block findMergePointFromDecision(Block decision) {
        for (Connection conn : decision.getOutgoingConnections()) {
            Block current = conn.getTargetBlock();
            while (current != null) {
                if (current.getType() == BlockType.MERGE) {
                    return current;
                }
                // Follow the first non-backward connection
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
}
