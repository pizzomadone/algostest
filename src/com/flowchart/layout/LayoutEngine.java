package com.flowchart.layout;

import com.flowchart.model.Block;
import com.flowchart.model.BlockType;
import com.flowchart.model.Connection;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.flowchart.layout.LayoutConstants.*;

/**
 * Layout engine that uses a two-pass algorithm to position blocks correctly.
 *
 * Pass 1 (Bottom-Up): Calculate required dimensions for each subtree
 * Pass 2 (Top-Down): Assign absolute positions based on calculated dimensions
 *
 * This approach ensures that:
 * - Nested IF blocks are handled correctly
 * - Branches have appropriate width based on their contents
 * - Merge points are centered correctly
 */
public class LayoutEngine {

    /**
     * Performs layout calculation on the entire flowchart tree.
     * This is the main entry point for the layout system.
     *
     * @param root The root block (START block)
     */
    public void performLayout(Block root) {
        // Build the layout tree structure
        LayoutNode layoutRoot = buildLayoutTree(root, new HashSet<>());

        if (layoutRoot == null) {
            return;
        }

        // Pass 1: Calculate required dimensions (bottom-up)
        calculateDimensions(layoutRoot);

        // Pass 2: Assign absolute positions (top-down)
        assignPositions(layoutRoot, START_X, START_Y);

        // Pass 3: Apply calculated positions to actual blocks
        applyPositionsToBlocks(layoutRoot);
    }

    /**
     * Builds a layout tree from the flowchart block structure.
     * This converts the graph structure into a tree suitable for layout calculations.
     *
     * @param block The current block to process
     * @param visited Set of already visited blocks to avoid cycles
     * @return The layout node for this block
     */
    private LayoutNode buildLayoutTree(Block block, Set<Block> visited) {
        if (block == null || visited.contains(block)) {
            return null;
        }

        visited.add(block);

        // Create layout node based on block type
        if (block.getType() == BlockType.DECISION) {
            return buildDecisionNode(block, visited);
        } else if (block.getType() == BlockType.MERGE) {
            // Merge points are handled as part of decision blocks
            LayoutNode node = new LayoutNode(LayoutNode.NodeType.MERGE, block);
            // Continue after merge point
            List<Connection> outgoing = block.getOutgoingConnections();
            if (!outgoing.isEmpty()) {
                LayoutNode child = buildLayoutTree(outgoing.get(0).getTargetBlock(), visited);
                if (child != null) {
                    node.addChild(child);
                }
            }
            return node;
        } else if (isLoopBlock(block.getType())) {
            return buildLoopNode(block, visited);
        } else {
            return buildLinearNode(block, visited);
        }
    }

    /**
     * Builds a layout node for a decision (IF) block.
     * Creates a structure with left branch, right branch, and merge point.
     */
    private LayoutNode buildDecisionNode(Block decisionBlock, Set<Block> visited) {
        LayoutNode node = new LayoutNode(LayoutNode.NodeType.DECISION, decisionBlock);

        List<Connection> outgoing = decisionBlock.getOutgoingConnections();

        if (outgoing.size() < 2) {
            // Malformed decision block
            return node;
        }

        // Find the left (SI) and right (NO) branches
        Connection leftConnection = null;
        Connection rightConnection = null;

        for (Connection conn : outgoing) {
            if ("left".equals(conn.getMergeBranch()) || conn.getLabel().contains("SI")) {
                leftConnection = conn;
            } else if ("right".equals(conn.getMergeBranch()) || conn.getLabel().contains("NO")) {
                rightConnection = conn;
            }
        }

        // Build left branch
        if (leftConnection != null) {
            LayoutNode leftBranch = buildBranchNode(
                    leftConnection.getTargetBlock(),
                    LayoutNode.BranchSide.LEFT,
                    visited
            );
            node.setLeftBranch(leftBranch);
        }

        // Build right branch
        if (rightConnection != null) {
            LayoutNode rightBranch = buildBranchNode(
                    rightConnection.getTargetBlock(),
                    LayoutNode.BranchSide.RIGHT,
                    visited
            );
            node.setRightBranch(rightBranch);
        }

        return node;
    }

    /**
     * Builds a layout node for a branch of a decision block.
     * Follows the branch until reaching the merge point.
     */
    private LayoutNode buildBranchNode(Block startBlock, LayoutNode.BranchSide side, Set<Block> visited) {
        LayoutNode branchNode = new LayoutNode(LayoutNode.NodeType.BRANCH);
        branchNode.setBranchSide(side);

        Block current = startBlock;

        // Follow the branch until we hit a MERGE block
        while (current != null && current.getType() != BlockType.MERGE && !visited.contains(current)) {
            visited.add(current);

            // Check if this block is a decision (nested IF)
            if (current.getType() == BlockType.DECISION) {
                LayoutNode decisionNode = buildDecisionNode(current, visited);
                branchNode.addChild(decisionNode);

                // After a decision, we need to find what comes after its merge
                Block mergePoint = findMergePoint(current);
                if (mergePoint != null && !visited.contains(mergePoint)) {
                    visited.add(mergePoint);
                    // Continue after the merge point
                    List<Connection> afterMerge = mergePoint.getOutgoingConnections();
                    if (!afterMerge.isEmpty()) {
                        current = afterMerge.get(0).getTargetBlock();
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                // Regular block in the branch
                LayoutNode blockNode = new LayoutNode(LayoutNode.NodeType.LINEAR, current);
                branchNode.addChild(blockNode);

                // Move to next block
                List<Connection> outgoing = current.getOutgoingConnections();
                if (!outgoing.isEmpty()) {
                    Connection next = outgoing.get(0);
                    if (!next.isBackwardConnection()) {
                        current = next.getTargetBlock();
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return branchNode;
    }

    /**
     * Builds a layout node for a loop block.
     */
    private LayoutNode buildLoopNode(Block loopBlock, Set<Block> visited) {
        LayoutNode node = new LayoutNode(LayoutNode.NodeType.LOOP, loopBlock);

        // Find the loop body and exit connections
        for (Connection conn : loopBlock.getOutgoingConnections()) {
            if (conn.getTargetBlock().getType() == BlockType.LOOP_BODY) {
                // Loop body - don't recurse into it to avoid cycles
                LayoutNode bodyNode = new LayoutNode(LayoutNode.NodeType.LINEAR, conn.getTargetBlock());
                node.addChild(bodyNode);
            } else if (!conn.isBackwardConnection()) {
                // Exit connection - continue building tree
                LayoutNode exitNode = buildLayoutTree(conn.getTargetBlock(), visited);
                if (exitNode != null) {
                    node.addChild(exitNode);
                }
            }
        }

        return node;
    }

    /**
     * Builds a layout node for a regular (linear) block.
     */
    private LayoutNode buildLinearNode(Block block, Set<Block> visited) {
        LayoutNode node = new LayoutNode(LayoutNode.NodeType.LINEAR, block);

        // Continue with next block
        List<Connection> outgoing = block.getOutgoingConnections();
        if (!outgoing.isEmpty()) {
            Connection next = outgoing.get(0);
            if (!next.isBackwardConnection()) {
                LayoutNode child = buildLayoutTree(next.getTargetBlock(), visited);
                if (child != null) {
                    node.addChild(child);
                }
            }
        }

        return node;
    }

    /**
     * Pass 1: Calculate required dimensions for each node (bottom-up).
     *
     * For each node type:
     * - LINEAR: width = block width, height = block height + spacing + children height
     * - DECISION: width = max(branch widths) + offset, height = decision height + branch height + merge spacing
     * - BRANCH: width = max(children widths), height = sum(children heights + spacing)
     * - MERGE: width = merge point width, height = merge point height
     */
    private void calculateDimensions(LayoutNode node) {
        if (node == null) {
            return;
        }

        // First, calculate dimensions for all children (bottom-up)
        for (LayoutNode child : node.getChildren()) {
            calculateDimensions(child);
        }

        // Also calculate dimensions for branch nodes
        if (node.isDecision()) {
            if (node.getLeftBranch() != null) {
                calculateDimensions(node.getLeftBranch());
            }
            if (node.getRightBranch() != null) {
                calculateDimensions(node.getRightBranch());
            }
            if (node.getMergeNode() != null) {
                calculateDimensions(node.getMergeNode());
            }
        }

        // Now calculate this node's dimensions based on type
        switch (node.getNodeType()) {
            case LINEAR:
                calculateLinearDimensions(node);
                break;
            case DECISION:
                calculateDecisionDimensions(node);
                break;
            case BRANCH:
                calculateBranchDimensions(node);
                break;
            case MERGE:
                calculateMergeDimensions(node);
                break;
            case LOOP:
                calculateLoopDimensions(node);
                break;
        }
    }

    private void calculateLinearDimensions(LayoutNode node) {
        Block block = node.getBlock();
        Dimension blockSize = block != null ? block.getSize() : new Dimension(120, 60);

        int width = blockSize.width;
        int height = blockSize.height;

        // Add height of children
        if (!node.getChildren().isEmpty()) {
            LayoutNode child = node.getChildren().get(0);
            height += VERTICAL_SPACING + child.getRequiredHeight();
            width = Math.max(width, child.getRequiredWidth());
        }

        node.setRequiredWidth(width);
        node.setRequiredHeight(height);
    }

    private void calculateDecisionDimensions(LayoutNode node) {
        Block block = node.getBlock();
        Dimension blockSize = block != null ? block.getSize() : new Dimension(120, 60);

        // Get branch dimensions
        int leftWidth = node.getLeftBranch() != null ? node.getLeftBranch().getRequiredWidth() : 0;
        int leftHeight = node.getLeftBranch() != null ? node.getLeftBranch().getRequiredHeight() : 0;

        int rightWidth = node.getRightBranch() != null ? node.getRightBranch().getRequiredWidth() : 0;
        int rightHeight = node.getRightBranch() != null ? node.getRightBranch().getRequiredHeight() : 0;

        // Width: decision width + max branch width + horizontal offsets
        // We need space for: left offset + left branch + decision center + right branch + right offset
        int totalWidth = leftWidth + rightWidth + (2 * BRANCH_HORIZONTAL_OFFSET) + MIN_BRANCH_SPACING;
        totalWidth = Math.max(totalWidth, blockSize.width);

        // Height: decision height + max branch height + merge point + spacing + children after merge
        int maxBranchHeight = Math.max(leftHeight, rightHeight);
        int totalHeight = blockSize.height + VERTICAL_SPACING + maxBranchHeight + MERGE_POINT_SPACING + MERGE_POINT_SIZE;

        // Add height of nodes after the merge point
        if (!node.getChildren().isEmpty()) {
            LayoutNode child = node.getChildren().get(0);
            totalHeight += VERTICAL_SPACING + child.getRequiredHeight();
        }

        node.setRequiredWidth(totalWidth);
        node.setRequiredHeight(totalHeight);
    }

    private void calculateBranchDimensions(LayoutNode node) {
        int maxWidth = 120; // Minimum width for a branch
        int totalHeight = 0;

        for (LayoutNode child : node.getChildren()) {
            maxWidth = Math.max(maxWidth, child.getRequiredWidth());
            totalHeight += child.getRequiredHeight();
            if (child != node.getChildren().get(node.getChildren().size() - 1)) {
                totalHeight += VERTICAL_SPACING;
            }
        }

        node.setRequiredWidth(maxWidth);
        node.setRequiredHeight(totalHeight);
    }

    private void calculateMergeDimensions(LayoutNode node) {
        node.setRequiredWidth(MERGE_POINT_SIZE);
        node.setRequiredHeight(MERGE_POINT_SIZE);

        // Add height of children after merge
        if (!node.getChildren().isEmpty()) {
            LayoutNode child = node.getChildren().get(0);
            node.setRequiredHeight(node.getRequiredHeight() + VERTICAL_SPACING + child.getRequiredHeight());
            node.setRequiredWidth(Math.max(node.getRequiredWidth(), child.getRequiredWidth()));
        }
    }

    private void calculateLoopDimensions(LayoutNode node) {
        Block block = node.getBlock();
        Dimension blockSize = block != null ? block.getSize() : new Dimension(120, 60);

        int width = blockSize.width;
        int height = blockSize.height;

        // Add dimensions for loop body and exit
        for (LayoutNode child : node.getChildren()) {
            height += VERTICAL_SPACING + child.getRequiredHeight();
            width = Math.max(width, child.getRequiredWidth());
        }

        node.setRequiredWidth(width);
        node.setRequiredHeight(height);
    }

    /**
     * Pass 2: Assign absolute positions to all nodes (top-down).
     *
     * Starting from the root with its absolute position, calculate positions
     * for all children based on their required dimensions.
     */
    private void assignPositions(LayoutNode node, int x, int y) {
        if (node == null) {
            return;
        }

        node.setAbsoluteX(x);
        node.setAbsoluteY(y);

        switch (node.getNodeType()) {
            case LINEAR:
                assignLinearPositions(node, x, y);
                break;
            case DECISION:
                assignDecisionPositions(node, x, y);
                break;
            case BRANCH:
                assignBranchPositions(node, x, y);
                break;
            case MERGE:
                assignMergePositions(node, x, y);
                break;
            case LOOP:
                assignLoopPositions(node, x, y);
                break;
        }
    }

    private void assignLinearPositions(LayoutNode node, int x, int y) {
        // Children are positioned directly below
        if (!node.getChildren().isEmpty()) {
            LayoutNode child = node.getChildren().get(0);
            Block block = node.getBlock();
            int nextY = y + (block != null ? block.getSize().height : 60) + VERTICAL_SPACING;
            assignPositions(child, x, nextY);
        }
    }

    private void assignDecisionPositions(LayoutNode node, int x, int y) {
        Block decisionBlock = node.getBlock();
        Dimension blockSize = decisionBlock != null ? decisionBlock.getSize() : new Dimension(120, 60);

        // Calculate center X of the decision block
        int centerX = x + blockSize.width / 2;

        // Position left branch
        if (node.getLeftBranch() != null) {
            int leftX = centerX - BRANCH_HORIZONTAL_OFFSET - node.getLeftBranch().getRequiredWidth() / 2;
            int branchY = y + blockSize.height + VERTICAL_SPACING;
            assignBranchPositions(node.getLeftBranch(), leftX, branchY);
        }

        // Position right branch
        if (node.getRightBranch() != null) {
            int rightX = centerX + BRANCH_HORIZONTAL_OFFSET - node.getRightBranch().getRequiredWidth() / 2;
            int branchY = y + blockSize.height + VERTICAL_SPACING;
            assignBranchPositions(node.getRightBranch(), rightX, branchY);
        }

        // Calculate merge point position
        // It should be centered horizontally under the decision block
        // and vertically below the longest branch
        int maxBranchHeight = 0;
        if (node.getLeftBranch() != null) {
            maxBranchHeight = Math.max(maxBranchHeight, node.getLeftBranch().getRequiredHeight());
        }
        if (node.getRightBranch() != null) {
            maxBranchHeight = Math.max(maxBranchHeight, node.getRightBranch().getRequiredHeight());
        }

        int mergeX = centerX - MERGE_POINT_SIZE / 2;
        int mergeY = y + blockSize.height + VERTICAL_SPACING + maxBranchHeight + MERGE_POINT_SPACING;

        // Position nodes after merge point
        if (!node.getChildren().isEmpty()) {
            LayoutNode afterMerge = node.getChildren().get(0);
            int afterMergeY = mergeY + MERGE_POINT_SIZE + VERTICAL_SPACING;
            assignPositions(afterMerge, x, afterMergeY);
        }

        // Store merge point position for later use
        node.setAbsoluteX(x);
        node.setAbsoluteY(y);

        // If there's a merge block associated, position it
        Block mergeBlock = findMergePoint(decisionBlock);
        if (mergeBlock != null) {
            mergeBlock.setPosition(new Point(mergeX, mergeY));
        }
    }

    private void assignBranchPositions(LayoutNode node, int x, int y) {
        int currentY = y;

        for (LayoutNode child : node.getChildren()) {
            // Center each child horizontally within the branch
            int childX = x + (node.getRequiredWidth() - child.getRequiredWidth()) / 2;
            assignPositions(child, childX, currentY);

            currentY += child.getRequiredHeight() + VERTICAL_SPACING;
        }
    }

    private void assignMergePositions(LayoutNode node, int x, int y) {
        if (!node.getChildren().isEmpty()) {
            LayoutNode child = node.getChildren().get(0);
            int nextY = y + MERGE_POINT_SIZE + VERTICAL_SPACING;
            assignPositions(child, x, nextY);
        }
    }

    private void assignLoopPositions(LayoutNode node, int x, int y) {
        Block block = node.getBlock();
        int nextY = y + (block != null ? block.getSize().height : 60) + VERTICAL_SPACING;

        for (LayoutNode child : node.getChildren()) {
            assignPositions(child, x, nextY);
            nextY += child.getRequiredHeight() + VERTICAL_SPACING;
        }
    }

    /**
     * Pass 3: Apply calculated positions to actual Block objects.
     */
    private void applyPositionsToBlocks(LayoutNode node) {
        if (node == null) {
            return;
        }

        // Apply position to this node's block
        if (node.hasBlock()) {
            node.getBlock().setPosition(new Point(node.getAbsoluteX(), node.getAbsoluteY()));
        }

        // Recursively apply to children
        for (LayoutNode child : node.getChildren()) {
            applyPositionsToBlocks(child);
        }

        // Apply to branches if this is a decision node
        if (node.isDecision()) {
            if (node.getLeftBranch() != null) {
                applyPositionsToBlocks(node.getLeftBranch());
            }
            if (node.getRightBranch() != null) {
                applyPositionsToBlocks(node.getRightBranch());
            }
        }
    }

    /**
     * Helper method to find the merge point block connected to a decision block.
     */
    private Block findMergePoint(Block decisionBlock) {
        for (Connection conn : decisionBlock.getOutgoingConnections()) {
            Block current = conn.getTargetBlock();
            Set<Block> visited = new HashSet<>();

            while (current != null && !visited.contains(current)) {
                if (current.getType() == BlockType.MERGE) {
                    return current;
                }
                visited.add(current);

                // Follow first non-backward connection
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
     * Helper method to check if a block type is a loop.
     */
    private boolean isLoopBlock(BlockType type) {
        return type == BlockType.FOR_LOOP ||
               type == BlockType.WHILE_LOOP ||
               type == BlockType.DO_WHILE_LOOP;
    }
}
