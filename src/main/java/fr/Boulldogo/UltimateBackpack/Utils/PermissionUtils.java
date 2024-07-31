package fr.Boulldogo.UltimateBackpack.Utils;

import org.bukkit.entity.Player;

public class PermissionUtils {
	
	public String OPEN_BACKPACK = "backpack.open.himself";
	public String OPEN_BACKPACK_OTHER = "backpack.open.everyone";
	public String BYPASS_BLACKLIST_ITEMS = "backpack.bypass.blacklist_items";
	public String BYPASS_HIT_COOLDOWN = "backpack.bypass.hit_cooldown";
	public String BYPASS_REGION_LIMITATION = "backpack.bypass.region_limitation";
	public String BYPASS_WORLD_LIMITATION = "backpack.bypass.world_limitation";
	
	public int getPlayerBackpackSize(Player player) {
		if(player.hasPermission("backpack.size.6")) {
			return 6;
		} else if(player.hasPermission("backpack.size.5")) {
			return 5;
		} else if(player.hasPermission("backpack.size.4")) {
			return 4;
		} else if(player.hasPermission("backpack.size.3")) {
			return 3;
		} else if(player.hasPermission("backpack.size.2")) {
			return 2;
		} else if(player.hasPermission("backpack.size.1")) {
			return 1;
		}
		return 1;
	}

}
