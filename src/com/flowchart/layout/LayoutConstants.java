package com.flowchart.layout;

/**
 * Constants used for layout calculations.
 * Centralizes all magic numbers for easy configuration.
 */
public class LayoutConstants {

    // Vertical spacing between blocks in a linear sequence
    public static final int VERTICAL_SPACING = 100;

    // Horizontal spacing between branches (left and right of a decision block)
    public static final int HORIZONTAL_SPACING = 200;

    // Starting position for the root block
    public static final int START_X = 400;
    public static final int START_Y = 50;

    // Horizontal offset for decision block branches
    // This is the distance from the decision center to the vertical line of each branch
    public static final int BRANCH_HORIZONTAL_OFFSET = 60;

    // Vertical spacing before and after merge point
    public static final int MERGE_POINT_SPACING = 40;

    // Minimum horizontal spacing between nested branches
    public static final int MIN_BRANCH_SPACING = 40;

    // Size of merge point (blue dot)
    public static final int MERGE_POINT_SIZE = 20;

    private LayoutConstants() {
        // Utility class, no instantiation
    }
}
