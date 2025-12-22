package io.hxneyw.repo.content.recipes;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;

/**
 * Extended heat checking that supports RADIANT tier
 */
public class ExtendedHeatCondition {

    public static final String RADIANT_KEY = "radiant";
    public static final int RADIANT_COLOR = 0xAA00FF; // Purple

    /**
     * Parse heat requirement string into Create's HeatCondition
     * Returns null for "radiant" since it's custom
     */
    public static HeatCondition parseHeatCondition(String heatRequirement) {
        if (RADIANT_KEY.equalsIgnoreCase(heatRequirement)) {
            // Return SUPERHEATED as a visual representation
            // The actual RADIANT check happens in the mixin
            return HeatCondition.SUPERHEATED;
        }

        try {
            return HeatCondition.valueOf(heatRequirement.toUpperCase());
        } catch (IllegalArgumentException e) {
            return HeatCondition.NONE;
        }
    }

    /**
     * Check if heat requirement is RADIANT
     */
    public static boolean isRadiant(String heatRequirement) {
        return RADIANT_KEY.equalsIgnoreCase(heatRequirement);
    }

    /**
     * Test if actual heat level satisfies the requirement
     * For standard conditions, use Create's system
     * For RADIANT, this returns false - the mixin handles it
     */
    public static boolean testHeatLevel(HeatCondition condition, BlazeBurnerBlock.HeatLevel actualHeatLevel) {
        if (condition == null)
            return true;
        return condition.testBlazeBurner(actualHeatLevel);
    }
}