package com.msicraft.consumefood.events;

import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Custom_Food_Interact_Event implements Listener {

    Plugin plugin = ConsumeFood.getPlugin(ConsumeFood.class);
    Map<String, Long> cooldowns = new HashMap<String, Long>();
    Random randomchance = new Random();


    @EventHandler
    public void Custom_Food_interact(PlayerInteractEvent e) {
        Player player = e.getPlayer();
    }


}
