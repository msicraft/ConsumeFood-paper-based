package com.msicraft.consumefood;

import com.msicraft.consumefood.Files.CustomFood_Data;
import com.msicraft.consumefood.command.HungerCommand;
import com.msicraft.consumefood.command.TabComplete;
import com.msicraft.consumefood.command.custom_food_command;
import com.msicraft.consumefood.events.ConsumeFoodEvents;
import com.msicraft.consumefood.events.Custom_Food_Block_Place;
import com.msicraft.consumefood.events.Custom_Food_Interact_Event;
import com.msicraft.consumefood.events.Food_Interact_Event;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConsumeFood extends JavaPlugin {

    public static ConsumeFood plugin;

    public static CustomFood_Data customfooddata;

    public static ConsumeFood getPlugin() {
        return plugin;
    }


    public static Set<String> foodnamelist() {
        Set<String> foodname = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("Food")).getKeys(false);
        for (String foodlist : foodname) {
            if (foodlist == null) {
                System.out.print("");
            }
        }
        return foodname;
    }


    public static Set<String> buff_food_list() {
        Set<String> buffdebufffoodname = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("Buff-Debuff_Food")).getKeys(false);
        for (String buffdebufffoodlist : buffdebufffoodname) {
            if (buffdebufffoodlist == null) {
                System.out.print("");
            }
        }
        return  buffdebufffoodname;
    }


    // get internal_name
    public static Set<String> custom_food_list() {
        Set<String> customfoodname = customfooddata.getConfig().getConfigurationSection("Custom_Food").getKeys(false);
        for (String s : customfoodname) {
            if (s == null) {
                System.out.print("");
            }
        }
        return customfoodname;
    }


    public static ArrayList<String> custom_food_material() {
        ArrayList<String> material_list = new ArrayList<>();
        for (String internal_name : custom_food_list()) {
            String material = ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material" );
            material_list.add(material);
        }
        return material_list;
    }



    protected FileConfiguration config;

    private FileConfiguration potiontypeconfig;

    private File messageconfigfile;
    private FileConfiguration messageconfig;


    @Override
    public void onEnable() {
        create_files();
        customfooddata = new CustomFood_Data(this);
        plugin = this;
        final int configVersion = plugin.getConfig().contains("config-version", true) ? plugin.getConfig().getInt("config-version") : -1;
        if (configVersion != 2) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Consume Food] You are using the old config");
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Consume Food] Created the latest config.yml after replacing the old config.yml with config_old.yml");
            replaceconfig();
            createFiles();
        } else {
            getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Consume Food] You are using the latest version of config.yml");
        }
        reload_files();
        getCommand("hunger").setExecutor(new HungerCommand());
        getCommand("saturation").setExecutor(new HungerCommand());
        getCommand("gethunger").setExecutor(new HungerCommand());
        getCommand("getsaturation").setExecutor(new HungerCommand());
        getCommand("consumefood").setExecutor(new custom_food_command());
        getCommand("consumefood").setTabCompleter(new TabComplete());
        getServer().getPluginManager().registerEvents(new ConsumeFoodEvents(), this);
        getServer().getPluginManager().registerEvents(new Food_Interact_Event(), this);
        getServer().getPluginManager().registerEvents(new Custom_Food_Block_Place(), this);
        getServer().getPluginManager().registerEvents(new Custom_Food_Interact_Event(), this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Consume Food] Plugin Enable");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Consume Food] Plugin Disable");
    }

    public void createFiles() {
        File configf = new File(getDataFolder(), "config.yml");

        if (!configf.exists()) {
            configf.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        FileConfiguration config = new YamlConfiguration();

        try {
            config.load(configf);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }


    public void createpotiontypefile() {
        File potiontypeconfigfile = new File(getDataFolder(), "potiontype.yml");
        if (!potiontypeconfigfile.exists()){
            potiontypeconfigfile.getParentFile().mkdirs();
            saveResource("potiontype.yml",false);
        }
        potiontypeconfig = new YamlConfiguration();
        try {
            potiontypeconfig.load(potiontypeconfigfile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }


    public FileConfiguration getpotiondata() {
        return this.potiontypeconfig;
    }

    public void create_message_file() {
        messageconfigfile = new File(getDataFolder(), "message.yml");
        if (!messageconfigfile.exists()){
            messageconfigfile.getParentFile().mkdirs();
            saveResource("message.yml", false);
        }
        messageconfig = new YamlConfiguration();
        try {
            messageconfig.load(messageconfigfile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getmessageconfig() {
        return this.messageconfig;
    }



    public void customfood_randomUUID() {
        String random_uuid = UUID.randomUUID().toString();
        ArrayList<String> customfoodlist = new ArrayList<>(custom_food_list());
        for (String internal_name : customfoodlist) {
            String get_uuid = ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".uuid");
            Material material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
            if (Material.PLAYER_HEAD.equals(material) && get_uuid == null) {
                ConsumeFood.customfooddata.getConfig().set("Custom_Food." + internal_name + ".uuid", random_uuid);
                ConsumeFood.customfooddata.saveConfig();
            }
        }
    }


    public void custom_food_uppercase() {
        ArrayList<String> customfoodlist = new ArrayList<>(custom_food_list());
        for (String internal_name : customfoodlist) {
            String get_material_name = ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material");
            if (get_material_name != null) {
                String upper_material_name = get_material_name.toUpperCase();
                ConsumeFood.customfooddata.getConfig().set("Custom_Food." + internal_name + ".material", upper_material_name);
                ConsumeFood.customfooddata.saveConfig();
            }
        }
    }


    public void replaceconfig() {
        File file = new File(getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        File config_old = new File(getDataFolder(),"config_old-" + dateFormat.format(date) + ".yml");
        file.renameTo(config_old);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Consume Food] Plugin replaced the old config.yml with config_old.yml and created a new config.yml");
    }

    public void create_files() {
        createFiles();
        createpotiontypefile();
        create_message_file();
    }

    public void reload_files() {
        foodnamelist();
        buff_food_list();
        custom_food_list();
        messageconfig = YamlConfiguration.loadConfiguration(messageconfigfile);
        custom_food_material();
        customfooddata.reloadConfig();
        custom_food_uppercase();
        customfood_randomUUID();
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("consumefood.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
        if (cmd.getName().equalsIgnoreCase("consumefoodreload")) {
            if (args.length == 0) {
                plugin.reloadConfig();
                reload_files();
                sender.sendMessage(ChatColor.GREEN + "Reloaded [Consume Food] Plugin Config");
                getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Reloaded [Consume Food] Plugin Config");
            }
            if (args.length >= 1) {
                sender.sendMessage(ChatColor.RED + "/consumefoodreload");
            }
        }
        return true;
    }

}
