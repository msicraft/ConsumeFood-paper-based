package com.msicraft.consumefood.events;

import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.security.auth.callback.CallbackHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConsumeFoodEvents implements Listener {

    Plugin plugin = ConsumeFood.getPlugin(ConsumeFood.class);
    Random randomchance = new Random();


    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent e) {
        String foodnlist = String.valueOf(ConsumeFood.foodnamelist());
        Player player = e.getPlayer();
        String itemstack = e.getItem().getType().name().toUpperCase();
        int maxfoodlevel = plugin.getConfig().getInt("MaxSetting.FoodLevel");
        float maxsaturation = (float) plugin.getConfig().getDouble("MaxSetting.Saturation");
        String buffdebufffoodlist = String.valueOf(ConsumeFood.buff_food_list());
        String buffdebuffpotioneffect = String.valueOf(plugin.getConfig().getStringList("Buff-Debuff_Food." + itemstack + ".PotionEffect"));
        double potioneffectchange = plugin.getConfig().getDouble("Buff-Debuff_Food." + itemstack + ".Chance");
        if (foodnlist.contains(itemstack)) {
            if (foodnlist.contains(player.getInventory().getItemInOffHand().getType().name().toUpperCase())) {
                e.setCancelled(true);
                player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                e.getPlayer().getInventory().getItemInOffHand().setAmount(e.getPlayer().getInventory().getItemInOffHand().getAmount() - 1);
            }  else {
                e.setCancelled(true);
                player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            }
        } else {
            if (buffdebufffoodlist.contains(itemstack)) {
                if (buffdebufffoodlist.contains(player.getInventory().getItemInOffHand().getType().name().toUpperCase())) {
                    e.setCancelled(true);
                    player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Buff-Debuff_Food." + itemstack + ".FoodLevel"));
                    player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Buff-Debuff_Food." + itemstack + ".Saturation")));
                    e.getPlayer().getInventory().getItemInOffHand().setAmount(e.getPlayer().getInventory().getItemInOffHand().getAmount() - 1);
                    if (buffdebuffpotioneffect != null) {
                        if (randomchance.nextDouble() <= potioneffectchange) {
                            List<String> getPotionEffects = plugin.getConfig().getStringList("Buff-Debuff_Food." + itemstack + ".PotionEffect");
                            for (String effectlistf : getPotionEffects) {
                                String [] effectlist = effectlistf.split(":");
                                PotionEffectType listpotiontype = PotionEffectType.getByName(effectlist[0]);
                                int listpotionlvl = Integer.parseInt(effectlist[1]);
                                int listpotionduration = Integer.parseInt(effectlist[2]);
                                if (listpotiontype != null) {
                                    player.addPotionEffect(new PotionEffect(listpotiontype, listpotionduration * 20, listpotionlvl - 1));
                                }
                            }
                        }
                    }
                }
                if (buffdebufffoodlist.contains(player.getInventory().getItemInMainHand().getType().name().toUpperCase())) {
                    e.setCancelled(true);
                    player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Buff-Debuff_Food." + itemstack + ".FoodLevel"));
                    player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Buff-Debuff_Food." + itemstack + ".Saturation")));
                    e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                    if (buffdebuffpotioneffect != null) {
                        if (randomchance.nextDouble() <= potioneffectchange) {
                            List<String> getPotionEffects = plugin.getConfig().getStringList("Buff-Debuff_Food." + itemstack + ".PotionEffect");
                            for (String effectlistf : getPotionEffects) {
                                String [] effectlist = effectlistf.split(":");
                                PotionEffectType listpotiontype = PotionEffectType.getByName(effectlist[0]);
                                int listpotionlvl = Integer.parseInt(effectlist[1]);
                                int listpotionduration = Integer.parseInt(effectlist[2]);
                                if (listpotiontype != null) {
                                    player.addPotionEffect(new PotionEffect(listpotiontype, listpotionduration * 20, listpotionlvl - 1));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (player.getFoodLevel() >= maxfoodlevel) {
            player.setFoodLevel(maxfoodlevel);
        }
        if (player.getSaturation() >= maxsaturation) {
            player.setSaturation(maxsaturation);
        }
    }


    // Custom Food (Excluding PLAYER_HEAD)
    @EventHandler
    public void PlayerConsumeCustomFood(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        Material get_material = Material.valueOf(e.getItem().getType().name().toUpperCase());
        ItemMeta get_item_meta = e.getItem().getItemMeta();
        String consume_item = e.getItem().getType().name().toUpperCase();
        String item_name = e.getItem().getItemMeta().getDisplayName();
        String custom_material_list = String.valueOf(ConsumeFood.custom_food_material());
        ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
        ArrayList<String> lore = new ArrayList<>();
        List<String> item_lore = get_item_meta.getLore();
        if (custom_material_list.contains(consume_item) && get_material != Material.PLAYER_HEAD && get_item_meta.hasLore()) {
            for (String internal_name : customfoodlist) {
                List<String> lore_list = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".lore");
                String name = ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".name");
                for (String s : lore_list) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', s));
                }
                if (name == null) {
                    name = "";
                } else {
                    name = ChatColor.translateAlternateColorCodes('&', name);
                }
                if (lore.containsAll(item_lore) && name.equals(item_name)) {
                    int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                    float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                    e.setCancelled(true);
                    player.setFoodLevel(player.getFoodLevel() + food_level);
                    player.setSaturation(player.getSaturation() + saturation);
                    break;
                }
            }
        }
    }

}