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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RewardsGUI implements Listener {
    private final CrateCore plugin;
    private final Player player;
    private final Crate crate;
    private Inventory inv;
    private int page = 0;
    private static final int SLOTS_PER_PAGE = 28;

    public RewardsGUI(CrateCore plugin, Player player, Crate crate) {
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
        inv = Bukkit.createInventory(null, 54, ColorUtils.color("&8Rewards &7» &6" + crate.getId()));
        ItemStack glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        // Drag zone indicator (slots 10-16, 19-25, 28-34, 37-43)
        int[] rewardSlots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
        for (int slot : rewardSlots) {
            inv.setItem(slot, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("&7Drag reward item here").build());
        }

        List<CrateReward> rewards = crate.getRewards();
        int start = page * SLOTS_PER_PAGE;
        for (int i = 0; i < rewardSlots.length && (start + i) < rewards.size(); i++) {
            CrateReward reward = rewards.get(start + i);
            ItemStack display = reward.getDisplayItem() != null ? reward.getDisplayItem().clone() : new ItemStack(Material.PAPER);
            ItemMeta meta = display.getItemMeta();
            if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(display.getType());
            meta.setDisplayName(ColorUtils.color(reward.getDisplayName()));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&7Chance: &f" + reward.getChance() + "%"));
            lore.add(ColorUtils.color("&7Items: &f" + reward.getItems().size()));
            lore.add(ColorUtils.color("&7Commands: &f" + reward.getCommands().size()));
            lore.add(ColorUtils.color("&7Broadcast: &f" + reward.isBroadcast()));
            lore.add(ColorUtils.color("&7Max Wins: &f" + (reward.getMaxWins() == -1 ? "Unlimited" : reward.getMaxWins())));
            lore.add("");
            lore.add(ColorUtils.color("&eLeft-click to edit"));
            lore.add(ColorUtils.color("&cRight-click to delete"));
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(rewardSlots[i], display);
        }

        // Add reward button
        inv.setItem(4, new ItemBuilder(Material.LIME_DYE)
            .name("&a&lAdd Reward")
            .lore("&7Drag an item to any green slot",
                  "&7to create a new reward",
                  "",
                  "&7Or click here to add empty reward")
            .build());

        // Total chance display
        double total = rewards.stream().mapToDouble(CrateReward::getChance).sum();
        inv.setItem(49, new ItemBuilder(Material.PAPER)
            .name("&7Total Chance: &f" + String.format("%.1f", total) + "%")
            .lore("&7Rewards auto-normalize to 100%")
            .build());

        // Back
        inv.setItem(45, new ItemBuilder(Material.ARROW).name("&7Back").build());

        // Page nav
        if (page > 0) inv.setItem(46, new ItemBuilder(Material.ARROW).name("&7Previous Page").build());
        if ((page + 1) * SLOTS_PER_PAGE < rewards.size())
            inv.setItem(52, new ItemBuilder(Material.ARROW).name("&7Next Page").build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv) || !e.getWhoClicked().equals(player)) return;
        int slot = e.getRawSlot();

        int[] rewardSlots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};

        // Check if clicking a reward slot with cursor item = drag to create reward
        for (int i = 0; i < rewardSlots.length; i++) {
            if (slot == rewardSlots[i]) {
                ItemStack cursor = e.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR && e.isLeftClick()) {
                    // Create reward from dragged item
                    e.setCancelled(true);
                    CrateReward reward = new CrateReward("reward_" + UUID.randomUUID().toString().substring(0, 6));
                    reward.setDisplayItem(cursor.clone());
                    ItemMeta meta = cursor.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        reward.setDisplayName(meta.getDisplayName().replace("\u00a7", "&"));
                    } else {
                        reward.setDisplayName(cursor.getType().name());
                    }
                    reward.addItem(cursor.clone());
                    crate.addReward(reward);
                    plugin.getCrateManager().saveAll();
                    player.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aReward added! Click it to edit."));
                    build();
                    player.openInventory(inv);
                    return;
                }
                // Right click existing = delete
                int rewardIndex = page * SLOTS_PER_PAGE + i;
                if (rewardIndex < crate.getRewards().size() && e.isRightClick()) {
                    e.setCancelled(true);
                    crate.getRewards().remove(rewardIndex);
                    plugin.getCrateManager().saveAll();
                    build();
                    player.openInventory(inv);
                    return;
                }
                // Left click existing = edit
                if (rewardIndex < crate.getRewards().size() && e.isLeftClick()) {
                    e.setCancelled(true);
                    player.closeInventory();
                    new RewardEditGUI(plugin, player, crate, crate.getRewards().get(rewardIndex)).open();
                    return;
                }
            }
        }

        e.setCancelled(true);

        switch (slot) {
            case 4 -> {
                CrateReward reward = new CrateReward("reward_" + UUID.randomUUID().toString().substring(0, 6));
                reward.setDisplayItem(new ItemStack(Material.PAPER));
                crate.addReward(reward);
                plugin.getCrateManager().saveAll();
                build();
                player.openInventory(inv);
            }
            case 45 -> {
                player.closeInventory();
                new CrateEditorGUI(plugin, player, crate).open();
            }
            case 46 -> { if (page > 0) { page--; build(); player.openInventory(inv); } }
            case 52 -> { page++; build(); player.openInventory(inv); }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!e.getInventory().equals(inv) || !e.getWhoClicked().equals(player)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inv) && e.getPlayer().equals(player)) HandlerList.unregisterAll(this);
    }
}
