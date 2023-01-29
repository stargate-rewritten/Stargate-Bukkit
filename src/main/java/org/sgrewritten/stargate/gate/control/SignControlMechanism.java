package org.sgrewritten.stargate.gate.control;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.BlockSetAction;
import org.sgrewritten.stargate.api.event.StargateSignFormatEvent;
import org.sgrewritten.stargate.api.gate.GatePosition;
import org.sgrewritten.stargate.api.gate.control.GateTextDisplayHandler;
import org.sgrewritten.stargate.api.gate.control.MechanismType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.network.portal.formatting.LegacyLineColorFormatter;
import org.sgrewritten.stargate.network.portal.formatting.LineColorFormatter;
import org.sgrewritten.stargate.network.portal.formatting.NoLineColorFormatter;
import org.sgrewritten.stargate.property.NonLegacyMethod;
import org.sgrewritten.stargate.util.colors.ColorConverter;

import java.util.Objects;
import java.util.logging.Level;

public class SignControlMechanism extends GatePosition implements GateTextDisplayHandler {

    private @NotNull Gate gate;
    private LineFormatter colorDrawer;

    public SignControlMechanism(@NotNull BlockVector positionLocation, @NotNull Gate gate) {
        super(positionLocation);
        this.gate = Objects.requireNonNull(gate);
        colorDrawer = new NoLineColorFormatter();
    }

    @Override
    public void displayText(String[] lines) {
        Location signLocation = gate.getLocation(this.positionLocation);
        BlockState signState = signLocation.getBlock().getState();
        if (!(signState instanceof Sign)) {
            Stargate.log(Level.FINE, "Could not find sign at position " + signLocation);
            return;
        }
        Sign sign = (Sign) signState;
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, lines[i]);
        }
        Stargate.addSynchronousTickAction(new BlockSetAction(sign, true));
    }

    @Override
    public MechanismType getType() {
        return MechanismType.SIGN;
    }

    @Override
    public boolean onBlockClick(PlayerInteractEvent event, RealPortal portal) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && dyePortalSignText(event, portal)) {
            portal.setSignColor(ColorConverter.getDyeColorFromMaterial(event.getMaterial()));
            event.setUseInteractedBlock(Event.Result.ALLOW);
            return false;
        }
        event.setUseInteractedBlock(Event.Result.DENY);
        if (portal.isOpenFor(event.getPlayer())) {
            Stargate.log(Level.FINEST, "Player name=" + event.getPlayer().getName());
            portal.onSignClick(event);
        }
        return true;
    }

    @Override
    public void setSignColor(DyeColor color, RealPortal portal) {
        Location location = gate.getLocation(positionLocation);
        Sign sign = (Sign) location.getBlock().getState();
        if (color == null) {
            color = sign.getColor();
        }

        if (NonLegacyMethod.CHAT_COLOR.isImplemented()) {
            colorDrawer = new LineColorFormatter(color, sign.getType());
        } else {
            colorDrawer = new LegacyLineColorFormatter();
        }
        //TODO: The StargateSignFormatEvent should be called each time a sign is formatted. This implementation 
        // will only update the formatter when run on startup, or if changed with a dye. Instead, this should either
        // be called every time a color formatting happens, or be replaced with an API method
        StargateSignFormatEvent formatEvent = new StargateSignFormatEvent(portal, colorDrawer, color);
        Bukkit.getPluginManager().callEvent(formatEvent);
        this.colorDrawer = formatEvent.getLineFormatter();
    }


    /**
     * Tries to dye the text of a portals sign if the player is holding a dye and has enough permissions
     *
     * @param event  <p>The interact event causing this method to be triggered</p>
     * @param portal <p>The portal whose sign to apply dye to<p>
     * @return <p>True if the dye should be applied</p>
     */
    private boolean dyePortalSignText(PlayerInteractEvent event, RealPortal portal) {
        ItemStack item = event.getItem();
        if (!itemIsDye(item)) {
            return false;
        }

        StargatePermissionManager permissionManager = new StargatePermissionManager(event.getPlayer());
        boolean hasPermission = permissionManager.hasCreatePermissions(portal);
        if (!hasPermission) {
            event.getPlayer().sendMessage(permissionManager.getDenyMessage());
        }
        return hasPermission;
    }


    /**
     * Checks if the given item stack is a type of dye
     *
     * @param item <p>The item to check</p>
     * @return <p>True if the item stack is a type of dye</p>
     */
    private boolean itemIsDye(ItemStack item) {
        if (item == null) {
            return false;
        }
        String itemName = item.getType().toString();
        Material glowInkSac = Material.matchMaterial("GLOW_INK_SAC");
        if (glowInkSac != null && item.getType() == glowInkSac) {
            return true;
        }
        return (itemName.contains("DYE") || item.getType() == Material.INK_SAC);
    }
}
