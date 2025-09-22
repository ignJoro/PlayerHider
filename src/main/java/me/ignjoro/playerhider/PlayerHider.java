package me.ignjoro.playerhider;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerHider extends JavaPlugin implements Listener, TabExecutor {

    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Set<UUID> whitelistedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

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
            } else {
                other.hidePlayer(this, player);
            }
        }
        player.setGlowing(true);
        player.sendMessage(Util.text("$YELLOW ⚠ $GRAY you are currently $RED hidden $YELLOW from other players."));
    }

    private void unhidePlayer(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.showPlayer(this, player);
        }
        player.setGlowing(false);
        player.sendMessage(Util.text("$GREEN ✅ $GRAY you are no longer $yellow hidden."));
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
    public void onChat(AsyncChatEvent event) {
        if (hiddenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Util.text("$RED ❌ $GRAY you cannot chat while hidden"));
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
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Util.text("$RED ❌ $GRAY you do not have permission to access this"));
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "hideplayer":
                if (args.length != 1) return false;
                Player toHide = Bukkit.getPlayer(args[0]);
                if (toHide != null) {
                    hiddenPlayers.add(toHide.getUniqueId());
                    hidePlayer(toHide);
                    sender.sendMessage(Util.text("$YELLOW 👁 $GRAY")+toHide.getName() + Util.text(" is now $RED hidden."));
                }
                return true;

            case "unhideplayer":
                if (args.length != 1) return false;
                Player toUnhide = Bukkit.getPlayer(args[0]);
                if (toUnhide != null) {
                    hiddenPlayers.remove(toUnhide.getUniqueId());
                    unhidePlayer(toUnhide);
                    sender.sendMessage(Util.text("$GREEN ✅ $GRAY")+toUnhide.getName() + Util.text(" is no longer $YELLOW hidden."));
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
                    sender.sendMessage(Util.text("$AQUA 👥 $GRAY")+toWhitelist.getName() + Util.text(" can now see $yellow hidden players"));
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
                    sender.sendMessage(Util.text("$RED 🚫 $GRAY")+toUnwhitelist.getName() + Util.text(" can no longer see $YELLOW hidden players"));
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
                sender.sendMessage(Util.text("$GOLD 🔄 $GRAY internalsystem $YELLOW reloaded."));
                return true;
        }
        return false;
    }
}
