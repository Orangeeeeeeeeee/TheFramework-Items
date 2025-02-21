package me.mcplugins.theFrameworkItems.managers;

import me.mcplugins.theframework.TheFramework;
import me.mcplugins.theframework.managers.TextManager;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public class ItemManager {
	private static final Map<String, CustomItem> STORAGE = ItemLoader.load();

	public static CustomItem getItem(String id) {
		return STORAGE.get(id);
	}
	public static Map<String, CustomItem> getItems() {
		return STORAGE;
	}

	public static CustomItem getItem(ItemStack item) {
		if (item == null || !item.hasItemMeta()) return null;

		PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
		NamespacedKey key = new NamespacedKey(TheFramework.asPlugin(), "custom_item");

		String itemId = data.get(key, PersistentDataType.STRING);
		return itemId == null ? null : getItem(itemId);
	}

	public static int getFromInventory(Inventory inventory, CustomItem search) {
		int amount = 0;

		for (ItemStack item : inventory.getContents()) {
			if (item == null || !item.hasItemMeta()) continue;

			PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
			NamespacedKey key = new NamespacedKey(TheFramework.asPlugin(), "custom_item");

			String itemId = data.get(key, PersistentDataType.STRING);
			if (itemId != null && itemId.equals(search.getId()))
				amount += item.getAmount();
		}

		return amount;
	}

	public static void removeFromInventory(Inventory inventory, CustomItem search, int amount) {
		for (ItemStack item : inventory.getContents()) {
			if (item == null || !item.hasItemMeta()) continue;

			PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
			NamespacedKey key = new NamespacedKey(TheFramework.asPlugin(), "custom_item");

			String itemId = data.get(key, PersistentDataType.STRING);
			if (itemId == null || !itemId.equals(search.getId())) continue;

			int stack = item.getAmount();

			if (stack > amount) {
				item.setAmount(stack - amount);
				return;
			} else {
				item.setAmount(0);
				amount -= stack;
			}

			if (amount <= 0) return;
		}
	}

	public static void reload(Player player) {
		PlayerInventory inventory = player.getInventory();

		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack item = inventory.getItem(i);
			if (item == null || !item.hasItemMeta()) continue;

			PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
			NamespacedKey tag = new NamespacedKey(TheFramework.asPlugin(), "custom_item");

			String itemId = data.get(tag, PersistentDataType.STRING);
			if (itemId == null) return;
			CustomItem updatedItem = ItemManager.getItem(itemId);

			if (updatedItem == null) {
				inventory.setItem(i, null);
				continue;
			}

			ItemMeta oldMeta = item.getItemMeta();
			int oldDamage = (oldMeta instanceof Damageable damageable) ? damageable.getDamage() : 0;

			ItemStack newItem = updatedItem.create(item.getAmount());
			ItemMeta newMeta = newItem.getItemMeta();

			if (newMeta instanceof Damageable newDamageable) {
				newDamageable.setDamage(oldDamage);

				if (updatedItem.getDurability() > 0) {
					List<Component> lore = newMeta.lore();
					List<Component> itemLore = item.lore();
					if (lore != null && !lore.isEmpty() && itemLore == null) {
						int customDurability = updatedItem.getDurability();
						int defaultDurability = updatedItem.getMaterial().getMaxDurability();
						int remainingDurability = Math.max(0, customDurability - (int) Math.ceil((double) oldDamage * customDurability / defaultDurability));

						lore.set(lore.size() - 1, TextManager.format(
							"&8&o" + updatedItem.getType() + " " + (remainingDurability == customDurability ? customDurability : remainingDurability + "/" + customDurability)));

						newMeta.lore(lore);
					}
				}

				newItem.setItemMeta(newMeta);
			}

			inventory.setItem(i, newItem);
		}
	}
}