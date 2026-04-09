package me.cratecore.gui;

import me.cratecore.CrateCore;
import me.cratecore.models.Crate;
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

public class CrateListGUI implements Listener {
    private final CrateCore plugin;
    private final Player player;
    private Inventory inv;

    public CrateListGUI(CrateCore plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        build();
        player.openInventory(inv);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void build() {
        inv = Bukkit.createInventory(null, 54, ColorUtils.color("&8CrateCore &7» &6All Crates"));
        ItemStack glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        List<Crate> crates = new ArrayList<>(plugin.getCrateManager().getAllCrates());
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};

        for (int i = 0; i < slots.length && i < crates.size(); i++) {
            Crate crate = crates.get(i);
            inv.setItem(slots[i], new ItemBuilder(crate.getBlockMaterial())
                .name(ColorUtils.color(crate.getDisplayName()))
                .lore("&7ID: &f" + crate.getId(),
                      "&7Rewards: &f" + crate.getRewards().size(),
                      "&7Locations: &f" + crate.getLocations().size(),
                      "&7Hologram: " + (crate.isHologramEnabled() ? "&aOn" : "&cOff"),
                      "",
                      "&eLeft-click: Edit",
                      "&cRight-click: Delete")
                .build());
        }

        // Add new crate button
        inv.setItem(49, new ItemBuilder(Material.LIME_DYE)
            .name("&a&lCreate New Crate")
            .lore("&7Click to create a new crate")
            .build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv) || !e.getWhoClicked().equals(player)) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();

        List<Crate> crates = new ArrayList<>(plugin.getCrateManager().getAllCrates());
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};

        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i] && i < crates.size()) {
                Crate crate = crates.get(i);
                if (e.isLeftClick()) {
                    player.closeInventory();
                    new CrateEditorGUI(plugin, player, crate).open();
                } else if (e.isRightClick()) {
                    plugin.getCrateManager().removeCrate(crate.getId());
                    player.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &cCrate &f" + crate.getId() + " &cdeleted."));
                    build();
                    player.openInventory(inv);
                }
                return;
            }
        }

        if (slot == 49) {
            player.closeInventory();
            player.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &eType the crate ID in chat:"));
            plugin.getChatInputManager().waitForInput(player, input -> {
                if (input.isEmpty()) return;
                Crate crate = new Crate(input);
                plugin.getCrateManager().addCrate(crate);
                player.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aCrate &f" + input + " &acreated!"));
                new CrateEditorGUI(plugin, player, crate).open();
            });
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inv) && e.getPlayer().equals(player)) HandlerList.unregisterAll(this);
    }
}
