package fr.Boulldogo.UltimateBackpack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.Boulldogo.UltimateBackpack.Commands.BackpackCommand;
import fr.Boulldogo.UltimateBackpack.Listeners.PlayerListener;
import fr.Boulldogo.UltimateBackpack.Utils.GithubUpdater;
import fr.Boulldogo.UltimateBackpack.Utils.PermissionUtils;
import fr.Boulldogo.UltimateBackpack.Utils.YamlUpdater;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {
	
    private final List<UUID> restrictedBackpacks = new ArrayList<>();
    private final List<UUID> openBackpacks = new ArrayList<>();
    private final Map<Player, Integer> hitCooldown = new HashMap<>();
    private static Economy econ = null;
    private boolean vaultEnable;
    private PermissionUtils pUtils;
    private boolean worldGuardEnable;
	
	public void onEnable() {
		saveDefaultConfig();
		
		new Metrics(this, 22805);
		String version = this.getDescription().getVersion();
		
	    if(!setupEconomy()) {
	        this.getLogger().warning("Vault Economy not found. Economy features are disabled.");
	        vaultEnable = false;
	    } else {
	        vaultEnable = true;
	    }
        
        if(Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            this.worldGuardEnable = true;
        } else {
            this.getLogger().warning("WorldGuard not found! Regions features are disabled.");
            this.worldGuardEnable = false;
        }
        
        YamlUpdater updater = new YamlUpdater(this);
        String[] filesToUpdate = {"config.yml"};
        updater.updateYamlFiles(filesToUpdate);
        this.pUtils = new PermissionUtils();
		
        this.processCooldownVerification();
		this.getLogger().info("UltimateBackpack v" + version + " by Boulldogo loaded with success !");
		
		GithubUpdater gUpdater = new GithubUpdater(this);
		gUpdater.checkForUpdates();
		
		File folder = new File(this.getDataFolder(), "players-datas");
		if(!folder.exists()) {
			folder.mkdir();
		}
	
		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		this.getCommand("backpack").setExecutor(new BackpackCommand(this));
	}
	
	public void onDisable() {
		String version = this.getDescription().getVersion();
		this.getLogger().info("UltimateBackpack v" + version + " by Boulldogo loaded with success !");
	}
	
    public void processCooldownVerification() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(hitCooldown.isEmpty()) return;

                Iterator<Entry<Player, Integer>> iterator = hitCooldown.entrySet().iterator();
                while(iterator.hasNext()) {
                    Entry<Player, Integer> entry = iterator.next();
                    Player player = entry.getKey();
                    int cooldown = entry.getValue();

                    if(cooldown > 1) {
                        hitCooldown.put(player, cooldown - 1);
                    } else {
                        iterator.remove();
                    }
                }
            }
        }.runTaskTimer(this, 0, 20L);
    }
    
    public boolean isWorldguardEnable() {
    	return worldGuardEnable;
    }
    
    public boolean playerHasHitCooldown(Player player) {
    	if(hitCooldown.isEmpty()) return false;
    	return hitCooldown.containsKey(player);
    }
    
    public int getPlayerHitCooldown(Player player) {
    	if(hitCooldown.isEmpty()) return 0;
    	if(!playerHasHitCooldown(player)) return 0;
    	
    	return hitCooldown.get(player);
    }
    
    public void addPlayerCooldown(Player player) {
    	if(!this.getConfig().getBoolean("hit-cooldown.enable")) return;
    	int cooldown = this.getConfig().getInt("hit-cooldown.cooldown");
    	
    	if(player.hasPermission(pUtils.BYPASS_HIT_COOLDOWN)) return;
    	
    	hitCooldown.put(player, cooldown);
    }
    
    public void forceRemovePlayerCooldown(Player player) {
    	if(hitCooldown.isEmpty()) return;
    	if(!playerHasHitCooldown(player)) return;
    	
    	hitCooldown.remove(player);
    }
	
	public void addRestrictedPlayer(Player player) {
		UUID playerUUID = player.getUniqueId();
		if(!restrictedBackpacks.contains(playerUUID)) {
			restrictedBackpacks.add(playerUUID);
		}
	}
	
	public void removeRestrictedPlayer(Player player) {
		if(restrictedBackpacks.isEmpty()) return;
		UUID playerUUID = player.getUniqueId();
		if(restrictedBackpacks.contains(playerUUID)) {
			restrictedBackpacks.remove(playerUUID);
		}
	}
	
	public boolean isPlayerRestricted(Player player) {
		if(restrictedBackpacks.isEmpty()) return false;
		UUID playerUUID = player.getUniqueId();
		return restrictedBackpacks.contains(playerUUID);
	}
	
	public void addOpenBackpack(Player player) {
		UUID playerUUID = player.getUniqueId();
		if(!openBackpacks.contains(playerUUID)) {
			openBackpacks.add(playerUUID);
		}
	}
	
	public void removeOpenBackpack(Player player) {
		if(openBackpacks.isEmpty()) return;
		UUID playerUUID = player.getUniqueId();
		if(openBackpacks.contains(playerUUID)) {
			openBackpacks.remove(playerUUID);
		}
	}
	
	public boolean isOpenBackpack(Player player) {
		if(openBackpacks.isEmpty()) return false;
		UUID playerUUID = player.getUniqueId();
		return openBackpacks.contains(playerUUID);
	}
	
    private boolean setupEconomy() {
        if(getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public Economy getEconomy() {
        return econ;
    }
    
    public boolean isVaultEnabled() {
    	return vaultEnable;
    }
}
