package com.msicraft.consumefood.events;

import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.Bukkit;
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

    private final Map<String, Long> personal_cooldown = new HashMap<String, Long>();
    private final Map<UUID, Long> global_cooldown = new HashMap<UUID, Long>();


    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        String foodnlist = String.valueOf(ConsumeFood.foodnamelist());
        String buffdebufffoodlist = String.valueOf(ConsumeFood.buff_food_list());
        String itemstack = e.getItem().getType().name().toUpperCase();
        ItemMeta get_item_meta = e.getItem().getItemMeta();
        int maxfoodlevel = plugin.getConfig().getInt("MaxSetting.FoodLevel");
        float maxsaturation = (float) plugin.getConfig().getDouble("MaxSetting.Saturation");
        String buffdebuffpotioneffect = String.valueOf(plugin.getConfig().getStringList("Buff-Debuff_Food." + itemstack + ".PotionEffect"));
        double potioneffectchange = plugin.getConfig().getDouble("Buff-Debuff_Food." + itemstack + ".Chance");
        PersistentDataContainer get_item_id = get_item_meta.getPersistentDataContainer();
        boolean check_id = get_item_id.has(new NamespacedKey(ConsumeFood.getPlugin(), "custom_id"), PersistentDataType.STRING);
        String get_cooldown_type = plugin.getConfig().getString("Cooldown.Type");
        if (get_cooldown_type == null) {
            get_cooldown_type = "disable";
        }
        if (get_cooldown_type.equals("global")) {
            long get_global_cooldown = plugin.getConfig().getLong("Cooldown.Global_Cooldown");
            if (foodnlist.contains(itemstack) && !get_item_meta.hasLore() &&!check_id) {
                if (global_cooldown.containsKey(player.getUniqueId())) {
                    if (global_cooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
                        String cooldown_path = ConsumeFood.plugin.getmessageconfig().getString("global_cooldown");
                        long global_timeleft = (global_cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
                        if (cooldown_path != null) {
                            cooldown_path = cooldown_path.replaceAll("%global_time_left%", String.valueOf(global_timeleft));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldown_path));
                        }
                        e.setCancelled(true);
                        return;
                    }
                }
                global_cooldown.put(player.getUniqueId(), System.currentTimeMillis() + (get_global_cooldown * 1000));
                if (foodnlist.contains(player.getInventory().getItemInMainHand().getType().name().toUpperCase())) {
                    e.setCancelled(true);
                    player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                    player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                    e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                } else if (foodnlist.contains(player.getInventory().getItemInOffHand().getType().name().toUpperCase())) {
                    e.setCancelled(true);
                    player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                    player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                    e.getPlayer().getInventory().getItemInOffHand().setAmount(e.getPlayer().getInventory().getItemInOffHand().getAmount() - 1);
                }
            } else if (buffdebufffoodlist.contains(itemstack) && !get_item_meta.hasLore() && !check_id) {
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
                } else if (buffdebufffoodlist.contains(player.getInventory().getItemInOffHand().getType().name().toUpperCase())) {
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
            }
        } else if (get_cooldown_type.equals("personal")) {
            if (foodnlist.contains(itemstack) && !get_item_meta.hasLore() && !check_id) {
                String get_uuid_food = player.getUniqueId() + ":" + itemstack;
                long get_personal_cooldown = plugin.getConfig().getLong("Food." + itemstack + ".Cooldown");
                if (personal_cooldown.containsKey(get_uuid_food)) {
                    if (personal_cooldown.get(get_uuid_food) > System.currentTimeMillis()) {
                        String cooldown_path = ConsumeFood.plugin.getmessageconfig().getString("personal_cooldown");
                        long timeleft = (personal_cooldown.get(get_uuid_food) - System.currentTimeMillis()) / 1000;
                        if (cooldown_path != null) {
                            cooldown_path = cooldown_path.replaceAll("%personal_time_left%", String.valueOf(timeleft));
                            cooldown_path = cooldown_path.replaceAll("%food_name%", e.getItem().getItemMeta().getDisplayName());
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldown_path));
                        }
                        e.setCancelled(true);
                        return;
                    }
                }
                personal_cooldown.put(get_uuid_food, System.currentTimeMillis() + (get_personal_cooldown * 1000));
                if (foodnlist.contains(player.getInventory().getItemInMainHand().getType().name().toUpperCase())) {
                    e.setCancelled(true);
                    player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                    player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                    e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                } else if (foodnlist.contains(player.getInventory().getItemInOffHand().getType().name().toUpperCase())) {
                    e.setCancelled(true);
                    player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                    player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                    e.getPlayer().getInventory().getItemInOffHand().setAmount(e.getPlayer().getInventory().getItemInOffHand().getAmount() - 1);
                }
            } else if (buffdebufffoodlist.contains(itemstack) && !get_item_meta.hasLore() && !check_id) {
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
                } else if (buffdebufffoodlist.contains(player.getInventory().getItemInOffHand().getType().name().toUpperCase())) {
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
            }
        } else if (get_cooldown_type.equals("disable")) {
            if (foodnlist.contains(itemstack) && !get_item_meta.hasLore() && !(get_item_id.has(new NamespacedKey(ConsumeFood.getPlugin(), "custom_id"), PersistentDataType.STRING))) {
                if (foodnlist.contains(player.getInventory().getItemInMainHand().getType().name().toUpperCase())) {
                    e.setCancelled(true);
                    player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                    player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                    e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                } else if (foodnlist.contains(player.getInventory().getItemInOffHand().getType().name().toUpperCase())) {
                    e.setCancelled(true);
                    player.setFoodLevel(player.getFoodLevel() + plugin.getConfig().getInt("Food." + itemstack + ".FoodLevel"));
                    player.setSaturation((float) (player.getSaturation() + plugin.getConfig().getDouble("Food." + itemstack + ".Saturation")));
                    e.getPlayer().getInventory().getItemInOffHand().setAmount(e.getPlayer().getInventory().getItemInOffHand().getAmount() - 1);
                }
            } else {
                if (buffdebufffoodlist.contains(itemstack) && !get_item_meta.hasLore() && !(get_item_id.has(new NamespacedKey(ConsumeFood.getPlugin(), "custom_id"), PersistentDataType.STRING))) {
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
                    } else if (buffdebufffoodlist.contains(player.getInventory().getItemInOffHand().getType().name().toUpperCase())) {
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

    private final Map<String, Long> custom_food_personal_cooldown = new HashMap<String, Long>();
    private final Map<UUID, Long> custom_food_global_cooldown = new HashMap<UUID, Long>();

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
        // String custom_material_list = String.valueOf(ConsumeFood.custom_food_material());
        PersistentDataContainer get_item_id = get_item_meta.getPersistentDataContainer();
        boolean custom_id = get_item_id.has(new NamespacedKey(ConsumeFood.getPlugin(), "custom_id"), PersistentDataType.STRING);
        if (custom_id && get_material != Material.PLAYER_HEAD) {
            ArrayList<String> consume_food_list = new ArrayList<>(Arrays.asList("APPLE", "BEEF", "BEETROOT", "BAKED_POTATO", "BREAD", "BEETROOT_SOUP", "CHICKEN", "COD", "CARROT", "COOKED_CHICKEN", "COOKED_RABBIT",
                    "COOKED_PORKCHOP", "COOKED_BEEF", "COOKED_MUTTON", "COOKED_COD", "COOKED_SALMON", "COOKIE", "DRIED_KELP", "GLOW_BERRIES", "GOLDEN_CARROT", "HONEY_BOTTLE",
                    "MUTTON", "MELON_SLICE", "MUSHROOM_STEW", "POTATO", "PORKCHOP", "PUMPKIN_PIE", "RABBIT", "RABBIT_STEW", "SALMON", "SWEET_BERRIES", "TROPICAL_FISH",
                    "ENCHANTED_GOLDEN_APPLE", "GOLDEN_APPLE", "PUFFERFISH", "POISONOUS_POTATO", "ROTTEN_FLESH", "SPIDER_EYE"));
            String get_cooldown_type = ConsumeFood.customfooddata.getConfig().getString("Custom_Food_Cooldown.Type");
            if (get_cooldown_type == null) {
                get_cooldown_type = "disable";
            }
            if (get_cooldown_type.equals("global")) {
                long get_global_cooldown = ConsumeFood.customfooddata.getConfig().getLong("Custom_Food_Cooldown.Global_Cooldown");
                if (consume_food_list.contains(consume_item)) {
                    if (custom_food_global_cooldown.containsKey(player.getUniqueId())) {
                        if (custom_food_global_cooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
                            String cooldown_path = ConsumeFood.plugin.getmessageconfig().getString("custom_food_global_cooldown");
                            long global_timeleft = (custom_food_global_cooldown.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
                            if (cooldown_path != null) {
                                cooldown_path = cooldown_path.replaceAll("%custom_food_global_time_left%", String.valueOf(global_timeleft));
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldown_path));
                            }
                            e.setCancelled(true);
                            return;
                        }
                    }
                    custom_food_global_cooldown.put(player.getUniqueId(), System.currentTimeMillis() + (get_global_cooldown * 1000));
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
                                List<String> command_list = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".commands");
                                if (!command_list.isEmpty()) {
                                    for (String get_command_list : command_list) {
                                        String [] commands = get_command_list.split(":");
                                        String sender = commands[0];
                                        String command = commands[1];
                                        if (sender.equals("player")) {
                                            String replace_command = command.replaceAll("%player%", player.getName());
                                            Bukkit.getServer().dispatchCommand(player, replace_command);
                                        } else if (sender.equals("console")) {
                                            String replace_command = command.replaceAll("%player%", player.getName());
                                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), replace_command);
                                        }
                                    }
                                }
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
                        } else if (off_hand_material != Material.AIR) {
                            if (player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                                e.setCancelled(true);
                                player.setFoodLevel(player.getFoodLevel() + food_level);
                                player.setSaturation(player.getSaturation() + saturation);
                                player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
                                List<String> command_list = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".commands");
                                if (!command_list.isEmpty()) {
                                    for (String get_command_list : command_list) {
                                        String [] commands = get_command_list.split(":");
                                        String sender = commands[0];
                                        String command = commands[1];
                                        if (sender.equals("player")) {
                                            String replace_command = command.replaceAll("%player%", player.getName());
                                            Bukkit.getServer().dispatchCommand(player, replace_command);
                                        } else if (sender.equals("console")) {
                                            String replace_command = command.replaceAll("%player%", player.getName());
                                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), replace_command);
                                        }
                                    }
                                }
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
            } else if (get_cooldown_type.equals("personal")) {
                if (consume_food_list.contains(consume_item)) {
                    ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
                    Material main_hand_material = player.getInventory().getItemInMainHand().getType();
                    Material off_hand_material = player.getInventory().getItemInOffHand().getType();
                    for (String internal_name : customfoodlist) {
                        if (get_item_id.has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                            long get_personal_cooldown = ConsumeFood.customfooddata.getConfig().getLong("Custom_Food." + internal_name + ".cooldown");
                            String cooldown_key = player.getUniqueId() + internal_name;
                            if (custom_food_personal_cooldown.containsKey(cooldown_key)) {
                                if (custom_food_personal_cooldown.get(cooldown_key) > System.currentTimeMillis()) {
                                    String cooldown_path = ConsumeFood.plugin.getmessageconfig().getString("custom_food_personal_cooldown");
                                    String custom_food_name = ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".name");
                                    long timeleft = (custom_food_personal_cooldown.get(cooldown_key) - System.currentTimeMillis()) / 1000;
                                    if (cooldown_path != null) {
                                        cooldown_path = cooldown_path.replaceAll("%custom_food_personal_time_left%", String.valueOf(timeleft));
                                        if (custom_food_name != null) {
                                            cooldown_path = cooldown_path.replaceAll("%custom_food_name%", custom_food_name);
                                        }
                                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldown_path));
                                    }
                                    e.setCancelled(true);
                                    return;
                                }
                            }
                            custom_food_personal_cooldown.put(cooldown_key, System.currentTimeMillis() + (get_personal_cooldown * 1000));
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
                                    List<String> command_list = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".commands");
                                    if (!command_list.isEmpty()) {
                                        for (String get_command_list : command_list) {
                                            String [] commands = get_command_list.split(":");
                                            String sender = commands[0];
                                            String command = commands[1];
                                            if (sender.equals("player")) {
                                                String replace_command = command.replaceAll("%player%", player.getName());
                                                Bukkit.getServer().dispatchCommand(player, replace_command);
                                            } else if (sender.equals("console")) {
                                                String replace_command = command.replaceAll("%player%", player.getName());
                                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), replace_command);
                                            }
                                        }
                                    }
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
                            } else if (off_hand_material != Material.AIR) {
                                if (player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                                    e.setCancelled(true);
                                    player.setFoodLevel(player.getFoodLevel() + food_level);
                                    player.setSaturation(player.getSaturation() + saturation);
                                    player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
                                    List<String> command_list = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".commands");
                                    if (!command_list.isEmpty()) {
                                        for (String get_command_list : command_list) {
                                            String [] commands = get_command_list.split(":");
                                            String sender = commands[0];
                                            String command = commands[1];
                                            if (sender.equals("player")) {
                                                String replace_command = command.replaceAll("%player%", player.getName());
                                                Bukkit.getServer().dispatchCommand(player, replace_command);
                                            } else if (sender.equals("console")) {
                                                String replace_command = command.replaceAll("%player%", player.getName());
                                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), replace_command);
                                            }
                                        }
                                    }
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
                }
            } else if (get_cooldown_type.equals("disable")) {
                if (consume_food_list.contains(consume_item)) {
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
                                List<String> command_list = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".commands");
                                if (!command_list.isEmpty()) {
                                    for (String get_command_list : command_list) {
                                        String [] commands = get_command_list.split(":");
                                        String sender = commands[0];
                                        String command = commands[1];
                                        if (sender.equals("player")) {
                                            String replace_command = command.replaceAll("%player%", player.getName());
                                            Bukkit.getServer().dispatchCommand(player, replace_command);
                                        } else if (sender.equals("console")) {
                                            String replace_command = command.replaceAll("%player%", player.getName());
                                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), replace_command);
                                        }
                                    }
                                }
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
                        } else if (off_hand_material != Material.AIR) {
                            if (player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                                e.setCancelled(true);
                                player.setFoodLevel(player.getFoodLevel() + food_level);
                                player.setSaturation(player.getSaturation() + saturation);
                                player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
                                List<String> command_list = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".commands");
                                if (!command_list.isEmpty()) {
                                    for (String get_command_list : command_list) {
                                        String [] commands = get_command_list.split(":");
                                        String sender = commands[0];
                                        String command = commands[1];
                                        if (sender.equals("player")) {
                                            String replace_command = command.replaceAll("%player%", player.getName());
                                            Bukkit.getServer().dispatchCommand(player, replace_command);
                                        } else if (sender.equals("console")) {
                                            String replace_command = command.replaceAll("%player%", player.getName());
                                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), replace_command);
                                        }
                                    }
                                }
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
