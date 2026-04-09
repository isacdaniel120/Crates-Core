package me.cratecore.managers;

import me.cratecore.CrateCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class LocationSetManager implements Listener {
    private final Map<UUID, Consumer<Location>> waiting = new HashMap<>();

    public LocationSetManager(CrateCore plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void waitForLocation(Player player, Consumer<Location> callback) {
        waiting.put(player.getUniqueId(), callback);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        UUID uuid = e.getPlayer().getUniqueId();
        if (!waiting.containsKey(uuid)) return;
        e.setCancelled(true);
        Consumer<Location> callback = waiting.remove(uuid);
        callback.accept(e.getClickedBlock().getLocation());
    }
}
