package com.github.theFrameworkItems.managers;

import com.github.theFramework.TheFramework;
import com.github.theFramework.managers.FileReader;
import com.github.theFramework.managers.TextManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemLoader {

	public static Map<String, CustomItem> load() {
		Map<String, CustomItem> STORAGE = new HashMap<>();

		Plugin plugin = TheFramework.asPlugin();
		File itemsFolder = new File(plugin.getDataFolder(), "items");

		if (!itemsFolder.exists() && !itemsFolder.mkdirs()) {
			TextManager.console(TextManager.format("&c[Items Loader] Could not create items folder!"));
			return STORAGE;
		}

		for (String fileKey : FileReader.getFileKeys()) {
			if (!fileKey.startsWith("items/")) continue;

			YamlConfiguration itemsConfig = FileReader.getFile(fileKey);
			if (itemsConfig == null) {
				TextManager.console(TextManager.format("&c[Items Loader] Failed to load file: " + fileKey));
				continue;
			}

			for (String itemId : itemsConfig.getKeys(false)) {
				itemId = itemId.toLowerCase();
				CustomItem item = parseItem(itemsConfig, itemId);
				STORAGE.put(itemId, item);
			}
		}

		TextManager.console(TextManager.format("&b[Items Loader] Loaded " + STORAGE.size() + " custom items"));
		return STORAGE;
	}

	private static CustomItem parseItem(YamlConfiguration itemsConfig, String itemId) {
		String name = itemsConfig.getString(itemId + ".name", "Custom Item");
		int customModelData = itemsConfig.getInt(itemId + ".custom-model-data", 0);

		Material material = Material.matchMaterial(itemsConfig.getString(itemId + ".material", "STICK"));
		if (material == null) material = Material.STICK;

		double luck = itemsConfig.getDouble(itemId + ".luck", 0);
		int dupeLevel = itemsConfig.getInt(itemId + ".dupe.level", 0);
		double dupeChance = itemsConfig.getDouble(itemId + ".dupe.chance", 0.0);
		int durability = itemsConfig.getInt(itemId + ".durability", -1);

		String slotStr = itemsConfig.getString(itemId + ".slot", "HAND").toUpperCase();
		EquipmentSlotGroup slot = EquipmentSlotGroup.getByName(slotStr);

		String type = itemsConfig.getString(itemId + ".type", "");
		String rarity = itemsConfig.getString(itemId + ".rarity", "");

		Map<Enchantment, Integer> enchantments = loadEnchantments(itemsConfig.getConfigurationSection(itemId + ".enchants"));
		Map<Attribute, Double> attributes = loadAttributes(itemsConfig.getConfigurationSection(itemId + ".attributes"));

		List<Component> lore = itemsConfig.contains(itemId + ".lore")
			? itemsConfig.getStringList(itemId + ".lore").stream()
			.map(TextManager::format)
			.collect(Collectors.toList())
			: null;

		return new CustomItem(itemId, name, lore, material,
			customModelData, enchantments, attributes, durability,
			slot, type, rarity, luck, dupeLevel, dupeChance);
	}

	private static Map<Attribute, Double> loadAttributes(ConfigurationSection section) {
		Map<Attribute, Double> attributes = new LinkedHashMap<>();
		if (section == null) return attributes;

		for (String attrKey : section.getKeys(false)) {
			try {
				NamespacedKey key = TheFramework.getNamespacedKey(attrKey.toUpperCase());
				Attribute attribute = Registry.ATTRIBUTE.get(key);
				double value = section.getDouble(attrKey, 0.0);
				attributes.put(attribute, value);
			} catch (IllegalArgumentException e) {
				TextManager.console(TextManager.format("&c[Items Loader] Invalid attribute: " + attrKey));
			}
		}
		return attributes;
	}
	private static Map<Enchantment, Integer> loadEnchantments(ConfigurationSection section) {
		Map<Enchantment, Integer> enchantments = new HashMap<>();
		if (section == null) return enchantments;

		for (String enchKey : section.getKeys(false)) {
			try {
				NamespacedKey key = TheFramework.getNamespacedKey(enchKey.toUpperCase());
				Enchantment enchantment = Registry.ENCHANTMENT.get(key);
				if (enchantment == null) {
					TextManager.console(TextManager.format("&c[Items Loader] Invalid enchantment: " + key));
					continue;
				}

				int level = section.getInt(enchKey, 0);
				enchantments.put(enchantment, level);
			} catch (Exception e) {
				TextManager.console(TextManager.format("&c[Items Loader] Error loading enchantment: " + enchKey));
			}
		}
		return enchantments;
	}
}
