package me.cratecore.managers;

import me.cratecore.CrateCore;
import me.cratecore.models.Crate;
import me.cratecore.models.CrateKey;
import me.cratecore.models.CrateReward;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CrateManager {
    private final CrateCore plugin;
    private final Map<String, Crate> crates = new LinkedHashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public CrateManager(CrateCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "crates.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        crates.clear();

        ConfigurationSection sec = dataConfig.getConfigurationSection("crates");
        if (sec == null) return;

        for (String id : sec.getKeys(false)) {
            ConfigurationSection cs = sec.getConfigurationSection(id);
            if (cs == null) continue;
            Crate crate = new Crate(id);
            crate.setDisplayName(cs.getString("displayName", "&6" + id));
            String mat = cs.getString("material", "CHEST");
            try { crate.setBlockMaterial(Material.valueOf(mat)); } catch (Exception ignored) {}
            crate.setHologramEnabled(cs.getBoolean("hologram.enabled", true));
            crate.setHologramHeight(cs.getDouble("hologram.height", 2.5));
            crate.setHologramLines(cs.getStringList("hologram.lines"));
            crate.setAnimationType(cs.getString("animation", "SPIN"));
            crate.setBroadcastEnabled(cs.getBoolean("broadcast.enabled", true));
            crate.setBroadcastMessage(cs.getString("broadcast.message", ""));
            crate.setRequireKey(cs.getBoolean("requireKey", true));
            crate.setCooldownSeconds(cs.getInt("cooldown", 0));

            // Load key
            ConfigurationSection ks = cs.getConfigurationSection("key");
            if (ks != null) {
                CrateKey key = new CrateKey(id);
                key.setDisplayName(ks.getString("displayName", "&6" + id + " Key"));
                String km = ks.getString("material", "TRIPWIRE_HOOK");
                try { key.setMaterial(Material.valueOf(km)); } catch (Exception ignored) {}
                key.setLore(ks.getStringList("lore"));
                key.setGlowing(ks.getBoolean("glowing", false));
                crate.setKey(key);
            }

            // Load rewards
            ConfigurationSection rs = cs.getConfigurationSection("rewards");
            if (rs != null) {
                for (String rid : rs.getKeys(false)) {
                    ConfigurationSection rc = rs.getConfigurationSection(rid);
                    if (rc == null) continue;
                    CrateReward reward = new CrateReward(rid);
                    reward.setDisplayName(rc.getString("displayName", rid));
                    reward.setChance(rc.getDouble("chance", 10.0));
                    reward.setBroadcast(rc.getBoolean("broadcast", false));
                    reward.setBroadcastMessage(rc.getString("broadcastMessage", ""));
                    reward.setMaxWins(rc.getInt("maxWins", -1));
                    reward.setCommands(rc.getStringList("commands"));

                    // Display item
                    String dm = rc.getString("displayItem", "DIAMOND");
                    try {
                        ItemStack di = new ItemStack(Material.valueOf(dm));
                        reward.setDisplayItem(di);
                    } catch (Exception ignored) {}

                    // Reward items - stored as base64
                    List<ItemStack> items = new ArrayList<>();
                    List<String> itemStrings = rc.getStringList("items");
                    for (String s : itemStrings) {
                        try {
                            ItemStack is = deserializeItem(s);
                            if (is != null) items.add(is);
                        } catch (Exception ignored) {}
                    }
                    reward.setItems(items);
                    crate.addReward(reward);
                }
            }

            // Load locations
            List<String> locs = cs.getStringList("locations");
            for (String locStr : locs) {
                Location loc = deserializeLocation(locStr);
                if (loc != null) crate.addLocation(loc);
            }

            crates.put(id.toLowerCase(), crate);
        }
        plugin.getLogger().info("Loaded " + crates.size() + " crate(s).");
    }

    public void saveAll() {
        dataConfig.set("crates", null);
        for (Crate crate : crates.values()) {
            String path = "crates." + crate.getId();
            dataConfig.set(path + ".displayName", crate.getDisplayName());
            dataConfig.set(path + ".material", crate.getBlockMaterial().name());
            dataConfig.set(path + ".hologram.enabled", crate.isHologramEnabled());
            dataConfig.set(path + ".hologram.height", crate.getHologramHeight());
            dataConfig.set(path + ".hologram.lines", crate.getHologramLines());
            dataConfig.set(path + ".animation", crate.getAnimationType());
            dataConfig.set(path + ".broadcast.enabled", crate.isBroadcastEnabled());
            dataConfig.set(path + ".broadcast.message", crate.getBroadcastMessage());
            dataConfig.set(path + ".requireKey", crate.isRequireKey());
            dataConfig.set(path + ".cooldown", crate.getCooldownSeconds());

            // Save key
            CrateKey key = crate.getKey();
            dataConfig.set(path + ".key.displayName", key.getDisplayName());
            dataConfig.set(path + ".key.material", key.getMaterial().name());
            dataConfig.set(path + ".key.lore", key.getLore());
            dataConfig.set(path + ".key.glowing", key.isGlowing());

            // Save rewards
            for (CrateReward reward : crate.getRewards()) {
                String rp = path + ".rewards." + reward.getId();
                dataConfig.set(rp + ".displayName", reward.getDisplayName());
                dataConfig.set(rp + ".chance", reward.getChance());
                dataConfig.set(rp + ".broadcast", reward.isBroadcast());
                dataConfig.set(rp + ".broadcastMessage", reward.getBroadcastMessage());
                dataConfig.set(rp + ".maxWins", reward.getMaxWins());
                dataConfig.set(rp + ".commands", reward.getCommands());
                if (reward.getDisplayItem() != null) {
                    dataConfig.set(rp + ".displayItem", reward.getDisplayItem().getType().name());
                }
                List<String> itemStrings = new ArrayList<>();
                for (ItemStack is : reward.getItems()) {
                    try { itemStrings.add(serializeItem(is)); } catch (Exception ignored) {}
                }
                dataConfig.set(rp + ".items", itemStrings);
            }

            // Save locations
            List<String> locs = new ArrayList<>();
            for (Location loc : crate.getLocations()) locs.add(serializeLocation(loc));
            dataConfig.set(path + ".locations", locs);
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public Crate getCrate(String id) { return crates.get(id.toLowerCase()); }
    public Collection<Crate> getAllCrates() { return crates.values(); }
    public void addCrate(Crate crate) { crates.put(crate.getId().toLowerCase(), crate); saveAll(); }
    public boolean removeCrate(String id) {
        if (crates.remove(id.toLowerCase()) == null) return false;
        saveAll(); return true;
    }

    public Crate getCrateAtLocation(Location loc) {
        for (Crate c : crates.values()) {
            for (Location cl : c.getLocations()) {
                if (cl.getWorld() != null && cl.getWorld().equals(loc.getWorld())
                    && cl.getBlockX() == loc.getBlockX()
                    && cl.getBlockY() == loc.getBlockY()
                    && cl.getBlockZ() == loc.getBlockZ()) return c;
            }
        }
        return null;
    }

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    private Location deserializeLocation(String s) {
        try {
            String[] p = s.split(",");
            return new Location(Bukkit.getWorld(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]));
        } catch (Exception e) { return null; }
    }

    private String serializeItem(ItemStack item) {
        return item.getType().name() + ":" + item.getAmount();
    }

    private ItemStack deserializeItem(String s) {
        String[] p = s.split(":");
        Material mat = Material.valueOf(p[0]);
        int amount = p.length > 1 ? Integer.parseInt(p[1]) : 1;
        return new ItemStack(mat, amount);
    }
}
