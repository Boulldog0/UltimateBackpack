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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import fr.Boulldogo.UltimateBackpack.Commands.BackpackCommand;
import fr.Boulldogo.UltimateBackpack.Listeners.PlayerListener;
import fr.Boulldogo.UltimateBackpack.Listeners.UltimateBackpackListener;
import fr.Boulldogo.UltimateBackpack.Utils.GithubUpdater;
import fr.Boulldogo.UltimateBackpack.Utils.PermissionUtils;
import fr.Boulldogo.UltimateBackpack.Utils.YamlUpdater;
import fr.Boulldogo.UltimateBackpack.WorldGuard.RegionManager;
import fr.Boulldogo.UltimateBackpack.WorldGuard.WG6RegionManager;
import fr.Boulldogo.UltimateBackpack.WorldGuard.WG7RegionManager;
import fr.Boulldogo.WatchLogs.WatchLogsPlugin;
import fr.Boulldogo.WatchLogs.API.WatchLogsAPI;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

    private final List<UUID> restrictedBackpacks = new ArrayList<>();
    private final List<UUID> openBackpacks = new ArrayList<>();
    private final Map<Player, Integer> hitCooldown = new HashMap<>();
    private final Map<Player, UUID> adminBackpackOpened = new HashMap<>();
    private static Economy econ = null;
    private boolean vaultEnable;
    private PermissionUtils pUtils;
    private boolean worldGuardEnable;
    private RegionManager regionManager;
    private boolean watchLogsEnable;
    private WatchLogsPlugin watchLogsPlugin;

    public void onEnable() {
        saveDefaultConfig();

        new Metrics(this, 22805);
        String version = this.getDescription().getVersion();

        if (!setupEconomy()) {
            this.getLogger().warning("Vault Economy not found. Economy features are disabled.");
            vaultEnable = false;
        } else {
            vaultEnable = true;
        }

        WorldGuardPlugin wgPlugin = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
        WorldEditPlugin wePlugin = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
        if (wgPlugin != null && wePlugin != null) {
            this.worldGuardEnable = true;
            String wgVersion = wgPlugin.getDescription().getVersion();
            if (wgVersion.startsWith("7") || wgVersion.startsWith("8")) {
                regionManager = new WG7RegionManager();
                this.getLogger().info("WorldGuard version found : " + wgVersion + ". UltimateBackpack starts with WG 7 API.");
            } else {
                regionManager = new WG6RegionManager(this.getServer().getPluginManager().getPlugin("WorldGuard"));
                this.getLogger().info("WorldGuard version found : " + wgVersion + ". UltimateBackpack starts with WG 6 API.");
            }
        } else {
            this.getLogger().warning("WorldGuard or WorldEdit not found! Regions features are disabled.");
            this.worldGuardEnable = false;
        }
        
        if(this.getServer().getPluginManager().getPlugin("WatchLogs") != null && this.getConfig().getBoolean("integration.watchlogs.enable")) {
        	this.watchLogsPlugin = WatchLogsAPI.getWatchLogsPlugin();
        	this.watchLogsEnable = true;
        	
        	WatchLogsAPI api = new WatchLogsAPI(watchLogsPlugin);
        	String v = watchLogsPlugin.getVersion();
        	
        	this.getLogger().info("WatchLogs version found : " + v + ". UltimateBackpack starts with WatchLogs API (Advanced Logging).");
        	
        	api.addCustomAction(this, "backpack-item-add", "Backpack Item Add");
        	api.addCustomAction(this, "backpack-item-remove", "Backpack Item Remove");
        } else {
            this.getLogger().warning("WatchLogs not found! Advanced logging features are disabled.");
        	this.watchLogsEnable = false;
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
        if (!folder.exists()) {
            folder.mkdir();
        }

        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new UltimateBackpackListener(this, watchLogsPlugin), this);
        this.getCommand("backpack").setExecutor(new BackpackCommand(this));
    }

    public void onDisable() {
        String version = this.getDescription().getVersion();
        this.getLogger().info("UltimateBackpack v" + version + " by Boulldogo loaded with success !");
    }
    
    public boolean isWatchLogsEnable() {
    	return watchLogsEnable;
    }
    
    public Plugin getWatchLogsPlugin() {
    	return watchLogsPlugin;
    }

    public List<String> getPlayerRegions(Player player) {
        return regionManager.getRegions(player);
    }

    public void processCooldownVerification() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hitCooldown.isEmpty()) return;

                Iterator<Entry<Player, Integer>> iterator = hitCooldown.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<Player, Integer> entry = iterator.next();
                    Player player = entry.getKey();
                    int cooldown = entry.getValue();

                    if (cooldown > 1) {
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

    public boolean isPlayerAdminOpenedBackpack(Player player) {
        return adminBackpackOpened.containsKey(player);
    }

    public UUID getBackpackOpenedByAdmin(Player player) {
        return adminBackpackOpened.getOrDefault(player, player.getUniqueId());
    }

    public void addAdminPlayerOpenedBackpack(Player player, UUID playerUUID) {
    	adminBackpackOpened.put(player, playerUUID);
    }

    public void removeAdminPlayerOpenedBackpack(Player player) {
    	adminBackpackOpened.remove(player);
    }

    public boolean playerHasHitCooldown(Player player) {
        return hitCooldown.containsKey(player);
    }

    public int getPlayerHitCooldown(Player player) {
        return hitCooldown.getOrDefault(player, 0);
    }

    public void addPlayerCooldown(Player player) {
        if (!this.getConfig().getBoolean("hit-cooldown.enable")) return;
        int cooldown = this.getConfig().getInt("hit-cooldown.cooldown");

        if (player.hasPermission(pUtils.BYPASS_HIT_COOLDOWN)) return;

        hitCooldown.put(player, cooldown);
    }

    public void forceRemovePlayerCooldown(Player player) {
        hitCooldown.remove(player);
    }

    public void addRestrictedPlayer(OfflinePlayer player) {
        UUID playerUUID = player.getUniqueId();
        if (!restrictedBackpacks.contains(playerUUID)) {
            restrictedBackpacks.add(playerUUID);
        }
    }

    public void removeRestrictedPlayer(OfflinePlayer player) {
        restrictedBackpacks.remove(player.getUniqueId());
    }

    public boolean isPlayerRestricted(OfflinePlayer player) {
        return restrictedBackpacks.contains(player.getUniqueId());
    }

    public void addOpenBackpack(OfflinePlayer player) {
        UUID playerUUID = player.getUniqueId();
        if (!openBackpacks.contains(playerUUID)) {
            openBackpacks.add(playerUUID);
        }
    }

    public void removeOpenBackpack(OfflinePlayer player) {
        openBackpacks.remove(player.getUniqueId());
    }

    public boolean isOpenBackpack(OfflinePlayer player) {
        return openBackpacks.contains(player.getUniqueId());
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
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
