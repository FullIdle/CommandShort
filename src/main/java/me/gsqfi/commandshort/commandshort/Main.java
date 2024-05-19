package me.gsqfi.commandshort.commandshort;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;

public class Main extends JavaPlugin implements Listener {
    private static final Field commandMap_field;
    private static SimpleCommandMap commandMap;


    public static Main plugin;

    @SneakyThrows
    @Override
    public void onEnable() {
        plugin = this;

        reloadConfig();

        PluginCommand command = this.getCommand(this.getDescription().getName().toLowerCase());
        commandMap = (SimpleCommandMap) commandMap_field.get(command);
        command.setExecutor(this);
        this.getServer().getPluginManager().registerEvents(this, this);

        this.getLogger().info("§aPlugin loaded!");
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfig();
        super.reloadConfig();
        Bukkit.getScheduler().runTask(this,()->setShortCmd());
    }

    @SneakyThrows
    private void setShortCmd() {
        Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
        field.setAccessible(true);
        Map<String, Command> map = (Map<String, Command>) field.get(commandMap);

        getLogger().info("§3§l已缩短以下命令:");
        for (String key : this.getConfig().getKeys(false)) {
            Command command = map.get(key);
            map.remove(key);
            map.remove(key+":"+key);
            command.unregister(commandMap);
            List<String> list = this.getConfig().getStringList(key);
            List<String> aliases = command.getAliases();
            list.removeAll(aliases);
            list.addAll(aliases);
            command.setAliases(list);
            commandMap.register(key,command);
            getLogger().info("§3§l  - "+key);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()){
            sender.sendMessage("§c§lYou don't have permissions");
            return false;
        }
        Main.plugin.reloadConfig();
        sender.sendMessage("§aReloaded!");
        return false;
    }


    static {
        try {
            commandMap_field = Command.class.getDeclaredField("commandMap");
            commandMap_field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}