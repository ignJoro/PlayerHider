package me.ignjoro.playerhider;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PlayerHider extends JavaPlugin implements Listener {

    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Set<UUID> whitelistedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        Objects.requireNonNull(getCommand("hideplayer")).setExecutor(this::onCommand);
        Objects.requireNonNull(getCommand("unhideplayer")).setExecutor(this::onCommand);
        Objects.requireNonNull(getCommand("whitelistplayer")).setExecutor(this::onCommand);
        Objects.requireNonNull(getCommand("unwhitelistplayer")).setExecutor(this::onCommand);
        Objects.requireNonNull(getCommand("internalreload")).setExecutor(this::onCommand);

        if (getConfig().isList("hiddenPlayers")) {
            for (String uuid : getConfig().getStringList("hiddenPlayers"))
                hiddenPlayers.add(UUID.fromString(uuid));
        }
        if (getConfig().isList("whitelistedPlayers")) {
            for (String uuid : getConfig().getStringList("whitelistedPlayers"))
                whitelistedPlayers.add(UUID.fromString(uuid));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hiddenPlayers.contains(player.getUniqueId())) hidePlayer(player);
        }
    }

    @Override
    public void onDisable() {
        savePlayers();
    }

    private void savePlayers() {
        getConfig().set("hiddenPlayers", hiddenPlayers.stream().map(UUID::toString).toList());
        getConfig().set("whitelistedPlayers", whitelistedPlayers.stream().map(UUID::toString).toList());
        saveConfig();
    }

    private void hidePlayer(Player player) {
        player.setSilent(true);

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;

            if (whitelistedPlayers.contains(other.getUniqueId())) {
                other.showPlayer(this, player);
            } else {
                other.hidePlayer(this, player);
            }
        }

        player.setGlowing(true); // glow visible to self + whitelisted players
        hiddenPlayers.add(player.getUniqueId());
    }

    private void unhidePlayer(Player player) {
        player.setSilent(false);
        player.setGlowing(false);

        for (Player other : Bukkit.getOnlinePlayers()) {
            other.showPlayer(this, player);
        }

        hiddenPlayers.remove(player.getUniqueId());
    }

    private void updateVisibilityForWhitelisted(Player target) {
        for (UUID hiddenUUID : hiddenPlayers) {
            Player hidden = Bukkit.getPlayer(hiddenUUID);
            if (hidden != null) {
                if (whitelistedPlayers.contains(target.getUniqueId())) {
                    target.showPlayer(this, hidden);
                    hidden.setGlowing(true);
                } else {
                    target.hidePlayer(this, hidden);
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (hiddenPlayers.contains(player.getUniqueId())) {
            hidePlayer(player);
            player.sendMessage("§cYou are currently hidden.");
        }
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (hiddenPlayers.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (hiddenPlayers.contains(event.getEntity().getUniqueId())) event.setDeathMessage(null);
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        if (hiddenPlayers.contains(event.getPlayer().getUniqueId())) {
            if (event.getAdvancement().getDisplay() != null) {
                event.getPlayer().sendTitle("", "", 0, 0, 0); // hides toast
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.isOp()) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("hideplayer")) {
            if (args.length != 1) { sender.sendMessage("§cUsage: /hideplayer <player>"); return true; }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target != null) { hidePlayer(target); sender.sendMessage("§a" + target.getName() + " is now hidden."); }
            else sender.sendMessage("§cPlayer not found.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("unhideplayer")) {
            if (args.length != 1) { sender.sendMessage("§cUsage: /unhideplayer <player>"); return true; }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target != null) { unhidePlayer(target); sender.sendMessage("§a" + target.getName() + " is now visible."); }
            else sender.sendMessage("§cPlayer not found.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("whitelistplayer")) {
            if (args.length != 1) { sender.sendMessage("§cUsage: /whitelistplayer <player>"); return true; }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target != null) {
                whitelistedPlayers.add(target.getUniqueId());
                sender.sendMessage("§a" + target.getName() + " is now whitelisted.");
                updateVisibilityForWhitelisted(target);
            } else sender.sendMessage("§cPlayer not found.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("unwhitelistplayer")) {
            if (args.length != 1) { sender.sendMessage("§cUsage: /unwhitelistplayer <player>"); return true; }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target != null) {
                whitelistedPlayers.remove(target.getUniqueId());
                sender.sendMessage("§a" + target.getName() + " is no longer whitelisted.");
                updateVisibilityForWhitelisted(target);
            } else sender.sendMessage("§cPlayer not found.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("internalreload")) {
            reloadConfig();
            savePlayers();
            for (Player player : Bukkit.getOnlinePlayers()) if (hiddenPlayers.contains(player.getUniqueId())) hidePlayer(player);
            sender.sendMessage("§aInternalSystem reloaded.");
            return true;
        }

        return false;
    }
}
