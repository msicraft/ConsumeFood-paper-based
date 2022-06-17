package com.msicraft.consumefood.events;

import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Custom_Food_Interact_Event implements Listener {

    Plugin plugin = ConsumeFood.getPlugin(ConsumeFood.class);

    private final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();
    private final Map<UUID, String> custom_food_cooldown = new HashMap<UUID, String>();


    Random randomchance = new Random();


    @EventHandler
    public void Custom_Food_interact(PlayerInteractEvent e) {
        int max_food_level = plugin.getConfig().getInt("MaxSetting.FoodLevel");
        float max_saturation = (float) plugin.getConfig().getDouble("MaxSetting.Saturation");
        String max_foodlevel_path = ConsumeFood.plugin.getmessageconfig().getString("max_food_level");
        String max_saturation_path = ConsumeFood.plugin.getmessageconfig().getString("max_saturation");
        Player player = e.getPlayer();
        ItemStack get_item = e.getItem();
        ItemMeta get_item_meta = null;
        if (get_item != null) {
            get_item_meta = get_item.getItemMeta();
        }
        PersistentDataContainer get_item_data = null;
        if (get_item_meta != null) {
            get_item_data = get_item_meta.getPersistentDataContainer();
        }
        boolean check_custom_id = false;
        if (get_item_data != null) {
            check_custom_id = get_item_data.has(new NamespacedKey(ConsumeFood.getPlugin(), "custom_id"), PersistentDataType.STRING);
        }
        boolean max_consumable = ConsumeFood.customfooddata.getConfig().getBoolean("Custom_Food_Max_Consumable.Enabled");
        String custom_material_list = String.valueOf(ConsumeFood.custom_food_material());
        Material get_material = null;
        if (get_item != null) {
            get_material = get_item.getType();
        }
        //
        if (max_consumable) {
            //Consume food level is 20 or higher
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR) && check_custom_id && custom_material_list.contains(e.getItem().getType().name().toUpperCase()) && player.getFoodLevel() >= 20) {
                long get_cooldown = ConsumeFood.customfooddata.getConfig().getLong("Custom_Food_Max_Consumable.Cooldown");
                ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
                String cooldown_path = ConsumeFood.plugin.getmessageconfig().getString("custom_food_cooldown");
                //
                if (custom_food_cooldown.containsKey(player.getUniqueId())) {
                    String custom_name_cooldown = custom_food_cooldown.get(player.getUniqueId());
                }
                String name_cooldowns = e.getItem().getType().name().toUpperCase() + ":" + System.currentTimeMillis();
                custom_food_cooldown.put(player.getUniqueId(), e.getItem().getType().name().toUpperCase());
                if (cooldowns.containsKey(player.getUniqueId())) {
                    if (cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
                        long timeleft = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
                        if (cooldown_path != null) {
                            cooldown_path = cooldown_path.replaceAll("%custom_food_time_left%", String.valueOf(timeleft));
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldown_path));
                        }
                        return;
                    }
                }
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (get_cooldown * 1000));
                for (String internal_name : customfoodlist) {
                    Material config_material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
                    int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                    float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                    List<String> potioneffectlist = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".potion-effect");
                    double potioneffect_chance = ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".Chance");
                    Material main_hand_material = player.getInventory().getItemInMainHand().getType();
                    Material off_hand_material = player.getInventory().getItemInOffHand().getType();
                    if (get_material == Material.PLAYER_HEAD && config_material == Material.PLAYER_HEAD) {
                        SkullMeta skullmeta = (SkullMeta) get_item.getItemMeta();
                        UUID uuid = UUID.fromString(Objects.requireNonNull(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".uuid")));
                        UUID get_skull_uuid = Objects.requireNonNull(skullmeta.getPlayerProfile()).getId();
                        if (uuid.equals(get_skull_uuid) && main_hand_material != Material.AIR && player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                            player.setFoodLevel(player.getFoodLevel() + food_level);
                            player.setSaturation(player.getSaturation() + saturation);
                            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                            player.getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
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
                        }
                        if (uuid.equals(get_skull_uuid) && off_hand_material != Material.AIR && player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                            player.setFoodLevel(player.getFoodLevel() + food_level);
                            player.setSaturation(player.getSaturation() + saturation);
                            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                            player.getInventory().getItemInOffHand().setAmount((player.getInventory().getItemInOffHand().getAmount() - 1));
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
                        }
                        if (uuid.equals(get_skull_uuid)) {
                            break;
                        }
                    } else {
                        if (main_hand_material != Material.AIR && player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                            player.setFoodLevel(player.getFoodLevel() + food_level);
                            player.setSaturation(player.getSaturation() + saturation);
                            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                            player.getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
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
                        }
                        if (off_hand_material != Material.AIR && player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                            player.setFoodLevel(player.getFoodLevel() + food_level);
                            player.setSaturation(player.getSaturation() + saturation);
                            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                            player.getInventory().getItemInOffHand().setAmount((player.getInventory().getItemInOffHand().getAmount() - 1));
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
            } else {
                // Consume food level is 19 or less (Material player_head) (Max_consumable is true)
                if (e.getAction().equals(Action.RIGHT_CLICK_AIR) && check_custom_id && custom_material_list.contains(e.getItem().getType().name().toUpperCase()) && player.getFoodLevel() < 20) {
                    if (get_material == Material.PLAYER_HEAD) {
                        ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
                        for (String internal_name : customfoodlist) {
                            Material config_material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
                            int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                            float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                            List<String> potioneffectlist = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".potion-effect");
                            double potioneffect_chance = ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".Chance");
                            Material main_hand_material = player.getInventory().getItemInMainHand().getType();
                            Material off_hand_material = player.getInventory().getItemInOffHand().getType();
                            if (config_material == Material.PLAYER_HEAD) {
                                SkullMeta skullmeta = (SkullMeta) get_item.getItemMeta();
                                UUID uuid = UUID.fromString(Objects.requireNonNull(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".uuid")));
                                UUID get_skull_uuid = Objects.requireNonNull(skullmeta.getPlayerProfile()).getId();
                                if (uuid.equals(get_skull_uuid) && main_hand_material != Material.AIR && player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                                    player.setFoodLevel(player.getFoodLevel() + food_level);
                                    player.setSaturation(player.getSaturation() + saturation);
                                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                                    player.getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
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
                                                String[] effectlist = effectlistf.split(":");
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
                                if (uuid.equals(get_skull_uuid) && off_hand_material != Material.AIR && player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                                    player.setFoodLevel(player.getFoodLevel() + food_level);
                                    player.setSaturation(player.getSaturation() + saturation);
                                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                                    player.getInventory().getItemInOffHand().setAmount((player.getInventory().getItemInOffHand().getAmount() - 1));
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
                                                String[] effectlist = effectlistf.split(":");
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
                                if (uuid.equals(get_skull_uuid)) {
                                    break;
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
        } else {
            // Consume food level is 19 or less (Material player_head) (Max_consumable is false)
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR) && check_custom_id && custom_material_list.contains(e.getItem().getType().name().toUpperCase()) && player.getFoodLevel() < 20) {
                if (get_material == Material.PLAYER_HEAD) {
                    long get_cooldown = ConsumeFood.customfooddata.getConfig().getLong("Custom_Food_Max_Consumable.Cooldown");
                    String cooldown_path = ConsumeFood.plugin.getmessageconfig().getString("custom_food_cooldown");
                    if (cooldowns.containsKey(player.getUniqueId())) {
                        if (cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
                            long timeleft = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
                            if (cooldown_path != null) {
                                cooldown_path = cooldown_path.replaceAll("%custom_food_time_left%", String.valueOf(timeleft));
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldown_path));
                            }
                            return;
                        }
                    }
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (get_cooldown * 1000));
                    ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
                    for (String internal_name : customfoodlist) {
                        Material config_material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
                        int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                        float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                        List<String> potioneffectlist = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".potion-effect");
                        double potioneffect_chance = ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".Chance");
                        Material main_hand_material = player.getInventory().getItemInMainHand().getType();
                        Material off_hand_material = player.getInventory().getItemInOffHand().getType();
                        if (config_material == Material.PLAYER_HEAD) {
                            SkullMeta skullmeta = (SkullMeta) get_item.getItemMeta();
                            UUID uuid = UUID.fromString(Objects.requireNonNull(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".uuid")));
                            UUID get_skull_uuid = (Objects.requireNonNull(skullmeta.getPlayerProfile())).getId();
                            if (uuid.equals(get_skull_uuid) && main_hand_material != Material.AIR && player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                                player.setFoodLevel(player.getFoodLevel() + food_level);
                                player.setSaturation(player.getSaturation() + saturation);
                                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                                player.getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
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
                                            String[] effectlist = effectlistf.split(":");
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
                                if (uuid.equals(get_skull_uuid) && off_hand_material != Material.AIR && player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                                    player.setFoodLevel(player.getFoodLevel() + food_level);
                                    player.setSaturation(player.getSaturation() + saturation);
                                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                                    player.getInventory().getItemInOffHand().setAmount((player.getInventory().getItemInOffHand().getAmount() - 1));
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
                                                String[] effectlist = effectlistf.split(":");
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
                            if (uuid.equals(get_skull_uuid)) {
                                break;
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
