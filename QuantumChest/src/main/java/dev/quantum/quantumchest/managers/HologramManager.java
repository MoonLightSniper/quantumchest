package dev.quantum.quantumchest.managers;

import dev.quantum.quantumchest.QuantumChest;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class HologramManager {

    private final QuantumChest plugin;
    private final Map<Location, String> holograms = new HashMap<>();
    private int counter = 0;

    public HologramManager(QuantumChest plugin) {
        this.plugin = plugin;
    }

    public void createHologram(Location location) {
        try {
            String name = "quantumchest_" + (counter++);
            double height = plugin.getConfig().getDouble("hologram.height", 1.5);
            String text = plugin.getConfig().getString("hologram.text", "&6&lInfinite Sell Chest");

            Location holoLoc = location.clone().add(0.5, height + 1, 0.5);
            Hologram holo = DHAPI.createHologram(name, holoLoc);
            DHAPI.addHologramLine(holo, color(text));

            holograms.put(normalizeLocation(location), name);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not create hologram: " + e.getMessage());
        }
    }

    public void removeHologram(Location location) {
        String name = holograms.remove(normalizeLocation(location));
        if (name != null) {
            try {
                DHAPI.removeHologram(name);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not remove hologram: " + e.getMessage());
            }
        }
    }

    public void removeAll() {
        for (String name : holograms.values()) {
            try {
                DHAPI.removeHologram(name);
            } catch (Exception ignored) {}
        }
        holograms.clear();
    }

    private Location normalizeLocation(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private String color(String text) {
        return text.replace("&", "\u00a7");
    }
}
