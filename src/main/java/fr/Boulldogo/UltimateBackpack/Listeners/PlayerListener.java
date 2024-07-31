package fr.Boulldogo.UltimateBackpack.Listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.Boulldogo.UltimateBackpack.Main;
import fr.Boulldogo.UltimateBackpack.Commands.BackpackCommand;
import fr.Boulldogo.UltimateBackpack.Utils.DataUtils;
import fr.Boulldogo.UltimateBackpack.Utils.PermissionUtils;
import fr.Boulldogo.UltimateBackpack.Utils.SkullCreator;
import fr.Boulldogo.UltimateBackpack.Utils.WorldGuardUtils;
import net.md_5.bungee.api.ChatColor;

public class PlayerListener implements Listener {

    private final Main plugin;
    private final DataUtils dataUtils;
    private final BackpackCommand backpackCommand;
    private final WorldGuardUtils worldGuardUtils;
    
    public PlayerListener(Main plugin) {
        this.plugin = plugin;
        this.dataUtils = new DataUtils(plugin);
        this.backpackCommand = new BackpackCommand(plugin);
        this.worldGuardUtils = new WorldGuardUtils();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID playerUUID = e.getPlayer().getUniqueId();
        
        File folder = new File(plugin.getDataFolder(), "players-datas");
        if(!folder.exists()) {
            folder.mkdir();
        }
        
        File file = new File(folder, playerUUID + ".yml");
        if(!file.exists()) {
            try {
                file.createNewFile();
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                
                if(!plugin.getConfig().getString("upgrade.upgrade-system").equals("UPGRADE")) {
                    PermissionUtils utils = new PermissionUtils();
                    
                    int row = utils.getPlayerBackpackSize(e.getPlayer());
                    config.set("backpack-row", row);
                } else {
                    config.set("backpack-row", 1);
                }
                config.save(file);
            } catch(IOException e1) {
                e1.printStackTrace();
            }
        } 
        
        if(plugin.getConfig().getBoolean("give-item-on-join")) {
            giveBackpackItem(e.getPlayer());
        }
    }

    private void giveBackpackItem(Player player) {
        if(playerHasBackpack(player) || isInventoryFull(player)) {
            return;
        }

        Material material = Material.valueOf(plugin.getConfig().getString("item.backpack-item"));
        int data = plugin.getConfig().getInt("item.item-data");
        String customName = plugin.getConfig().getString("item.custom-name");
        List<String> customLore = plugin.getConfig().getStringList("item.custom-lore");

        ItemStack item = new ItemStack(material, 1,(short) data);
        ItemMeta meta = item.getItemMeta();

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

        player.getInventory().addItem(item);
    }

    private boolean playerHasBackpack(Player player) {
        Material material = Material.valueOf(plugin.getConfig().getString("item.backpack-item"));
        int data = plugin.getConfig().getInt("item.item-data");

        for(ItemStack item : player.getInventory().getContents()) {
            if(item != null && item.getType() == material && item.getDurability() ==(short) data) {
                return true;
            }
        }
        return false;
    }

    private boolean isInventoryFull(Player player) {
        for(ItemStack item : player.getInventory().getContents()) {
            if(item == null) {
                return false;
            }
        }
        return true;
    }
    
    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        if(plugin.getConfig().getBoolean("disable-backpack-drop")) {
            ItemStack stack = e.getItemDrop().getItemStack();

            if(stack != null) {
                String backpackItem = plugin.getConfig().getString("item.backpack-item");
                int data = plugin.getConfig().getInt("item.item-data");

                if(!stack.getType().toString().equals(backpackItem)) {
                    return;
                }

                if(data > 0 && stack.getDurability() !=(short) data) {
                    return;
                }

                if(plugin.getConfig().getBoolean("item.can-have-custom-name")) {
                    ItemMeta meta = stack.getItemMeta();
                    if(meta == null || !translateString(meta.getDisplayName()).equals(translateString(plugin.getConfig().getString("item.custom-name")))) {
                        return;
                    }
                }

                e.setCancelled(true);
                e.getPlayer().sendMessage(prefix + translateString(plugin.getConfig().getString("messages.item-cant-be-drop")));
            }
        }
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        plugin.removeOpenBackpack(e.getPlayer());
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        if(e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) return;
        Player player = e.getPlayer();
        ItemStack stack = e.getItem();
        PermissionUtils pUtils = new PermissionUtils();

        if(stack != null) {
            String backpackItem = plugin.getConfig().getString("item.backpack-item");
            int data = plugin.getConfig().getInt("item.item-data");

            if(!stack.getType().toString().equals(backpackItem)) {
                return;
            }

            if(data > 0 && stack.getData().getData() !=(short) data) {
                return;
            }

            if(plugin.getConfig().getBoolean("item.can-have-custom-name")) {
                ItemMeta meta = stack.getItemMeta();
                if(meta == null || !translateString(meta.getDisplayName()).equals(translateString(plugin.getConfig().getString("item.custom-name")))) {
                    return;
                }
            }
            
            if(!plugin.getConfig().getStringList("disable-backpack-worlds").isEmpty() && plugin.getConfig().getStringList("disable-backpack-worlds").contains(player.getWorld().getName())) {
            	if(!player.hasPermission(pUtils.BYPASS_WORLD_LIMITATION)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-disable-in-world")));
                    e.setCancelled(true);
                    return;
            	}
            }
            
            if(plugin.isWorldguardEnable() && !plugin.getConfig().getStringList("disable-backpack-regions").isEmpty()) {
            	if(!player.hasPermission(pUtils.BYPASS_REGION_LIMITATION)) {
                	for(String s : plugin.getConfig().getStringList("disable-backpack-regions")) {
                		if(worldGuardUtils.getRegionsForPlayer(player).contains(s)) {
                            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-disable-in-region")));
                            e.setCancelled(true);
                            return;
                		}
                	}
            	}
            }
            
            if(plugin.playerHasHitCooldown(player)) {
                e.setCancelled(true);
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-hit-cooldown").replace("%s", String.valueOf(plugin.getPlayerHitCooldown(player)))));
                return;
            }

            if(plugin.isPlayerRestricted(player)) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.backpack-restricted")));
                e.setCancelled(true);
                return;
            } else {
                if(!plugin.getConfig().getString("sound-on-open").isEmpty()) {
                    Sound sound = Sound.valueOf(plugin.getConfig().getString("sound-on-open"));
                    player.playSound(player.getLocation(), sound, 1, 1);
                }
                backpackCommand.openBackpackGui(player);
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerReceiveDamage(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            if(!plugin.getConfig().getBoolean("hit-cooldown.enable")) return;
            Player victim =(Player) e.getEntity();
            Player attacker =(Player) e.getDamager();
            
            plugin.addPlayerCooldown(attacker);
            plugin.addPlayerCooldown(victim);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player =(Player) e.getWhoClicked();
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        String personalBackpackTitle = translateString(plugin.getConfig().getString("backpack-name").replace("%p", player.getName()));
        String otherBackpackTitle = translateString(plugin.getConfig().getString("backpack-other-name").replace("%p", ""));

        if(e.getInventory().getHolder() == null &&(e.getView().getTitle().equals(personalBackpackTitle) || e.getView().getTitle().startsWith(otherBackpackTitle))) {
            int slot = e.getRawSlot();
            UUID ownerUUID = getOwnerUUIDFromTitle(e.getView().getTitle(), player);
            Player owner = Bukkit.getPlayer(ownerUUID);

            if(slot < e.getInventory().getSize() && e.getCurrentItem() != null && e.getCursor() != null) {
                ItemStack cursorItem = e.getCursor();
                if(isBlacklistedItem(cursorItem, player)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.item-blacklist")));
                    e.setCancelled(true);
                    return;
                }
            }

            if(slot > 0 && slot <(getPlayerRows(player) * 9)) {
                Bukkit.getScheduler().runTaskLater(plugin,() -> {
                    ItemStack item = e.getInventory().getItem(slot);
                    if(item != null) {
                        if(isBlacklistedItem(item, owner)) {
                        	processRegiveItem(owner, item);
                            owner.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.item-blacklist")));
                            return;
                        }
                        
                        String backpackItem = plugin.getConfig().getString("item.backpack-item");
                        int data = plugin.getConfig().getInt("item.item-data");

                        if(item.getType().toString().equals(backpackItem)) {
                            if((data > 0 && item.getDurability() ==(short) data) || data <= 0) {
                                if(plugin.getConfig().getBoolean("item.can-have-custom-name")) {
                                    ItemMeta meta = item.getItemMeta();
                                    if(meta == null || !translateString(meta.getDisplayName()).equals(translateString(plugin.getConfig().getString("item.custom-name")))) {
                                    	processRegiveItem(owner, item);
                                        owner.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant-put-backpack-into-backpack")));
                                        return;
                                    }
                                } else {
                                	processRegiveItem(owner, item);
                                    owner.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant-put-backpack-into-backpack")));
                                    return;
                                }
                            }
                        }
                        dataUtils.addItem(item, owner, slot);
                    } else {
                        dataUtils.removeItem(owner, slot);
                    }
                }, 1L);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player =(Player) e.getPlayer();
        String personalBackpackTitle = translateString(plugin.getConfig().getString("backpack-name").replace("%p", player.getName()));
        String otherBackpackTitle = translateString(plugin.getConfig().getString("backpack-other-name").replace("%p", ""));
        
        if(e.getInventory().getHolder() == null &&(e.getView().getTitle().equals(personalBackpackTitle) || e.getView().getTitle().startsWith(otherBackpackTitle))) {
            UUID ownerUUID = getOwnerUUIDFromTitle(e.getView().getTitle(), player);
            Player owner = Bukkit.getPlayer(ownerUUID);
            
            if(!plugin.getConfig().getString("sound-on-close").equals("")) {
                Sound sound = Sound.valueOf(plugin.getConfig().getString("sound-on-close"));
                player.playSound(player.getLocation(), sound, 1, 1);
            }
            
            if(player != owner) {
                plugin.removeRestrictedPlayer(owner);
            } else {
                plugin.removeOpenBackpack(player);
            }

            saveBackpackData(e.getInventory(), owner);
        }
    }

    private void saveBackpackData(Inventory inventory, Player owner) {
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        for(int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if(item != null) {
                if(isBlacklistedItem(item, owner)) {
                	processRegiveItem(owner, item);
                    owner.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.item-blacklist")));
                    continue;
                }
                
                String backpackItem = plugin.getConfig().getString("item.backpack-item");
                int data = plugin.getConfig().getInt("item.item-data");

                if(item.getType().toString().equals(backpackItem)) {
                    if((data > 0 && item.getDurability() ==(short) data) || data <= 0) {
                        if(plugin.getConfig().getBoolean("item.can-have-custom-name")) {
                            ItemMeta meta = item.getItemMeta();
                            if(meta == null || !translateString(meta.getDisplayName()).equals(translateString(plugin.getConfig().getString("item.custom-name")))) {
                            	processRegiveItem(owner, item);
                                owner.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant-put-backpack-into-backpack")));
                                continue;
                            }
                        } else {
                        	processRegiveItem(owner, item);
                            owner.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant-put-backpack-into-backpack")));
                            continue;
                        }
                    }
                }
                dataUtils.addItem(item, owner, slot);
            } else {
                dataUtils.removeItem(owner, slot);
            }
        }
    }
    
    private void processRegiveItem(Player player, ItemStack stack) {
        boolean isFull = true;
        for(int i = 0; i < 36; i++) {
            if(player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR) {
                isFull = false;
                break;
            }
        }

        if(isFull) {
            Location playerLocation = player.getLocation();
            player.getWorld().dropItem(playerLocation, stack);
        } else {
            player.getInventory().addItem(stack);
        }
    }

    private int getPlayerRows(Player player) {
        UUID playerUUID = player.getUniqueId();

        File folder = new File(plugin.getDataFolder(), "players-datas");
        if(!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(folder, playerUUID + ".yml");
        if(!file.exists()) {
            return 1;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        int rows = config.getInt("backpack-row", 1);
        
        return rows;
    }
    
    private boolean isBlacklistedItem(ItemStack item, Player player) {
        String material = item.getType().toString();
        PermissionUtils pUtils = new PermissionUtils();
        
        if(plugin.getConfig().getStringList("blacklist-items").contains(material) && !player.hasPermission(pUtils.BYPASS_BLACKLIST_ITEMS)) {
            return true;
        }
        return false;
    }
    
    @SuppressWarnings("deprecation")
    private UUID getOwnerUUIDFromTitle(String title, Player opener) {
        if(title.startsWith(translateString(plugin.getConfig().getString("backpack-other-name").replace("%p", "")))) {
            String playerName = title.replace(translateString(plugin.getConfig().getString("backpack-other-name").replace("%p", "")), "");
            Player owner = Bukkit.getPlayer(playerName);
            if(owner != null) {
                return owner.getUniqueId();
            } else {
                return Bukkit.getOfflinePlayer(playerName).getUniqueId();
            }
        } else {
            return opener.getUniqueId();
        }
    }

    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
