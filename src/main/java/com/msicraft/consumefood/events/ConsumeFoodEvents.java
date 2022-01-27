package com.msicraft.consumefood.events;

import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

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
        ItemMeta get_item_meta = e.getItem().getItemMeta();
        PersistentDataContainer get_item_id = get_item_meta.getPersistentDataContainer();
        if (foodnlist.contains(itemstack) && !get_item_meta.hasLore() && !(get_item_id.has(new NamespacedKey(ConsumeFood.getPlugin(), "custom_id"), PersistentDataType.STRING))) {
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
            if (buffdebufffoodlist.contains(itemstack) && !get_item_meta.hasLore() && !(get_item_id.has(new NamespacedKey(ConsumeFood.getPlugin(), "custom_id"), PersistentDataType.STRING))) {
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
        if (!(get_item_id.has(new NamespacedKey(ConsumeFood.getPlugin(), "custom_id"), PersistentDataType.STRING))) {
            if (player.getFoodLevel() >= maxfoodlevel) {
                player.setFoodLevel(maxfoodlevel);
            }
            if (player.getSaturation() >= maxsaturation) {
                player.setSaturation(maxsaturation);
            }
        }
    }


    // Custom Food (Excluding PLAYER_HEAD)
    @EventHandler
    public void PlayerConsumeCustomFood(PlayerItemConsumeEvent e) {
        int maxfoodlevel = plugin.getConfig().getInt("MaxSetting.FoodLevel");
        float maxsaturation = (float) plugin.getConfig().getDouble("MaxSetting.Saturation");
        Player player = e.getPlayer();
        Material get_material = Material.valueOf(e.getItem().getType().name().toUpperCase());
        ItemStack get_item = e.getItem();
        ItemMeta get_item_meta = e.getItem().getItemMeta();
        String consume_item = get_item.getType().name().toUpperCase();
        String custom_material_list = String.valueOf(ConsumeFood.custom_food_material());
        PersistentDataContainer get_item_id = get_item_meta.getPersistentDataContainer();
        boolean custom_id = get_item_id.has(new NamespacedKey(ConsumeFood.getPlugin(), "custom_id"), PersistentDataType.STRING);
        if (custom_material_list.contains(consume_item) && get_material != Material.PLAYER_HEAD && custom_id) {
            ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
            Material main_hand_material = player.getInventory().getItemInMainHand().getType();
            Material off_hand_material = player.getInventory().getItemInOffHand().getType();
            for (String internal_name : customfoodlist) {
                int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                List<String> potioneffectlist = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".potion-effect");
                double potioneffect_chance = ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".Chance");
                if (main_hand_material != Material.AIR) {
                    if (player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                        e.setCancelled(true);
                        player.setFoodLevel(player.getFoodLevel() + food_level);
                        player.setSaturation(player.getSaturation() + saturation);
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        if (!potioneffectlist.isEmpty()) {
                            if (randomchance.nextDouble() <= potioneffect_chance) {
                                for (String effectlistf : potioneffectlist) {
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
                        break;
                    }
                }
                if (off_hand_material != Material.AIR) {
                    if (player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                        e.setCancelled(true);
                        player.setFoodLevel(player.getFoodLevel() + food_level);
                        player.setSaturation(player.getSaturation() + saturation);
                        player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
                        if (!potioneffectlist.isEmpty()) {
                            if (randomchance.nextDouble() <= potioneffect_chance) {
                                for (String effectlistf : potioneffectlist) {
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
                        break;
                    }
                }
            }
        }
        if (custom_id) {
            if (player.getFoodLevel() >= maxfoodlevel) {
                player.setFoodLevel(maxfoodlevel);
            }
            if (player.getSaturation() >= maxsaturation) {
                player.setSaturation(maxsaturation);
            }
        }
    }

}