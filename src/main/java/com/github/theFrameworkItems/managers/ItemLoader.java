package com.github.theFrameworkItems.managers;

import com.github.theFramework.TheFramework;
import com.github.theFramework.managers.EnumManager;
import com.github.theFramework.managers.FileReader;
import com.github.theFramework.managers.TextManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
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
		File pluginFolder = plugin.getDataFolder();
		File itemsFolder = new File(pluginFolder, "items");

		if (!itemsFolder.exists()) {
			itemsFolder.mkdirs();
		}

		for (String fileKey : FileReader.getFileKeys()) {
			if (!fileKey.startsWith("items/")) continue;

			YamlConfiguration itemsConfig = FileReader.getFile(fileKey);
			if (itemsConfig == null) continue;

			for (String itemId : itemsConfig.getKeys(false)) {
				itemId = itemId.toLowerCase();
				CustomItem item = parseItem(itemsConfig, itemId);
				STORAGE.put(itemId, item);
			}
		}

		return STORAGE;
	}

	private static CustomItem parseItem(YamlConfiguration itemsConfig, String itemId) {
		String name = itemsConfig.getString(itemId + ".name", "Custom Item");
		int customModelData = itemsConfig.getInt(itemId + ".custom-model-data", 0);

		String materialStr = itemsConfig.getString(itemId + ".material", "STICK");
		Material material = EnumManager.get(Material.class, materialStr, Material.STICK);

		double luck = itemsConfig.getDouble(itemId + ".luck", 0);
		int dupeLevel = itemsConfig.getInt(itemId + ".dupe.level", 0);
		double dupeChance = itemsConfig.getDouble(itemId + ".dupe.chance", 0.0);
		int durability = itemsConfig.getInt(itemId + ".durability");

		String slotStr = itemsConfig.getString(itemId + ".slot", "HAND").toUpperCase();
		EquipmentSlotGroup slot = EquipmentSlotGroup.getByName(slotStr);

		String type = itemsConfig.getString(itemId + ".type", "");
		String rarity = itemsConfig.getString(itemId + ".rarity", "");

		Map<Attribute, Double> attributes = new LinkedHashMap<>();
		ConfigurationSection attrSection = itemsConfig.getConfigurationSection(itemId + ".attributes");

		if (attrSection != null) {
			for (String attrKey : attrSection.getKeys(false)) {
				NamespacedKey key = TheFramework.getNamespacedKey(attrKey);
				Attribute attribute = Registry.ATTRIBUTE.get(key);

				if (attribute != null) {
					double value = attrSection.getDouble(attrKey, 0.0);
					attributes.put(attribute, value);
				}
			}
		}

		List<String> loreStrings = itemsConfig.getStringList(itemId + ".lore");
		List<Component> lore = loreStrings.isEmpty() ? null : loreStrings.stream()
			.map(TextManager::format)
			.collect(Collectors.toList());

		return new CustomItem(itemId, name, customModelData, material,
			luck, dupeLevel, dupeChance, durability,
			slot, type, rarity, attributes, lore);
	}
}
