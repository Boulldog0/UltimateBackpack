package fr.Boulldogo.UltimateBackpack.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.Boulldogo.UltimateBackpack.Main;
import fr.Boulldogo.UltimateBackpack.Events.BackpackItemAddEvent;
import fr.Boulldogo.UltimateBackpack.Events.BackpackItemRemoveEvent;

public class DataUtils {

    private final Main plugin;

    public DataUtils(Main plugin) {
        this.plugin = plugin;
    }

    public void addItem(ItemStack item, UUID playerUUID, int slot, Player saver) {
        File file = getPlayerDataFile(playerUUID);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("items." + slot, serializeItemStack(item));
        try {
            config.save(file);
            BackpackItemAddEvent event = new BackpackItemAddEvent(Bukkit.getPlayer(playerUUID), slot, item, Bukkit.getOfflinePlayer(playerUUID).getName(), playerUUID.equals(saver.getUniqueId()));
            Bukkit.getServer().getPluginManager().callEvent(event);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public ItemStack getStackOnSlot(UUID playerUUID, int slot) {
        File file = getPlayerDataFile(playerUUID);
        if(!file.exists()) {
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("items." + slot);
        
        return section != null ? deserializeItemStack(section.getValues(false)) : null;
    }

    public void removeItem(UUID playerUUID, int slot, Player saver) {
        File file = getPlayerDataFile(playerUUID);
        ItemStack stack = getStackOnSlot(playerUUID, slot);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("items." + slot, null);
        try {
            config.save(file);
            BackpackItemRemoveEvent event = new BackpackItemRemoveEvent(Bukkit.getPlayer(playerUUID), slot, stack, Bukkit.getOfflinePlayer(playerUUID).getName(), playerUUID.equals(saver.getUniqueId()));
            Bukkit.getServer().getPluginManager().callEvent(event);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private File getPlayerDataFile(UUID playerUUID) {
        File folder = new File(plugin.getDataFolder(), "players-datas");
        if (!folder.exists()) {
            folder.mkdir();
        }

        return new File(folder, playerUUID.toString() + ".yml");
    }

    private Map<String, Object> serializeItemStack(ItemStack item) {
        return item.serialize();
    }

    private ItemStack deserializeItemStack(Map<String, Object> serializedItem) {
        return ItemStack.deserialize(serializedItem);
    }
}
