package me.ignjoro.playerhider;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;

import java.util.*;

public class PlayerHider extends JavaPlugin implements Listener, TabExecutor {

    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Set<UUID> whitelistedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        Objects.requireNonNull(getCommand("hideplayer")).setExecutor(this);
        Objects.requireNonNull(getCommand("unhideplayer")).setExecutor(this);
        Objects.requireNonNull(getCommand("whitelistplayer")).setExecutor(this);
        Objects.requireNonNull(getCommand("unwhitelistplayer")).setExecutor(this);
        Objects.requireNonNull(getCommand("internalreload")).setExecutor(this);

        List<String> hidden = getConfig().getStringList("hiddenPlayers");
        for (String uuid : hidden) hiddenPlayers.add(UUID.fromString(uuid));
        List<String> whitelisted = getConfig().getStringList("whitelistedPlayers");
        for (String uuid : whitelisted) whitelistedPlayers.add(UUID.fromString(uuid));

        for (UUID uuid : hiddenPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) hidePlayer(p);
        }
    }

    @Override
    public void onDisable() {
        getConfig().set("hiddenPlayers", hiddenPlayers.stream().map(UUID::toString).toList());
        getConfig().set("whitelistedPlayers", whitelistedPlayers.stream().map(UUID::toString).toList());
        saveConfig();
    }

    private void hidePlayer(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            if (whitelistedPlayers.contains(other.getUniqueId())) {
                other.showPlayer(this, player);
                player.setGlowing(true);
            } else {
                other.hidePlayer(this, player);
            }
        }
        player.sendMessage(ChatColor.GRAY + "You are currently hidden.");
    }

    private void unhidePlayer(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.showPlayer(this, player);
        }
        player.setGlowing(false);
        player.sendMessage(ChatColor.GRAY + "You are no longer hidden.");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (hiddenPlayers.contains(player.getUniqueId())) {
            hidePlayer(player);
            event.joinMessage(null);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (hiddenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.quitMessage(null);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (hiddenPlayers.contains(event.getEntity().getUniqueId())) {
            event.deathMessage(null);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (hiddenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot chat while hidden.");
        }
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (hiddenPlayers.contains(event.getPlayer().getUniqueId())) {
            if (event.message() != null) {
                event.message(null);
            }
        }
    }

    @EventHandler
    public void onPing(PaperServerListPingEvent event) {
        int visibleCount = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!hiddenPlayers.contains(p.getUniqueId())) {
                visibleCount++;
            }
        }
        event.setNumPlayers(visibleCount);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "hideplayer":
                if (args.length != 1) return false;
                Player toHide = Bukkit.getPlayer(args[0]);
                if (toHide != null) {
                    hiddenPlayers.add(toHide.getUniqueId());
                    hidePlayer(toHide);
                    sender.sendMessage(ChatColor.GREEN + toHide.getName() + " is now hidden.");
                }
                return true;

            case "unhideplayer":
                if (args.length != 1) return false;
                Player toUnhide = Bukkit.getPlayer(args[0]);
                if (toUnhide != null) {
                    hiddenPlayers.remove(toUnhide.getUniqueId());
                    unhidePlayer(toUnhide);
                    sender.sendMessage(ChatColor.GREEN + toUnhide.getName() + " is no longer hidden.");
                }
                return true;

            case "whitelistplayer":
                if (args.length != 1) return false;
                Player toWhitelist = Bukkit.getPlayer(args[0]);
                if (toWhitelist != null) {
                    whitelistedPlayers.add(toWhitelist.getUniqueId());
                    for (UUID uuid : hiddenPlayers) {
                        Player hidden = Bukkit.getPlayer(uuid);
                        if (hidden != null) {
                            toWhitelist.showPlayer(this, hidden);
                            hidden.setGlowing(true);
                        }
                    }
                    sender.sendMessage(ChatColor.GREEN + toWhitelist.getName() + " can now see hidden players.");
                }
                return true;

            case "unwhitelistplayer":
                if (args.length != 1) return false;
                Player toUnwhitelist = Bukkit.getPlayer(args[0]);
                if (toUnwhitelist != null) {
                    whitelistedPlayers.remove(toUnwhitelist.getUniqueId());
                    for (UUID uuid : hiddenPlayers) {
                        Player hidden = Bukkit.getPlayer(uuid);
                        if (hidden != null) {
                            toUnwhitelist.hidePlayer(this, hidden);
                        }
                    }
                    sender.sendMessage(ChatColor.GREEN + toUnwhitelist.getName() + " can no longer see hidden players.");
                }
                return true;

            case "internalreload":
                reloadConfig();
                hiddenPlayers.clear();
                whitelistedPlayers.clear();
                for (String uuid : getConfig().getStringList("hiddenPlayers")) {
                    hiddenPlayers.add(UUID.fromString(uuid));
                }
                for (String uuid : getConfig().getStringList("whitelistedPlayers")) {
                    whitelistedPlayers.add(UUID.fromString(uuid));
                }
                for (UUID uuid : hiddenPlayers) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) hidePlayer(p);
                }
                sender.sendMessage(ChatColor.YELLOW + "InternalSystem reloaded.");
                return true;
        }
        return false;
    }
}
