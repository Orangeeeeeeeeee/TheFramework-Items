package com.github.theFrameworkItems.managers;

import com.github.theFramework.TheFramework;
import com.github.theFramework.managers.Placeholders;
import com.github.theFramework.managers.TextManager;
import com.github.theFrameworkItems.Config;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
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
import java.util.stream.Collectors;

@Getter
public class CustomItem {
	private final String id;
	private final String name;
	private final List<Component> lore;
	private final Material material;
	private final int customModelData;
	private final Map<Enchantment, Integer> enchantments;
	private final Map<Attribute, Double> attributes;
	private final int durability;
	private final EquipmentSlotGroup slot;
	private final String type;
	private final String rarity;
	private final double luck;
	private final int dupeLevel;
	private final double dupeChance;

	public CustomItem(String id, String name, List<Component> lore, Material material,
	                  int customModelData, Map<Enchantment, Integer> enchantments, Map<Attribute, Double> attributes, int durability,
	                  EquipmentSlotGroup slot, String type, String rarity, double luck, int dupeLevel, double dupeChance
	) {
		this.id = id;
		this.name = name;
		this.lore = lore;
		this.material = material;
		this.customModelData = customModelData;
		this.enchantments = enchantments;
		this.attributes = attributes;
		this.durability = durability;
		this.slot = slot;
		this.type = type;
		this.rarity = rarity;
		this.luck = luck;
		this.dupeLevel = dupeLevel;
		this.dupeChance = dupeChance;
	}

	public int getEnchantment(Enchantment enchantment) {
		return enchantments.getOrDefault(enchantment, 0);
	}
	public double getAttribute(Attribute attribute) {
		return attributes.getOrDefault(attribute, 0.0);
	}

	public ItemStack create(int amount) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		Plugin plugin = TheFramework.asPlugin();
		PersistentDataContainer metaData = meta.getPersistentDataContainer();
		NamespacedKey tag = new NamespacedKey(plugin, "custom_item");
		metaData.set(tag, PersistentDataType.STRING, id);

		meta.displayName(TextManager.format(Config.Format.NAME.replace("%name%", name)));

		boolean unbreakable = durability < 0;
		meta.setUnbreakable(unbreakable);

		if (lore != null && !lore.isEmpty() && "HIDE".equalsIgnoreCase(TextManager.toString(lore.getFirst()))) {
			meta.setHideTooltip(true);
			item.setItemMeta(meta);
			return item;
		}

		meta.addItemFlags(
			ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
			ItemFlag.HIDE_STORED_ENCHANTS,
			ItemFlag.HIDE_UNBREAKABLE,
			ItemFlag.HIDE_ATTRIBUTES,
			ItemFlag.HIDE_ARMOR_TRIM,
			ItemFlag.HIDE_PLACED_ON,
			ItemFlag.HIDE_ENCHANTS,
			ItemFlag.HIDE_DESTROYS,
			ItemFlag.HIDE_DYE
		);

		enchantments.forEach((enchant, level) -> meta.addEnchant(enchant, level, true));

		Multimap<Attribute, AttributeModifier> attributeModifiers = HashMultimap.create();
		attributes.forEach((attribute, value) -> {
			AttributeModifier modifier = new AttributeModifier(
				new NamespacedKey(plugin, attribute.toString()),
				value,
				AttributeModifier.Operation.ADD_NUMBER,
				slot
			);
			attributeModifiers.put(attribute, modifier);
		});
		meta.setAttributeModifiers(attributeModifiers);

		meta.lore(lore != null ? lore : generateLore(unbreakable));
		item.setItemMeta(meta);
		return item;
	}
	private List<Component> generateLore(boolean unbreakable) {
		List<Component> loreLines = new ArrayList<>();
		List<String> formatLore = Config.Format.LORE;

		if (!formatLore.isEmpty()) {
			Map<String, String> placeholders = Map.of(
				"%type%", Config.Format.TYPE.replace("%type%", type),
				"%rarity%", getRarityText(),
				"%unbreakable%", unbreakable ? Config.Format.UNBREAKABLE : "",
				"%luck%", getLuckText(),
				"%dupe_level%", getDupeLevelText(),
				"%dupe_chance%", getDupeChanceText()
			);

			for (String line : formatLore) {
				if (line.isEmpty()) continue;

				if ("%attributes%".equals(line)) {
					loreLines.addAll(getAttributeText());
				} else if ("%enchantments%".equals(line)) {
					loreLines.addAll(getEnchantmentText());
				} else {
					String processedLine = Placeholders.apply(line, this, placeholders);
					for (String subLine : processedLine.split("%new_line%"))
						if (!subLine.isEmpty()) loreLines.add(TextManager.format(subLine));
				}
			}
		}

		return loreLines;
	}

	private List<Component> getEnchantmentText() {
		List<Component> enchantLore = enchantments.entrySet().stream()
			.map(entry -> {
				String format = getTextFromList(Config.Format.ENCHANTS, Config.Format.ENCHANTS_DEFAULT, entry.getKey().getKey().getKey());
				return TextManager.format(format.replace("%level%", String.valueOf(entry.getValue())));
			})
			.collect(Collectors.toList());

		if (Config.Format.ENCHANTS_SPACE && !enchantLore.isEmpty()) enchantLore.add(Component.empty());
		return enchantLore;
	}
	private List<Component> getAttributeText() {
		List<Component> attributeLore = attributes.entrySet().stream()
			.map(entry -> {
				String format = getTextFromList(Config.Format.ATTRIBUTES, Config.Format.ATTRIBUTES_DEFAULT, entry.getKey().toString());
				return TextManager.format(format.replace("%amount%", String.valueOf(entry.getValue())));
			})
			.collect(Collectors.toList());

		if (Config.Format.ATTRIBUTES_SPACE && !attributeLore.isEmpty()) attributeLore.add(Component.empty());
		return attributeLore;
	}

	private String getRarityText() {
		return Config.RARITY.getOrDefault(rarity, "");
	}
	private String getLuckText() {
		if (luck == 0.0) return "";
		return getTextFromList(Config.Format.LUCK_VALUES, Config.Format.LUCK_DEFAULT, luck)
			.replace("%luck%", String.valueOf(luck));
	}
	private String getDupeLevelText() {
		if (dupeLevel == 0) return "";
		return getTextFromList(Config.Format.DUPE_LEVEL, Config.Format.DUPE_LEVEL_DEFAULT, dupeLevel)
			.replace("%level%", String.valueOf(dupeLevel));
	}
	private String getDupeChanceText() {
		if (dupeChance == 0) return "";
		return getTextFromList(Config.Format.DUPE_CHANCE, Config.Format.DUPE_CHANCE_DEFAULT, dupeChance)
			.replace("%chance%", String.valueOf(dupeChance));
	}

	private String getTextFromList(Map<String, String> section, String def, String value) {
		return section.getOrDefault(value.toUpperCase(), def);
	}
	private String getTextFromList(Map<Integer, String> section, String def, double value) {
		return section.entrySet().stream()
			.filter(entry -> value >= entry.getKey())
			.map(Map.Entry::getValue)
			.reduce((first, second) -> second)
			.orElse(def);
	}
}