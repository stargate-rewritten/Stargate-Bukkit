package net.TheDgtl.Stargate.refactoring.retcons;

import net.TheDgtl.Stargate.TwoTuple;
import net.TheDgtl.Stargate.network.StargateFactory;
import net.TheDgtl.Stargate.util.FileHelper;
import org.bukkit.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RetCon1_0_0 extends Modifier {

    /**
     * A list of every old setting-name and what it changed to in this ret-con
     */
    static private final HashMap<String, String> CONFIG_CONVERSIONS = new HashMap<>();

    static {
        //Read all config migrations
        Map<String, String> migrationFields;
        try {
            migrationFields = FileHelper.readKeyValuePairs(FileHelper.getBufferedReaderFromInputStream(
                    FileHelper.getInputStreamForInternalFile("/migration/config-migrations-1_0_0.txt")));
            for (String key : migrationFields.keySet()) {
                String value = migrationFields.get(key);
                if (value.trim().isEmpty()) {
                    CONFIG_CONVERSIONS.put(key, null);
                } else {
                    CONFIG_CONVERSIONS.put(key, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Server server;
    private final StargateFactory factory;
    private Map<String, Object> oldConfig;

    /**
     * Instantiates a new Ret-Con 1.0.0
     *
     * @param server  <p>The server to use for loading legacy portals</p>
     * @param factory <p>The stargate factory to use for loading legacy portals</p>
     */
    public RetCon1_0_0(Server server, StargateFactory factory) {
        this.server = server;
        this.factory = factory;
    }

    @Override
    public Map<String, Object> getConfigModifications(Map<String, Object> oldConfig) {
        Map<String, Object> newConfig = super.getConfigModifications(oldConfig);
        this.oldConfig = oldConfig;

        Level logLevel = Level.INFO;
        if ((oldConfig.get("permdebug") != null && (boolean) oldConfig.get("permdebug")) ||
                (oldConfig.get("debugging.permdebug") != null) && (boolean) oldConfig.get("debugging.permdebug")) {
            logLevel = Level.CONFIG;
        }
        if ((oldConfig.get("debug") != null && (boolean) oldConfig.get("debug")) ||
                (oldConfig.get("debugging.debug") != null && (boolean) oldConfig.get("debugging.debug"))) {
            logLevel = Level.FINE;
        }
        newConfig.put("loggingLevel", logLevel.toString());
        return newConfig;
    }

    @Override
    public void run() {
        try {
            String[] possiblePortalFoldersSetting = {"portal-folder", "folders.portalFolder"};
            for (String portalFolder : possiblePortalFoldersSetting) {
                if (oldConfig.get(portalFolder) != null) {
                    LegacyPortalStorageLoader.loadPortalsFromStorage((String) oldConfig.get(portalFolder), server, factory);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected TwoTuple<String, Object> getNewSetting(TwoTuple<String, Object> oldSetting) {
        if (!CONFIG_CONVERSIONS.containsKey(oldSetting.getFirstValue())) {
            return oldSetting;
        }
        String newKey = CONFIG_CONVERSIONS.get(oldSetting.getFirstValue());

        if (newKey == null)
            return null;

        if (oldSetting.getFirstValue().equals("freegatesgreen") ||
                oldSetting.getFirstValue().equals("economy.freeGatesColored"))
            return new TwoTuple<>(newKey, 2);

        return new TwoTuple<>(newKey, oldSetting.getSecondValue());
    }

    @Override
    public int getConfigVersion() {
        return 6;
    }

}
