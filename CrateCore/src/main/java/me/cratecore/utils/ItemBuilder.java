package me.cratecore.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material mat) {
        this.item = new ItemStack(mat);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (meta != null) meta.setDisplayName(ColorUtils.color(name));
        return this;
    }

    public ItemBuilder lore(String... lines) {
        if (meta != null) meta.setLore(Arrays.stream(lines).map(ColorUtils::color).collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        if (meta != null) meta.setLore(ColorUtils.color(lines));
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder cmd(int cmd) {
        if (meta != null) meta.setCustomModelData(cmd);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack glass(String name) {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(name).build();
    }
}
