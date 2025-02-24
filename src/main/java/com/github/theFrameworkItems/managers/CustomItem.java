package com.github.theFrameworkItems.managers;

import com.github.theFramework.TheFramework;
import com.github.theFramework.managers.FileReader;
import com.github.theFramework.managers.Placeholders;
import com.github.theFramework.managers.TextManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class CustomItem {
	private final String id;
	private final int customModelData;
	private final String name;
	private final Material material;
	private final double luck;
	private final int dupeLevel;
	private final double dupeChance;
	private final int durability;
	private final EquipmentSlotGroup slot;
	private final String type;
	private final String rarity;
	private final Map<Attribute, Double> attributes;
	private final List<Component> lore;

	public CustomItem(String id, String name, int customModelData, Material material,
	                  double luck, int dupeLevel, double dupeChance, int durability,
	                  EquipmentSlotGroup slot, String type, String rarity,
	                  Map<Attribute, Double> attributes, List<Component> lore
	) {
		this.id = id;
		this.name = name;
		this.customModelData = customModelData;
		this.luck = luck;
		this.dupeLevel = dupeLevel;
		this.dupeChance = dupeChance;
		this.material = material;
		this.durability = durability;
		this.slot = slot;
		this.type = type;
		this.rarity = rarity;
		this.attributes = attributes;
		this.lore = lore;
	}

	public double getAttribute(Attribute attribute) {
		return attributes.get(attribute);
	}

	private String getRarityText() {
		if (rarity == null) return "";
		return FileReader.getString("config", "items.rarity." + rarity, "");
	}
	private String getLuckText() {
		if (luck == 0.0) return "";
		return getTextFromList(FileReader.getSection("config", "items.format.luck"),
			FileReader.getString("config", "items.format.luck-default", ""),
			luck).replace("%value%", String.valueOf(luck));
	}
	private String getDupeText() {
		if (dupeLevel == 0.0) return "";
		String text = FileReader.getString("config", "items.format.dupe.text", "%level% %chance%");

		String levelFormat = getTextFromList(
			FileReader.getSection("config", "items.format.dupe.level"),
			FileReader.getString("config", "items.format.dupe.level-default", ""),
			dupeLevel
		).replace("%value%", String.valueOf(dupeLevel));

		String chanceFormat = getTextFromList(
			FileReader.getSection("config", "items.format.dupe.chance"),
			FileReader.getString("config", "items.format.dupe.chance-default", ""),
			dupeChance
		).replace("%value%", String.valueOf(dupeChance));

		return Placeholders.apply(text, this, Map.of(
			"%level%", levelFormat,
			"%chance%", chanceFormat
		));
	}
	private List<Component> getAttributeText() {
		List<Component> attrLines = new ArrayList<>();

		attributes.forEach((key, value) -> {
			String format = getTextFromList(FileReader.getSection("config", "items.format.attributes"),
				FileReader.getString("config", "items.format.attributes-default", ""), key.toString());

			String text = format.replace("%value%", String.valueOf(value));
			attrLines.add(TextManager.format(text));
		});

		return attrLines;
	}

	private String getTextFromList(ConfigurationSection section, String def, double value) {
		if (section == null) return null;
		String text = null;

		for (String key : section.getKeys(false)) {
			int required = Integer.parseInt(key);
			if (value >= required)
				text = section.getString(key);
		}

		return text == null ? def : text;
	}
	private String getTextFromList(ConfigurationSection section, String def, String value) {
		if (section == null) return null;

		for (String key : section.getKeys(false)) {
			if (value.equalsIgnoreCase(key))
				return section.getString(key);
		}

		return def;
	}

	public ItemStack create(int amount) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta meta = item.getItemMeta();
		YamlConfiguration config = FileReader.getFile("config");

		if (meta == null)
			return item;

		Plugin plugin = TheFramework.asPlugin();
		PersistentDataContainer metaData = meta.getPersistentDataContainer();
		NamespacedKey tag = new NamespacedKey(plugin, "custom_item");
		metaData.set(tag, PersistentDataType.STRING, id);

		String nameFormat = config.getString("items.format.name", "%name%")
			.replace("%name%", name);
		meta.displayName(TextManager.format(nameFormat));

		boolean unbreakable = durability < 0;
		if (unbreakable)
			meta.setUnbreakable(true);

		if (lore != null && "HIDE".equalsIgnoreCase(TextManager.toString(lore.getFirst()))) {
			meta.setHideTooltip(true);
			item.setItemMeta(meta);
			return item;
		}

		meta.addItemFlags(
			ItemFlag.HIDE_ENCHANTS,
			ItemFlag.HIDE_ATTRIBUTES,
			ItemFlag.HIDE_DESTROYS,
			ItemFlag.HIDE_DYE,
			ItemFlag.HIDE_PLACED_ON,
			ItemFlag.HIDE_STORED_ENCHANTS,
			ItemFlag.HIDE_UNBREAKABLE,
			ItemFlag.HIDE_ARMOR_TRIM,
			ItemFlag.HIDE_ADDITIONAL_TOOLTIP
		);

		Multimap<Attribute, AttributeModifier> attributeModifiers = HashMultimap.create();
		for (Map.Entry<Attribute, Double> entry : attributes.entrySet()) {
			Attribute key = entry.getKey();
			double value = entry.getValue();

			AttributeModifier modifier = new AttributeModifier(
				new NamespacedKey(plugin, entry.getKey().toString()),
				value,
				AttributeModifier.Operation.ADD_NUMBER,
				slot
			);
			attributeModifiers.put(key, modifier);
		}
		meta.setAttributeModifiers(attributeModifiers);

		if (lore != null) meta.lore(lore);
		else {
			List<Component> lore = new ArrayList<>();
			List<String> formatLore = config.getStringList("items.format.lore");

			if (!formatLore.isEmpty()) {
				Map<String, String> placeholders = Map.of(
					"%rarity%", getRarityText(),
					"%luck%", getLuckText(),
					"%dupe%", getDupeText(),
					"%unbreakable%", unbreakable ? config.getString("items.format.unbreakable", "&3Unbreakable") : "",
					"%type%", config.getString("items.format.type", "").replace("%value%", type)
				);

				for (String line : formatLore) {
					if (line.isEmpty()) continue;

					if ("%attributes%".equals(line)) {
						lore.addAll(getAttributeText());
					} else {
						String processedLine = Placeholders.apply(line, this, placeholders);
						if (!processedLine.isEmpty()) lore.add(TextManager.format(processedLine));
					}
				}
			}

			meta.lore(lore);
		}

		item.setItemMeta(meta);
		return item;
	}
}