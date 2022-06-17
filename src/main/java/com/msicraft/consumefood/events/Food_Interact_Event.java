package com.msicraft.consumefood.events;

import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Food_Interact_Event implements Listener {

    Plugin plugin = ConsumeFood.getPlugin(ConsumeFood.class);

    Random randomchance = new Random();

    private final Map<String, Long> personal_cooldown = new HashMap<String, Long>();
    private final Map<UUID, Long> global_cooldown = new HashMap<UUID, Long>();


    @EventHandler
    public void Food_Interact(PlayerInteractEvent e) {
        boolean max_consumable = plugin.getConfig().getBoolean("Max_Consumable.Enabled");
        String foodnlist = String.valueOf(ConsumeFood.foodnamelist());
        String buffdebufffoodlist = String.valueOf(ConsumeFood.buff_food_list());
        String itemstack = e.getMaterial().name().toUpperCase();
        if (max_consumable) {
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                if (foodnlist.contains(itemstack) || buffdebufffoodlist.contains(itemstack)) {
                    Player player = e.getPlayer();
                    String buffdebuffpotioneffect = String.valueOf(plugin.getConfig().getStringList("Buff-Debuff_Food." + itemstack + ".PotionEffect"));
                    double potioneffectchange = plugin.getConfig().getDouble("Buff-Debuff_Food." + itemstack + ".Chance");
                    int p_food_level = player.getFoodLevel();
                    int max_food_level = plugin.getConfig().getInt("MaxSetting.FoodLevel");
                    float max_saturation = (float) plugin.getConfig().getDouble("MaxSetting.Saturation");
                    String max_foodlevel_path = ConsumeFood.plugin.getmessageconfig().getString("max_food_level");
                    String max_saturation_path = ConsumeFood.plugin.getmessageconfig().getString("max_saturation");
                    ItemStack get_item = e.getItem();
                    ItemMeta get_item_meta = Objects.requireNonNull(get_item).getItemMeta();
                    PersistentDataContainer get_item_id = get_item_meta.getPersistentDataContainer();
                    boolean check_item_id = get_item_id.has(new NamespacedKey(ConsumeFood.getPlugin(), "custom_id"), PersistentDataType.STRING);
                    String get_cooldown_type = plugin.getConfig().getString("Cooldown.Type");
                    if (get_cooldown_type == null) {
                        get_cooldown_type = "disable";
                    }
                    if (get_cooldown_type.equals("global")) {
                        long get_global_cooldown = plugin.getConfig().getLong("Cooldown.Global_Cooldown");
                        if (global_cooldown.containsKey(player.getUniqueId())) {
                            if (global_cooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
                                String cooldown_path = ConsumeFood.plugin.getmessageconfig().getString("global_cooldown");
                                long global_timeleft = (global_cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
                                if (cooldown_path != null) {
                                    cooldown_path = cooldown_path.replaceAll("%global_time_left%", String.valueOf(global_timeleft));
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldown_path));
                                }
                                return;
                            }
                        }
                        global_cooldown.put(player.getUniqueId(), System.currentTimeMillis() + (get_global_cooldown * 1000));
                        if (foodnlist.contains(itemstack) && p_food_level >= 20 && !check_item_id) {
                            if (player.getInventory().getItemInMainHand().getType().name().toUpperCase().equals(itemstack)) {
                                player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                                player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                                player.getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                            } else if (player.getInventory().getItemInOffHand().getType().name().toUpperCase().equals(itemstack)){
                                player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                                player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                                player.getInventory().getItemInOffHand().setAmount(e.getPlayer().getInventory().getItemInOffHand().getAmount() - 1);
                                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                            }
                        } else if (buffdebufffoodlist.contains(itemstack) && p_food_level >= 20 && !check_item_id) {
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
                    } else if (get_cooldown_type.equals("personal")) {

                    }
                    if (player.getFoodLevel() >= max_food_level) {
                        player.setFoodLevel(max_food_level);
                        if (max_foodlevel_path != null && !check_item_id) {
                            max_foodlevel_path = max_foodlevel_path.replaceAll("%max_foodlevel%", String.valueOf(max_food_level));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', max_foodlevel_path));
                        }
                    }
                    if (player.getSaturation() >= max_saturation) {
                        player.setSaturation(max_saturation);
                        if (max_saturation_path != null && !check_item_id) {
                            max_saturation_path = max_saturation_path.replaceAll("%max_saturation%", String.valueOf(max_saturation));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', max_saturation_path));
                        }
                    }
                }
            }
        }
    }





}
