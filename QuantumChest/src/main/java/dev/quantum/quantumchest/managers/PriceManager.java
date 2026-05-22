package dev.quantum.quantumchest.managers;

import dev.quantum.quantumchest.QuantumChest;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PriceManager {

    private final QuantumChest plugin;
    private final Map<Material, Double> prices = new HashMap<>();

    public PriceManager(QuantumChest plugin) {
        this.plugin = plugin;
        loadPrices();
    }

    public void loadPrices() {
        prices.clear();
        File file = new File(plugin.getDataFolder(), "prices.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("prices")) return;

        for (String key : config.getConfigurationSection("prices").getKeys(false)) {
            try {
                Material mat = Material.valueOf(key.toUpperCase());
                double price = config.getDouble("prices." + key);
                if (price > 0) {
                    prices.put(mat, price);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in prices.yml: " + key);
            }
        }
        plugin.getLogger().info("Loaded " + prices.size() + " sell prices.");
    }

    public double getPrice(Material material) {
        return prices.getOrDefault(material, 0.0);
    }

    public boolean hasPrize(Material material) {
        return prices.containsKey(material) && prices.get(material) > 0;
    }

    public Map<Material, Double> getAllPrices() {
        return prices;
    }
}
