package com.msicraft.consumefood.command;

import com.destroystokyo.paper.Namespaced;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.Lists;
import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.io.IOError;
import java.io.IOException;
import java.util.*;


public class custom_food_command implements CommandExecutor {

    Plugin plugin = ConsumeFood.getPlugin(ConsumeFood.class);


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only Players can use that command");
            return true;
        }
        if (command.getName().equalsIgnoreCase("consumefood")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "/consumefood get <internal_name> <amount>");
            }
            if (args.length == 1) {
                player.sendMessage(ChatColor.RED + "/consumefood get <internal_name> <amount>");
            }
            if (args.length == 2) {
                player.sendMessage(ChatColor.RED + "/consumefood get <internal_name> <amount>");
            }
            if (args.length == 3) {
                String internal_name = args[1];
                int amount = Integer.parseInt(args[2]);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                String name = ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".name");
                String value = ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".value");
                UUID uuid = UUID.fromString(Objects.requireNonNull(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".uuid")));
                ArrayList<String> lore = new ArrayList<>();
                List<String> lore_list = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".lore");
                Material material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
                PlayerProfile playerprofile = Bukkit.createProfile(uuid, name);
                if (Material.PLAYER_HEAD.equals(material)) {
                    if (value != null) {
                        playerprofile.setProperty(new ProfileProperty("textures", value));
                        skullMeta.setPlayerProfile(playerprofile);
                        if (name == null) {
                            skullMeta.setDisplayName("");
                        } else {
                            skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                        }
                        for (String s : lore_list) {
                            lore.add(ChatColor.translateAlternateColorCodes('&', s));
                        }
                        skullMeta.setLore(lore);
                        skull.setItemMeta(skullMeta);
                        for (int i = 0; i<amount ; i++) {
                            player.getInventory().addItem(skull);
                        }
                    }
                } else {
                    ItemStack custom_food = new ItemStack(material, 1);
                    ItemMeta custom_food_meta = custom_food.getItemMeta();
                    if (name == null) {
                        custom_food_meta.setDisplayName("");
                    } else {
                        custom_food_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                    }
                    for (String s : lore_list) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', s));
                    }
                    custom_food_meta.setLore(lore);
                    custom_food.setItemMeta(custom_food_meta);
                    for (int i = 0; i<amount ; i++) {
                        player.getInventory().addItem(custom_food);
                    }
                }
            }
        }


        return true;
    }
}