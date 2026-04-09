package me.cratecore.models;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class CrateReward {
    private String id;
    private String displayName;
    private double chance;
    private ItemStack displayItem;
    private List<ItemStack> items = new ArrayList<>();
    private List<String> commands = new ArrayList<>();
    private boolean broadcast;
    private String broadcastMessage;
    private int maxWins;
    private int currentWins;

    public CrateReward(String id) {
        this.id = id;
        this.chance = 10.0;
        this.broadcast = false;
        this.maxWins = -1;
        this.currentWins = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName != null ? displayName : id; }
    public void setDisplayName(String n) { this.displayName = n; }
    public double getChance() { return chance; }
    public void setChance(double c) { this.chance = c; }
    public ItemStack getDisplayItem() { return displayItem; }
    public void setDisplayItem(ItemStack i) { this.displayItem = i; }
    public List<ItemStack> getItems() { return items; }
    public void setItems(List<ItemStack> i) { this.items = i; }
    public void addItem(ItemStack i) { this.items.add(i); }
    public List<String> getCommands() { return commands; }
    public void setCommands(List<String> c) { this.commands = c; }
    public void addCommand(String c) { this.commands.add(c); }
    public boolean isBroadcast() { return broadcast; }
    public void setBroadcast(boolean b) { this.broadcast = b; }
    public String getBroadcastMessage() { return broadcastMessage; }
    public void setBroadcastMessage(String m) { this.broadcastMessage = m; }
    public int getMaxWins() { return maxWins; }
    public void setMaxWins(int m) { this.maxWins = m; }
    public int getCurrentWins() { return currentWins; }
    public void incrementWins() { this.currentWins++; }
    public boolean canBeWon() { return maxWins == -1 || currentWins < maxWins; }
}
