package dev.quantum.quantumchest.gui;

import dev.quantum.quantumchest.QuantumChest;
import dev.quantum.quantumchest.QuantumChestData;
import dev.quantum.quantumchest.util.FactionHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.*;

public class ChestGUI {

    private final QuantumChest plugin;
    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.00");
    private static final String GUI_TITLE = "\u00a76\u00a7lQuantum Chest";

    public ChestGUI(QuantumChest plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, QuantumChestData chest) {
        // Build a 6-row inventory (54 slots)
        // Slots 0-44: items display
        // Slots 45-53: bottom bar (sell button, info)
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);

        // Fill bottom bar with glass
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, glass);
        }

        // Add item display slots (0-44)
        List<Map.Entry<Material, Long>> sortedItems = new ArrayList<>(chest.getItems().entrySet());
        sortedItems.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        int slot = 0;
        for (Map.Entry<Material, Long> entry : sortedItems) {
            if (slot >= 45) break;
            if (entry.getValue() <= 0) continue;

            Material mat = entry.getKey();
            long amount = entry.getValue();
            double price = plugin.getPriceManager().getPrice(mat);
            double totalValue = price * amount;

            ItemStack display = new ItemStack(mat, 1);
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color("&f" + formatMaterialName(mat)));
                List<String> lore = new ArrayList<>();
                lore.add(color("&7Amount: &f" + FORMAT.format(amount)));
                if (price > 0) {
                    lore.add(color("&7Price per item: &6$" + FORMAT.format(price)));
                    lore.add(color("&7Total value: &6$" + FORMAT.format(totalValue)));
                } else {
                    lore.add(color("&cNo sell value"));
                }
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inv.setItem(slot++, display);
        }

        // Sell button at slot 49 (middle of bottom bar)
        boolean canSell = FactionHelper.isSameFaction(player, chest.getOwnerUUID());
        double totalWorth = calculateTotalWorth(chest);

        if (canSell) {
            ItemStack sellBtn = createItemWithLore(
                Material.LIME_STAINED_GLASS_PANE,
                color("&a&lSELL ALL"),
                Arrays.asList(
                    color("&7Total value: &6$" + FORMAT.format(totalWorth)),
                    color(""),
                    color("&eClick to sell everything!")
                )
            );
            inv.setItem(49, sellBtn);
        } else {
            ItemStack noSell = createItemWithLore(
                Material.RED_STAINED_GLASS_PANE,
                color("&c&lCANNOT SELL"),
                Arrays.asList(
                    color("&7You must be in the same"),
                    color("&7faction as the chest owner.")
                )
            );
            inv.setItem(49, noSell);
        }

        // Info button at slot 45
        ItemStack info = createItemWithLore(
            Material.CHEST,
            color("&6&lQuantum Chest"),
            Arrays.asList(
                color("&7Owner: &f" + chest.getOwnerName()),
                color("&7Total items: &f" + FORMAT.format(chest.getTotalItems())),
                color("&7Total value: &6$" + FORMAT.format(totalWorth))
            )
        );
        inv.setItem(45, info);

        player.openInventory(inv);
    }

    public double calculateTotalWorth(QuantumChestData chest) {
        double total = 0;
        for (Map.Entry<Material, Long> entry : chest.getItems().entrySet()) {
            double price = plugin.getPriceManager().getPrice(entry.getKey());
            total += price * entry.getValue();
        }
        return total;
    }

    public boolean isChestGUI(Inventory inv) {
        return inv.getSize() == 54 && inv.getTitle() != null && inv.getTitle().equals(GUI_TITLE);
    }

    public boolean isSellButton(int slot) {
        return slot == 49;
    }

    private double calculateItemWorth(Material mat, long amount) {
        return plugin.getPriceManager().getPrice(mat) * amount;
    }

    private String formatMaterialName(Material mat) {
        String name = mat.name().replace("_", " ");
        StringBuilder formatted = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
            }
        }
        return formatted.toString().trim();
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItemWithLore(Material material, String name, List<String> lore) {
        ItemStack item = createItem(material, name);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String color(String text) {
        return text.replace("&", "\u00a7");
    }
}
