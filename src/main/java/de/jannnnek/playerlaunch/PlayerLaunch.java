package de.jannnnek.playerlaunch;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class PlayerLaunch extends JavaPlugin implements Listener {

    private final Material[] launchBlockType = new Material[]{Material.DIAMOND_BLOCK, Material.GOLD_BLOCK};     // TODO: Set your blocks
    private final double launchHeight = 1.0;           // TODO: Set your height
    private final double launchDistance = 2.0;          // TODO: Set your distance
    private final int cooldownSeconds = 20;             // TODO: Set your cooldown seconds
    private final long messageCooldownSeconds = 5;      // TODO: Set your message cooldown seconds

    private final Map<Player, Long> launchCooldowns = new HashMap<>();
    private final Map<Player, Long> messageCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        displayParticlesAroundBlocks(player.getWorld(), player.getLocation(), 15, Effect.HAPPY_VILLAGER, 3);
        Block blockBeneath = player.getLocation().subtract(0, 1, 0).getBlock();
        if (!launchCooldowns.containsKey(player) || System.currentTimeMillis() - launchCooldowns.get(player) >= cooldownSeconds * 1000) {
            for (Material material : launchBlockType) {
                if (blockBeneath.getType() == material) {
                    Location location = blockBeneath.getLocation();
                    float yaw = player.getLocation().getYaw();
                    double launchX = -Math.sin(Math.toRadians(yaw));
                    double launchZ = Math.cos(Math.toRadians(yaw));
                    Vector launchVector = new Vector(launchX * launchDistance, launchHeight, launchZ * launchDistance);
                    player.setVelocity(launchVector);
                    World world = location.getWorld();
                    player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 10.0f, 1.0f);
                    world.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 0.3f, 1.0f);
                    launchCooldowns.put(player, System.currentTimeMillis());
                    messageCooldowns.put(player, System.currentTimeMillis());
                    break;
                }
            }
        } else {
            for (Material material : launchBlockType) {
                if (blockBeneath.getType() == material) {
                    if (!messageCooldowns.containsKey(player) || System.currentTimeMillis() - messageCooldowns.get(player) >= messageCooldownSeconds * 1000) {
                        player.sendMessage(ChatColor.RED + "You need to wait " + cooldownSeconds + " seconds before launching again.");
                        messageCooldowns.put(player, System.currentTimeMillis());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!messageCooldowns.containsKey(player)) {
            messageCooldowns.put(player, System.currentTimeMillis()-messageCooldownSeconds);
        }
        if (!launchCooldowns.containsKey(player)) {
            launchCooldowns.put(player, System.currentTimeMillis()-cooldownSeconds);
        }
    }

    private void displayParticlesAroundBlocks(World world, Location centerLocation, int radius, Effect effect, int amount) {
        int minX = centerLocation.getBlockX() - radius;
        int minY = centerLocation.getBlockY() - radius;
        int minZ = centerLocation.getBlockZ() - radius;
        int maxX = centerLocation.getBlockX() + radius;
        int maxY = centerLocation.getBlockY() + radius;
        int maxZ = centerLocation.getBlockZ() + radius;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (isLaunchBlockType(block.getType())) {
                        Location blockLocation = block.getLocation().add(0.55, 1.2, 0.55);
                        world.playEffect(blockLocation, effect, amount);
                    }
                }
            }
        }
    }

    private boolean isLaunchBlockType(Material material) {
        for (Material launchMaterial : launchBlockType) {
            if (launchMaterial == material) {
                return true;
            }
        }
        return false;
    }
}
