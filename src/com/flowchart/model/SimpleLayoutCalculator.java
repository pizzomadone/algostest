package com.flowchart.model;

import static com.flowchart.layout.LayoutConstants.*;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple and direct layout calculator for flowchart blocks.
 *
 * Uses a straightforward recursive approach:
 * 1. Calculate dimensions of each branch by traversing it
 * 2. Position blocks based on calculated dimensions
 * 3. No complex tree structures - just direct calculations
 */
public class SimpleLayoutCalculator {

    /**
     * Dimensions of a branch or subtree
     */
    public static class BranchDimensions {
        public int width;
        public int height;

        public BranchDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    /**
     * Main entry point - recalculates layout for entire flowchart
     */
    public void recalculateLayout(Block root) {
        System.out.println("=== SIMPLE LAYOUT STARTING ===");
        Set<Block> visited = new HashSet<>();
        layoutBlock(root, START_X, START_Y, visited);
        System.out.println("=== SIMPLE LAYOUT DONE ===\n");
    }

    /**
     * Recursively layout a block and all its children
     * Returns the Y coordinate of the bottom of this subtree
     */
    private int layoutBlock(Block block, int x, int y, Set<Block> visited) {
        if (block == null || visited.contains(block)) {
            return y;
        }

        visited.add(block);

        System.out.println("Laying out " + block.getType() + " (" + block.getText() + ") at " + x + "," + y);

        // Special case: MERGE blocks don't move (positioned by their decision)
        if (block.getType() == BlockType.MERGE) {
            System.out.println("  MERGE - position already set");
            return layoutAfterMerge(block, x, y, visited);
        }

        // Position this block
        block.setPosition(new Point(x, y));
        int blockHeight = block.getSize().height;

        // Get forward (non-backward) connections
        List<Connection> forward = getForwardConnections(block);

        if (forward.isEmpty()) {
            System.out.println("  No forward connections");
            return y + blockHeight;
        }

        // DECISION BLOCK: Special layout for two branches
        if (block.getType() == BlockType.DECISION) {
            return layoutDecision(block, x, y, visited);
        }

        // LOOP BLOCK: Special layout for loop body
        if (isLoopBlock(block)) {
            return layoutLoop(block, x, y, visited);
        }

        // LINEAR BLOCK: Just continue downward
        if (forward.size() == 1) {
            int nextY = y + blockHeight + VERTICAL_SPACING;
            Block next = forward.get(0).getTargetBlock();
            return layoutBlock(next, x, nextY, visited);
        }

        System.out.println("  WARNING: Unexpected connection count: " + forward.size());
        return y + blockHeight;
    }

    /**
     * Layout a decision block with its two branches and merge point
     */
    private int layoutDecision(Block decision, int x, int y, Set<Block> visited) {
        System.out.println("  DECISION BLOCK - laying out branches");

        int blockHeight = decision.getSize().height;
        int branchStartY = y + blockHeight + VERTICAL_SPACING;

        // Find the merge point
        Block mergePoint = findMergePoint(decision);
        if (mergePoint == null) {
            System.out.println("  ERROR: No merge point found!");
            return y + blockHeight;
        }

        // Find left (SI) and right (NO) connections
        Connection leftConn = null;
        Connection rightConn = null;

        for (Connection conn : decision.getOutgoingConnections()) {
            if ("left".equals(conn.getMergeBranch()) || conn.getLabel().contains("SI")) {
                leftConn = conn;
            } else if ("right".equals(conn.getMergeBranch()) || conn.getLabel().contains("NO")) {
                rightConn = conn;
            }
        }

        // Calculate dimensions of each branch
        BranchDimensions leftDim = calculateBranchDimensions(leftConn, mergePoint, new HashSet<>());
        BranchDimensions rightDim = calculateBranchDimensions(rightConn, mergePoint, new HashSet<>());

        System.out.println("  Left branch: " + leftDim.width + "x" + leftDim.height);
        System.out.println("  Right branch: " + rightDim.width + "x" + rightDim.height);

        // Calculate X positions for each branch
        // Decision center is at x + blockWidth/2
        int decisionCenterX = x + decision.getSize().width / 2;

        // Left branch: position to the left of center
        int leftBranchX = decisionCenterX - BRANCH_HORIZONTAL_OFFSET - leftDim.width / 2;

        // Right branch: position to the right of center
        int rightBranchX = decisionCenterX + BRANCH_HORIZONTAL_OFFSET - rightDim.width / 2;

        System.out.println("  Left branch X: " + leftBranchX);
        System.out.println("  Right branch X: " + rightBranchX);

        // Layout left branch
        int leftBottomY = branchStartY;
        if (leftConn != null && leftConn.getTargetBlock() != mergePoint) {
            leftBottomY = layoutBranch(leftConn, leftBranchX, branchStartY, mergePoint, visited);
        }

        // Layout right branch
        int rightBottomY = branchStartY;
        if (rightConn != null && rightConn.getTargetBlock() != mergePoint) {
            rightBottomY = layoutBranch(rightConn, rightBranchX, branchStartY, mergePoint, visited);
        }

        // Position merge point: centered horizontally, below both branches
        int maxBranchBottom = Math.max(leftBottomY, rightBottomY);
        int mergeY = maxBranchBottom + MERGE_POINT_SPACING;
        int mergeX = decisionCenterX - MERGE_POINT_SIZE / 2;

        mergePoint.setPosition(new Point(mergeX, mergeY));
        System.out.println("  Merge point at: " + mergeX + "," + mergeY);

        // Continue after merge point
        return layoutAfterMerge(mergePoint, decisionCenterX, mergeY, visited);
    }

    /**
     * Layout blocks inside a branch (between decision and merge)
     */
    private int layoutBranch(Connection startConn, int x, int y, Block mergePoint, Set<Block> visited) {
        Block current = startConn.getTargetBlock();
        int currentY = y;

        while (current != null && current != mergePoint && !visited.contains(current)) {
            visited.add(current);

            // Position this block
            current.setPosition(new Point(x, currentY));
            System.out.println("    Branch block " + current.getType() + " at " + x + "," + currentY);

            int blockHeight = current.getSize().height;

            // If this is a nested decision, handle it specially
            if (current.getType() == BlockType.DECISION) {
                currentY = layoutDecision(current, x, currentY, visited);

                // After nested decision, find merge and continue
                Block nestedMerge = findMergePoint(current);
                if (nestedMerge != null) {
                    current = findNextAfterBlock(nestedMerge, mergePoint);
                    if (current != null) {
                        currentY += VERTICAL_SPACING;
                    }
                } else {
                    break;
                }
            } else if (isLoopBlock(current)) {
                currentY = layoutLoop(current, x, currentY, visited);

                // Continue after loop
                current = findNextAfterBlock(current, mergePoint);
                if (current != null) {
                    currentY += VERTICAL_SPACING;
                }
            } else {
                // Regular block - move to next
                currentY += blockHeight + VERTICAL_SPACING;
                current = findNextAfterBlock(current, mergePoint);
            }
        }

        return currentY;
    }

    /**
     * Calculate dimensions needed by a branch (without positioning)
     */
    private BranchDimensions calculateBranchDimensions(Connection startConn, Block stopAt, Set<Block> visited) {
        if (startConn == null) {
            return new BranchDimensions(120, 0);
        }

        Block start = startConn.getTargetBlock();
        if (start == stopAt) {
            return new BranchDimensions(120, 0);
        }

        int maxWidth = 120;
        int totalHeight = 0;

        Block current = start;
        Set<Block> localVisited = new HashSet<>(visited);

        while (current != null && current != stopAt && !localVisited.contains(current)) {
            localVisited.add(current);

            if (current.getType() == BlockType.DECISION) {
                // For nested decision, calculate its full dimensions
                Block nestedMerge = findMergePoint(current);
                BranchDimensions decisionDim = calculateDecisionDimensions(current, nestedMerge, localVisited);
                maxWidth = Math.max(maxWidth, decisionDim.width);
                totalHeight += decisionDim.height + VERTICAL_SPACING;

                // Continue after the nested decision's merge
                current = nestedMerge != null ? findNextAfterBlock(nestedMerge, stopAt) : null;
            } else if (isLoopBlock(current)) {
                // For loops, include loop + body height
                Dimension size = current.getSize();
                maxWidth = Math.max(maxWidth, size.width);
                totalHeight += size.height + VERTICAL_SPACING + 80; // Approx body height

                current = findNextAfterBlock(current, stopAt);
            } else {
                // Regular block
                Dimension size = current.getSize();
                maxWidth = Math.max(maxWidth, size.width);
                totalHeight += size.height + VERTICAL_SPACING;

                current = findNextAfterBlock(current, stopAt);
            }
        }

        return new BranchDimensions(maxWidth, totalHeight);
    }

    /**
     * Calculate dimensions for a decision block (including its branches and merge)
     */
    private BranchDimensions calculateDecisionDimensions(Block decision, Block mergePoint, Set<Block> visited) {
        // Find branches
        Connection leftConn = null;
        Connection rightConn = null;

        for (Connection conn : decision.getOutgoingConnections()) {
            if ("left".equals(conn.getMergeBranch()) || conn.getLabel().contains("SI")) {
                leftConn = conn;
            } else if ("right".equals(conn.getMergeBranch()) || conn.getLabel().contains("NO")) {
                rightConn = conn;
            }
        }

        BranchDimensions leftDim = calculateBranchDimensions(leftConn, mergePoint, visited);
        BranchDimensions rightDim = calculateBranchDimensions(rightConn, mergePoint, visited);

        int totalWidth = leftDim.width + rightDim.width + (2 * BRANCH_HORIZONTAL_OFFSET) + MIN_BRANCH_SPACING;
        int totalHeight = decision.getSize().height + VERTICAL_SPACING +
                         Math.max(leftDim.height, rightDim.height) +
                         MERGE_POINT_SPACING + MERGE_POINT_SIZE;

        return new BranchDimensions(totalWidth, totalHeight);
    }

    /**
     * Layout a loop block
     */
    private int layoutLoop(Block loop, int x, int y, Set<Block> visited) {
        System.out.println("  LOOP BLOCK");

        int blockHeight = loop.getSize().height;
        int bodyY = y + blockHeight + VERTICAL_SPACING;

        // Find loop body
        for (Connection conn : loop.getOutgoingConnections()) {
            if (conn.getTargetBlock().getType() == BlockType.LOOP_BODY) {
                Block body = conn.getTargetBlock();
                body.setPosition(new Point(x, bodyY));
                visited.add(body);
                System.out.println("  Loop body at: " + x + "," + bodyY);
                break;
            }
        }

        // Return Y after loop structure
        return bodyY + 60 + VERTICAL_SPACING;
    }

    /**
     * Continue layout after a merge point
     */
    private int layoutAfterMerge(Block mergePoint, int x, int y, Set<Block> visited) {
        List<Connection> outgoing = mergePoint.getOutgoingConnections();
        if (outgoing.isEmpty()) {
            return y + MERGE_POINT_SIZE;
        }

        Block next = outgoing.get(0).getTargetBlock();
        int nextY = y + MERGE_POINT_SIZE + VERTICAL_SPACING;

        return layoutBlock(next, x, nextY, visited);
    }

    /**
     * Find the merge point connected to a decision block
     */
    private Block findMergePoint(Block decision) {
        for (Connection conn : decision.getOutgoingConnections()) {
            Block current = conn.getTargetBlock();
            Set<Block> visited = new HashSet<>();

            while (current != null && !visited.contains(current)) {
                if (current.getType() == BlockType.MERGE) {
                    return current;
                }
                visited.add(current);

                List<Connection> forward = getForwardConnections(current);
                current = forward.isEmpty() ? null : forward.get(0).getTargetBlock();
            }
        }
        return null;
    }

    /**
     * Find the next block after current, stopping at stopAt
     */
    private Block findNextAfterBlock(Block current, Block stopAt) {
        if (current == stopAt) {
            return null;
        }

        List<Connection> forward = getForwardConnections(current);
        if (forward.isEmpty()) {
            return null;
        }

        Block next = forward.get(0).getTargetBlock();
        return next == stopAt ? null : next;
    }

    /**
     * Get forward (non-backward) connections
     */
    private List<Connection> getForwardConnections(Block block) {
        List<Connection> forward = new ArrayList<>();
        for (Connection conn : block.getOutgoingConnections()) {
            if (!conn.isBackwardConnection() && conn.getTargetBlock() != null) {
                forward.add(conn);
            }
        }
        return forward;
    }

    /**
     * Check if block is a loop type
     */
    private boolean isLoopBlock(Block block) {
        BlockType type = block.getType();
        return type == BlockType.FOR_LOOP ||
               type == BlockType.WHILE_LOOP ||
               type == BlockType.DO_WHILE_LOOP;
    }
}
