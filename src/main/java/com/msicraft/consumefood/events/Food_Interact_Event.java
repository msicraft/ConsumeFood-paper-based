package com.msicraft.consumefood.events;

import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Food_Interact_Event implements Listener {

    Plugin plugin = ConsumeFood.getPlugin(ConsumeFood.class);
    Map<String, Long> cooldowns = new HashMap<String, Long>();
    Random randomchance = new Random();


    @EventHandler
    public void Food_Interact(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        String foodnlist = String.valueOf(ConsumeFood.foodnamelist());
        String buffdebufffoodlist = String.valueOf(ConsumeFood.buff_food_list());
        String itemstack = e.getMaterial().name().toUpperCase();
        String buffdebuffpotioneffect = String.valueOf(plugin.getConfig().getStringList("Buff-Debuff_Food." + itemstack + ".PotionEffect"));
        double potioneffectchange = plugin.getConfig().getDouble("Buff-Debuff_Food." + itemstack + ".Chance");
        int p_food_level = player.getFoodLevel();
        int max_food_level = plugin.getConfig().getInt("MaxSetting.FoodLevel");
        float max_saturation = (float) plugin.getConfig().getDouble("MaxSetting.Saturation");
        boolean max_consumable = plugin.getConfig().getBoolean("Max_Consumable.Enabled");
        long cooldown = plugin.getConfig().getLong("Max_Consumable.Cooldown");
        String cooldown_path = ConsumeFood.plugin.getmessageconfig().getString("cooldown");
        String max_foodlevel_path = ConsumeFood.plugin.getmessageconfig().getString("max_food_level");
        String max_saturation_path = ConsumeFood.plugin.getmessageconfig().getString("max_saturation");
        if (max_consumable) {
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                if (foodnlist.contains(itemstack) || buffdebufffoodlist.contains(itemstack)) {
                    if (cooldowns.containsKey(player.getName()) && p_food_level >= 20) {
                        if (cooldowns.get(player.getName()) > System.currentTimeMillis()) {
                            long timeleft = (cooldowns.get(player.getName()) - System.currentTimeMillis()) / 1000;
                            if (cooldown_path != null) {
                                cooldown_path = cooldown_path.replaceAll("%time_left%", String.valueOf(timeleft));
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldown_path));
                            }
                            return;
                        }
                    }
                    cooldowns.put(player.getName(), System.currentTimeMillis() + (cooldown * 1000));
                    if (foodnlist.contains(itemstack) && p_food_level >= 20) {
                        if (player.getInventory().getItemInMainHand().getType().name().toUpperCase().equals(itemstack)) {
                            player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                            player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                            player.getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                        } else {
                            player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                            player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                            player.getInventory().getItemInOffHand().setAmount(e.getPlayer().getInventory().getItemInOffHand().getAmount() - 1);
                            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                        }
                    } else {
                        if (buffdebufffoodlist.contains(itemstack) && p_food_level >= 20) {
                            if (player.getInventory().getItemInMainHand().getType().name().toUpperCase().equals(itemstack)) {
                                player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Buff-Debuff_Food." + itemstack + ".FoodLevel"));
                                player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Buff-Debuff_Food." + itemstack + ".Saturation")));
                                e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
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
                            } else {
                                player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Buff-Debuff_Food." + itemstack + ".FoodLevel"));
                                player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Buff-Debuff_Food." + itemstack + ".Saturation")));
                                e.getPlayer().getInventory().getItemInOffHand().setAmount(e.getPlayer().getInventory().getItemInOffHand().getAmount() - 1);
                                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
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
                    if (player.getFoodLevel() >= max_food_level) {
                        player.setFoodLevel(max_food_level);
                        if (max_foodlevel_path != null) {
                            max_foodlevel_path = max_foodlevel_path.replaceAll("%max_foodlevel%", String.valueOf(max_food_level));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', max_foodlevel_path));
                        }
                    }
                    if (player.getSaturation() >= max_saturation) {
                        player.setSaturation(max_saturation);
                        if (max_saturation_path != null) {
                            max_saturation_path = max_saturation_path.replaceAll("%max_saturation%", String.valueOf(max_saturation));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', max_saturation_path));
                        }
                    }
                }
            }
        }
    }





}
