package net.knarcraft.stargate;

import org.bukkit.Material;
import org.bukkit.Tag;

/**
 * This class helps decide properties of materials not already present in the Spigot API
 */
public class MaterialHelper {

    /**
     * Checks whether the given material is a dead or alive wall coral
     * @param material <p>The material to check</p>
     * @return <p>True if the material is a wall coral</p>
     */
    public static boolean isWallCoral(Material material) {
        return Tag.WALL_CORALS.isTagged(material) ||
                material.equals(Material.DEAD_BRAIN_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_BUBBLE_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_FIRE_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_HORN_CORAL_WALL_FAN) ||
                material.equals(Material.DEAD_TUBE_CORAL_WALL_FAN);
    }

}
