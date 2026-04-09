package me.cratecore.managers;

import me.cratecore.CrateCore;
import me.cratecore.models.Crate;
import me.cratecore.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramManager {
    private final CrateCore plugin;
    private final Map<String, List<ArmorStand>> holograms = new HashMap<>();

    public HologramManager(CrateCore plugin) {
        this.plugin = plugin;
    }

    public void spawnForCrate(Crate crate) {
        removeForCrate(crate.getId());
        if (!crate.isHologramEnabled()) return;
        for (Location loc : crate.getLocations()) {
            spawnHologram(crate, loc);
        }
    }

    private void spawnHologram(Crate crate, Location loc) {
        List<String> lines = crate.getHologramLines();
        List<ArmorStand> stands = new ArrayList<>();
        double height = crate.getHologramHeight();
        for (int i = 0; i < lines.size(); i++) {
            Location standLoc = loc.clone().add(0.5, height - (i * 0.25), 0.5);
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomName(ColorUtils.color(lines.get(i)));
            stand.setCustomNameVisible(true);
            stand.setSmall(true);
            stand.setMarker(true);
            stands.add(stand);
        }
        holograms.computeIfAbsent(crate.getId() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ(), k -> new ArrayList<>()).addAll(stands);
    }

    public void removeForCrate(String crateId) {
        holograms.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(crateId + "_")) {
                entry.getValue().forEach(ArmorStand::remove);
                return true;
            }
            return false;
        });
    }

    public void refreshCrate(Crate crate) {
        removeForCrate(crate.getId());
        if (crate.isHologramEnabled()) spawnForCrate(crate);
    }

    public void removeAll() {
        holograms.values().forEach(list -> list.forEach(ArmorStand::remove));
        holograms.clear();
    }
}
