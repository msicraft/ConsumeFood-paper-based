package com.msicraft.consumefood.events;

import com.msicraft.consumefood.ConsumeFood;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
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

    private final Map<String, Long> custom_food_personal_cooldown = new HashMap<String, Long>();
    private final Map<UUID, Long> custom_food_global_cooldown = new HashMap<UUID, Long>();

    private final Map<String, String> check_eating = new HashMap<String,String>();


    Random randomchance = new Random();


    private final ArrayList<String> consume_food_list = new ArrayList<>(Arrays.asList("APPLE", "BEEF", "BEETROOT", "BAKED_POTATO", "BREAD", "BEETROOT_SOUP", "CHICKEN", "COD", "CARROT", "COOKED_CHICKEN", "COOKED_RABBIT",
            "COOKED_PORKCHOP", "COOKED_BEEF", "COOKED_MUTTON", "COOKED_COD", "COOKED_SALMON", "COOKIE", "DRIED_KELP", "GLOW_BERRIES", "GOLDEN_CARROT", "HONEY_BOTTLE",
            "MUTTON", "MELON_SLICE", "MUSHROOM_STEW", "POTATO", "PORKCHOP", "PUMPKIN_PIE", "RABBIT", "RABBIT_STEW", "SALMON", "SWEET_BERRIES", "TROPICAL_FISH",
            "ENCHANTED_GOLDEN_APPLE", "GOLDEN_APPLE", "PUFFERFISH", "POISONOUS_POTATO", "ROTTEN_FLESH", "SPIDER_EYE"));


    @EventHandler
    public void Custom_Food_interact(PlayerInteractEvent e) {
        boolean max_consumable = ConsumeFood.customfooddata.getConfig().getBoolean("Custom_Food_Max_Consumable.Enabled");
        Player player = e.getPlayer();
        int max_food_level = plugin.getConfig().getInt("MaxSetting.FoodLevel");
        float max_saturation = (float) plugin.getConfig().getDouble("MaxSetting.Saturation");
        String max_foodlevel_path = ConsumeFood.plugin.getmessageconfig().getString("max_food_level");
        String max_saturation_path = ConsumeFood.plugin.getmessageconfig().getString("max_saturation");
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
        String custom_material_list = String.valueOf(ConsumeFood.custom_food_material());
        String get_cooldown_type = ConsumeFood.customfooddata.getConfig().getString("Custom_Food_Cooldown.Type");
        if (get_cooldown_type == null) {
            get_cooldown_type = "disable";
        }
        if (max_consumable) {
            //Consume food level is 20 or higher
            if (e.getAction() == Action.RIGHT_CLICK_AIR && player.getFoodLevel() >= 20 && check_custom_id) {
                if (get_cooldown_type.equals("global")) {
                    long get_global_cooldown = ConsumeFood.customfooddata.getConfig().getLong("Custom_Food_Cooldown.Global_Cooldown");
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
                        Material config_material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
                        int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                        float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                        List<String> potioneffectlist = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".potion-effect");
                        double potioneffect_chance = ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".Chance");
                        if (e.getMaterial() == Material.PLAYER_HEAD && e.getMaterial() == config_material) {
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
                            }
                        } else if (e.getMaterial() != Material.PLAYER_HEAD && e.getMaterial() == config_material) {
                            if (e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
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
                                } else {
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
                        }
                    }
                } else if (get_cooldown_type.equals("personal")) {
                    ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
                    Material main_hand_material = player.getInventory().getItemInMainHand().getType();
                    Material off_hand_material = player.getInventory().getItemInOffHand().getType();
                    for (String internal_name : customfoodlist) {
                        if (e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                            long get_personal_cooldown = ConsumeFood.customfooddata.getConfig().getLong("Custom_Food." + internal_name + ".Cooldown");
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
                            Material config_material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
                            int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                            float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                            List<String> potioneffectlist = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".potion-effect");
                            double potioneffect_chance = ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".Chance");
                            if (e.getMaterial() == Material.PLAYER_HEAD && e.getMaterial() == config_material) {
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
                                            String[] commands = get_command_list.split(":");
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
                                }
                            }  else if (e.getMaterial() != Material.PLAYER_HEAD && e.getMaterial() == config_material) {
                                if (e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
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
                                    } else {
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
                            }
                        }
                    }
                } else if (get_cooldown_type.equals("disable")) {
                    ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
                    Material main_hand_material = player.getInventory().getItemInMainHand().getType();
                    Material off_hand_material = player.getInventory().getItemInOffHand().getType();
                    for (String internal_name : customfoodlist) {
                        Material config_material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
                        int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                        float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                        List<String> potioneffectlist = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".potion-effect");
                        double potioneffect_chance = ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".Chance");
                        if (e.getMaterial() == Material.PLAYER_HEAD && e.getMaterial() == config_material) {
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
                            }
                        } else if (e.getMaterial() != Material.PLAYER_HEAD && e.getMaterial() == config_material) {
                            if (e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
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
                                } else {
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
        } else {
            //Consume food level is 19 or lower
            if (e.getAction() == Action.RIGHT_CLICK_AIR && player.getFoodLevel() < 20 && check_custom_id) {
                if (get_cooldown_type.equals("global")) {
                    long get_global_cooldown = ConsumeFood.customfooddata.getConfig().getLong("Custom_Food_Cooldown.Global_Cooldown");
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
                        Material config_material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
                        int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                        float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                        List<String> potioneffectlist = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".potion-effect");
                        double potioneffect_chance = ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".Chance");
                        if (e.getMaterial() == Material.PLAYER_HEAD && e.getMaterial() == config_material) {
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
                            }
                        } else if (!(consume_food_list.contains(e.getMaterial().name().toUpperCase())) && e.getMaterial() != Material.PLAYER_HEAD && e.getMaterial() == config_material) {
                            if (e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
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
                                } else {
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
                        }
                    }
                } else if (get_cooldown_type.equals("personal")) {
                    ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
                    Material main_hand_material = player.getInventory().getItemInMainHand().getType();
                    Material off_hand_material = player.getInventory().getItemInOffHand().getType();
                    for (String internal_name : customfoodlist) {
                        if (e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
                            long get_personal_cooldown = ConsumeFood.customfooddata.getConfig().getLong("Custom_Food." + internal_name + ".Cooldown");
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
                            Material config_material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
                            int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                            float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                            List<String> potioneffectlist = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".potion-effect");
                            double potioneffect_chance = ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".Chance");
                            if (e.getMaterial() == Material.PLAYER_HEAD && e.getMaterial() == config_material) {
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
                                            String[] commands = get_command_list.split(":");
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
                                }
                            }  else if (!(consume_food_list.contains(e.getMaterial().name().toUpperCase())) && e.getMaterial() != Material.PLAYER_HEAD && e.getMaterial() == config_material) {
                                if (e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
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
                                    } else {
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
                            }
                        }
                    }
                } else if (get_cooldown_type.equals("disable")) {
                    ArrayList<String> customfoodlist = new ArrayList<>(ConsumeFood.custom_food_list());
                    Material main_hand_material = player.getInventory().getItemInMainHand().getType();
                    Material off_hand_material = player.getInventory().getItemInOffHand().getType();
                    for (String internal_name : customfoodlist) {
                        Material config_material = Material.valueOf(ConsumeFood.customfooddata.getConfig().getString("Custom_Food." + internal_name + ".material"));
                        int food_level = ConsumeFood.customfooddata.getConfig().getInt("Custom_Food." + internal_name + ".foodlevel");
                        float saturation = (float) ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".saturation");
                        List<String> potioneffectlist = ConsumeFood.customfooddata.getConfig().getStringList("Custom_Food." + internal_name + ".potion-effect");
                        double potioneffect_chance = ConsumeFood.customfooddata.getConfig().getDouble("Custom_Food." + internal_name + ".Chance");
                        if (e.getMaterial() == Material.PLAYER_HEAD && e.getMaterial() == config_material) {
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
                            }
                        } else if (!(consume_food_list.contains(e.getMaterial().name().toUpperCase())) &&e.getMaterial() != Material.PLAYER_HEAD && e.getMaterial() == config_material) {
                            if (e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ConsumeFood.getPlugin(), internal_name), PersistentDataType.STRING)) {
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
                                } else {
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
