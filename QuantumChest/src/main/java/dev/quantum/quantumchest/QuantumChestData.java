package dev.quantum.quantumchest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuantumChestData {

    private final Location location;
    private final UUID ownerUUID;
    private final String ownerName;
    private final Map<Material, Long> items;

    public QuantumChestData(Location location, UUID ownerUUID, String ownerName) {
        this.location = location;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.items = new HashMap<>();
    }

    public void addItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        items.merge(item.getType(), (long) item.getAmount(), Long::sum);
    }

    public void addItems(ItemStack[] items) {
        if (items == null) return;
        for (ItemStack item : items) {
            addItem(item);
        }
    }

    public Map<Material, Long> getItems() { return items; }
    public Location getLocation() { return location; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }

    public long getItemCount(Material material) {
        return items.getOrDefault(material, 0L);
    }

    public void clearItems() { items.clear(); }

    public boolean isEmpty() { return items.isEmpty() || items.values().stream().allMatch(v -> v <= 0); }

    public long getTotalItems() {
        return items.values().stream().mapToLong(Long::longValue).sum();
    }
}
