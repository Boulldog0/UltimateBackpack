package fr.Boulldogo.UltimateBackpack.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class BackpackItemRemoveEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final int slot;
	private final ItemStack stack;
	private final String ownerName;
	private final boolean playerEditInAdmin;
	
	public BackpackItemRemoveEvent(Player player, int slot, ItemStack item, String ownerName, boolean playerEditInAdmin) {
		this.player = player;
		this.slot = slot;
		this.stack = item;
		this.ownerName = ownerName;
		this.playerEditInAdmin = playerEditInAdmin;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public ItemStack getItemStack() {
		return stack;
	}
	
	public String getOwnerName() {
		return ownerName;
	}
	
	public boolean playerEditInAdmin() {
		return playerEditInAdmin;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
