package me.cratecore.animations;

import me.cratecore.CrateCore;
import me.cratecore.models.Crate;
import me.cratecore.models.CrateReward;
import me.cratecore.utils.ColorUtils;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class CrateAnimation {
    private final CrateCore plugin;
    private final Player player;
    private final Crate crate;
    private final CrateReward reward;
    private final Location location;
    private final Runnable onComplete;
    private final List<ArmorStand> stands = new ArrayList<>();

    public CrateAnimation(CrateCore plugin, Player player, Crate crate, CrateReward reward, Location location, Runnable onComplete) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.reward = reward;
        this.location = location;
        this.onComplete = onComplete;
    }

    public void play() {
        switch (crate.getAnimationType()) {
            case "SPIN" -> playSpin();
            case "FIRE" -> playFire();
            case "COMPACT" -> playCompact();
            default -> playDefault();
        }
    }

    private void playSpin() {
        Location center = location.clone().add(0.5, 1.5, 0.5);
        List<ItemStack> rewardItems = new ArrayList<>(reward.getItems());
        if (rewardItems.isEmpty() && reward.getDisplayItem() != null) rewardItems.add(reward.getDisplayItem());

        // Spawn spinning items
        double radius = 1.2;
        int count = Math.min(8, Math.max(3, crate.getRewards().size()));
        List<CrateReward> display = new ArrayList<>();
        for (int i = 0; i < count; i++) display.add(crate.getRewards().get(i % crate.getRewards().size()));

        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            Location standLoc = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setCustomNameVisible(false);
            ItemStack displayItem = display.get(i).getDisplayItem();
            if (displayItem != null) stand.setHelmet(displayItem);
            stands.add(stand);
        }

        final int[] tick = {0};
        final double[] angle = {0};

        new BukkitRunnable() {
            @Override
            public void run() {
                tick[0]++;
                angle[0] += 0.1;

                // Rotate stands
                for (int i = 0; i < stands.size(); i++) {
                    double a = angle[0] + (2 * Math.PI / stands.size()) * i;
                    stands.get(i).teleport(center.clone().add(Math.cos(a) * radius, 0, Math.sin(a) * radius));
                }

                // Particles
                location.getWorld().spawnParticle(Particle.ENCHANT, center, 5, 0.5, 0.5, 0.5, 0.1);

                if (tick[0] >= 60) {
                    cleanup();
                    // Final particles
                    location.getWorld().spawnParticle(Particle.FIREWORK, center, 30, 0.5, 0.5, 0.5, 0.1);
                    location.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    onComplete.run();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void playFire() {
        Location center = location.clone().add(0.5, 0.5, 0.5);
        final int[] tick = {0};

        new BukkitRunnable() {
            @Override
            public void run() {
                tick[0]++;
                location.getWorld().spawnParticle(Particle.FLAME, center, 10, 0.3, 0.5, 0.3, 0.05);
                location.getWorld().spawnParticle(Particle.LAVA, center, 3, 0.2, 0.3, 0.2, 0);
                if (tick[0] % 10 == 0) location.getWorld().playSound(center, Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1f);

                if (tick[0] >= 50) {
                    location.getWorld().spawnParticle(Particle.EXPLOSION, center, 5, 0.5, 0.5, 0.5, 0);
                    location.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
                    onComplete.run();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void playCompact() {
        Location center = location.clone().add(0.5, 1.0, 0.5);
        final int[] tick = {0};

        new BukkitRunnable() {
            @Override
            public void run() {
                tick[0]++;
                double progress = (double) tick[0] / 40;
                location.getWorld().spawnParticle(Particle.PORTAL, center, 15, 0.3 * (1 - progress), 0.3, 0.3 * (1 - progress), 0.1);

                if (tick[0] >= 40) {
                    location.getWorld().spawnParticle(Particle.SPELL_WITCH, center, 20, 0.5, 0.5, 0.5, 0.1);
                    location.getWorld().playSound(center, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
                    onComplete.run();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void playDefault() {
        Location center = location.clone().add(0.5, 1.0, 0.5);
        final int[] tick = {0};

        new BukkitRunnable() {
            @Override
            public void run() {
                tick[0]++;
                location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, center, 5, 0.5, 0.5, 0.5, 0);

                if (tick[0] >= 30) {
                    location.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    onComplete.run();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void cleanup() {
        for (ArmorStand stand : stands) stand.remove();
        stands.clear();
    }
}
