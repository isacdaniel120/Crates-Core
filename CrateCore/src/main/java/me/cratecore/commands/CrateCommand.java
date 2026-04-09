package me.cratecore.commands;

import me.cratecore.CrateCore;
import me.cratecore.gui.CrateEditorGUI;
import me.cratecore.gui.CrateListGUI;
import me.cratecore.gui.PreviewGUI;
import me.cratecore.models.Crate;
import me.cratecore.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrateCommand implements CommandExecutor, TabCompleter {
    private final CrateCore plugin;

    public CrateCommand(CrateCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) { sendHelp(sender); return true; }
        switch (args[0].toLowerCase()) {
            case "editor", "edit" -> cmdEditor(sender, args);
            case "create"         -> cmdCreate(sender, args);
            case "delete"         -> cmdDelete(sender, args);
            case "give"           -> cmdGive(sender, args);
            case "givevirtual"    -> cmdGiveVirtual(sender, args);
            case "setlocation"    -> cmdSetLocation(sender);
            case "preview"        -> cmdPreview(sender, args);
            case "reload"         -> cmdReload(sender);
            case "list"           -> cmdList(sender);
            case "keys"           -> cmdKeys(sender, args);
            default               -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(ColorUtils.color("&8&m────────────────────────────────"));
        s.sendMessage(ColorUtils.color(" &6CrateCore &7v1.0 &8| &7Commands"));
        s.sendMessage(ColorUtils.color("&8&m────────────────────────────────"));
        s.sendMessage(ColorUtils.color(" &e/crates editor &8- &7Open GUI editor"));
        s.sendMessage(ColorUtils.color(" &e/crates create <id> &8- &7Create new crate"));
        s.sendMessage(ColorUtils.color(" &e/crates delete <id> &8- &7Delete crate"));
        s.sendMessage(ColorUtils.color(" &e/crates give <player> <crate> <amount> &8- &7Give physical key"));
        s.sendMessage(ColorUtils.color(" &e/crates givevirtual <player> <crate> <amount> &8- &7Give virtual key"));
        s.sendMessage(ColorUtils.color(" &e/crates setlocation <crate> &8- &7Set crate location"));
        s.sendMessage(ColorUtils.color(" &e/crates preview <crate> &8- &7Preview rewards"));
        s.sendMessage(ColorUtils.color(" &e/crates keys [player] &8- &7View virtual keys"));
        s.sendMessage(ColorUtils.color(" &e/crates list &8- &7List all crates"));
        s.sendMessage(ColorUtils.color(" &e/crates reload &8- &7Reload plugin"));
        s.sendMessage(ColorUtils.color("&8&m────────────────────────────────"));
    }

    private void cmdEditor(CommandSender s, String[] args) {
        Player p = requirePlayer(s); if (p == null) return;
        if (!p.hasPermission("cratecore.admin")) { noPerms(s); return; }
        if (args.length >= 2) {
            Crate crate = plugin.getCrateManager().getCrate(args[1]);
            if (crate == null) { s.sendMessage(ColorUtils.color("&cCrate not found!")); return; }
            new CrateEditorGUI(plugin, p, crate).open();
        } else {
            new CrateListGUI(plugin, p).open();
        }
    }

    private void cmdCreate(CommandSender s, String[] args) {
        if (!s.hasPermission("cratecore.admin")) { noPerms(s); return; }
        if (args.length < 2) { s.sendMessage(ColorUtils.color("&cUsage: /crates create <id>")); return; }
        String id = args[1];
        if (plugin.getCrateManager().getCrate(id) != null) {
            s.sendMessage(ColorUtils.color("&cCrate &f" + id + " &calready exists!"));
            return;
        }
        Crate crate = new Crate(id);
        plugin.getCrateManager().addCrate(crate);
        s.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aCrate &f" + id + " &acreated!"));
        if (s instanceof Player p) new CrateEditorGUI(plugin, p, crate).open();
    }

    private void cmdDelete(CommandSender s, String[] args) {
        if (!s.hasPermission("cratecore.admin")) { noPerms(s); return; }
        if (args.length < 2) { s.sendMessage(ColorUtils.color("&cUsage: /crates delete <id>")); return; }
        if (!plugin.getCrateManager().removeCrate(args[1])) {
            s.sendMessage(ColorUtils.color("&cCrate not found!"));
        } else {
            plugin.getHologramManager().removeForCrate(args[1]);
            s.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aCrate &f" + args[1] + " &adeleted."));
        }
    }

    private void cmdGive(CommandSender s, String[] args) {
        if (!s.hasPermission("cratecore.give")) { noPerms(s); return; }
        if (args.length < 4) { s.sendMessage(ColorUtils.color("&cUsage: /crates give <player> <crate> <amount>")); return; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { s.sendMessage(ColorUtils.color("&cPlayer not found!")); return; }
        Crate crate = plugin.getCrateManager().getCrate(args[2]);
        if (crate == null) { s.sendMessage(ColorUtils.color("&cCrate not found!")); return; }
        int amount;
        try { amount = Integer.parseInt(args[3]); } catch (Exception e) { s.sendMessage(ColorUtils.color("&cInvalid amount!")); return; }
        for (int i = 0; i < amount; i++) {
            target.getInventory().addItem(crate.getKey().buildItem()).forEach((slot, leftover) ->
                target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        }
        target.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aYou received &f" + amount + "x " + ColorUtils.color(crate.getKey().getDisplayName()) + "&a!"));
        s.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aGave &f" + amount + "x &akey to &f" + target.getName() + "&a."));
    }

    private void cmdGiveVirtual(CommandSender s, String[] args) {
        if (!s.hasPermission("cratecore.give")) { noPerms(s); return; }
        if (args.length < 4) { s.sendMessage(ColorUtils.color("&cUsage: /crates givevirtual <player> <crate> <amount>")); return; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { s.sendMessage(ColorUtils.color("&cPlayer not found!")); return; }
        Crate crate = plugin.getCrateManager().getCrate(args[2]);
        if (crate == null) { s.sendMessage(ColorUtils.color("&cCrate not found!")); return; }
        int amount;
        try { amount = Integer.parseInt(args[3]); } catch (Exception e) { s.sendMessage(ColorUtils.color("&cInvalid amount!")); return; }
        plugin.getVirtualKeyManager().addKeys(target, crate.getId(), amount);
        target.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aYou received &f" + amount + "x &avirtual key for &f" + ColorUtils.color(crate.getDisplayName()) + "&a!"));
        s.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aGave &f" + amount + "x &avirtual key to &f" + target.getName() + "&a."));
    }

    private void cmdSetLocation(CommandSender s) {
        Player p = requirePlayer(s); if (p == null) return;
        if (!p.hasPermission("cratecore.admin")) { noPerms(s); return; }
        p.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &eRight-click a block to set as crate location. Type the crate ID first:"));
        plugin.getChatInputManager().waitForInput(p, input -> {
            Crate crate = plugin.getCrateManager().getCrate(input);
            if (crate == null) { p.sendMessage(ColorUtils.color("&cCrate not found!")); return; }
            p.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &eNow right-click the block where the crate should be."));
            plugin.getLocationSetManager().waitForLocation(p, loc -> {
                loc.getBlock().setType(crate.getBlockMaterial());
                crate.addLocation(loc);
                plugin.getCrateManager().saveAll();
                plugin.getHologramManager().spawnForCrate(crate);
                p.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aCrate location set!"));
            });
        });
    }

    private void cmdPreview(CommandSender s, String[] args) {
        Player p = requirePlayer(s); if (p == null) return;
        if (args.length < 2) { s.sendMessage(ColorUtils.color("&cUsage: /crates preview <id>")); return; }
        Crate crate = plugin.getCrateManager().getCrate(args[1]);
        if (crate == null) { s.sendMessage(ColorUtils.color("&cCrate not found!")); return; }
        new PreviewGUI(plugin, p, crate).open();
    }

    private void cmdKeys(CommandSender s, String[] args) {
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) { s.sendMessage(ColorUtils.color("&cPlayer not found!")); return; }
        } else {
            target = requirePlayer(s);
            if (target == null) return;
        }
        s.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &7Virtual keys for &f" + target.getName() + "&7:"));
        for (Crate crate : plugin.getCrateManager().getAllCrates()) {
            int keys = plugin.getVirtualKeyManager().getKeys(target, crate.getId());
            if (keys > 0) s.sendMessage(ColorUtils.color("  &7- &f" + crate.getId() + "&7: &f" + keys));
        }
    }

    private void cmdList(CommandSender s) {
        if (!s.hasPermission("cratecore.admin")) { noPerms(s); return; }
        if (s instanceof Player p) {
            new CrateListGUI(plugin, p).open();
        } else {
            s.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &7Crates:"));
            for (Crate c : plugin.getCrateManager().getAllCrates()) {
                s.sendMessage(ColorUtils.color("  &7- &f" + c.getId() + " &7(" + c.getRewards().size() + " rewards, " + c.getLocations().size() + " locations)"));
            }
        }
    }

    private void cmdReload(CommandSender s) {
        if (!s.hasPermission("cratecore.reload")) { noPerms(s); return; }
        plugin.getHologramManager().removeAll();
        plugin.getCrateManager().load();
        plugin.getVirtualKeyManager().load();
        plugin.reloadConfig();
        for (Crate c : plugin.getCrateManager().getAllCrates()) plugin.getHologramManager().spawnForCrate(c);
        s.sendMessage(ColorUtils.color("&8[&6CrateCore&8] &aReloaded!"));
    }

    private Player requirePlayer(CommandSender s) {
        if (s instanceof Player p) return p;
        s.sendMessage(ColorUtils.color("&cOnly players can use this!"));
        return null;
    }

    private void noPerms(CommandSender s) {
        s.sendMessage(ColorUtils.color("&cYou don't have permission!"));
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        if (args.length == 1) return filter(List.of("editor","create","delete","give","givevirtual","setlocation","preview","keys","list","reload"), args[0]);
        if (args.length == 2) switch (args[0].toLowerCase()) {
            case "editor","delete","preview","setlocation" -> { return filterCrates(args[1]); }
            case "give","givevirtual" -> { return filterPlayers(args[1]); }
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("givevirtual")))
            return filterCrates(args[2]);
        return Collections.emptyList();
    }

    private List<String> filterCrates(String prefix) {
        List<String> list = new ArrayList<>();
        plugin.getCrateManager().getAllCrates().forEach(c -> list.add(c.getId()));
        return filter(list, prefix);
    }

    private List<String> filterPlayers(String prefix) {
        List<String> list = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(p -> list.add(p.getName()));
        return filter(list, prefix);
    }

    private List<String> filter(List<String> src, String prefix) {
        List<String> result = new ArrayList<>();
        src.stream().filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase())).forEach(result::add);
        return result;
    }
}
