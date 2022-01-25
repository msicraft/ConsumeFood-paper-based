package com.msicraft.consumefood.command;

import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TabComplete implements TabCompleter {

    Plugin plugin = ConsumeFood.getPlugin(ConsumeFood.class);

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        if (cmd.getName().equalsIgnoreCase("consumefood")) {
            if (args.length == 1) {
                List<String> arguments = new ArrayList<>();
                arguments.add("get");

                return arguments;
            }
            if (args.length == 2) {
                List<String> custom_internal_name = new ArrayList<>();
                Set<String> list = ConsumeFood.customfooddata.getConfig().getConfigurationSection("Custom_Food").getKeys(false);
                for (String s : list) {
                    custom_internal_name.add(s);
                }
                return custom_internal_name;
            }
        }



        return null;
    }
}

