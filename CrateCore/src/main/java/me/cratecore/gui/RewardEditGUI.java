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

public class RewardEditGUI implements Listener {
    private final CrateCore plugin;
    private final Player player;
    private final Crate crate;
    private final CrateReward reward;
    private Inventory inv;

    public RewardEditGUI(CrateCore plugin, Player player, Crate crate, CrateReward reward) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.reward = reward;
    }

    public void open() {
        build();
        player.openInventory(inv);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void build() {
        inv = Bukkit.createInventory(null, 54, ColorUtils.color("&8Edit Reward &7» &6" + reward.getId()));
        ItemStack glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        // Display item
        ItemStack display = reward.getDisplayItem() != null ? reward.getDisplayItem().clone() : new ItemStack(Material.PAPER);
        ItemMeta dm = display.getItemMeta();
        if (dm != null) {
            dm.setDisplayName(ColorUtils.color("&e&lDisplay Item"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&7Drag item here to change"));
            dm.setLore(lore);
            display.setItemMeta(dm);
        }
        inv.setItem(13, display);

        // Chance buttons
        inv.setItem(20, new ItemBuilder(Material.RED_DYE)
            .name("&c-5% Chance")
            .lore("&7Current: &f" + reward.getChance() + "%")
            .build());
        inv.setItem(21, new ItemBuilder(Material.ORANGE_DYE)
            .name("&6-1% Chance")
            .lore("&7Current: &f" + reward.getChance() + "%")
            .build());
        inv.setItem(22, new ItemBuilder(Material.PAPER)
            .name("&7Chance: &f" + reward.getChance() + "%")
            .lore("&7Left/Right click +/- buttons to adjust")
            .build());
        inv.setItem(23, new ItemBuilder(Material.LIME_DYE)
            .name("&a+1% Chance")
            .lore("&7Current: &f" + reward.getChance() + "%")
            .build());
        inv.setItem(24, new ItemBuilder(Material.GREEN_DYE)
            .name("&a+5% Chance")
            .lore("&7Current: &f" + reward.getChance() + "%")
            .build());

        // Broadcast toggle
        inv.setItem(29, new ItemBuilder(reward.isBroadcast() ? Material.LIME_DYE : Material.GRAY_DYE)
            .name("&6Broadcast")
            .lore("&7Status: " + (reward.isBroadcast() ? "&aEnabled" : "&cDisabled"),
                  "",
                  "&eClick to toggle")
            .build());

        // Max wins
        inv.setItem(31, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
            .name("&bMax Wins")
            .lore("&7Current: &f" + (reward.getMaxWins() == -1 ? "Unlimited" : reward.getMaxWins()),
                  "",
                  "&eLeft-click: +1",
                  "&eRight-click: -1",
                  "&eShift-click: Unlimited")
            .build());

        // Items slot - drag to add
        inv.setItem(33, new ItemBuilder(Material.CHEST)
            .name("&aReward Items (&f" + reward.getItems().size() + "&a)")
            .lore("&7Click to manage items")
            .build());

        // Back
        inv.setItem(45, new ItemBuilder(Material.ARROW).name("&7Back").build());

        // Save
        inv.setItem(49, new ItemBuilder(Material.LIME_DYE).name("&a&lSave").build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv) || !e.getWhoClicked().equals(player)) return;
        int slot = e.getRawSlot();

        // Drag to display slot
        if (slot == 13) {
            ItemStack cursor = e.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                e.setCancelled(true);
                reward.setDisplayItem(cursor.clone());
                ItemMeta meta = cursor.getItemMeta();
                if (meta != null && meta.hasDisplayName()) reward.setDisplayName(meta.getDisplayName().replace("\u00a7", "&"));
                else reward.setDisplayName(cursor.getType().name());
                plugin.getCrateManager().saveAll();
                build();
                player.openInventory(inv);
                return;
            }
        }

        e.setCancelled(true);

        switch (slot) {
            case 20 -> { reward.setChance(Math.max(0.1, reward.getChance() - 5)); plugin.getCrateManager().saveAll(); build(); player.openInventory(inv); }
            case 21 -> { reward.setChance(Math.max(0.1, reward.getChance() - 1)); plugin.getCrateManager().saveAll(); build(); player.openInventory(inv); }
            case 23 -> { reward.setChance(Math.min(100, reward.getChance() + 1)); plugin.getCrateManager().saveAll(); build(); player.openInventory(inv); }
            case 24 -> { reward.setChance(Math.min(100, reward.getChance() + 5)); plugin.getCrateManager().saveAll(); build(); player.openInventory(inv); }
            case 29 -> { reward.setBroadcast(!reward.isBroadcast()); plugin.getCrateManager().saveAll(); build(); player.openInventory(inv); }
            case 31 -> {
                if (e.isShiftClick()) { reward.setMaxWins(-1); }
                else if (e.isLeftClick()) { reward.setMaxWins(reward.getMaxWins() == -1 ? 1 : reward.getMaxWins() + 1); }
                else if (e.isRightClick() && reward.getMaxWins() > 1) { reward.setMaxWins(reward.getMaxWins() - 1); }
                plugin.getCrateManager().saveAll(); build(); player.openInventory(inv);
            }
            case 33 -> { player.closeInventory(); new RewardItemsGUI(plugin, player, crate, reward).open(); }
            case 45 -> { player.closeInventory(); new RewardsGUI(plugin, player, crate).open(); }
            case 49 -> { plugin.getCrateManager().saveAll(); player.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aSaved!")); player.closeInventory(); new RewardsGUI(plugin, player, crate).open(); }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inv) && e.getPlayer().equals(player)) HandlerList.unregisterAll(this);
    }
}
