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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class CrateEditorGUI implements Listener {
    private final CrateCore plugin;
    private final Player player;
    private final Crate crate;
    private Inventory inv;

    public CrateEditorGUI(CrateCore plugin, Player player, Crate crate) {
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
        inv = Bukkit.createInventory(null, 54, ColorUtils.color("&8Editor &7» &6" + crate.getId()));
        ItemStack glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        // Crate info
        inv.setItem(4, new ItemBuilder(crate.getBlockMaterial())
            .name("&6&l" + crate.getDisplayName())
            .lore("&7ID: &f" + crate.getId(),
                  "&7Block: &f" + crate.getBlockMaterial().name(),
                  "",
                  "&eClick to change block material")
            .build());

        // Key setup
        inv.setItem(20, new ItemBuilder(crate.getKey().getMaterial())
            .name("&b&lKey Setup")
            .lore("&7Key: &f" + crate.getKey().getDisplayName(),
                  "&7Material: &f" + crate.getKey().getMaterial().name(),
                  "",
                  "&eDrag your custom item here to set key",
                  "&eOr click to change material")
            .build());

        // Rewards
        inv.setItem(22, new ItemBuilder(Material.CHEST)
            .name("&a&lManage Rewards")
            .lore("&7Rewards: &f" + crate.getRewards().size(),
                  "",
                  "&eClick to manage rewards")
            .build());

        // Hologram toggle
        inv.setItem(24, new ItemBuilder(crate.isHologramEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
            .name("&b&lHologram")
            .lore("&7Status: " + (crate.isHologramEnabled() ? "&aEnabled" : "&cDisabled"),
                  "",
                  "&eClick to toggle")
            .build());

        // Broadcast toggle
        inv.setItem(29, new ItemBuilder(crate.isBroadcastEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
            .name("&6&lBroadcast")
            .lore("&7Status: " + (crate.isBroadcastEnabled() ? "&aEnabled" : "&cDisabled"),
                  "",
                  "&eClick to toggle")
            .build());

        // Animation type
        inv.setItem(31, new ItemBuilder(Material.FIREWORK_ROCKET)
            .name("&d&lAnimation")
            .lore("&7Current: &f" + crate.getAnimationType(),
                  "",
                  "&eClick to cycle: SPIN / FIRE / COMPACT / DEFAULT")
            .build());

        // Require key toggle
        inv.setItem(33, new ItemBuilder(crate.isRequireKey() ? Material.TRIPWIRE_HOOK : Material.BARRIER)
            .name("&e&lRequire Key")
            .lore("&7Status: " + (crate.isRequireKey() ? "&aYes" : "&cNo"),
                  "",
                  "&eClick to toggle")
            .build());

        // Preview rewards
        inv.setItem(40, new ItemBuilder(Material.BOOK)
            .name("&f&lPreview Rewards")
            .lore("&eClick to preview all rewards")
            .build());

        // Key drop zone indicator
        inv.setItem(11, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
            .name("&a&lDrag Key Item Here")
            .lore("&7Drag any item here to set it as the crate key")
            .build());

        // Save
        inv.setItem(49, new ItemBuilder(Material.LIME_DYE)
            .name("&a&lSave & Close")
            .lore("&7Save all changes")
            .build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv) || !e.getWhoClicked().equals(player)) return;

        int slot = e.getRawSlot();

        // Allow drag to slot 11 (key drop zone)
        if (slot == 11) {
            ItemStack cursor = e.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                e.setCancelled(true);
                // Set key material from dragged item
                crate.getKey().setMaterial(cursor.getType());
                ItemMeta meta = cursor.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    crate.getKey().setDisplayName(meta.getDisplayName().replace("\u00a7", "&"));
                }
                plugin.getCrateManager().saveAll();
                player.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aKey item updated!"));
                build();
                player.openInventory(inv);
                return;
            }
        }

        e.setCancelled(true);

        if (slot >= 54) return;

        switch (slot) {
            case 20 -> {
                // Cycle key material
                player.closeInventory();
                new MaterialSelectorGUI(plugin, player, "KEY_MATERIAL", crate, null).open();
            }
            case 4 -> {
                player.closeInventory();
                new MaterialSelectorGUI(plugin, player, "CRATE_MATERIAL", crate, null).open();
            }
            case 22 -> {
                player.closeInventory();
                new RewardsGUI(plugin, player, crate).open();
            }
            case 24 -> {
                crate.setHologramEnabled(!crate.isHologramEnabled());
                plugin.getCrateManager().saveAll();
                plugin.getHologramManager().refreshCrate(crate);
                build();
                player.openInventory(inv);
            }
            case 29 -> {
                crate.setBroadcastEnabled(!crate.isBroadcastEnabled());
                plugin.getCrateManager().saveAll();
                build();
                player.openInventory(inv);
            }
            case 31 -> {
                String[] anims = {"SPIN", "FIRE", "COMPACT", "DEFAULT"};
                int current = Arrays.asList(anims).indexOf(crate.getAnimationType());
                crate.setAnimationType(anims[(current + 1) % anims.length]);
                plugin.getCrateManager().saveAll();
                build();
                player.openInventory(inv);
            }
            case 33 -> {
                crate.setRequireKey(!crate.isRequireKey());
                plugin.getCrateManager().saveAll();
                build();
                player.openInventory(inv);
            }
            case 40 -> {
                player.closeInventory();
                new PreviewGUI(plugin, player, crate).open();
            }
            case 49 -> {
                plugin.getCrateManager().saveAll();
                player.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aSaved!"));
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!e.getInventory().equals(inv) || !e.getWhoClicked().equals(player)) return;
        // Allow drag only to slot 11
        for (int slot : e.getRawSlots()) {
            if (slot != 11 && slot < 54) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inv) && e.getPlayer().equals(player)) {
            HandlerList.unregisterAll(this);
        }
    }
}
