package me.cratecore.models;

import me.cratecore.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class CrateKey {
    private String crateId;
    private String displayName;
    private Material material;
    private List<String> lore = new ArrayList<>();
    private boolean glowing;
    private static final int KEY_CMD = 74001;

    public CrateKey(String crateId) {
        this.crateId = crateId;
        this.displayName = "&6" + crateId + " Key";
        this.material = Material.TRIPWIRE_HOOK;
        this.glowing = false;
    }

    public ItemStack buildItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(ColorUtils.color(displayName));
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) coloredLore.add(ColorUtils.color(line));
        coloredLore.add(ColorUtils.color("&8[CrateCore:" + crateId + "]"));
        meta.setLore(coloredLore);
        meta.setCustomModelData(KEY_CMD + crateId.hashCode());
        item.setItemMeta(meta);
        return item;
    }

    public boolean isKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;
        List<String> l = meta.getLore();
        if (l == null) return false;
        return l.stream().anyMatch(line -> line.contains("[CrateCore:" + crateId + "]"));
    }

    public String getCrateId() { return crateId; }
    public void setCrateId(String i) { this.crateId = i; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String n) { this.displayName = n; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material m) { this.material = m; }
    public List<String> getLore() { return lore; }
    public void setLore(List<String> l) { this.lore = l; }
    public boolean isGlowing() { return glowing; }
    public void setGlowing(boolean g) { this.glowing = g; }
}
