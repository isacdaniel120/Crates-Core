package me.cratecore.managers;

import me.cratecore.CrateCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualKeyManager {
    private final CrateCore plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, Map<String, Integer>> keys = new HashMap<>();

    public VirtualKeyManager(CrateCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "virtual_keys.yml");
        if (!dataFile.exists()) try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        keys.clear();
        for (String uuidStr : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Integer> playerKeys = new HashMap<>();
                for (String crate : dataConfig.getConfigurationSection(uuidStr).getKeys(false)) {
                    playerKeys.put(crate, dataConfig.getInt(uuidStr + "." + crate));
                }
                keys.put(uuid, playerKeys);
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        dataConfig.set(null, null);
        for (Map.Entry<UUID, Map<String, Integer>> entry : keys.entrySet()) {
            for (Map.Entry<String, Integer> ke : entry.getValue().entrySet()) {
                dataConfig.set(entry.getKey().toString() + "." + ke.getKey(), ke.getValue());
            }
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public int getKeys(Player p, String crateId) {
        return keys.getOrDefault(p.getUniqueId(), new HashMap<>()).getOrDefault(crateId.toLowerCase(), 0);
    }

    public void addKeys(Player p, String crateId, int amount) {
        keys.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>())
            .merge(crateId.toLowerCase(), amount, Integer::sum);
        save();
    }

    public boolean takeKey(Player p, String crateId) {
        Map<String, Integer> playerKeys = keys.get(p.getUniqueId());
        if (playerKeys == null) return false;
        int current = playerKeys.getOrDefault(crateId.toLowerCase(), 0);
        if (current <= 0) return false;
        playerKeys.put(crateId.toLowerCase(), current - 1);
        save();
        return true;
    }

    public boolean hasKey(Player p, String crateId) {
        return getKeys(p, crateId) > 0;
    }
}
