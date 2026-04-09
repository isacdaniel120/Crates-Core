package me.cratecore.managers;

import me.cratecore.CrateCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputManager implements Listener {
    private final Map<UUID, Consumer<String>> waiting = new HashMap<>();

    public ChatInputManager(CrateCore plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void waitForInput(Player player, Consumer<String> callback) {
        waiting.put(player.getUniqueId(), callback);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (!waiting.containsKey(uuid)) return;
        e.setCancelled(true);
        Consumer<String> callback = waiting.remove(uuid);
        String input = e.getMessage().trim();
        e.getPlayer().getServer().getScheduler().runTask(
            e.getPlayer().getServer().getPluginManager().getPlugin("CrateCore"),
            () -> callback.accept(input)
        );
    }
}
