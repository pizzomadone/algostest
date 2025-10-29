package com.flowchart.layout;

import com.flowchart.model.Block;
import com.flowchart.model.BlockType;
import com.flowchart.model.Connection;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the layout tree structure.
 * Each LayoutNode corresponds to a Block or a structural element (branch, merge).
 *
 * The layout tree is used to:
 * 1. Calculate required dimensions for each subtree (bottom-up pass)
 * 2. Assign absolute positions based on dimensions (top-down pass)
 */
public class LayoutNode {

    /**
     * Type of layout node
     */
    public enum NodeType {
        /** A single block in linear flow */
        LINEAR,

        /** A decision block with two branches */
        DECISION,

        /** A branch (left or right) of a decision block */
        BRANCH,

        /** A merge point that joins two branches */
        MERGE,

        /** A loop structure (while, for, do-while) */
        LOOP
    }

    // Type of this layout node
    private final NodeType nodeType;

    // The flowchart block this node represents (null for structural nodes like BRANCH)
    private final Block block;

    // Children layout nodes
    private final List<LayoutNode> children;

    // Calculated dimensions (set during bottom-up pass)
    private int requiredWidth;
    private int requiredHeight;

    // Assigned position (set during top-down pass)
    private int absoluteX;
    private int absoluteY;

    // For DECISION nodes: stores the left and right branch nodes
    private LayoutNode leftBranch;
    private LayoutNode rightBranch;
    private LayoutNode mergeNode;

    // For BRANCH nodes: stores which branch this is
    private BranchSide branchSide;

    public enum BranchSide {
        LEFT, RIGHT
    }

    /**
     * Creates a layout node for a single block
     */
    public LayoutNode(NodeType nodeType, Block block) {
        this.nodeType = nodeType;
        this.block = block;
        this.children = new ArrayList<>();
        this.requiredWidth = 0;
        this.requiredHeight = 0;
    }

    /**
     * Creates a structural layout node (no associated block)
     */
    public LayoutNode(NodeType nodeType) {
        this(nodeType, null);
    }

    // Getters and setters

    public NodeType getNodeType() {
        return nodeType;
    }

    public Block getBlock() {
        return block;
    }

    public List<LayoutNode> getChildren() {
        return children;
    }

    public void addChild(LayoutNode child) {
        children.add(child);
    }

    public int getRequiredWidth() {
        return requiredWidth;
    }

    public void setRequiredWidth(int width) {
        this.requiredWidth = width;
    }

    public int getRequiredHeight() {
        return requiredHeight;
    }

    public void setRequiredHeight(int height) {
        this.requiredHeight = height;
    }

    public int getAbsoluteX() {
        return absoluteX;
    }

    public void setAbsoluteX(int x) {
        this.absoluteX = x;
    }

    public int getAbsoluteY() {
        return absoluteY;
    }

    public void setAbsoluteY(int y) {
        this.absoluteY = y;
    }

    public LayoutNode getLeftBranch() {
        return leftBranch;
    }

    public void setLeftBranch(LayoutNode leftBranch) {
        this.leftBranch = leftBranch;
        if (leftBranch != null) {
            leftBranch.branchSide = BranchSide.LEFT;
        }
    }

    public LayoutNode getRightBranch() {
        return rightBranch;
    }

    public void setRightBranch(LayoutNode rightBranch) {
        this.rightBranch = rightBranch;
        if (rightBranch != null) {
            rightBranch.branchSide = BranchSide.RIGHT;
        }
    }

    public LayoutNode getMergeNode() {
        return mergeNode;
    }

    public void setMergeNode(LayoutNode mergeNode) {
        this.mergeNode = mergeNode;
    }

    public BranchSide getBranchSide() {
        return branchSide;
    }

    public void setBranchSide(BranchSide side) {
        this.branchSide = side;
    }

    /**
     * Returns true if this is a decision node (has branches)
     */
    public boolean isDecision() {
        return nodeType == NodeType.DECISION;
    }

    /**
     * Returns true if this is a branch node
     */
    public boolean isBranch() {
        return nodeType == NodeType.BRANCH;
    }

    /**
     * Returns true if this is a merge node
     */
    public boolean isMerge() {
        return nodeType == NodeType.MERGE;
    }

    /**
     * Returns true if this node has an associated block
     */
    public boolean hasBlock() {
        return block != null;
    }

    @Override
    public String toString() {
        String blockInfo = hasBlock() ? " (" + block.getType() + ": " + block.getText() + ")" : "";
        return "LayoutNode{" +
                "type=" + nodeType +
                blockInfo +
                ", width=" + requiredWidth +
                ", height=" + requiredHeight +
                ", pos=(" + absoluteX + "," + absoluteY + ")" +
                "}";
    }
}
