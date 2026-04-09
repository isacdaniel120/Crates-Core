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

import java.util.ArrayList;
import java.util.List;

public class RewardItemsGUI implements Listener {
    private final CrateCore plugin;
    private final Player player;
    private final Crate crate;
    private final CrateReward reward;
    private Inventory inv;

    public RewardItemsGUI(CrateCore plugin, Player player, Crate crate, CrateReward reward) {
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
        inv = Bukkit.createInventory(null, 54, ColorUtils.color("&8Items &7» &6" + reward.getId()));
        ItemStack glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        inv.setItem(4, new ItemBuilder(Material.BOOK)
            .name("&e&lReward Items")
            .lore("&7Drag items into green slots",
                  "&7Right-click items to remove them",
                  "&7Items count: &f" + reward.getItems().size())
            .build());

        int[] itemSlots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        for (int slot : itemSlots) {
            inv.setItem(slot, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("&7Drag item here").build());
        }

        List<ItemStack> items = reward.getItems();
        for (int i = 0; i < itemSlots.length && i < items.size(); i++) {
            inv.setItem(itemSlots[i], items.get(i).clone());
        }

        inv.setItem(45, new ItemBuilder(Material.ARROW).name("&7Back").build());
        inv.setItem(49, new ItemBuilder(Material.LIME_DYE).name("&a&lSave").build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv) || !e.getWhoClicked().equals(player)) return;
        int slot = e.getRawSlot();
        int[] itemSlots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};

        for (int i = 0; i < itemSlots.length; i++) {
            if (slot == itemSlots[i]) {
                ItemStack cursor = e.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR && e.isLeftClick()) {
                    // Add item
                    e.setCancelled(true);
                    List<ItemStack> items = reward.getItems();
                    if (i < items.size()) items.set(i, cursor.clone());
                    else items.add(cursor.clone());
                    plugin.getCrateManager().saveAll();
                    build();
                    player.openInventory(inv);
                    return;
                }
                if (e.isRightClick() && i < reward.getItems().size()) {
                    e.setCancelled(true);
                    reward.getItems().remove(i);
                    plugin.getCrateManager().saveAll();
                    build();
                    player.openInventory(inv);
                    return;
                }
            }
        }

        e.setCancelled(true);
        if (slot == 45) { player.closeInventory(); new RewardEditGUI(plugin, player, crate, reward).open(); }
        if (slot == 49) { plugin.getCrateManager().saveAll(); player.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aSaved!")); player.closeInventory(); new RewardEditGUI(plugin, player, crate, reward).open(); }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inv) && e.getPlayer().equals(player)) HandlerList.unregisterAll(this);
    }
}
