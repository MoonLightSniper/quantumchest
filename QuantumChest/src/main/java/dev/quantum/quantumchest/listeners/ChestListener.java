package dev.quantum.quantumchest.listeners;

import dev.quantum.quantumchest.QuantumChest;
import dev.quantum.quantumchest.QuantumChestData;
import dev.quantum.quantumchest.gui.ChestGUI;
import dev.quantum.quantumchest.util.FactionHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestListener implements Listener {

    private final QuantumChest plugin;
    private final ChestGUI gui;
    private final DecimalFormat FORMAT = new DecimalFormat("#,##0.00");
    private final Map<UUID, Location> openChests = new HashMap<>();

    public ChestListener(QuantumChest plugin) {
        this.plugin = plugin;
        this.gui = new ChestGUI(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (!plugin.getChestManager().isQuantumChestItem(item)) return;

        Block block = event.getBlockPlaced();
        Location loc = block.getLocation();

        plugin.getChestManager().placeChest(loc, player.getUniqueId(), player.getName());
        player.sendMessage(color(getMsg("prefix") + getMsg("chest-placed")));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (!plugin.getChestManager().isQuantumChest(loc)) return;

        event.setDropItems(false);
        plugin.getChestManager().removeChest(loc);

        // Drop the quantum chest item
        ItemStack chestItem = plugin.getChestManager().createChestItem(1);
        block.getWorld().dropItemNaturally(loc, chestItem);

        event.getPlayer().sendMessage(color(getMsg("prefix") + getMsg("chest-broken")));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) return;

        Location loc = block.getLocation();
        if (!plugin.getChestManager().isQuantumChest(loc)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        QuantumChestData chest = plugin.getChestManager().getChest(loc);
        if (chest == null) return;

        openChests.put(player.getUniqueId(), loc);
        gui.open(player, chest);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!gui.isChestGUI(event.getInventory())) return;
        event.setCancelled(true);

        if (!gui.isSellButton(event.getSlot())) return;

        Location chestLoc = openChests.get(player.getUniqueId());
        if (chestLoc == null) return;

        QuantumChestData chest = plugin.getChestManager().getChest(chestLoc);
        if (chest == null) return;

        // Faction check
        if (!FactionHelper.isSameFaction(player, chest.getOwnerUUID())) {
            player.sendMessage(color(getMsg("prefix") + getMsg("not-faction-member")));
            return;
        }

        if (chest.isEmpty()) {
            player.sendMessage(color(getMsg("prefix") + getMsg("nothing-to-sell")));
            return;
        }

        // Calculate and pay
        double total = 0;
        boolean anyValue = false;
        for (Map.Entry<Material, Long> entry : chest.getItems().entrySet()) {
            double price = plugin.getPriceManager().getPrice(entry.getKey());
            if (price > 0) {
                total += price * entry.getValue();
                anyValue = true;
            }
        }

        if (!anyValue) {
            player.sendMessage(color(getMsg("prefix") + getMsg("nothing-to-sell")));
            return;
        }

        chest.clearItems();
        plugin.getEconomy().depositPlayer(player, total);
        plugin.getChestManager().saveAll();

        String msg = getMsg("sold-all").replace("%amount%", FORMAT.format(total));
        player.sendMessage(color(getMsg("prefix") + msg));

        // Refresh GUI
        player.closeInventory();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> gui.open(player, chest), 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (gui.isChestGUI(event.getInventory())) {
            openChests.remove(player.getUniqueId());
        }
    }

    private String getMsg(String key) {
        return plugin.getConfig().getString("messages." + key, "");
    }

    private String color(String text) {
        return text.replace("&", "\u00a7");
    }
}
