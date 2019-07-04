package plugin.arcwolf.neopaintingswitch;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.permission.Permission;

public class neoPaintingSwitch extends JavaPlugin {

    public static final Logger LOGGER = Logger.getLogger("Minecraft.neoPaintingSwitch");

    private net.milkbowl.vault.permission.Permission vaultPerms;
    public WorldGuardPlugin wgp;

    public boolean free4All = false;
    public boolean worldguard = false;

    private boolean permissionsEr = false;
    private boolean permissionsSet = false;
    private boolean debug = false;
    private Server server;
    private PluginDescriptionFile pdfFile;
    private PluginManager pm;
    private String pluginName;

    @Override
    public void onEnable() {
        server = this.getServer();
        pdfFile = getDescription();
        pluginName = pdfFile.getName();
        pm = server.getPluginManager();

        PluginDescriptionFile pdfFile = getDescription();
        setupConfig();
        getPermissionsPlugin();
        wgp = getWorldGuard();
        worldguard = wgp != null;

        pm.registerEvents(new npPlayerEvent(this), this);
        pm.registerEvents(new npPaintingBreakEvent(), this);

        LOGGER.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
    }

    public void setupConfig() {
        File configFile = new File(this.getDataFolder() + "/config.yml");
        FileConfiguration config = this.getConfig();
        if (!configFile.exists()) {
            config.set("free4All", Boolean.valueOf(false));
            config.set("debug", Boolean.valueOf(debug));
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        free4All = config.getBoolean("free4All", false);
        debug = config.getBoolean("debug", false);
    }

    @Override
    public void onDisable() {
        PluginDescriptionFile pdfFile = getDescription();
        LOGGER.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!");
    }

    // get worldguard plugin
    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) { return null; }

        return (WorldGuardPlugin) plugin;
    }

    public boolean hasPermission(Player player, String permission) {
        if (debug) { // For use with permissions debugging
            getPermissionsPlugin();
            if (vaultPerms != null) {
                String pName = player.getName();
                String gName = vaultPerms.getPrimaryGroup(player);
                Boolean permissions = vaultPerms.has(player, permission);
                LOGGER.info("Vault permissions, group for '" + pName + "' = " + gName);
                LOGGER.info("Permission for " + permission + " is " + permissions);
            }
            else if (server.getPluginManager().getPlugin("PermissionsBukkit") != null) {
                LOGGER.info("Bukkit Permissions " + permission + " " + player.hasPermission(permission));
            }
            else if (permissionsEr && (player.isOp() || player.hasPermission(permission))) {
                LOGGER.info("Unknown permissions plugin " + permission + " " + player.hasPermission(permission));
            }
            else {
                LOGGER.info("Unknown permissions plugin " + permission + " " + player.hasPermission(permission));
            }
        }// -- End Debug permissions hooks --
        return player.isOp() || player.hasPermission(permission);
    }

    // permissions plugin debug information
    private void getPermissionsPlugin() {
        if (server.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (!permissionsSet) {
                LOGGER.info(pluginName + ": Vault detected, permissions enabled...");
                permissionsSet = true;
            }
            vaultPerms = rsp.getProvider();
        }
        else if (server.getPluginManager().getPlugin("PermissionsBukkit") != null) {
            if (!permissionsSet) {
                LOGGER.info(pluginName + ": Bukkit permissions detected, permissions enabled...");
                permissionsSet = true;
            }
        }
        else {
            if (!permissionsEr) {
                LOGGER.info(pluginName + ": Unknown permissions detected, Using Generic Permissions...");
                permissionsEr = true;
            }
        }
    }
}
