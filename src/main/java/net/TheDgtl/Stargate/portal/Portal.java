package net.TheDgtl.Stargate.portal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.SyncronousPopulator;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.InvalidStructure;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.portal.PortalFlag.NoFlagFound;

public abstract class Portal{
	/**
	 * 
	 */
	protected Network network;
	/**
	 * Behaviours: - Cycle through PortalStates, make current state listener for
	 * movements - (Constructor) Check validity, write sign, add self to a list in
	 * the network
	 * 
	 * Added behaviours - (Listener) Listen for stargate clock (maybe 1 tick per
	 * minute or something) maybe follow an external script that gives when the
	 * states should change
	 */
	int delay = 20*20; // ticks
	private Gate gate;
	HashSet<PortalFlag> flags;
	String name;
	Player openFor;
	Portal overrideDesti = null;
	private long openTime = -1;
	
	
	Portal(Network network, Block sign, String[] lines) throws NameError, NoFormatFound, GateConflict{
		
		this.network = network;
		this.name = lines[0];
		if (name.isBlank())
			throw new NameError("empty");
		if (this.network.portalList.containsKey(name)) {
			throw new NameError("taken");
		}
		
		/*
		 * Get the block behind the sign; the material of that block is stored in a
		 * register with available gateFormats
		 */
		Directional signDirection = (Directional) sign.getBlockData();
		Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
		List<GateFormat> gateFormats = GateFormat.getPossibleGatesFromControll(behind.getType());
		setGate(FindMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing()));

		flags = getFlags(lines[3]);
		String msg = "Selected with flags ";
		for(PortalFlag flag : flags) {
			msg = msg + flag.label;
		}
		this.network.addPortal(this);
		Stargate.log(Level.FINE, msg);
		for(GateStructureType key : getGate().getFormat().portalParts.keySet()) {
			if(!Network.portalFromPartsMap.containsKey(key)) {
				Network.portalFromPartsMap.put(key, new HashMap<SGLocation, Portal>());
			}
			List<SGLocation> locations = getGate().getLocations(key);
			Network.portalFromPartsMap.get(key).putAll(generateLocationHashMap(locations));
		}
	}
	
	private Gate FindMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing)
			throws NoFormatFound, GateConflict {
		Stargate.log(Level.FINE, "Amount of GateFormats: " + gateFormats.size());
		for (GateFormat gateFormat : gateFormats) {
			Stargate.log(Level.FINE, "--------- " + gateFormat.name + " ---------");
			try {
				return new Gate(gateFormat, signLocation, signFacing);
			} catch (InvalidStructure e) {
			}
		}
		throw new NoFormatFound();
	}
	
	/**
	 * Go through every character in line, and
	 * 
	 * @param line
	 */
	private HashSet<PortalFlag> getFlags(String line) {
		HashSet<PortalFlag> foundFlags = new HashSet<>();
		char[] charArray = line.toUpperCase().toCharArray();
		for (char character : charArray) {
			try {
				foundFlags.add(PortalFlag.valueOf(character));
			} catch (NoFlagFound e) {
			}
		}
		return foundFlags;
	}
	
	private HashMap<SGLocation, Portal> generateLocationHashMap(List<SGLocation> locations) {
		HashMap<SGLocation, Portal> output = new HashMap<>();
		for (SGLocation loc : locations) {
			output.put(loc, this);
		}
		return output;
	}
	
	public abstract void onSignClick(Action action, Player actor);
	
	public abstract void drawControll();
	
	public abstract Portal getDestination();
	
	public boolean isOpen() {
		return getGate().isOpen();
	}
	
	public Network getNetwork() {
		return this.network;
	}
	
	public HashSet<PortalFlag> getFlags(){
		return flags;
	}
	/**
	 * Remove all information stored on this gate
	 */
	public void destroy() {
		this.network.portalList.remove(name);
		String[] lines = new String[] {name,"","",""};
		getGate().drawControll(lines);
		for(GateStructureType formatType : Network.portalFromPartsMap.keySet()) {
			for(SGLocation loc : this.getGate().getLocations(formatType)) {
				Network.portalFromPartsMap.get(formatType).remove(loc);
			}
		}
		
		// Refresh all portals in this network. TODO is this too extensive?
		for (String portal : this.network.portalList.keySet()) {
			this.network.portalList.get(portal).drawControll();
		}
	}
	
	public void open(Player actor) {
		getGate().open();
		this.openFor = actor;
		long openTime = System.currentTimeMillis();
		this.openTime = openTime;

		// Create action which will close this portal
		SyncronousPopulator.Action action = new SyncronousPopulator.Action() {

			@Override
			public void run(boolean forceEnd) {
				close(openTime);
			}

			@Override
			public boolean isFinished() {
				return true;
			}
		};
		// Make the action on a delay
		Stargate.syncPopulator.new DelayedAction(delay, action);
	}
	
	/**
	 * Everytime most of the portals opens, there is going to be a scheduled event
	 * to close it after a specific time. If a player enters the portal before this,
	 * then it is going to close, but the scheduled close event is still going to be
	 * there. And if the portal gets activated again, it is going to close
	 * prematurely, because of this already scheduled event. Solution to avoid this
	 * is to assign a opentime for each scheduled close event and only close if the 
	 * related open time matches with the most recent time the portal was closed.
	 * 
	 * @param relatedOpenTime
	 */
	public void close(long relatedOpenTime) {
		if (relatedOpenTime == openTime)
			close();
	}
	
	public void close() {
		getGate().close();
		drawControll();
	}
	
	public boolean isOpenFor(Player player) {
		// TODO Auto-generated method stub
		return ((player == openFor) || (openFor == null));
	}
	
	public Location getExit() {
		return getGate().getExit();
	}
	
	public void setOverrideDesti(Portal desti) {
		this.overrideDesti = desti;
	}
	
	public void setNetwork(Network net) {
		this.network = net;
		this.drawControll();
	}
	
	public Portal getFinalDesti() {
		Portal destination;
		if(overrideDesti != null) {
			destination = overrideDesti;
			overrideDesti = null;
		} else {
			destination = getDestination();
		}
		return destination;
	}
	
	/**
	 * Surrounds one string with two strings
	 * @param target
	 * @param surrounding
	 * @return
	 */
	protected String surroundWith(String target, String[] surrounding) {
		return surrounding[0] + target + surrounding[1];
	}
	
	public void openDestAndThis(Player player) {
		// TODO Auto-generated method stub
		if (getDestination() == null) {
			// TODO write message?
			return;
		}
		// TODO checkPerms
		open(player);
		getDestination().open(player);
	}


	public Gate getGate() {
		return gate;
	}

	public void setGate(Gate gate) {
		this.gate = gate;
	}

}