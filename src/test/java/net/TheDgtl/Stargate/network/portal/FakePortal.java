package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.network.Network;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FakePortal implements IPortal {

    private final Location signLocation;
    private final String portalName;
    private final Network network;
    private final UUID ownerUUID;

    public FakePortal(Location signLocation, String portalName, Network network, UUID ownerUUID) {
        this.signLocation = signLocation;
        this.portalName = portalName;
        this.network = network;
        this.ownerUUID = ownerUUID;
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isOpenFor(Entity target) {
        return false;
    }

    @Override
    public void teleportHere(Entity target, Portal origin) {

    }

    @Override
    public void doTeleport(Entity target) {

    }

    @Override
    public void close(boolean force) {

    }

    @Override
    public void open(Player player) {

    }

    @Override
    public String getName() {
        return this.portalName;
    }

    @Override
    public void setNetwork(Network targetNet) {

    }

    @Override
    public void setOverrideDestination(IPortal destination) {

    }

    @Override
    public Network getNetwork() {
        return this.network;
    }

    @Override
    public boolean hasFlag(PortalFlag flag) {
        return false;
    }

    @Override
    public String getAllFlagsString() {
        return "ABQF";
    }

    @Override
    public Location getSignPos() {
        return this.signLocation;
    }

    @Override
    public String getDesignName() {
        return null;
    }

    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void update() {
        // TODO Auto-generated method stub

    }
}
