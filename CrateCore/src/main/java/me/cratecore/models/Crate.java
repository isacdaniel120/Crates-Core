package me.cratecore.models;

import org.bukkit.Location;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

public class Crate {
    private String id;
    private String displayName;
    private Material blockMaterial;
    private List<CrateReward> rewards = new ArrayList<>();
    private CrateKey key;
    private List<Location> locations = new ArrayList<>();
    private boolean hologramEnabled;
    private List<String> hologramLines = new ArrayList<>();
    private double hologramHeight;
    private String animationType;
    private boolean broadcastEnabled;
    private String broadcastMessage;
    private boolean requireKey;
    private int cooldownSeconds;

    public Crate(String id) {
        this.id = id;
        this.displayName = "&6" + id;
        this.blockMaterial = Material.CHEST;
        this.key = new CrateKey(id);
        this.hologramEnabled = true;
        this.hologramHeight = 2.5;
        this.animationType = "SPIN";
        this.broadcastEnabled = true;
        this.requireKey = true;
        this.cooldownSeconds = 0;
        this.hologramLines.add("&6&l" + id);
        this.hologramLines.add("&eRight-click to open!");
    }

    public CrateReward rollReward() {
        List<CrateReward> eligible = rewards.stream().filter(CrateReward::canBeWon).toList();
        if (eligible.isEmpty()) return null;
        double total = eligible.stream().mapToDouble(CrateReward::getChance).sum();
        double roll = Math.random() * total;
        double cumulative = 0;
        for (CrateReward r : eligible) {
            cumulative += r.getChance();
            if (roll <= cumulative) return r;
        }
        return eligible.get(eligible.size() - 1);
    }

    public String getId() { return id; }
    public void setId(String i) { this.id = i; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String n) { this.displayName = n; }
    public Material getBlockMaterial() { return blockMaterial; }
    public void setBlockMaterial(Material m) { this.blockMaterial = m; }
    public List<CrateReward> getRewards() { return rewards; }
    public void setRewards(List<CrateReward> r) { this.rewards = r; }
    public void addReward(CrateReward r) { this.rewards.add(r); }
    public CrateKey getKey() { return key; }
    public void setKey(CrateKey k) { this.key = k; }
    public List<Location> getLocations() { return locations; }
    public void addLocation(Location l) { this.locations.add(l); }
    public boolean isHologramEnabled() { return hologramEnabled; }
    public void setHologramEnabled(boolean h) { this.hologramEnabled = h; }
    public List<String> getHologramLines() { return hologramLines; }
    public void setHologramLines(List<String> l) { this.hologramLines = l; }
    public double getHologramHeight() { return hologramHeight; }
    public void setHologramHeight(double h) { this.hologramHeight = h; }
    public String getAnimationType() { return animationType; }
    public void setAnimationType(String a) { this.animationType = a; }
    public boolean isBroadcastEnabled() { return broadcastEnabled; }
    public void setBroadcastEnabled(boolean b) { this.broadcastEnabled = b; }
    public String getBroadcastMessage() { return broadcastMessage; }
    public void setBroadcastMessage(String m) { this.broadcastMessage = m; }
    public boolean isRequireKey() { return requireKey; }
    public void setRequireKey(boolean r) { this.requireKey = r; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int c) { this.cooldownSeconds = c; }
}
