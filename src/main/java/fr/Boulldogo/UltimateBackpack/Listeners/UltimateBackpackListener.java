package fr.Boulldogo.UltimateBackpack.Listeners;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import fr.Boulldogo.UltimateBackpack.Main;
import fr.Boulldogo.UltimateBackpack.Events.BackpackItemAddEvent;
import fr.Boulldogo.UltimateBackpack.Events.BackpackItemRemoveEvent;

import fr.Boulldogo.WatchLogs.WatchLogsPlugin;
import fr.Boulldogo.WatchLogs.API.WatchLogsAPI;

public class UltimateBackpackListener implements Listener {
	
	private final Main plugin;
	private final WatchLogsPlugin watchLogs;
	
	@Nullable
	public UltimateBackpackListener(Main plugin, WatchLogsPlugin watchLogs) {
		this.plugin = plugin;
		this.watchLogs = watchLogs;
	}
	
	@EventHandler
	public void onBackpackItemAddEvent(BackpackItemAddEvent e) {
		if(watchLogs == null) return;
		if(plugin.getConfig().getBoolean("integration.watchlogs.enable")) {
			WatchLogsAPI api = new WatchLogsAPI(watchLogs);
			if(api.customActionIsEnable("UltimateBackpack", "backpack-item-add")) {
				Player player = e.getPlayer();
				ItemStack stack = e.getItemStack();
				String ownerName = e.getOwnerName();
				Location loc = player.getLocation();
				
				api.addCustomLog("backpack-item-add", player, loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ(), player.getWorld().getName().toString(), "Item : " + stack.getType().toString() + "(x" + stack.getAmount() + ") | Backpack of " + ownerName);
			}
		}
	}
	
	@EventHandler
	public void onBackpackItemRemoveEvent(BackpackItemRemoveEvent e) {
		if(watchLogs == null) return;
		if(plugin.getConfig().getBoolean("integration.watchlogs.enable")) {
			WatchLogsAPI api = new WatchLogsAPI(watchLogs);
			if(api.customActionIsEnable("UltimateBackpack", "backpack-item-remove")) {
				Player player = e.getPlayer();
				ItemStack stack = e.getItemStack();
				String ownerName = e.getOwnerName();
				Location loc = player.getLocation();
				
				api.addCustomLog("backpack-item-remove", player, loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ(), player.getWorld().getName().toString(), "Item : " + stack.getType().toString() + "(x" + stack.getAmount() + ") | Backpack of " + ownerName);
			}
		}
	}

}
