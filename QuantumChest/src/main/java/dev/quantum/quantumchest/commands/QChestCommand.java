package dev.quantum.quantumchest.commands;

import dev.quantum.quantumchest.QuantumChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class QChestCommand implements CommandExecutor {

    private final QuantumChest plugin;

    public QChestCommand(QuantumChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("quantumchest.admin")) {
            sender.sendMessage(color(getMsg("prefix") + getMsg("no-permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(color("&6&lQuantumChest &7Commands:"));
            sender.sendMessage(color("&f/qchest give <player> [amount] &7- Give a QuantumChest"));
            sender.sendMessage(color("&f/qchest reload &7- Reload config and prices"));
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 2) {
                sender.sendMessage(color("&cUsage: /qchest give <player> [amount]"));
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(color(getMsg("prefix") + "&cPlayer not found!"));
                return true;
            }

            int amount = 1;
            if (args.length >= 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount < 1) amount = 1;
                    if (amount > 64) amount = 64;
                } catch (NumberFormatException e) {
                    sender.sendMessage(color("&cInvalid amount!"));
                    return true;
                }
            }

            ItemStack chestItem = plugin.getChestManager().createChestItem(amount);
            target.getInventory().addItem(chestItem);

            String giveMsg = getMsg("given-chest").replace("%amount%", String.valueOf(amount)).replace("%player%", target.getName());
            String receiveMsg = getMsg("received-chest").replace("%amount%", String.valueOf(amount));

            sender.sendMessage(color(getMsg("prefix") + giveMsg));
            target.sendMessage(color(getMsg("prefix") + receiveMsg));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getPriceManager().loadPrices();
            sender.sendMessage(color(getMsg("prefix") + "&aConfig and prices reloaded!"));
            return true;
        }

        sender.sendMessage(color("&cUnknown subcommand. Use /qchest for help."));
        return true;
    }

    private String getMsg(String key) {
        return plugin.getConfig().getString("messages." + key, "");
    }

    private String color(String text) {
        return text.replace("&", "\u00a7");
    }
}
