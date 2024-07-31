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

    public void addItem(ItemStack item, Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        File folder = new File(plugin.getDataFolder(), "players-datas");
        if(!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(folder, playerUUID + ".yml");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException e1) {
                e1.printStackTrace();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("items." + slot, serializeItemStack(item));
        try {
            config.save(file);
            BackpackItemAddEvent event = new BackpackItemAddEvent(player, slot, item);
            Bukkit.getServer().getPluginManager().callEvent(event);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public ItemStack getStackOnSlot(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        File folder = new File(plugin.getDataFolder(), "players-datas");
        if(!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(folder, playerUUID + ".yml");
        if(!file.exists()) {
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("items." + slot);
        
        return section != null ? deserializeItemStack(section.getValues(false)) : null;
    }

    public void removeItem(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        File folder = new File(plugin.getDataFolder(), "players-datas");
        if(!folder.exists()) {
            folder.mkdir();
        }
        
        ItemStack stack = getStackOnSlot(player, slot);

        File file = new File(folder, playerUUID + ".yml");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException e1) {
                e1.printStackTrace();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("items." + slot, null);
        try {
            config.save(file);
            BackpackItemRemoveEvent event = new BackpackItemRemoveEvent(player, slot, stack);
            Bukkit.getServer().getPluginManager().callEvent(event);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> serializeItemStack(ItemStack item) {
        return item.serialize();
    }

    private ItemStack deserializeItemStack(Map<String, Object> serializedItem) {
        return ItemStack.deserialize(serializedItem);
    }
}
