package com.msicraft.consumefood.events;

import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Custom_Food_Block_Place implements Listener {

    Plugin plugin = ConsumeFood.getPlugin(ConsumeFood.class);

    @EventHandler
    public void Custom_Food_Place(BlockPlaceEvent e) {
        Material block_material = e.getBlockPlaced().getBlockData().getMaterial();
        ItemStack get_item = e.getItemInHand();
        ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
        ArrayList<String> lore = new ArrayList<>();
        List<String> item_lore = get_item.getLore();
        for (String internal_name : customfoodlist) {
            List<String> lore_list = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".lore");
            for (String s : lore_list) {
                lore.add(ChatColor.translateAlternateColorCodes('&', s));
            }
        }
        if (block_material == Material.PLAYER_HEAD && item_lore != null && lore.containsAll(item_lore)) {
            e.setCancelled(true);
        } else {
            if (block_material == Material.PLAYER_WALL_HEAD && item_lore != null && lore.containsAll(item_lore)) {
                e.setCancelled(true);
            }
        }

    }


}
