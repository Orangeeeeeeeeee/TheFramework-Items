package com.github.theFrameworkItems;

import com.github.theFramework.managers.FileReader;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Config {
	public static final YamlConfiguration SELF = FileReader.getFile("config");

	public static final List<String> ITEM_TYPES = SELF.getStringList("items.types");
	public static final Map<String, String> RARITY = SELF.getConfigurationSection("items.rarity") != null
		? SELF.getConfigurationSection("items.rarity").getValues(false).entrySet().stream()
		.collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())))
		: null;

	public static class Format {
		public static final String NAME = SELF.getString("items.format.name", "%name%");
		public static final List<String> LORE = SELF.getStringList("items.format.lore");

		public static final String TYPE = SELF.getString("items.format.type", "");
		public static final String UNBREAKABLE = SELF.getString("items.format.unbreakable", "");

		public static final boolean ENCHANTS_SPACE = SELF.getBoolean("items.format.enchants-space", true);
		public static final String ENCHANTS_DEFAULT = SELF.getString("items.format.enchants-default", "");
		public static final Map<String, String> ENCHANTS = SELF.getConfigurationSection("items.format.enchants") != null
			? SELF.getConfigurationSection("items.format.enchants").getValues(false).entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())))
			: null;

		public static final boolean ATTRIBUTES_SPACE = SELF.getBoolean("items.format.attributes-space", true);
		public static final String ATTRIBUTES_DEFAULT = SELF.getString("items.format.attributes-default", "");
		public static final Map<String, String> ATTRIBUTES = SELF.getConfigurationSection("items.format.attributes") != null
			? SELF.getConfigurationSection("items.format.attributes").getValues(false).entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())))
			: null;

		public static final String LUCK_DEFAULT = SELF.getString("items.format.luck-default", "");
		public static final Map<Integer, String> LUCK_VALUES = SELF.getConfigurationSection("items.format.luck") != null
			? SELF.getConfigurationSection("items.format.luck").getValues(false).entrySet().stream()
			.collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), e -> String.valueOf(e.getValue())))
			: null;

		public static final String DUPE_CHANCE_DEFAULT = SELF.getString("items.format.dupe.chance-default", "");
		public static final Map<Integer, String> DUPE_CHANCE = SELF.getConfigurationSection("items.format.dupe.chance") != null
			? SELF.getConfigurationSection("items.format.dupe.chance").getValues(false).entrySet().stream()
			.collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), e -> String.valueOf(e.getValue())))
			: null;

		public static final String DUPE_LEVEL_DEFAULT = SELF.getString("items.format.dupe.level-default", "");
		public static final Map<Integer, String> DUPE_LEVEL = SELF.getConfigurationSection("items.format.dupe.level") != null
			? SELF.getConfigurationSection("items.format.dupe.level").getValues(false).entrySet().stream()
			.collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), e -> String.valueOf(e.getValue())))
			: null;
	}
}
