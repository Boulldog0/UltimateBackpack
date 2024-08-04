package fr.Boulldogo.UltimateBackpack.WorldGuard;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WG7RegionManager implements RegionManager {

    @Override
    public List<String> getRegions(Player player) {
        List<String> regions = new ArrayList<>();
        try {
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object worldGuardInstance = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = worldGuardClass.getMethod("getPlatform").invoke(worldGuardInstance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);

            Method get = regionContainer.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World"));
            Object world = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter").getMethod("adapt", org.bukkit.World.class).invoke(null, player.getWorld());
            Object regionManager = get.invoke(regionContainer, world);

            Class<?> vectorClass = Class.forName("com.sk89q.worldedit.util.Location");
            Object location = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter").getMethod("adapt", org.bukkit.Location.class).invoke(null, player.getLocation());
            Method getApplicableRegions = regionManager.getClass().getMethod("getApplicableRegions", vectorClass);
            Object applicableRegions = getApplicableRegions.invoke(regionManager, location);

            Method getRegions = applicableRegions.getClass().getMethod("getRegions");
            Iterable<?> regionsIterable = (Iterable<?>) getRegions.invoke(applicableRegions);

            Method getId = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion").getMethod("getId");
            for(Object region : regionsIterable) {
                regions.add((String) getId.invoke(region));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return regions;
    }
}
