package me.cratecore;

import me.cratecore.commands.CrateCommand;
import me.cratecore.listeners.CrateListener;
import me.cratecore.managers.*;
import me.cratecore.models.Crate;
import org.bukkit.plugin.java.JavaPlugin;

public class CrateCore extends JavaPlugin {

    private static CrateCore instance;
    private CrateManager crateManager;
    private VirtualKeyManager virtualKeyManager;
    private HologramManager hologramManager;
    private ChatInputManager chatInputManager;
    private LocationSetManager locationSetManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        crateManager = new CrateManager(this);
        virtualKeyManager = new VirtualKeyManager(this);
        hologramManager = new HologramManager(this);
        chatInputManager = new ChatInputManager(this);
        locationSetManager = new LocationSetManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new CrateListener(this), this);

        // Register commands
        CrateCommand cmd = new CrateCommand(this);
        getCommand("cratecore").setExecutor(cmd);
        getCommand("cratecore").setTabCompleter(cmd);

        // Spawn holograms
        for (Crate crate : crateManager.getAllCrates()) {
            hologramManager.spawnForCrate(crate);
        }

        getLogger().info("CrateCore enabled! Loaded " + crateManager.getAllCrates().size() + " crate(s).");
    }

    @Override
    public void onDisable() {
        hologramManager.removeAll();
        crateManager.saveAll();
        virtualKeyManager.save();
        getLogger().info("CrateCore disabled.");
    }

    public static CrateCore getInstance() { return instance; }
    public CrateManager getCrateManager() { return crateManager; }
    public VirtualKeyManager getVirtualKeyManager() { return virtualKeyManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
    public LocationSetManager getLocationSetManager() { return locationSetManager; }
}
