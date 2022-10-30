package net.TheDgtl.Stargate.util.colors;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ColorPropertyTest {
    
    @Test
    void signSBTest() {
        Material[] materialsToCheckFor = new Material[] {
                Material.ACACIA_WALL_SIGN,
                Material.BIRCH_WALL_SIGN,
                Material.CRIMSON_WALL_SIGN,
                Material.DARK_OAK_WALL_SIGN,
                Material.JUNGLE_WALL_SIGN,
                Material.OAK_WALL_SIGN,
                Material.SPRUCE_WALL_SIGN,
                Material.WARPED_WALL_SIGN
        };
        for(Material materialToCheckFor : materialsToCheckFor) {
            System.out.print(materialToCheckFor);
            Assertions.assertNotNull(ColorProperty.getColorFromHue(materialToCheckFor, (short) 0, false));
            Assertions.assertNotNull(ColorProperty.getColorFromHue(materialToCheckFor, (short) 0, true));
        }
    }

}
