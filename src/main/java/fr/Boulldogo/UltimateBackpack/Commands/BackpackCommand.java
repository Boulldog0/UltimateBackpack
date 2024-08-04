package fr.Boulldogo.UltimateBackpack.Commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.Boulldogo.UltimateBackpack.Main;
import fr.Boulldogo.UltimateBackpack.Utils.DataUtils;
import fr.Boulldogo.UltimateBackpack.Utils.PermissionUtils;
import fr.Boulldogo.UltimateBackpack.Utils.SkullCreator;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class BackpackCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final DataUtils dataUtils;

    public BackpackCommand(Main plugin) {
        this.plugin = plugin;
        this.dataUtils = new DataUtils(plugin);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        if(!(sender instanceof Player)) {
            sender.sendMessage("Only online players can use that command!");
            return true;
        }

        Player player =(Player) sender;

        if(args.length < 1 || args[0].equals("help")) {
            List<String> helpCommand = plugin.getConfig().getStringList("help-message");
            for(String msg : helpCommand) {
                player.sendMessage(prefix + translateString(msg));
            }
            return true;
        }

        String subCommand = args[0];

        if(!isArgsValid(subCommand)) {
            List<String> helpCommand = plugin.getConfig().getStringList("help-message");
            for(String msg : helpCommand) {
                player.sendMessage(prefix + translateString(msg));
            }
            return true;
        }

        if(subCommand.equals("open")) {
            PermissionUtils pUtils = new PermissionUtils();
            if(args.length < 2) {
                if(!player.hasPermission(pUtils.OPEN_BACKPACK)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-permission")));
                    return true;
                }

                if(!plugin.getConfig().getStringList("disable-backpack-worlds").isEmpty() && plugin.getConfig().getStringList("disable-backpack-worlds").contains(player.getWorld().getName())) {
                    if(!player.hasPermission(pUtils.BYPASS_WORLD_LIMITATION)) {
                        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-disable-in-world")));
                        return true;
                    }
                }

                if(plugin.isWorldguardEnable() && !plugin.getConfig().getStringList("disable-backpack-regions").isEmpty()) {
                	if(!player.hasPermission(pUtils.BYPASS_REGION_LIMITATION)) {
                    	for(String s : plugin.getConfig().getStringList("disable-backpack-regions")) {
                    		if(plugin.getPlayerRegions(player).contains(s)) {
                                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-disable-in-region")));
                                return true;
                    		}
                    	}
                	}
                }

                if(plugin.playerHasHitCooldown(player)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-hit-cooldown").replace("%s", String.valueOf(plugin.getPlayerHitCooldown(player)))));
                    return true;
                }

                if(plugin.isPlayerRestricted(player)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-restricted")));
                    return true;
                }

                plugin.addOpenBackpack(player);
                openBackpackGui(player);
            } else if(args.length == 2) {
                String playerName = args[1];

                if(playerName.equals(player.getName())) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant-open-your-backpack")));
                    return true;
                }

                if(!playerName.equals(player.getName()) && !player.hasPermission(pUtils.OPEN_BACKPACK_OTHER)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-permission")));
                    return true;
                }

                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
                if(targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.unknow-player")));
                    return true;
                }

                UUID playerUUID = targetPlayer.getUniqueId();
                
                if(plugin.isPlayerRestricted(player)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-already-inspected")));
                    return true;
                }

                if(plugin.isOpenBackpack(targetPlayer)) {
                    Player opener = Bukkit.getPlayer(playerUUID);
                    if(opener != null && opener.isOnline()) {
                        opener.closeInventory();
                        plugin.removeOpenBackpack(targetPlayer);
                        opener.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-closed-by-admin")));
                        player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-closed-for-player").replace("%p", opener.getName())));
                    }
                }
                
                plugin.addRestrictedPlayer(targetPlayer);
                plugin.addAdminPlayerOpenedBackpack(player, playerUUID);
                openOtherBackpackGui(playerUUID, player);
            }
            return true;
        } 
        if(subCommand.equals("upgrade")) {
            if(!plugin.getConfig().getString("upgrade.upgrade-system").equals("UPGRADE")) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.upgrade-system-disable")));
                return true;
            }

            PermissionUtils pUtils = new PermissionUtils();
            int maxSize = pUtils.getPlayerBackpackSize(player);

            UUID playerUUID = player.getUniqueId();

            File folder = new File(plugin.getDataFolder(), "players-datas");
            if(!folder.exists()) {
                folder.mkdir();
            }

            File file = new File(folder, playerUUID + ".yml");
            if(!file.exists()) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.error")));
                return true;
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            int rows = config.getInt("backpack-row", 1);

            if(maxSize <= rows) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-already-upgrade-to-max")));
                return true;
            }

            int price = plugin.getConfig().getInt("upgrade.price-per-upgrade");

            if(price > 0) {
                if(!plugin.isVaultEnabled()) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.vault-not-found")));
                    return true;
                }

                Economy economy = plugin.getEconomy();
                if(economy.getBalance(player) < price) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.not-enought-money")));
                    return true;
                }

                economy.withdrawPlayer(player, price);
            }

            config.set("backpack-row", rows + 1);
            try {
                config.save(file);
            } catch(IOException e) {
                e.printStackTrace();
            }

            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-upgraded")));
        } else if(subCommand.equals("giveitem")) {
            if(!player.hasPermission("backpack.give-item")) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-permission")));
                return true;
            }

            Material material = Material.matchMaterial(plugin.getConfig().getString("item.backpack-item"));
            
            if (material == null) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.invalid-material")));
                return true;
            }

            int data = plugin.getConfig().getInt("item.item-data");
            String customName = plugin.getConfig().getString("item.custom-name");
            List<String> customLore = plugin.getConfig().getStringList("item.custom-lore");

            ItemStack item = new ItemStack(material, 1, (short) data);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                if(customName != null && !customName.isEmpty()) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
                }

                if(customLore != null && !customLore.isEmpty()) {
                    for(int i = 0; i < customLore.size(); i++) {
                        customLore.set(i, ChatColor.translateAlternateColorCodes('&', customLore.get(i)));
                    }
                    meta.setLore(customLore);
                }

                item.setItemMeta(meta);
            }

            if(material == Material.PLAYER_HEAD) {
                String textureBase64 = plugin.getConfig().getString("item.head-texture-base64");
                if(textureBase64 != null && !textureBase64.isEmpty()) {
                    SkullCreator creator = new SkullCreator();
                    item = creator.getCustomHead(textureBase64);
                    ItemMeta skullMeta = item.getItemMeta();
                    if(skullMeta != null) {
                        if(customName != null && !customName.isEmpty()) {
                            skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
                        }
                        if(customLore != null && !customLore.isEmpty()) {
                            skullMeta.setLore(customLore);
                        }
                        item.setItemMeta(skullMeta);
                    }
                }
            }

            try {
                player.getInventory().addItem(item);
                player.sendMessage(prefix + " " + translateString(plugin.getConfig().getString("messages.item-given")));
            } catch (Exception e) {
                plugin.getLogger().severe("Error while giving item: " + e.getMessage() + " | THIS IS NOT A BUG ! DONT REPORT IT TO DEVLOPERS !");
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.item-give-error")));
            }
        }

        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if(args.length == 1) {
            if(args[0].isEmpty()) {
                completions.addAll(Arrays.asList("help", "open", "upgrade", "giveitem"));
            } else {
                for(String option : Arrays.asList("help", "open", "upgrade", "giveitem")) {
                    if(option.startsWith(args[0].toLowerCase())) {
                        completions.add(option);
                    }
                }
            }
        } else if(args.length == 2) {
            if("open".equalsIgnoreCase(args[0])) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }

    public void openBackpackGui(Player player) {
        UUID playerUUID = player.getUniqueId();
        plugin.addOpenBackpack(player);
        
        if(player.getOpenInventory() != null) {
        	player.closeInventory();
        }

        File folder = new File(plugin.getDataFolder(), "players-datas");
        if(!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(folder, playerUUID + ".yml");
        if(!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        PermissionUtils pUtils = new PermissionUtils();
        int rows = config.getInt("backpack-row", 1);
        int maxRows = pUtils.getPlayerBackpackSize(player);

        if(plugin.getConfig().getBoolean("remove-non-permission-rows") && rows > maxRows) {
            rows = maxRows;
            config.set("backpack-row", rows);
            try {
                config.save(file);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        if(plugin.getConfig().getString("upgrade.upgrade-system").equals("UPGRADE") && rows < maxRows) {
            rows = maxRows;
            config.set("backpack-row", rows);
            try {
                config.save(file);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        Inventory backpack = Bukkit.createInventory(null, rows * 9, translateString(plugin.getConfig().getString("backpack-name").replace("%p", player.getName())));

        for(int slot = 0; slot < rows * 9; slot++) {
            if(config.contains("items." + slot)) {
                ItemStack item = dataUtils.getStackOnSlot(player.getUniqueId(), slot);
                backpack.setItem(slot, item);
            }
        }

        player.openInventory(backpack);
    }

    public void openOtherBackpackGui(UUID playerUUID, Player opener) {
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerUUID);
        
        if(opener.getOpenInventory() != null) {
        	opener.closeInventory();
        }

        File folder = new File(plugin.getDataFolder(), "players-datas");
        if(!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(folder, playerUUID + ".yml");
        if(!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        int rows = config.getInt("backpack-row", 1);

        Inventory backpack = Bukkit.createInventory(null, rows * 9, translateString(plugin.getConfig().getString("backpack-other-name").replace("%p", targetPlayer.getName())));

        for(int slot = 0; slot < rows * 9; slot++) {
            if(config.contains("items." + slot)) {
                ItemStack item = dataUtils.getStackOnSlot(playerUUID, slot);
                backpack.setItem(slot, item);
            }
        }
        opener.openInventory(backpack);
    }

    public boolean isArgsValid(String s) {
        return s.equals("help")
                || s.equals("open")
                || s.equals("upgrade")
                || s.equals("giveitem");
    }

    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
