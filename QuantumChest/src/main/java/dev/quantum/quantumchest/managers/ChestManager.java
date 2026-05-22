package dev.quantum.quantumchest.managers;

import dev.quantum.quantumchest.QuantumChest;
import dev.quantum.quantumchest.QuantumChestData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChestManager {

    private final QuantumChest plugin;
    private final Map<Location, QuantumChestData> chests = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public ChestManager(QuantumChest plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "chests.yml");
        loadAll();
    }

    public boolean isQuantumChest(Location location) {
        return chests.containsKey(normalizeLocation(location));
    }

    public QuantumChestData getChest(Location location) {
        return chests.get(normalizeLocation(location));
    }

    public void placeChest(Location location, UUID ownerUUID, String ownerName) {
        Location normalized = normalizeLocation(location);
        QuantumChestData data = new QuantumChestData(normalized, ownerUUID, ownerName);
        chests.put(normalized, data);
        plugin.getHologramManager().createHologram(normalized);
        saveAll();
    }

    public void removeChest(Location location) {
        Location normalized = normalizeLocation(location);
        chests.remove(normalized);
        plugin.getHologramManager().removeHologram(normalized);
        saveAll();
    }

    public ItemStack createChestItem(int amount) {
        ItemStack item = new ItemStack(Material.CHEST, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(plugin.getConfig().getString("chest-item.name", "&6&lQuantum Chest")));
            List<String> lore = new ArrayList<>();
            List<String> configLore = plugin.getConfig().getStringList("chest-item.lore");
            for (String line : configLore) {
                lore.add(color(line));
            }
            meta.setLore(lore);
            // Custom NBT tag to identify as quantum chest
            meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "quantum_chest"),
                org.bukkit.persistence.PersistentDataType.BYTE,
                (byte) 1
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isQuantumChestItem(ItemStack item) {
        if (item == null || item.getType() != Material.CHEST) return false;
        if (!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey(plugin, "quantum_chest"),
            org.bukkit.persistence.PersistentDataType.BYTE
        );
    }

    public Map<Location, QuantumChestData> getAllChests() {
        return Collections.unmodifiableMap(chests);
    }

    public void saveAll() {
        dataConfig = new YamlConfiguration();
        int i = 0;
        for (Map.Entry<Location, QuantumChestData> entry : chests.entrySet()) {
            QuantumChestData data = entry.getValue();
            String path = "chests." + i;
            dataConfig.set(path + ".world", data.getLocation().getWorld().getName());
            dataConfig.set(path + ".x", data.getLocation().getBlockX());
            dataConfig.set(path + ".y", data.getLocation().getBlockY());
            dataConfig.set(path + ".z", data.getLocation().getBlockZ());
            dataConfig.set(path + ".owner", data.getOwnerUUID().toString());
            dataConfig.set(path + ".ownerName", data.getOwnerName());
            // Save items
            for (Map.Entry<Material, Long> itemEntry : data.getItems().entrySet()) {
                dataConfig.set(path + ".items." + itemEntry.getKey().name(), itemEntry.getValue());
            }
            i++;
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save chests.yml: " + e.getMessage());
        }
    }

    public void loadAll() {
        if (!dataFile.exists()) return;
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (!dataConfig.contains("chests")) return;

        for (String key : dataConfig.getConfigurationSection("chests").getKeys(false)) {
            String path = "chests." + key;
            String worldName = dataConfig.getString(path + ".world");
            int x = dataConfig.getInt(path + ".x");
            int y = dataConfig.getInt(path + ".y");
            int z = dataConfig.getInt(path + ".z");
            String ownerStr = dataConfig.getString(path + ".owner");
            String ownerName = dataConfig.getString(path + ".ownerName", "Unknown");

            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            if (world == null) continue;

            Location loc = new Location(world, x, y, z);
            UUID owner = UUID.fromString(ownerStr);
            QuantumChestData data = new QuantumChestData(loc, owner, ownerName);

            // Load items
            if (dataConfig.contains(path + ".items")) {
                for (String matName : dataConfig.getConfigurationSection(path + ".items").getKeys(false)) {
                    try {
                        Material mat = Material.valueOf(matName);
                        long amount = dataConfig.getLong(path + ".items." + matName);
                        data.getItems().put(mat, amount);
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            chests.put(loc, data);
            plugin.getHologramManager().createHologram(loc);
        }
    }

    private Location normalizeLocation(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private String color(String text) {
        return text.replace("&", "\u00a7");
    }
}
