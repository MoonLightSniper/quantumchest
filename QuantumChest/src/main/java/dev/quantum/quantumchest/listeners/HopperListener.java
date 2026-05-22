package dev.quantum.quantumchest.listeners;

import dev.quantum.quantumchest.QuantumChest;
import dev.quantum.quantumchest.QuantumChestData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HopperListener implements Listener {

    private final QuantumChest plugin;

    public HopperListener(QuantumChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHopperMove(InventoryMoveItemEvent event) {
        Inventory destination = event.getDestination();

        // Check if destination is a chest that is a quantum chest
        if (destination.getType() != InventoryType.CHEST) return;
        if (destination.getLocation() == null) return;

        Location loc = destination.getLocation();
        if (!plugin.getChestManager().isQuantumChest(loc)) return;

        // It's a quantum chest - absorb the item and cancel normal hopper transfer
        event.setCancelled(true);

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        QuantumChestData chest = plugin.getChestManager().getChest(loc);
        if (chest == null) return;

        // Add item to quantum chest storage
        chest.addItem(item.clone());

        // Remove item from source inventory
        ItemStack source = event.getItem();
        Inventory sourceInv = event.getSource();

        for (int i = 0; i < sourceInv.getSize(); i++) {
            ItemStack slotItem = sourceInv.getItem(i);
            if (slotItem != null && slotItem.isSimilar(source)) {
                if (slotItem.getAmount() > source.getAmount()) {
                    slotItem.setAmount(slotItem.getAmount() - source.getAmount());
                    sourceInv.setItem(i, slotItem);
                } else {
                    sourceInv.setItem(i, null);
                }
                break;
            }
        }

        plugin.getChestManager().saveAll();
    }
}
