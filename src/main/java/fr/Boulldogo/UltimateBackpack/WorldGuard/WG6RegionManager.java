package fr.Boulldogo.UltimateBackpack.WorldGuard;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WG6RegionManager implements RegionManager {
    private final Plugin wgPlugin;

    public WG6RegionManager(Plugin plugin) {
        this.wgPlugin = plugin;
    }

    @Override
    public List<String> getRegions(Player player) {
        List<String> regions = new ArrayList<>();
        try {
            Object wgPluginInstance = wgPlugin;
            Method getRegionManager = wgPluginInstance.getClass().getMethod("getRegionManager", org.bukkit.World.class);
            Object regionManager = getRegionManager.invoke(wgPluginInstance, player.getWorld());

            Class<?> blockVectorClass = Class.forName("com.sk89q.worldedit.Vector");
            Method getApplicableRegions = regionManager.getClass().getMethod("getApplicableRegions", blockVectorClass);
            Object blockVector = blockVectorClass.getConstructor(double.class, double.class, double.class)
                    .newInstance(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
            Object applicableRegions = getApplicableRegions.invoke(regionManager, blockVector);

            Method getRegions = applicableRegions.getClass().getMethod("getRegions");
            Iterable<?> regionsIterable = (Iterable<?>) getRegions.invoke(applicableRegions);

            Class<?> protectedRegionClass = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion");
            Method getId = protectedRegionClass.getMethod("getId");
            for(Object region : regionsIterable) {
                regions.add((String) getId.invoke(region));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return regions;
    }
}
