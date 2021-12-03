package net.TheDgtl.Stargate.gate;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.actions.BlockSetAction;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.SGLocation;
import net.TheDgtl.Stargate.vectorlogic.VectorOperation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

/**
 * Acts as an interface for portals to modify worlds
 *
 * @author Thorin
 */
public class Gate {

    private GateFormat format;
    /*
     * a vector operation that goes from world -> format. Also contains the inverse
     * operation for this
     */
    VectorOperation converter;
    /**
     * WARNING: Don't modify this ever, always use .copy()
     */
    Location topLeft;
    /**
     * WARNING: Don't modify this ever, always use .copy()
     */
    public BlockVector signPos;
    public BlockVector buttonPos;
    public final BlockFace facing;
    private boolean isOpen = false;
    private final Portal portal;


    static final private Material DEFAULT_BUTTON = Material.STONE_BUTTON;
    static final private Material DEFAULT_WATER_BUTTON = Material.DEAD_TUBE_CORAL_WALL_FAN;
    static final public HashSet<Material> ALL_PORTAL_MATERIALS = new HashSet<>();

    /**
     * Compares the format to real world; If the format matches with the world,
     * independent of rotation and mirroring.
     *
     * @param format
     * @param loc
     * @throws InvalidStructureException
     * @throws GateConflictException
     */
    public Gate(GateFormat format, Location loc, BlockFace signFace, Portal portal) throws InvalidStructureException, GateConflictException {
        this.setFormat(format);
        facing = signFace;
        this.portal = portal;
        converter = new VectorOperation(signFace);

        if (matchesFormat(loc)) {
            return;
        }
        converter.setFlipZAxis(true);
        if (matchesFormat(loc))
            return;

        throw new InvalidStructureException();
    }

    /**
     * Checks if format matches independent of controlBlock
     * TODO: symmetric formats will be checked twice, make a way to determine if a format is symmetric to avoid this
     *
     * @param loc
     * @return
     * @throws GateConflictException
     */
    private boolean matchesFormat(Location loc) throws GateConflictException {
        List<BlockVector> controlBlocks = getFormat().getControlBlocks();
        for (BlockVector controlBlock : controlBlocks) {
            /*
             * Top-left is origin for the format, everything becomes easier if you calculate
             * this position in the world; this is a hypothetical position, calculated from
             * the position of the sign minus a vector of a hypothetical sign position in
             * format.
             */
            topLeft = loc.clone().subtract(converter.performInverseOperation(controlBlock));

            if (getFormat().matches(converter, topLeft)) {
                if (isGateConflict()) {
                    throw new GateConflictException();
                }
                /*
                 * Just a cheat to exclude the sign location, and determine the position of the
                 * button. Note that this will have weird behaviour if there's more than 3
                 * control-blocks
                 */
                signPos = controlBlock;
                for (BlockVector buttonVec : getFormat().getControlBlocks()) {
                    if (signPos == buttonVec)
                        continue;
                    buttonPos = buttonVec;
                    break;
                }

                return true;
            }
        }
        return false;
    }

    private boolean isGateConflict() {
        List<SGLocation> locations = this.getLocations(GateStructureType.FRAME);
        for (SGLocation loc : locations) {
            if (Network.getPortal(loc, GateStructureType.values()) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set button and draw sign
     *
     * @param signLines an array with 4 elements, representing each line of a sign
     */
    public void drawControlMechanism(String[] signLines, boolean isDrawButton) {
        Location signLoc = getLocation(signPos);
        BlockState signState = signLoc.getBlock().getState();
        if (!(signState instanceof Sign)) {
            Stargate.log(Level.FINE, "Could not find sign at position " + signLoc);
            return;
        }

        Sign sign = (Sign) signState;
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, signLines[i]);
        }
        Stargate.syncTickPopulator.addAction(new BlockSetAction(sign, true));
        if (!isDrawButton)
            return;

        Location buttonLoc = getLocation(buttonPos);
        Material buttonMat = getButtonMaterial();
        Directional buttonData = (Directional) Bukkit.createBlockData(buttonMat);
        buttonData.setFacing(facing);

        buttonLoc.getBlock().setBlockData(buttonData);

    }

    public Location getSignLoc() {
        return getLocation(signPos);
    }

    public Location getButtonLoc() {
        return getLocation(buttonPos);
    }

    private Material getButtonMaterial() {
        Material portalClosedMat = getFormat().getIrisMat(false);
        switch (portalClosedMat) {
            case AIR:
                return DEFAULT_BUTTON;
            case WATER:
                return DEFAULT_WATER_BUTTON;
            default:
                Stargate.log(Level.INFO, portalClosedMat.name() + " is currently not supported as a portal closed material");
                return DEFAULT_BUTTON;
        }
    }

    /**
     * @param structKey , key for the structure-type to be retrieved
     * @return
     */
    public List<SGLocation> getLocations(GateStructureType structKey) {
        List<SGLocation> output = new ArrayList<>();

        for (BlockVector vec : getFormat().portalParts.get(structKey).getStructureTypePositions()) {
            Location loc = getLocation(vec);
            output.add(new SGLocation(loc));
        }

        if (structKey == GateStructureType.CONTROL_BLOCK && portal.hasFlag(PortalFlag.ALWAYS_ON)) {
            Location buttonLoc = getLocation(buttonPos);
            output.remove(new SGLocation(buttonLoc));
        }
        return output;
    }

    private Location getLocation(Vector vec) {
        return topLeft.clone().add(converter.performInverseOperation(vec));
    }

    /**
     * Set the iris mat, note that nether portals have to be oriented in the right axis, and
     * force a location to prevent exit gateway generation.
     *
     * @param mat
     */
    private void setIrisMaterial(Material mat) {
        GateStructureType targetType = GateStructureType.IRIS;
        List<SGLocation> locations = getLocations(targetType);
        BlockData blockData = Bukkit.createBlockData(mat);

        if (blockData instanceof Orientable) {
            Orientable orientation = (Orientable) blockData;
            orientation.setAxis(converter.getIrisNormal());
        }

        for (SGLocation loc : locations) {
            Block blk = loc.getLocation().getBlock();
            blk.setBlockData(blockData);
            if (mat == Material.END_GATEWAY) {// force a location to prevent exit gateway generation
                EndGateway gateway = (EndGateway) blk.getState();
                // https://github.com/stargate-bukkit/Stargate-Bukkit/issues/36
                gateway.setAge(-9223372036854775808L);
                if (blk.getWorld().getEnvironment() == World.Environment.THE_END) {
                    gateway.setExitLocation(blk.getWorld().getSpawnLocation());
                    gateway.setExactTeleport(true);
                }
                gateway.update(false, false);
            }
        }
    }

    public void open() {
        Material mat = getFormat().getIrisMat(true);
        setIrisMaterial(mat);
        setOpen(true);

    }

    public void close() {
        Material mat = getFormat().getIrisMat(false);
        setIrisMaterial(mat);
        setOpen(false);
    }

    public Location getExit() {
        BlockVector formatExit = getFormat().getExit();
        return getLocation(formatExit);
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public GateFormat getFormat() {
        return format;
    }

    public void setFormat(GateFormat format) {
        this.format = format;
    }

    public BlockFace getFacing() {
        return converter.getFacing();
    }

    public Vector getRelativeVector(Location loc) {
        Vector vec = topLeft.clone().subtract(loc).toVector();
        return converter.performOperation(vec);
    }

}
