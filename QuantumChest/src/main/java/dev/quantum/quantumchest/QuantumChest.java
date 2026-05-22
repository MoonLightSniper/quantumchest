package dev.quantum.quantumchest;

import dev.quantum.quantumchest.commands.QChestCommand;
import dev.quantum.quantumchest.listeners.ChestListener;
import dev.quantum.quantumchest.listeners.HopperListener;
import dev.quantum.quantumchest.managers.ChestManager;
import dev.quantum.quantumchest.managers.HologramManager;
import dev.quantum.quantumchest.managers.PriceManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class QuantumChest extends JavaPlugin {

    private static QuantumChest instance;
    private Economy economy;
    private ChestManager chestManager;
    private HologramManager hologramManager;
    private PriceManager priceManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("prices.yml", false);

        if (!setupEconomy()) {
            getLogger().severe("Vault not found or no economy plugin! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        priceManager = new PriceManager(this);
        hologramManager = new HologramManager(this);
        chestManager = new ChestManager(this);

        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
        getServer().getPluginManager().registerEvents(new HopperListener(this), this);

        getCommand("qchest").setExecutor(new QChestCommand(this));

        getLogger().info("QuantumChest enabled!");
    }

    @Override
    public void onDisable() {
        if (chestManager != null) {
            chestManager.saveAll();
        }
        if (hologramManager != null) {
            hologramManager.removeAll();
        }
        getLogger().info("QuantumChest disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public static QuantumChest getInstance() { return instance; }
    public Economy getEconomy() { return economy; }
    public ChestManager getChestManager() { return chestManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public PriceManager getPriceManager() { return priceManager; }
}
