package me.cratecore.gui;

import me.cratecore.CrateCore;
import me.cratecore.models.Crate;
import me.cratecore.models.CrateReward;
import me.cratecore.utils.ColorUtils;
import me.cratecore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PreviewGUI implements Listener {
    private final CrateCore plugin;
    private final Player player;
    private final Crate crate;
    private Inventory inv;

    public PreviewGUI(CrateCore plugin, Player player, Crate crate) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
    }

    public void open() {
        build();
        player.openInventory(inv);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void build() {
        inv = Bukkit.createInventory(null, 54, ColorUtils.color("&8Preview &7» &6" + crate.getId()));
        ItemStack glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
        double total = crate.getRewards().stream().mapToDouble(CrateReward::getChance).sum();

        for (int i = 0; i < slots.length && i < crate.getRewards().size(); i++) {
            CrateReward reward = crate.getRewards().get(i);
            ItemStack display = reward.getDisplayItem() != null ? reward.getDisplayItem().clone() : new ItemStack(Material.PAPER);
            ItemMeta meta = display.getItemMeta();
            if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(display.getType());
            meta.setDisplayName(ColorUtils.color(reward.getDisplayName()));
            List<String> lore = new ArrayList<>();
            double pct = total > 0 ? (reward.getChance() / total) * 100 : 0;
            lore.add(ColorUtils.color(String.format("&7Chance: &f%.1f%%", pct)));
            if (reward.getMaxWins() != -1) lore.add(ColorUtils.color("&7Max Wins: &f" + reward.getMaxWins()));
            if (!reward.getCommands().isEmpty()) lore.add(ColorUtils.color("&7Commands: &f" + reward.getCommands().size()));
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(slots[i], display);
        }

        inv.setItem(49, new ItemBuilder(Material.ARROW).name("&7Close").build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv) || !e.getWhoClicked().equals(player)) return;
        e.setCancelled(true);
        if (e.getRawSlot() == 49) player.closeInventory();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inv) && e.getPlayer().equals(player)) HandlerList.unregisterAll(this);
    }
}
