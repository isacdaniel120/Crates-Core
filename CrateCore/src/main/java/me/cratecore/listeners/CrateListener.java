package me.cratecore.listeners;

import me.cratecore.CrateCore;
import me.cratecore.animations.CrateAnimation;
import me.cratecore.models.Crate;
import me.cratecore.models.CrateReward;
import me.cratecore.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CrateListener implements Listener {
    private final CrateCore plugin;
    private final Set<UUID> opening = new HashSet<>();

    public CrateListener(CrateCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        Player player = e.getPlayer();
        Location loc = e.getClickedBlock().getLocation();
        Crate crate = plugin.getCrateManager().getCrateAtLocation(loc);
        if (crate == null) return;

        e.setCancelled(true);

        if (opening.contains(player.getUniqueId())) {
            player.sendMessage(ColorUtils.color("&cPlease wait for the animation to finish!"));
            return;
        }

        // Check permission
        if (!player.hasPermission("cratecore.use")) {
            player.sendMessage(ColorUtils.color("&cYou don't have permission to open this crate!"));
            return;
        }

        // Shift right click = preview
        if (player.isSneaking()) {
            new me.cratecore.gui.PreviewGUI(plugin, player, crate).open();
            return;
        }

        // Check key
        if (crate.isRequireKey()) {
            boolean hasPhysicalKey = false;
            boolean hasVirtualKey = plugin.getVirtualKeyManager().hasKey(player, crate.getId());

            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (crate.getKey().isKey(handItem)) hasPhysicalKey = true;

            if (!hasPhysicalKey && !hasVirtualKey) {
                player.sendMessage(ColorUtils.color("&cYou need a &f" + crate.getKey().getDisplayName() + " &cto open this crate!"));
                return;
            }

            // Take key - physical first
            if (hasPhysicalKey) {
                handItem.setAmount(handItem.getAmount() - 1);
            } else {
                plugin.getVirtualKeyManager().takeKey(player, crate.getId());
            }
        }

        // Roll reward
        CrateReward reward = crate.rollReward();
        if (reward == null) {
            player.sendMessage(ColorUtils.color("&cNo rewards available in this crate!"));
            return;
        }

        opening.add(player.getUniqueId());

        new CrateAnimation(plugin, player, crate, reward, loc, () -> {
            giveReward(player, crate, reward);
            opening.remove(player.getUniqueId());
        }).play();
    }

    private void giveReward(Player player, Crate crate, CrateReward reward) {
        reward.incrementWins();

        // Give items
        for (ItemStack item : reward.getItems()) {
            player.getInventory().addItem(item.clone()).forEach((slot, leftover) ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }

        // Run commands
        for (String cmd : reward.getCommands()) {
            String parsed = cmd.replace("{player}", player.getName())
                               .replace("{crate}", crate.getId())
                               .replace("{reward}", reward.getDisplayName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }

        // Reward message
        player.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aYou won &f" + ColorUtils.color(reward.getDisplayName()) + " &afrom &f" + ColorUtils.color(crate.getDisplayName()) + "&a!"));

        // Broadcast
        boolean globalBroadcast = plugin.getConfig().getBoolean("broadcast-enabled", true);
        if (globalBroadcast && crate.isBroadcastEnabled() && reward.isBroadcast()) {
            String msg = reward.getBroadcastMessage().isEmpty()
                ? plugin.getConfig().getString("broadcast-message", "&6{player} &ewon &6{reward} &efrom &6{crate}!")
                : reward.getBroadcastMessage();
            msg = msg.replace("{player}", player.getName())
                     .replace("{reward}", ColorUtils.color(reward.getDisplayName()))
                     .replace("{crate}", ColorUtils.color(crate.getDisplayName()));
            Bukkit.broadcastMessage(ColorUtils.color(msg));
        }

        plugin.getCrateManager().saveAll();
    }
}
