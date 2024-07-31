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
	
	public BackpackItemRemoveEvent(Player player, int slot, ItemStack item) {
		this.player = player;
		this.slot = slot;
		this.stack = item;
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

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
