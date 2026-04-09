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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MaterialSelectorGUI implements Listener {
    private final CrateCore plugin;
    private final Player player;
    private final String type; // "CRATE_MATERIAL" or "KEY_MATERIAL"
    private final Crate crate;
    private final CrateReward reward;
    private Inventory inv;
    private int page = 0;
    private static final int PER_PAGE = 45;

    private static final List<Material> BLOCK_MATERIALS = Arrays.stream(Material.values())
        .filter(m -> m.isBlock() && !m.isAir() && m.isSolid())
        .collect(Collectors.toList());

    private static final List<Material> ITEM_MATERIALS = Arrays.stream(Material.values())
        .filter(m -> !m.isAir())
        .collect(Collectors.toList());

    public MaterialSelectorGUI(CrateCore plugin, Player player, String type, Crate crate, CrateReward reward) {
        this.plugin = plugin;
        this.player = player;
        this.type = type;
        this.crate = crate;
        this.reward = reward;
    }

    public void open() {
        build();
        player.openInventory(inv);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void build() {
        List<Material> materials = type.equals("CRATE_MATERIAL") ? BLOCK_MATERIALS : ITEM_MATERIALS;
        inv = Bukkit.createInventory(null, 54, ColorUtils.color("&8Select Material &7» &6" + type));

        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && (start + i) < materials.size(); i++) {
            Material mat = materials.get(start + i);
            inv.setItem(i, new ItemBuilder(mat).name("&f" + mat.name()).build());
        }

        if (page > 0) inv.setItem(45, new ItemBuilder(Material.ARROW).name("&7Previous").build());
        inv.setItem(49, new ItemBuilder(Material.BARRIER).name("&cBack").build());
        if ((page + 1) * PER_PAGE < materials.size())
            inv.setItem(53, new ItemBuilder(Material.ARROW).name("&7Next").build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inv) || !e.getWhoClicked().equals(player)) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == 49) { player.closeInventory(); new CrateEditorGUI(plugin, player, crate).open(); return; }
        if (slot == 45 && page > 0) { page--; build(); player.openInventory(inv); return; }
        if (slot == 53) { page++; build(); player.openInventory(inv); return; }

        if (slot < 45) {
            List<Material> materials = type.equals("CRATE_MATERIAL") ? BLOCK_MATERIALS : ITEM_MATERIALS;
            int idx = page * PER_PAGE + slot;
            if (idx < materials.size()) {
                Material selected = materials.get(idx);
                if (type.equals("CRATE_MATERIAL")) {
                    crate.setBlockMaterial(selected);
                } else {
                    crate.getKey().setMaterial(selected);
                }
                plugin.getCrateManager().saveAll();
                player.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aMaterial set to &f" + selected.name()));
                player.closeInventory();
                new CrateEditorGUI(plugin, player, crate).open();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inv) && e.getPlayer().equals(player)) HandlerList.unregisterAll(this);
    }
}
