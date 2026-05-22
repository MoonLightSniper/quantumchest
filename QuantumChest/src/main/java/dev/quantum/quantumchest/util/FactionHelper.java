package dev.quantum.quantumchest.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class FactionHelper {

    private static boolean factionsEnabled = false;
    private static boolean factionsBridgeEnabled = false;

    public static void init(Plugin plugin) {
        factionsEnabled = plugin.getServer().getPluginManager().getPlugin("Factions") != null;
        factionsBridgeEnabled = plugin.getServer().getPluginManager().getPlugin("FactionsBridge") != null;
        plugin.getLogger().info("Factions: " + factionsEnabled + ", FactionsBridge: " + factionsBridgeEnabled);
    }

    public static boolean isSameFaction(Player player, UUID ownerUUID) {
        // If no factions plugin, allow anyone to sell
        if (!factionsEnabled && !factionsBridgeEnabled) return true;
        // If player is the owner, always allow
        if (player.getUniqueId().equals(ownerUUID)) return true;

        try {
            if (factionsBridgeEnabled) {
                return checkFactionsBridge(player, ownerUUID);
            } else if (factionsEnabled) {
                return checkSaberFactions(player, ownerUUID);
            }
        } catch (Exception e) {
            // If faction check fails, fall back to allowing
            return true;
        }
        return false;
    }

    private static boolean checkSaberFactions(Player player, UUID ownerUUID) {
        try {
            com.massivecraft.factions.FPlayer fPlayer = com.massivecraft.factions.FPlayers.getInstance().getById(player.getUniqueId().toString());
            com.massivecraft.factions.FPlayer fOwner = com.massivecraft.factions.FPlayers.getInstance().getById(ownerUUID.toString());

            if (fPlayer == null || fOwner == null) return false;
            if (fPlayer.getFaction() == null || fOwner.getFaction() == null) return false;

            String playerFactionId = fPlayer.getFaction().getId();
            String ownerFactionId = fOwner.getFaction().getId();

            if (playerFactionId == null || ownerFactionId == null) return false;
            if (playerFactionId.equals("0") || ownerFactionId.equals("0")) return false; // wilderness

            return playerFactionId.equals(ownerFactionId);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkFactionsBridge(Player player, UUID ownerUUID) {
        try {
            org.bukkit.OfflinePlayer owner = player.getServer().getOfflinePlayer(ownerUUID);
            // Get faction of player and owner via FactionsBridge API
            com.alessiodp.factionsBridge.api.FactionsBridgeAPI api = com.alessiodp.factionsBridge.api.FactionsBridgeProvider.get();
            if (api == null) return checkSaberFactions(player, ownerUUID);

            com.alessiodp.factionsBridge.api.objects.FBPlayer fbPlayer = api.getPlayer(player.getUniqueId());
            com.alessiodp.factionsBridge.api.objects.FBPlayer fbOwner = api.getPlayer(ownerUUID);

            if (fbPlayer == null || fbOwner == null) return false;
            if (!fbPlayer.isInFaction() || !fbOwner.isInFaction()) return false;

            return fbPlayer.getFactionId().equals(fbOwner.getFactionId());
        } catch (Exception e) {
            // FactionsBridge API failed, try direct SaberFactions
            return checkSaberFactions(player, ownerUUID);
        }
    }
}
