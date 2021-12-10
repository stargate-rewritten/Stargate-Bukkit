package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Settings;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.util.ColorConverter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;

import java.util.logging.Level;

public class LineColorCompiler implements ILineCompiler {

    private final DyeColor signColor;
    private final boolean isLightSign;
    static private final ChatColor GRAY_SELECTOR_COLOR = ChatColor.of("#808080");
    static private final ChatColor ERROR_COLOR = ChatColor.RED;


    public LineColorCompiler(Sign sign) {
        this.signColor = sign.getColor();
        this.isLightSign = isLightSign(sign.getType());
    }

    public LineColorCompiler(DyeColor signColor, Material signMaterial) {
        this.signColor = signColor;
        this.isLightSign = isLightSign(signMaterial);
    }

    /**
     * Compiles how this portal will look like on a sign, includes the
     *
     * @param surround
     * @param portal
     * @return
     */
    @Override
    public String compilePortalName(HighlightingStyle surround, IPortal portal) {
        String name = portal.getName();

        ChatColor nameColor;
        ChatColor selectorColor;

        switch (Settings.getInteger(Setting.NAME_STYLE)) {
            case 1:
            case 2:
            default:
                nameColor = getNameColor(portal, isLightSign);
                break;
            case 3:
                nameColor = getDefaultColor(isLightSign);
                break;
        }

        switch (Settings.getInteger(Setting.NAME_STYLE)) {
            case 1:
                selectorColor = getDefaultColor(isLightSign);
                break;
            case 2:
                selectorColor = getNameColor(portal, isLightSign);
                break;
            case 4:
                selectorColor = getDefaultColor(!isLightSign);
                break;
            default:
                selectorColor = GRAY_SELECTOR_COLOR;
        }

        String coloredName = nameColor + name + selectorColor;
        return selectorColor + surround.getHighlightedName(coloredName);

    }

    @Override
    public String compileLine(String line) {
        return getColor(isLightSign) + line;
    }

    @Override
    public String compileErrorLine(String error, HighlightingStyle surround) {
        return getColor(isLightSign) + surround.getHighlightedName(ERROR_COLOR + error + getColor(isLightSign));
    }

    static protected boolean isLightSign(Material signMaterial) {

        switch (signMaterial) {
            // Dark signs
            case DARK_OAK_WALL_SIGN:
            case WARPED_WALL_SIGN:
            case CRIMSON_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
                return false;
            default:
                return true;
        }
    }

    private ChatColor getColor(boolean isLightSign) {
        if (signColor != DyeColor.BLACK) {
            if (signColor != null) {
                return ColorConverter.getChatColorFromDyeColor(signColor);
            } else {
                return null;
            }
        }
        return getDefaultColor(isLightSign);
    }

    private ChatColor getDefaultColor(boolean isLightSign) {
        return isLightSign ? Stargate.defaultLightSignColor : Stargate.defaultDarkColor;
    }

    private ChatColor getNameColor(IPortal portal, boolean isLightSign) {
        Stargate.log(Level.FINEST, " Gate " + portal.getName() + " has flags: " + portal.getAllFlagsString());
        ChatColor[] colors = new ChatColor[]{getColor(true), getColor(false)};

        if (portal.hasFlag(PortalFlag.BACKWARDS)) {
            colors = new ChatColor[]{ChatColor.of("#240023"), ChatColor.of("#b3baff")};
        }
        if (portal.hasFlag(PortalFlag.FORCE_SHOW)) {
            colors = new ChatColor[]{ChatColor.of("#002422"), ChatColor.of("#b3fffc")};
        }
        if (portal.hasFlag(PortalFlag.HIDDEN)) {
            colors = new ChatColor[]{ChatColor.of("#292800"), ChatColor.of("#fffcb3")};
        }
        if (portal.hasFlag(PortalFlag.FREE)) {
            colors = new ChatColor[]{ChatColor.of("#002402"), ChatColor.of("#b3ffb8")};
        }
        if (portal.hasFlag(PortalFlag.PRIVATE)) {
            colors = new ChatColor[]{ChatColor.of("#210000"), ChatColor.of("#ffb3b3")};
        }
        if (portal instanceof VirtualPortal) {
            colors = new ChatColor[]{ChatColor.of("#240023"), ChatColor.of("#FFE0FE")};
        }
        return (isLightSign ? colors[0] : colors[1]);
    }
}
